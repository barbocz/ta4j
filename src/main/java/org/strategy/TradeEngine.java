package org.strategy;

import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.test.TradeCenter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TradeEngine {

    public String symbol;
    public String info;

    public HashMap<Integer, MT4TimeSeries> timeSeries = new HashMap<>();

    public TimeSeriesRepo timeSeriesRepo;
    public List<Integer> timeFrames = new ArrayList<>();

    public TimeSeries series;
    public int timeFrame;


    public boolean islogged = false, backtestMode = false, detailedLogMode = true;


    HashMap<Integer, Integer> bufferIndexes = new HashMap<>(); // MT4 indicator buffer indexek HashMap<hashcode,bufferIndex>
    HashMap<Integer, Integer> subWindows = new HashMap<>(); // MT4 indicator subWindow indexek HashMap<hashcode,subWindow>
    HashMap<Integer, StringBuffer> bufferValues = new HashMap<>(); // MT4 indicator értékek
    StringBuffer exitPriceLogContent = new StringBuffer("");
    StringBuffer balanceEquityLogContent = new StringBuffer("");
    List<StringBuffer> loggedExitPrices = new ArrayList<>();

    double initialBalance = 10000.0, initialAmount = 100000.0;
    double balance, equity, lastBalanceMaximum, lastBalanceMinimum, balanceDrawDown = 0.0, equityMinimum, openMinimum = Double.MAX_VALUE, openMaximum = 0.0;
    int profitableTrade = 0, losingTrade = 0;


    //    public List<Rule> rulesForLog=new ArrayList<>();
    public Strategy entryStrategy, exitStrategy;
    public ZonedDateTime currentTradeTime;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    DateTimeFormatter simpleDateFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");

    public List<Order> openedOrders = new ArrayList<>(), closedOrders = new ArrayList<>();
    public int orderIndex = 0;

    public LogStrategy logStrategy;

    public enum ExitMode {
        TAKEPROFIT,
        STOPLOSS,
        ANY
    }

    public enum LogLevel {
        NONE,       //semmit sem naplóz
        BASIC,      // bar,trade
        EXTENDED,   // bar,trade,rule,indicator
        TOTAL       //mindent sem naplóz (bar,trade,rule,indicator, exit levels)
    }

    public LogLevel logLevel = LogLevel.NONE;

    public TradeEngine(TimeSeriesRepo timeSeriesRepo, int timeFrame, Strategy entryStrategy, Strategy exitStrategy, TradeCenter controller, LogLevel logLevel) {
        this( timeSeriesRepo, timeFrame,entryStrategy, exitStrategy, controller);
        this.logLevel = logLevel;
    }

    public TradeEngine( TimeSeriesRepo timeSeriesRepo,int timeFrame, Strategy entryStrategy, Strategy exitStrategy, TradeCenter controller) {

        this.entryStrategy = entryStrategy;
        this.exitStrategy = exitStrategy;
        this.timeFrame = timeFrame;
        this.timeSeriesRepo = timeSeriesRepo;
        symbol = timeSeriesRepo.symbol;

        series = getTimeSeries(timeFrame);
        System.out.print("TradeEngine started with " + entryStrategy.getClass().getSimpleName() + " / " + exitStrategy.getClass().getSimpleName() + " on " + timeFrame + " timeframe and "+symbol+" instrument");
    }


    public void initStrategy() throws Exception {
        logStrategy = new LogStrategy(this);
        timeSeriesRepo.tradeEngines.add(this);

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

        balance = initialBalance;
        equity = initialBalance;
        lastBalanceMaximum = initialBalance;
        lastBalanceMinimum = initialBalance;
        equityMinimum = initialBalance;
//        setLogOn();

    }




    //    public abstract void setTradeEngine(Strategy strategy,boolean logOn);


    public void onTradeEvent(Order order) throws Exception {
//        System.out.println("onTradeEvent ---------------------");
        order.id = orderIndex;
        order.barIndex = series.getCurrentIndex();
        order.openedAmount = initialAmount;
        if (logLevel != LogLevel.NONE) logStrategy.logTrade(true, order);

        orderIndex++;
        openedOrders.add(order);
//        System.out.println(order.id + ". "+order.type + "  "+order.openTime);
        exitStrategy.onTradeEvent(order);

    }

    //
    public void onTickEvent() throws Exception {
        exitStrategy.onTickEvent();
        entryStrategy.onTickEvent();
        checkExit();
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
//        System.out.println("te onBarChangeEvent");


        exitStrategy.onBarChangeEvent(timeFrame);
        entryStrategy.onBarChangeEvent(timeFrame);


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

    public void setExitPrice(Order order, double exitPrice, ExitMode exitMode, boolean conservativeMode) {
        boolean setTakeProfit = false, setStopLoss = false;
        if (order.type == Order.Type.BUY) {
            if (exitPrice > timeSeriesRepo.bid && (exitMode == ExitMode.TAKEPROFIT || exitMode == ExitMode.ANY)) {
                if (order.takeProfit == 0.0) setTakeProfit = true;
                else if (conservativeMode && exitPrice < order.takeProfit)
                    setTakeProfit = true;   // konzervatív mód esetén csak közelebbi takeprofit szint lehet
                else if (!conservativeMode && exitPrice > order.takeProfit)
                    setTakeProfit = true;  // agresszív mód esetén csak távolabbi takeprofit szint lehet
            } else if (exitPrice <= timeSeriesRepo.bid && (exitMode == ExitMode.STOPLOSS || exitMode == ExitMode.ANY)) {
                if (order.stopLoss == 0.0) setStopLoss = true;
                else if (conservativeMode && exitPrice > order.stopLoss) setStopLoss = true;
                else if (!conservativeMode && exitPrice < order.stopLoss) setStopLoss = true;
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


//        if (order.id==23) System.out.println(simpleDateFormatter.format(series.getCurrentTime())+" tp: "+order.takeProfit+"  sl: "+order.stopLoss);
    }

    public void checkExit() throws Exception {

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
        exitStrategy.onExitEvent(order);        // defaultból az order.closedAmount -  order.openAmount-ra lesz itt állítva, a metódusban lehetséges a részleges zárás specifikálása


        if (order.closedAmount > 0.0) {
            try {

                if (order.closePrice == 0.0) {
                    if (order.type == Order.Type.BUY) order.closePrice = timeSeriesRepo.bid;
                    else order.closePrice = timeSeriesRepo.ask;
                }

                order.closeTime = series.getCurrentTime();
                order.profit = order.getClosedProfit();
                if (order.profit > 0.0) profitableTrade++;
                if (order.profit < 0.0) losingTrade++;
                Order closedOrder = (Order) order.clone();
                if (logLevel != LogLevel.NONE) logStrategy.logTrade(false, closedOrder);
                closedOrders.add(closedOrder);

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

            order.openedAmount = order.openedAmount - order.closedAmount;
            if (order.openedAmount > 0.0) {
                order.id = orderIndex;
                if (logLevel != LogLevel.NONE) logStrategy.logTrade(true, order);
                orderIndex++;
            }
            ;
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
        for (int i = 1; i < timeSeriesRepo.coreSeries.getEndIndex() + 1; i++) {
            minuteBar = timeSeriesRepo.coreSeries.getBar(i);
            ZonedDateTime time = minuteBar.getBeginTime();
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

        if (logLevel.ordinal() > TradeEngine.LogLevel.NONE.ordinal()) logStrategy.getMT4data(logStrategy.id);


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

}


