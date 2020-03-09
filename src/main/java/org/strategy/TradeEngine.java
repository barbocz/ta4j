package org.strategy;

import javafx.application.Platform;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.apache.log4j.MDC;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.trading.rules.BooleanRule;
import org.test.TradeCenter;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.strategy.Order.ExitType.ENDSERIES;

public class TradeEngine {
    private static final Logger logger = LogManager.getLogger(TradeEngine.class);

    ZContext context;
    ZMQ.Socket socket;

    public String symbol;
    public String info;

    public HashMap<Integer, MT4TimeSeries> timeSeries = new HashMap<>();

    public TimeSeriesRepo timeSeriesRepo;
    public List<Integer> timeFrames = new ArrayList<>();

    public TimeSeries series;
    public int period, currentBarIndex;


    public boolean islogged = false, backtestMode = false, detailedLogMode = true;


    HashMap<Integer, Integer> bufferIndexes = new HashMap<>(); // MT4 indicator buffer indexek HashMap<hashcode,bufferIndex>
    HashMap<Integer, Integer> subWindows = new HashMap<>(); // MT4 indicator subWindow indexek HashMap<hashcode,subWindow>
    HashMap<Integer, StringBuffer> bufferValues = new HashMap<>(); // MT4 indicator értékek
    StringBuffer exitPriceLogContent = new StringBuffer("");
    StringBuffer balanceEquityLogContent = new StringBuffer("");
    List<StringBuffer> loggedExitPrices = new ArrayList<>();

    public double initialBalance = 10000.0, initialAmount = 100000.0, comissionPercent=0.00007;
    double balance, equity, lastBalanceMaximum, lastBalanceMinimum, balanceDrawDown = 0.0, equityMinimum, openMinimum = Double.MAX_VALUE, openMaximum = 0.0;
    int profitableTrade = 0, losingTrade = 0;


    //    public List<Rule> rulesForLog=new ArrayList<>();
    public Strategy entryStrategy, exitStrategy;
    public ZonedDateTime currentTradeTime;
    public Order lastBuyOrder = null, lastSellOrder = null;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    DateTimeFormatter simpleDateFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
    DecimalFormat decimalFormatWith2Dec, decimalFormatWith5Dec;


    public List<Order> openedOrders = new ArrayList<>(), closedOrders = new ArrayList<>();
    public int orderIndex = 0;

    public LogStrategy logStrategy;

    private final TradeCenter tradeCenter;

    JSONParser jsonParser = new JSONParser();
    boolean isMt4TradeOk =false;
    double mt4Profit=0.0;
    long mt4MagicNumber=0;

    public enum ExitMode {
        TAKEPROFIT,
        STOPLOSS,
        ANY
    }

    public enum LogLevel {
        NONE,       //semmit sem naplóz
        BASIC,      // bar,trade
        EXTENDED,   // bar,trade,rule,indicator
        TOTAL,       //mindent sem naplóz (bar,trade,rule,indicator, exit levels)
        ANALYSE     // mindent sem naplóz , ruleForSell,ruleForBuy teljesülése esetén trade jelzést is ad
    }

    public LogLevel logLevel = LogLevel.NONE;
    private LogLevel mt4TradeLogLevel;
    private StackTraceElement stackTraceElement;

    public TradeEngine(TimeSeriesRepo timeSeriesRepo, int period, Strategy entryStrategy, Strategy exitStrategy, TradeCenter controller, LogLevel logLevel) {
        this(timeSeriesRepo, period, entryStrategy, exitStrategy, controller);
        this.logLevel = logLevel;


    }

    public TradeEngine(TimeSeriesRepo timeSeriesRepo, int period, Strategy entryStrategy, Strategy exitStrategy, TradeCenter controller) {

        this.entryStrategy = entryStrategy;
        this.exitStrategy = exitStrategy;
        this.period = period;
        this.timeSeriesRepo = timeSeriesRepo;
        this.tradeCenter = controller;
        symbol = timeSeriesRepo.symbol;
        mt4MagicNumber=Math.abs(entryStrategy.getClass().getSimpleName().hashCode()+exitStrategy.getClass().getSimpleName().hashCode());

        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatWith2Dec = new DecimalFormat("#.00", decimalFormatSymbols);
        decimalFormatWith5Dec = new DecimalFormat("#.00000", decimalFormatSymbols);

        series = getTimeSeries(period);

        if (timeSeriesRepo.processType == TimeSeriesRepo.ProcessType.MT4) {  // MT4 feedelés esetén kinyitni a 6000 portot a trade-ek kezelésére, MT4 oldalon ehhez elindítani a TradeManager EA-t
            context = new ZContext();
            socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://*:6000");
            socket.setReceiveTimeOut(1000);
        }

        System.out.print("TradeEngine started with " + entryStrategy.getClass().getSimpleName() + " / " + exitStrategy.getClass().getSimpleName() + " on " + period + " timeframe and " + symbol + " instrument, magic: "+mt4MagicNumber+" with ");
  }


    public void initStrategy() throws Exception {
        logStrategy = new LogStrategy(this);
        timeSeriesRepo.tradeEngines.add(this);

        entryStrategy.ruleForBuy = new BooleanRule(false);
        entryStrategy.ruleForSell = new BooleanRule(false);
        entryStrategy.tradeEngine = this;
        entryStrategy.init();

        if (exitStrategy != null) {
            exitStrategy.tradeEngine = this;
            exitStrategy.init();
        }


        entryStrategy.ruleForBuy.setTradeEngine(this);
        entryStrategy.ruleForBuy.setTradeEngineForAllRule(entryStrategy.ruleForBuy);

        entryStrategy.ruleForSell.setTradeEngine(this);
        entryStrategy.ruleForSell.setTradeEngineForAllRule(entryStrategy.ruleForSell);


        if (exitStrategy.ruleForBuy != null) {
            exitStrategy.ruleForBuy.setTradeEngine(this);
            exitStrategy.ruleForBuy.setTradeEngineForAllRule(exitStrategy.ruleForBuy);
        }
        if (exitStrategy.ruleForSell != null) {
            exitStrategy.ruleForSell.setTradeEngine(this);
            exitStrategy.ruleForSell.setTradeEngineForAllRule(exitStrategy.ruleForSell);
        }


        if (logLevel != LogLevel.NONE) logStrategy.init();
        System.out.println(" with " + logStrategy.id + " logId.");

        MDC.put("action","INIT");
        MDC.put("orderId",0);
        MDC.put("mt4TicketNumber",0);
        MDC.put("strategyId",logStrategy.id);
        MDC.put("source","init");

        logger.info("TradeEngine started with " + entryStrategy.getClass().getSimpleName() + " / " + exitStrategy.getClass().getSimpleName() + " on " + period + " timeframe and " + symbol + " instrument, magic: "+mt4MagicNumber+" with ");


        balance = initialBalance;
        equity = initialBalance;
        lastBalanceMaximum = initialBalance;
        lastBalanceMinimum = initialBalance;
        equityMinimum = initialBalance;

        if (!backtestMode) {
            mt4TradeLogLevel=logLevel;
            logLevel=LogLevel.NONE;         // a loggolást kikapcsoljuk és csak az onBarChangeEvent-ben kapcsoljuk vissza amikor megjött az utolsó aktuális bar
        }
//        setLogOn();


    }


    //    public abstract void setTradeEngine(Strategy strategy,boolean logOn);


    public void onTradeEvent(Order order) throws Exception {
//        System.out.println("onTradeEvent ---------------------");

//        if (currentBarIndex > 1604) {
//            System.out.println(currentBarIndex + " - " + series.getCurrentTime());
//        }

        order.id = orderIndex;
        order.barIndex = series.getCurrentIndex();
        order.openedAmount = initialAmount;
        order.mt4MagicNumber = mt4MagicNumber;
        order.mt4Comment = entryStrategy.getClass().getSimpleName();

        if (logLevel == LogLevel.TOTAL) {
            stackTraceElement=Thread.currentThread().getStackTrace()[2];
            MDC.put("orderId", orderIndex);
            MDC.put("mt4TicketNumber", 0);
            MDC.put("action", "OPEN");
            MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());

            logger.info("type:" + order.type.toString() + ", lot:" + order.openedAmount + ", price:" + order.openPrice + ", stopLoss:" + order.stopLoss + ", takProfit:" + order.takeProfit);
        }
        exitStrategy.onTradeEvent(order);

        if (isMt4TradeOk) mt4OpenOrder(order);

        if (logLevel != LogLevel.NONE) logStrategy.logTrade(true, order);

        openedOrders.add(order);

        orderIndex++;
//        System.out.println(order.id + ". "+order.type + "  "+order.openTime);


    }

    //
    public void onTickEvent() throws Exception {

        exitStrategy.onTickEvent();
        entryStrategy.onTickEvent();
        checkExit();

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
//        System.out.println("te onBarChangeEvent");
        currentBarIndex = series.getCurrentIndex();
//        if (backtestMode) currentBarIndex = currentBarIndex - 1;

        if (!isMt4TradeOk && !backtestMode && timeSeriesRepo.lastMinuteBarTime!=null && ChronoUnit.MINUTES.between(timeSeriesRepo.lastMinuteBarTime, Instant.now()) < 2) {                              // mielőtt megkezdődne az MT4 kereskedés bezárjuk a nyitott order-eket
                isMt4TradeOk =true;
                logLevel=mt4TradeLogLevel;
                for (Order order : openedOrders) {
                    try {
                        order.forcedClose=true;
                        order.exitType=ENDSERIES;
                        closeOrder(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);
        }

        entryStrategy.onBarChangeEvent(timeFrame);
        exitStrategy.onBarChangeEvent(timeFrame);

        if (logLevel != LogLevel.NONE) logStrategy.onBarChangeEvent(timeFrame);

//        if (islogged) logIndicator(timeFrame, series.getCurrentTime());
//        if (detailedLogMode) logExitPrices();

//        if (backtestMode && this.timeFrame == timeFrame) {
//            if (Math.abs(backTestMinOpenProfit) > backTestMaxOpenProfit)
//                backTestEquity = prevBackTestBalance + backTestMinOpenProfit;
//            else backTestEquity = prevBackTestBalance + backTestMaxOpenProfit;
//            if (backTestEquity < equityMinimum) equityMinimum = backTestEquity;
//            if (prevBackTestEquity != backTestEquity)
//                balanceEquityLogContent.append("E|").append(dateFormatter.format(series.getCurrentTime())).append("|").append(backTestEquity).append("\n");
//            if (prevBackTestBalance != backTestBalance)
//                balanceEquityLogContent.append("B|").append(dateFormatter.format(series.getCurrentTime())).append("|").append(backTestBalance).append("\n");
//            prevBackTestBalance = backTestBalance;
//            prevBackTestEquity = backTestEquity;
//            backTestMaxOpenProfit = 0.0;
//            backTestMinOpenProfit = 0.0;
//        }

    }

    public void onOneMinuteDataEvent() {

//        System.out.println("currentBarIndex: "+currentTradeTime+"  "+currentTradeTime);
//        System.out.println("getCurrentBar: "+series.getBar(series.getEndIndex()).getBeginTime());

        entryStrategy.onOneMinuteDataEvent();
        exitStrategy.onOneMinuteDataEvent();

        double currentProfit = 0.0, openProfit = 0.0;
        for (Order order : openedOrders) {
            if (order.type == Order.Type.BUY) currentProfit = order.getCurrentProfit(timeSeriesRepo.bid);
            else currentProfit = order.getCurrentProfit(timeSeriesRepo.ask);
            openProfit += currentProfit;

            if (currentProfit > order.maxProfit) order.maxProfit = currentProfit;
            if (currentProfit < order.maxLoss) order.maxLoss = currentProfit;
//            System.out.println("currentProfit "+ currentProfit);
        }

        if (openProfit > openMaximum) openMaximum = openProfit;
        if (openProfit < openMinimum) openMinimum = openProfit;

        equity = balance + openProfit;

        if (equity < equityMinimum) equityMinimum = equity;

    }

    public void setExitPrice(Order order, double exitPrice) {

        double prevTakeProfit=order.takeProfit;
        double prevStopLoss=order.stopLoss;
        if (order.type == Order.Type.BUY) {
            if (exitPrice > timeSeriesRepo.bid) order.takeProfit=exitPrice;
            else  if (exitPrice>order.stopLoss) order.stopLoss=exitPrice;         // csak jobb stoplosst fogadunk el

        } else {
            if (exitPrice < timeSeriesRepo.ask) order.takeProfit=exitPrice;
            else  if (exitPrice<order.stopLoss) order.stopLoss=exitPrice;
        }

        if (logLevel == LogLevel.TOTAL) {

            stackTraceElement=Thread.currentThread().getStackTrace()[2];
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            MDC.put("action", "BREAK");
            MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());

            MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());
            if (prevTakeProfit!=order.takeProfit)  {
                MDC.put("action", "TP_BREAK");
                logger.info("takeProfit:" + decimalFormatWith5Dec.format(order.takeProfit) + ", prevTakeProfit:" + decimalFormatWith5Dec.format(prevTakeProfit));
            }
            else if (prevStopLoss!=order.stopLoss)  {
                MDC.put("action", "SL_BREAK");
                logger.info("stopLoss:" + decimalFormatWith5Dec.format(order.stopLoss) + ", prevStopLoss:" + decimalFormatWith5Dec.format(prevStopLoss));
            }
//            else {
//                logger.info("incorrect exitPrice:" + decimalFormatWith5Dec.format(exitPrice));
//            }


        }
    }

    public void setTakeProfit(Order order, double takeProfit) {
        double prevTakeProfit=order.takeProfit;
        if (order.type == Order.Type.BUY) {
            if (takeProfit > timeSeriesRepo.bid) order.takeProfit=takeProfit;

        } else {
            if (takeProfit < timeSeriesRepo.ask) order.takeProfit=takeProfit;

        }

        if (logLevel == LogLevel.TOTAL) {
            MDC.put("action", "TAKEPROFIT");
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            logger.info("takeProfit:" + decimalFormatWith5Dec.format(order.takeProfit) + ", prevTakeProfit:" + decimalFormatWith5Dec.format(prevTakeProfit));
        }


    }

    public void setStopLoss(Order order, double stopLoss) {
        double prevStopLoss=order.stopLoss;
        if (order.type == Order.Type.BUY) {
            if (stopLoss < timeSeriesRepo.bid) order.stopLoss=stopLoss;

        } else {
            if (stopLoss > timeSeriesRepo.ask) order.stopLoss=stopLoss;

        }

        if (logLevel == LogLevel.TOTAL) {
            stackTraceElement=Thread.currentThread().getStackTrace()[2];
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            MDC.put("action", "STOPLOSS");
            MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());
            logger.info("stopLoss:" + decimalFormatWith5Dec.format(order.stopLoss) + ", prevStopLoss:" + decimalFormatWith5Dec.format(prevStopLoss));
        }


    }

    public void setExitPrice(Order order, double exitPrice, ExitMode exitMode, boolean conservativeMode) {
        double prevTakeProfit=order.takeProfit;
        double prevStopLoss=order.stopLoss;

        boolean setTakeProfit = false, setStopLoss = false;
        if (order.type == Order.Type.BUY) {
            if (exitPrice > timeSeriesRepo.bid && (exitMode == ExitMode.TAKEPROFIT || exitMode == ExitMode.ANY)) {
                if (order.takeProfit == 0.0) setTakeProfit = true;
                else if (conservativeMode && exitPrice < order.takeProfit)
                    setTakeProfit = true;   // konzervatív mód esetén az előzőleg beálított takeProfit-nál csak közelebbi takeProfit szint állítható
                else if (!conservativeMode && exitPrice > order.takeProfit)
                    setTakeProfit = true;  // agresszív mód esetén az előzőleg beálított takeProfit-nál csak távolabbi takeProfit szint állítható
            } else if (exitPrice <= timeSeriesRepo.bid && (exitMode == ExitMode.STOPLOSS || exitMode == ExitMode.ANY)) {
                if (order.stopLoss == 0.0) setStopLoss = true;
                else if (conservativeMode && exitPrice > order.stopLoss)
                    setStopLoss = true;  // konzervatív mód esetén az előzőleg beálított stopLoss-nál csak közelebbi stopLoss szint állítható
                else if (!conservativeMode && exitPrice < order.stopLoss)
                    setStopLoss = true; // agresszív mód esetén az előzőleg beálított stopLoss-nál csak távolabbi stopLoss szint állítható
            }
        } else {
            if (exitPrice < timeSeriesRepo.ask && (exitMode == ExitMode.TAKEPROFIT || exitMode == ExitMode.ANY)) {
                if (order.takeProfit == 0.0) setTakeProfit = true;
                else if (conservativeMode && exitPrice > order.takeProfit)
                    setTakeProfit = true;
                else if (!conservativeMode && exitPrice < order.takeProfit)
                    setTakeProfit = true;
            } else if (exitPrice >= timeSeriesRepo.ask && (exitMode == ExitMode.STOPLOSS || exitMode == ExitMode.ANY)) {
                if (order.stopLoss == 0.0) setStopLoss = true;
                else if (conservativeMode && exitPrice < order.stopLoss) setStopLoss = true;
                else if (!conservativeMode && exitPrice > order.stopLoss) setStopLoss = true;
            }
        }

        if (setTakeProfit) order.takeProfit = exitPrice;
        if (setStopLoss) order.stopLoss = exitPrice;

        if (logLevel == LogLevel.TOTAL) {
            stackTraceElement=Thread.currentThread().getStackTrace()[2];
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            MDC.put("action", "SLTPEXIT");
            MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());
            if (prevTakeProfit!=order.takeProfit)  {
                MDC.put("action", "*TP_EXIT");
                logger.info("takeProfit:" + decimalFormatWith5Dec.format(order.takeProfit) + ", prevTakeProfit:" + decimalFormatWith5Dec.format(prevTakeProfit));
            }
            else if (prevStopLoss!=order.stopLoss)  {
                MDC.put("action", "*SL_EXIT");
                logger.info("stopLoss:" + decimalFormatWith5Dec.format(order.stopLoss) + ", prevStopLoss:" + decimalFormatWith5Dec.format(prevStopLoss));
            }
        }


//        if (order.id==23) System.out.println(simpleDateFormatter.format(series.getCurrentTime())+" tp: "+order.takeProfit+"  sl: "+order.stopLoss);
    }

    public void checkExit() throws Exception {
//
//        if (currentBarIndex>3027) {
//            System.out.println(currentBarIndex+" - "+series.getCurrentTime());
//        }

        for (Order order : openedOrders) {
            if (order.type == Order.Type.BUY) {
                if (timeSeriesRepo.bid < order.stopLoss) {
                    order.exitType = Order.ExitType.STOPLOSS;
                    if (backtestMode) order.closePrice = order.stopLoss;
                    else order.closePrice = timeSeriesRepo.bid;

                    closeOrder(order);
                }
                if (timeSeriesRepo.bid > order.takeProfit && order.takeProfit != 0.0) {
                    order.exitType = Order.ExitType.TAKEPROFIT;
                    if (backtestMode) order.closePrice = order.takeProfit;
                    else order.closePrice = timeSeriesRepo.bid;
                    closeOrder(order);
                }
            } else {
                if (timeSeriesRepo.ask > order.stopLoss && order.stopLoss != 0.0) {
                    order.exitType = Order.ExitType.STOPLOSS;
                    if (backtestMode) order.closePrice = order.stopLoss;
                    else order.closePrice = timeSeriesRepo.ask;
                    closeOrder(order);
                }
                if (timeSeriesRepo.ask < order.takeProfit) {
                    order.exitType = Order.ExitType.TAKEPROFIT;
                    if (backtestMode) order.closePrice = order.takeProfit;
                    else order.closePrice = timeSeriesRepo.ask;
                    closeOrder(order);
                }
            }
        }

        openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);

    }

    public void closeOrder(Order order) throws Exception {
        if (order.closedAmount==0.0) order.closedAmount=order.openedAmount;
//        if (!order.forcedClose) exitStrategy.onExitEvent(order);        // defaultból az order.closedAmount -  order.openAmount-ra lesz itt állítva, a metódusban lehetséges a részleges zárás specifikálása
//        else order.closedAmount = order.openedAmount;
        exitStrategy.onExitEvent(order);

        if (order.closedAmount > 0.0) {
            try {

                if (order.closePrice == 0.0) {
                    if (order.type == Order.Type.BUY) order.closePrice = timeSeriesRepo.bid;
                    else order.closePrice = timeSeriesRepo.ask;
                }

                order.closeTime = series.getCurrentTime();
                order.comission=order.closedAmount*comissionPercent;
                order.profit = order.getClosedProfit() - order.comission;

                if (order.profit > 0.0) profitableTrade++;
                if (order.profit < 0.0) losingTrade++;

//                logger.info("type: "+ order.type.toString()+", lot: "+order.openedAmount+", price: "+order.openPrice+", stopLoss: "+order.stopLoss+", takProfit: "+ order.takeProfit);

                if (order.mt4TicketNumber>0) {
                    mt4CloseOrder(order);
                    mt4Profit+=order.mt4Profit;
                }
                Order closedOrder = (Order) order.clone();

                if (logLevel == LogLevel.TOTAL) {
                    stackTraceElement=Thread.currentThread().getStackTrace()[2];
                    MDC.put("orderId", order.id);
                    MDC.put("mt4TicketNumber", order.mt4TicketNumber);
                    MDC.put("action", "CLOSE");
                    MDC.put("source",Thread.currentThread().getStackTrace()[2].getMethodName()+":"+Thread.currentThread().getStackTrace()[2].getLineNumber());
                    logger.info("type:" + order.type.toString() + ", lot:" + order.closedAmount + ", closePrice:" + decimalFormatWith5Dec.format(order.closePrice) + ", stopLoss:" + decimalFormatWith5Dec.format(order.stopLoss) + ", takProfit:" + decimalFormatWith5Dec.format(order.takeProfit) + ", profit:" + decimalFormatWith2Dec.format(order.profit) + ", exitBy:" + order.exitType.toString());
                }


                if (logLevel != LogLevel.NONE) logStrategy.logTrade(false, closedOrder);
                closedOrders.add(closedOrder);

                if (closedOrder.type == Order.Type.BUY) lastBuyOrder = closedOrder;
                if (closedOrder.type == Order.Type.SELL) lastSellOrder = closedOrder;

                balance += order.profit;
                if (balance > lastBalanceMaximum) {
                    lastBalanceMaximum = balance;
                    lastBalanceMinimum = lastBalanceMaximum;
                }
                if (balance < lastBalanceMinimum) lastBalanceMinimum = balance;
                if (lastBalanceMinimum - lastBalanceMaximum < balanceDrawDown)
                    balanceDrawDown = lastBalanceMinimum - lastBalanceMaximum;

//                    System.out.println("order closed ("+order.id+") "+order.closeTime+" cprice: "+order.closePrice+" "+order.closedAmount+" with profit: "+order.getProfit(order.closePrice));

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            // részleges zárás miatt még maradt nyitva:
            order.openedAmount = order.openedAmount - order.closedAmount;
            order.closedAmount=0.0;
            order.closePrice=0.0;
            order.closeTime=null;
            if (order.openedAmount > 0.0) {
                order.id = orderIndex;
                if (order.mt4TicketNumber>0) {
                    order.mt4TicketNumber=order.mt4NewTicketNumber;
                    order.mt4OpenPrice=order.mt4NewOpenPrice;
                    order.mt4OpenTime=order.mt4NewOpenTime;
                }
                if (logLevel != LogLevel.NONE) logStrategy.logTrade(true, order);
                orderIndex++;
            }

            if (tradeCenter != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        tradeCenter.updateBalance(logStrategy.id, mt4Profit);
                    }
                });
            }

        }



    }


    public void runBackTest() throws Exception {
        if (logLevel != LogLevel.NONE) logStrategy.setAutoCommit(false);

        backtestMode = true;


        long startTime = System.currentTimeMillis();
        HashMap<Integer, Integer> lastBarIndexes = new HashMap<>();

        for (Integer timeFrame : timeFrames) lastBarIndexes.put(timeFrame, -1);

        int index;
        double openPrice, highPrice, lowPrice, closePrice;
        TimeSeries currentSeries;
        Bar minuteBar;

        entryStrategy.orderAmount = initialAmount;
//        timeSeriesRepo.coreSeries.getEndIndex()-50
        for (int i = 0; i < timeSeriesRepo.coreSeries.getEndIndex() + 1; i++) {
            minuteBar = timeSeriesRepo.coreSeries.getBar(i);
            ZonedDateTime time = minuteBar.getBeginTime();
            if (series.getIndex(time)<0) continue;
            series.setCurrentTime(time);
            // tick
            openPrice = minuteBar.getOpenPrice().doubleValue();
            highPrice = minuteBar.getHighPrice().doubleValue();
            lowPrice = minuteBar.getLowPrice().doubleValue();
            closePrice = minuteBar.getClosePrice().doubleValue();
            timeSeriesRepo.bid = openPrice;
            timeSeriesRepo.ask = openPrice;

            onOneMinuteDataEvent();
//            System.out.println("setCurrentTime: "+simpleDateFormatter.format(time)+",  otime: "+simpleDateFormatter.format(series.getCurrentBar().getBeginTime())+", etime: "+simpleDateFormatter.format(series.getCurrentBar().getEndTime()));
            // barchange
            for (Integer timeFrame : timeFrames) {
                currentSeries = timeSeriesRepo.getTimeSeries(timeFrame);
                index = currentSeries.getIndex(time);
//                if (index>14665) {
//                    System.out.println("break");
//                }
//                System.out.println("timeFrame: "+timeFrame+", index:"+index);
                if (index != lastBarIndexes.get(timeFrame) && index > 0) {
//                    System.out.println("onBarChangeEvent on timeFrame: "+timeFrame);
                    lastBarIndexes.replace(timeFrame, index);
                    currentSeries.setCurrentTime(time);
                    onBarChangeEvent(timeFrame);
                }
            }

            onTickEvent();
            if (openPrice > closePrice) {
                timeSeriesRepo.bid = highPrice;
                timeSeriesRepo.ask = highPrice;
                onTickEvent();
                timeSeriesRepo.bid = lowPrice;
                timeSeriesRepo.ask = lowPrice;
                onTickEvent();
            } else {
                timeSeriesRepo.bid = lowPrice;
                timeSeriesRepo.ask = lowPrice;
                onTickEvent();
                timeSeriesRepo.bid = highPrice;
                timeSeriesRepo.ask = highPrice;
                onTickEvent();
            }
            timeSeriesRepo.bid = closePrice;
            timeSeriesRepo.ask = closePrice;
            onTickEvent();


        }

        logStrategy.logStrategyResult();
        logStrategy.getLastStrategyResults(4);

        if (logLevel != LogLevel.NONE) logStrategy.setAutoCommit(true);

        if (logLevel.ordinal() > LogLevel.BASIC.ordinal()) logStrategy.getMT4data(logStrategy.id);

        double openProfit=0.0;
        for (Order order: openedOrders) {
            openProfit+=order.getCurrentProfit(timeSeriesRepo.coreSeries.getBar(timeSeriesRepo.coreSeries.getEndIndex()).getClosePrice().doubleValue());
        }
        if (openedOrders.size()>0) System.out.println("Left opened: "+openedOrders.size()+ " with profit "+openProfit);

        logStrategy.getProfitByMonth(logStrategy.id);

//        for (int i = 0; i < series.getEndIndex(); i++) {
//            System.out.println(series.getBar(i).getEndTime()+" - "+series.getBar(i).getOrderType());
//        }


    }


    public TimeSeries getTimeSeries(Integer timeFramePeriod) {
        if (timeFrames.contains(timeFramePeriod)) return timeSeriesRepo.getTimeSeries(timeFramePeriod);
        else {
            timeFrames.add(timeFramePeriod);
            timeSeriesRepo.setTimeSeries(timeFramePeriod);
            return timeSeriesRepo.getTimeSeries(timeFramePeriod);
        }
    }


    public void log(Rule rule) {
        logStrategy.rulesForLog.add(rule);
    }

    public void log(Indicator indicator) {
        logStrategy.indicatorsForLog.add(indicator);
    }

    void mt4SetResponseToOrder(String response,Order order) {

        try {
            JSONObject mt4Order;
            JSONArray responseObject = (JSONArray) jsonParser.parse(response);
//            System.out.println(responseObject);
            JSONObject rootObject = (JSONObject) responseObject.get(0);

//            System.out.println(rootObject.get("orders"));
            JSONArray orderList = (JSONArray) rootObject.get("orders");
            if (orderList!=null) {
                for (Object orderObject : orderList) {
                    if (orderObject instanceof JSONObject) {
                        mt4Order = (JSONObject) orderObject;

                        if (mt4Order.get("ticketNumber") != null)
                            order.mt4TicketNumber = ((Long) mt4Order.get("ticketNumber")).intValue() ;
                        if (mt4Order.get("openPrice") != null) order.mt4OpenPrice = (double) mt4Order.get("openPrice");
                        if (mt4Order.get("openTime") != null)
                            order.mt4OpenTime = (ZonedDateTime) ZonedDateTime.parse((String) mt4Order.get("openTime"), timeSeriesRepo.zdtFormatterWithSeconds);


                        if (mt4Order.get("closePrice") != null)
                            order.mt4ClosePrice = (double) mt4Order.get("closePrice");
                        if (mt4Order.get("closeTime") != null)
                            order.mt4CloseTime = (ZonedDateTime) ZonedDateTime.parse((String) mt4Order.get("closeTime"), timeSeriesRepo.zdtFormatterWithSeconds);
                        if (mt4Order.get("profit") != null) order.mt4Profit = (double) mt4Order.get("profit");

                        // részleges zárás esetén az új order adatai
                        if (mt4Order.get("newTicketNumber") != null)
                            order.mt4NewTicketNumber =  ((Long) mt4Order.get("newTicketNumber")).intValue() ;
                        if (mt4Order.get("newOpenPrice") != null)
                            order.mt4NewOpenPrice = (double) mt4Order.get("newOpenPrice");
                        if (mt4Order.get("newOpenTime") != null)
                            order.mt4NewOpenTime = (ZonedDateTime) ZonedDateTime.parse((String) mt4Order.get("newOpenTime"), timeSeriesRepo.zdtFormatterWithSeconds);


//                    System.out.println(((JSONObject) mt4Order).get("ticketNumber"));
                    }
                }
            }

        } catch (                ParseException e) {
            System.out.println(response);
            e.printStackTrace();
        }
    }

    void mt4OpenOrder(Order order) {
        JSONObject mt4Order = new JSONObject();
        currentTradeTime =Instant.now().atZone(ZoneId.of(timeSeriesRepo.metaTradeTimeZone));
        mt4Order.put("time",timeSeriesRepo.zdtFormatter.format(currentTradeTime));
        mt4Order.put("action", "TRADE_OPEN");
        mt4Order.put("type", order.type.mt4OrderType());
        mt4Order.put("symbol", symbol);
        mt4Order.put("lot", order.openedAmount / 100000);
        mt4Order.put("stopLoss",order.stopLoss);
        mt4Order.put("takeProfit", order.takeProfit);
        mt4Order.put("magicNumber", order.mt4MagicNumber);
        mt4Order.put("comment",order.mt4Comment);

        if (logLevel == LogLevel.TOTAL) {
            stackTraceElement=Thread.currentThread().getStackTrace()[2];
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            MDC.put("action", "->MT4 OPEN");
            MDC.put("source","mt4OpenOrder");
            logger.info("type:" + order.type.toString() + ", lot:" + decimalFormatWith2Dec.format(order.openedAmount / 100000) );
        }

//        logger.info("OUT: "+mt4Order.toJSONString());
//        System.out.println(mt4Order.toJSONString());

        socket.send(mt4Order.toJSONString().getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
        if (reply == null) {
            System.out.println("NO REPLY FOR CURRENT ORDER OPEN REQUEST "+order.id);
            logger.error("NO REPLY FOR CURRENT ORDER OPEN REQUEST "+order.id);
            order.mt4TicketNumber=-1;
            order.mt4OpenTime=currentTradeTime;
        }
        else {
            String response = new String(reply, ZMQ.CHARSET);
            mt4SetResponseToOrder(response,order);
            if (logLevel == LogLevel.TOTAL) {
                MDC.put("action", "MT4-> OPEN");
                logger.info(response);
            }


//            System.out.println("response " + response);
        }

    }


    void mt4CloseOrder(Order order) {
        JSONObject mt4Order = new JSONObject();
        currentTradeTime =Instant.now().atZone(ZoneId.of(timeSeriesRepo.metaTradeTimeZone));
        mt4Order.put("time",timeSeriesRepo.zdtFormatter.format(currentTradeTime));
        if (order.openedAmount==order.closedAmount)  mt4Order.put("action", "TRADE_CLOSE");
        else  mt4Order.put("action", "TRADE_CLOSE_PARTIAL");
        mt4Order.put("lot", order.closedAmount / 100000);
        mt4Order.put("ticketNumber",order.mt4TicketNumber);

//        System.out.println(mt4Order.toJSONString());
//        logger.info("OUT: "+mt4Order.toJSONString());

        if (logLevel == LogLevel.TOTAL) {
            MDC.put("orderId", order.id);
            MDC.put("mt4TicketNumber", order.mt4TicketNumber);
            MDC.put("action", "->MT4 CLOSE");
            MDC.put("source","mt4CloseOrder");
            logger.info("type:" + order.type.toString() + ", lot:" + decimalFormatWith2Dec.format(order.closedAmount / 100000) );
        }

        socket.send(mt4Order.toJSONString().getBytes(ZMQ.CHARSET), 0);

        byte[] reply = socket.recv(0);
        if (reply == null) {
            System.out.println("NO REPLY FOR CURRENT ORDER CLOSE REQUEST "+order.id);
            logger.error("NO REPLY FOR CURRENT ORDER CLOSE REQUEST "+order.id);
        }
        else {
            String response = new String(reply, ZMQ.CHARSET);
            mt4SetResponseToOrder(response,order);
            if (logLevel == LogLevel.TOTAL) {
                MDC.put("action", "MT4-> CLOSE");
                logger.info(response);
            }
//            System.out.println("response " + response);
        }

    }




}


