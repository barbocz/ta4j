/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.extendedCore;

import org.strategy.AbstractStrategy;

import org.strategy.TimeSeriesRepo;
import org.ta4j.core.*;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.SLTPManager;
import org.util.SqlLiteConnector;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base implementation of a {@link TradingRecord}.
 * </p>
 */
public class BaseTradingRecordExtended extends BaseTradingRecord implements Core {

    private static final long serialVersionUID = -4436851731855891220L;

    private CostModel transactionCostModel;
    private CostModel holdingCostModel;

    private int entryOrderId = 1;
    private int exitOrderId = 1;

    /**
     * The recorded entry orders
     */
    private List<Order> entryOrders = new ArrayList<>();  // a nyitó orderek-nek

    /**
     * The recorded exit orders
     */
    private List<Order> exitOrders = new ArrayList<>();  // a záró orderek-nek

    public HashMap<Integer, Order> openOrders = new HashMap<>();   // aktuálisan nyitott order-ek

    public TimeSeries series;
    public int period = 0;

    // balance vezetésre
    public List<Num> balance;
    public List<Num> equity;

    public int tradeSize = 0;
    public double profitableTradesRatio = 0.0;
    public double totalProfitPerMonth = 0.0;

    public Num maximumBalance;
    public Num balanceDrawDown;
    public Num netBalanceDrawDown;

    public Num maximumOpenProfit;
    public Num maximumOpenLoss;
    public Num equityDrawDown;
    public Num totalProfit;

    public Num openBalance;
    private Num tradingAmount;

    public double barPerTime = 0.0;

    private int firstOpenOrderIndex = 0;
    public long strategyId = 0;
    public SqlLiteConnector sqlLiteConnector;

    public List<Indicator> indicitatorsToDebug = new ArrayList<>();
    public boolean debug = false;
    public int beginIndex, endIndex;
    PreparedStatement insertDebugInfo = null;

    private final AbstractStrategy strategy;


    public BaseTradingRecordExtended(AbstractStrategy strategy) {
        this.strategy = strategy;

        strategyId = new Date().getTime();
        sqlLiteConnector = new SqlLiteConnector();

        strategy.ruleForBuy.setCore(this);
        strategy.ruleForSell.setCore(this);
        if (strategy.ruleForBuy != null) strategy.ruleForBuy.setCore(this);
        if (strategy.ruleForSell != null) strategy.ruleForSell.setCore(this);

        // egyelőre a getDescription rekurzió viszi le a Core-t a subRule-okba
        strategy.ruleForBuy.getDescription();
        strategy.ruleForSell.getDescription();

        if (strategy.ruleForBuy != null) strategy.ruleForBuy.getDescription();
        if (strategy.ruleForSell != null) strategy.ruleForSell.getDescription();

        series = strategy.series;
        period = strategy.period;
        beginIndex = series.getBeginIndex();
        endIndex = series.getEndIndex();

//        buyStrategy = new BaseStrategyExtended(Order.OrderType.BUY, strategy.ruleForBuy, strategy.ruleForBuy, strategy.positionManagement.exitLevelsForBuy);
//        sellStrategy = new BaseStrategyExtended(Order.OrderType.SELL, strategy.ruleForSell, strategy.ruleForSell, strategy.positionManagement.exitLevelsForSell);

        strategy.positionManagement.setCore(this);

        transactionCostModel = new ZeroCostModel();
        holdingCostModel = new ZeroCostModel();

        openBalance = series.numOf(10000.0);
        balance = new ArrayList<>(Collections.singletonList(openBalance));
        equity = new ArrayList<>(Collections.singletonList(openBalance));
        maximumBalance = series.numOf(Double.MIN_VALUE);
        balanceDrawDown = series.numOf(0.0);
        netBalanceDrawDown = series.numOf(Double.MIN_VALUE);
        equityDrawDown = series.numOf(0.0);
        maximumOpenProfit = series.numOf(0.0);
        maximumOpenLoss = series.numOf(0.0);

        tradingAmount = series.numOf(100000.0);

        storeStrategy();

    }


    /**
     * Constructor.
     */

    public boolean hasOpenOrders() {
        return  openOrders.size()>0;
    }

    public HashMap<Integer, Order> getOpenOrders() {
        return openOrders;
    }

    public void exitByRule(int index) {

        if (strategy.ruleForBuy==null && strategy.ruleForSell==null) return;

        if (openOrders.size() > 0) {
            List<Integer> closedOrders=new ArrayList<>();
            for (Order entryOrder : openOrders.values()) {
                Order.OrderType entryType = entryOrder.getType();
                boolean shouldExit = entryType == Order.OrderType.BUY ? strategy.ruleForBuy.isSatisfied(index) :
                        strategy.ruleForSell.isSatisfied(index);



                if (shouldExit) {
                    entryOrder.amountToClose = entryOrder.getAmount();
                    entryOrder.closePrice = series.getBar(index).getClosePrice();
                    Order.closeAt(index, entryOrder, exitOrderId, Order.ExitType.EXITRULE);
                    exitOrderId++;
                    if (entryOrder.isClosed()) closedOrders.add(entryOrder.getId());
                }
            }
            openOrders.keySet().removeAll(closedOrders);
        }


    }

    public void exitByTakeProfit(int index) {

        if (openOrders.size() > 0) {
            Num lowPrice = null, highPrice = null;
            List<Integer> closedOrders=new ArrayList<>();
            lowPrice = series.getBar(index).getLowPrice();
            highPrice = series.getBar(index).getHighPrice();

            for (Order entryOrder : openOrders.values()) {
                if (entryOrder.takeProfit != null && entryOrder.takeProfit.isGreaterThanOrEqual(lowPrice) && entryOrder.takeProfit.isLessThanOrEqual(highPrice)) {
                    entryOrder.amountToClose = entryOrder.getAmount(); // ezt még javítani
                    Order.closeAt(index, entryOrder, exitOrderId, Order.ExitType.TAKEPROFIT);
                    exitOrderId++;
                    if (entryOrder.isClosed()) closedOrders.add(entryOrder.getId());
                }
            }

            openOrders.keySet().removeAll(closedOrders);
        }

//        Num takeProfitPrice, takeProfitAmount = null;
//        String takeProfitDescription = "";
//        Num lowPrice = series.getBar(index).getLowPrice();
//        Num highPrice = series.getBar(index).getHighPrice();
//        Num openPrice = series.getBar(index).getOpenPrice();
//        Order entryOrder;
//
//
//        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
//            entryOrder = entryOrders.get(i);
//            if (entryOrder.isOpen()) {
////                List<TakeProfit.Property> takeProfits = entryOrder.takeProfit.get(entryOrder.getId());
////                if (takeProfits == null) return;
////
////                if (entryOrder.getType().isBuy()) {
////
////                    takeProfitPrice = series.numOf(Double.MAX_VALUE); // a kissebb értéket vesszük
////
////                    for (TakeProfit.Property takeProfit : takeProfits) {
////                        if (takeProfit.level.isLessThan(takeProfitPrice)) {
//////                            if (takeProfit.strict && (takeProfit.level.isGreaterThan(highPrice) || takeProfit.level.isLessThan(lowPrice))) continue;
////                            takeProfitPrice = takeProfit.level;
////                            takeProfitAmount = takeProfit.amount;
////                            takeProfitDescription = takeProfit.description;
////
////                        }
////
////                    }
//////                    if (takeProfitPrice.isGreaterThan(highPrice)) return;
//////                    else {
//////                        if (!strict && openPrice.isGreaterThanOrEqual(takeProfitPrice)) takeProfitPrice=openPrice;
//////                    }
////                } else {
////
////                    takeProfitPrice = series.numOf(0.0); // a nagyobb értéket vesszük
////                    for (TakeProfit.Property takeProfit : takeProfits) {
////                        if (takeProfit.level.isGreaterThan(takeProfitPrice)) {
//////                            if (takeProfit.strict && (takeProfit.level.isGreaterThan(highPrice) || takeProfit.level.isLessThan(lowPrice))) continue;
////                            takeProfitPrice = takeProfit.level;
////                            takeProfitAmount = takeProfit.amount;
////                            takeProfitDescription = takeProfit.description;
////
////                        }
////                    }
//////                    if (takeProfitPrice.isLessThan(lowPrice)) return;
//////                    else {
//////                        if (!strict && openPrice.isLessThanOrEqual(takeProfitPrice)) takeProfitPrice=openPrice;
//////                    }
////                }
//                if (entryOrder.takeProfit != null && entryOrder.takeProfit.isGreaterThanOrEqual(lowPrice) && entryOrder.takeProfit.isLessThanOrEqual(highPrice)) {
//                    entryOrder.amountToClose = entryOrder.getAmount(); // ezt még javítani
//                    Order.closeAt(index, entryOrder, exitOrderId, Order.ExitType.TAKEPROFIT);
//                    exitOrderId++;
//                    if (!entryOrder.isClosed()) entryOrder.setTakeProfit(index);
//                }
//            }
//        }
    }

    public void exitByStopLoss(int index) {



        if (openOrders.size() > 0) {
            Num lowPrice = null, highPrice = null;
            List<Integer> closedOrders=new ArrayList<>();
            lowPrice = series.getBar(index).getLowPrice();
            highPrice = series.getBar(index).getHighPrice();

            for (Order entryOrder : openOrders.values()) {
                if (entryOrder.stopLoss != null && entryOrder.stopLoss.isGreaterThanOrEqual(lowPrice) && entryOrder.stopLoss.isLessThanOrEqual(highPrice)) {
                    entryOrder.amountToClose = entryOrder.getAmount(); // ezt még javítani
                    Order.closeAt(index, entryOrder, exitOrderId, Order.ExitType.STOPLOSS);
                    exitOrderId++;
                    if (entryOrder.isClosed()) closedOrders.add(entryOrder.getId());
                }
            }

            openOrders.keySet().removeAll(closedOrders);
        }


//        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
//            entryOrder = entryOrders.get(i);
//            if (entryOrder.isOpen()) {
//                List<StopLoss.Property> stopLosses = entryOrder.stopLoss.get(entryOrder.getId());
//                if (stopLosses == null) return;
//                stopLossPrice = null;
//
//                if (entryOrder.getType().isBuy()) {
//                    stopLossPrice = series.numOf(0.0); // a nagyobb értéket vesszük
//                    for (StopLoss.Property stopLoss : stopLosses) {
//                        if (stopLoss.level.isGreaterThanOrEqual(lowPrice) && stopLoss.level.isLessThanOrEqual(highPrice)) {
//                            stopLossPrice = stopLoss.level;
//                            stopLossAmount = stopLoss.amount;
//                            stopLossDescription = stopLoss.description;
//                            break;
//                        }
//                        if (stopLoss.level.isGreaterThan(stopLossPrice)) {
//
////                            if (stopLoss.strict && (stopLoss.level.isGreaterThan(highPrice) || stopLoss.level.isLessThan(lowPrice))) continue;
//                            stopLossPrice = stopLoss.level;
//                            stopLossAmount = stopLoss.amount;
//                            stopLossDescription = stopLoss.description;
//
//                        }
//                    }
////                    if (stopLossPrice.isLessThan(lowPrice)) return;
////                    else {
////                        if (!strict && openPrice.isLessThanOrEqual(stopLossPrice)) stopLossPrice=openPrice;
////                    }
//                } else {
//                    stopLossPrice = series.numOf(Double.MAX_VALUE); // a kissebb értéket vesszük
//                    for (StopLoss.Property stopLoss : stopLosses) {
//                        if (stopLoss.level.isGreaterThanOrEqual(lowPrice) && stopLoss.level.isLessThanOrEqual(highPrice)) {
//                            stopLossPrice = stopLoss.level;
//                            stopLossAmount = stopLoss.amount;
//                            stopLossDescription = stopLoss.description;
//                            break;
//                        }
//                        if (stopLoss.level.isLessThan(stopLossPrice)) {
////                            if (stopLoss.strict && (stopLoss.level.isGreaterThan(highPrice) || stopLoss.level.isLessThan(lowPrice))) continue;
//                            stopLossPrice = stopLoss.level;
//                            stopLossAmount = stopLoss.amount;
//                            stopLossDescription = stopLoss.description;
//
//                        }
//                    }
////                    if (stopLossPrice.isGreaterThan(highPrice)) return;
////                    else {
////                        if (!strict && openPrice.isGreaterThanOrEqual(stopLossPrice)) stopLossPrice=openPrice;
////                    }
//                }

//                if (entryOrder.stopLoss != null && entryOrder.stopLoss.isGreaterThanOrEqual(lowPrice) && entryOrder.stopLoss.isLessThanOrEqual(highPrice)) {
//                    entryOrder.amountToClose=entryOrder.getAmount(); // ezt még javítani
//                    Order.closeAt(index, entryOrder, exitOrderId, Order.ExitType.STOPLOSS);
//                    exitOrderId++;
//                    if (!entryOrder.isClosed()) entryOrder.setStopLoss(index);
//                }
//            }
//        }
    }


    public void setExitLevels(int index) {

        for (SLTPManager exitLevelManager : strategy.positionManagement.exitLevelsForBuy) {
            exitLevelManager.process(index,Order.OrderType.BUY);
        }

        for (SLTPManager exitLevelManager : strategy.positionManagement.exitLevelsForSell) {
            exitLevelManager.process(index,Order.OrderType.SELL);
        }

        //        Num stopLossValue;
//        if (type.isBuy()) {
//            for (SLTPManager stopLossManager : tradingRecord.buyStrategy.stopLossManagers) {
//                stopLossValue=stopLossManager.getValue(index,this);
//                if (stopLossValue!=null) {
//
////                    stopLoss.set(id, stopLossValue, getOpenAmount(), stopLossManager.getName());
//                    if (tradingRecord.debug) tradingRecord.debug(index, id, stopLossValue.getDelegate(), stopLossManager.hashCode());
//                }
//            }
//        } else {
//            for (SLTPManager stopLossManager : tradingRecord.sellStrategy.stopLossManagers) {
//                stopLossValue=stopLossManager.getValue(index,this);
//                if (stopLossValue!=null) {
////                    stopLoss.set(id, stopLossValue, getOpenAmount(), stopLossManager.getName());
//                    if (tradingRecord.debug) tradingRecord.debug(index, id, stopLossValue.getDelegate(), stopLossManager.hashCode());
//                }
//            }
//        }

//        Order entryOrder;
//        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
//            entryOrder = entryOrders.get(i);
//            if (entryOrder.isOpen()) {
//                entryOrder.setStopLoss(index);
//            }
//        }
    }

//    public void setTakeProfit(int index) {
////        Order entryOrder;
////        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
////            entryOrder = entryOrders.get(i);
////            if (entryOrder.isOpen()) {
////                entryOrder.setTakeProfit(index);
////            }
////        }
//
//    }


//    public boolean hasOpenOrders() {
//        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
//            if (entryOrders.get(i).isOpen()) {
//                firstOpenOrderIndex = i;
//                return true;
//            }
//        }
//
//
////        for (Order entryOrder : entryOrders) {
////            if (entryOrder.isOpen()) return true;
////        }
//        return false;
//    }

//    public List<Order> getOpenOrders() {
//        List<Order> openOrders=new ArrayList<>();
//        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
//            if (entryOrders.get(i).isOpen())openOrders.add(entryOrders.get(i));
//        }
//        return openOrders;
//    }


    public Order entryOrder(Order.OrderType orderType, int index, Num price, Num amount) {
        Order newOrder = Order.openAt(orderType, index, price, amount, entryOrderId, this);
        entryOrders.add(newOrder);
        entryOrderId++;
        return newOrder;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseTradingRecord:\n");
        for (Order order : entryOrders) {
            sb.append(order.toString()).append("\n");
        }
        return sb.toString();
    }

    public Stream<Order> buyOrders() {
        return entryOrders.stream().filter(Order::isOpen).filter(Order::isBuy);
    }

    public Stream<Order> sellOrders() {
        return entryOrders.stream().filter(Order::isOpen).filter(Order::isSell);
    }

    public void setTrades() {

        entryOrders.stream().forEach(order -> {
            Trade currentTrade;
            if (order.exitOrders.size() == 0) {
                Order orderAtTheEnd;
                if (order.getType() == Order.OrderType.SELL)
                    orderAtTheEnd = Order.buyAt(series.getEndIndex(), series.getBar(series.getEndIndex()).getClosePrice(), order.getAmount());
                else
                    orderAtTheEnd = Order.sellAt(series.getEndIndex(), series.getBar(series.getEndIndex()).getClosePrice(), order.getAmount());
                orderAtTheEnd.exitType = Order.ExitType.ENDSERIES;
                currentTrade = new Trade(order, orderAtTheEnd);
                currentTrade.closed = true;
                trades.add(currentTrade);
            }
            if (order.exitOrders.size() == 1) {
//                if (order.getIndex()==7071) {
//                    System.out.println("HERE");
//                }

                currentTrade = new Trade(order, order.exitOrders.get(0));
                currentTrade.closed = true;
                trades.add(currentTrade);
            }
            if (order.exitOrders.size() > 1) {
                for (Order exitOrder : order.exitOrders) {
                    Order splittedEntryOrder;
                    if (order.getType() == Order.OrderType.BUY)
                        splittedEntryOrder = Order.buyAt(order.getIndex(), order.getPricePerAsset(), exitOrder.getAmount());
                    else
                        splittedEntryOrder = Order.sellAt(order.getIndex(), order.getPricePerAsset(), exitOrder.getAmount());
                    currentTrade = new Trade(splittedEntryOrder, exitOrder);
                    currentTrade.closed = true;
                    trades.add(currentTrade);
                }

            }

        });


        try {

//            stm.execute("DELETE FROM STRATEGY");
            PreparedStatement pstmt = sqlLiteConnector.con.prepareStatement("INSERT INTO STRATEGY_TRADE_HISTORY (" +
                    "  STRATEGY_ID ,\n" +
                    "  ORDER_ID ,\n" +
                    "  TYPE ,\n" +
                    "  AMOUNT ,\n" +
                    "  OPEN_TIME ,\n" +
                    "  OPEN_INDEX ,\n" +
                    "  OPEN_PRICE ,\n" +
                    "  CLOSE_TIME ,\n" +
                    "  CLOSE_INDEX ,\n" +
                    "  CLOSE_PRICE ,\n" +
                    "  CLOSE_BY ,\n" +
                    "  DURATION ,\n" +
                    "  PROFIT ,\n " +
                    "  COMMENT, \n " +
                    "  MAX_PROFIT, \n " +
                    "  MAX_LOSS ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");


            for (Trade trade : trades) {

                pstmt.setLong(1, strategyId);
                pstmt.setInt(2, trade.getEntry().getId());
                pstmt.setString(3, trade.getEntry().getType().toString());
                pstmt.setDouble(4, trade.getEntry().getAmount().doubleValue());
                pstmt.setString(5, series.getBar(trade.getEntry().getIndex()).getEndTime().toString());
                pstmt.setInt(6, trade.getEntry().getIndex());
                pstmt.setDouble(7, trade.getEntry().getNetPrice().doubleValue());

                pstmt.setString(8, series.getBar(trade.getExit().getIndex()).getEndTime().toString());
                pstmt.setInt(9, trade.getExit().getIndex());
                pstmt.setDouble(10, new BigDecimal(trade.getExit().getNetPrice().doubleValue()).setScale(5, RoundingMode.HALF_UP).doubleValue());

                pstmt.setString(11, trade.getExit().exitType.toString());
                pstmt.setInt(12, (trade.getExit().getIndex() - trade.getEntry().getIndex()));
                pstmt.setDouble(13, new BigDecimal(trade.getProfit().doubleValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
                pstmt.setString(14, trade.getExit().comment);
                pstmt.setDouble(15, trade.getEntry().maxProfit.doubleValue());
                pstmt.setDouble(16, trade.getEntry().maxLoss.doubleValue());
                pstmt.executeUpdate();


//                System.out.println("strategyId: "+strategyId +
//                                    "\r\n orderId: "+trade.getEntry().getId()+
//                        "\r\n type: "+trade.getEntry().getType()+
//                        "\r\n amount: "+trade.getEntry().getAmount()+
//                        "\r\n openTime: "+series.getBar(trade.getEntry().getIndex()).getEndTime()+
//                        "\r\n openIndex: "+trade.getEntry().getIndex()+
//                        "\r\n openPrice: "+trade.getEntry().getNetPrice()+
//                        "\r\n closeTime: "+series.getBar(trade.getExit().getIndex()).getEndTime()+
//                        "\r\n closeIndex: "+trade.getExit().getIndex()+
//                        "\r\n closePrice: "+trade.getExit().getNetPrice()+
//                        "\r\n duration: "+(trade.getExit().getIndex() - trade.getEntry().getIndex()) +
//                        "\r\n orderId: "+trade.getProfit()
//                );
//                System.out.println(" ");

            }

            Double formatttedValue;
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("#.00", otherSymbols);

            double totalTrade = 0.0, profitableTrade = 0.0, totalProfit = 0.0, losingTrade = 0.0;
            for (Trade trade : trades) {
                totalProfit += trade.getProfit().doubleValue();
                if (trade.getProfit().isGreaterThan(series.numOf(0.0))) profitableTrade++;
                if (trade.getProfit().isLessThan(series.numOf(0.0))) losingTrade++;
                totalTrade++;
            }

//            System.out.println("total: "+totalTrade+"  profitableTrade:"+profitableTrade+"  losingTrade:"+losingTrade);

            if (totalTrade == 0.0) return;

            pstmt = sqlLiteConnector.con.prepareStatement("update STRATEGY set TRADE_NUMBER=?," +
                    "PROFITABLE_TRADES_RATIO=?,PROFIT_DRAWDOWN=?,BALANCE_DRAWDOWN=?," +
                    "EQUITY_DRAWDOWN=?,MAX_OPEN_PROFIT=?,TOTAL_PROFIT=?,TOTAL_PROFIT_PER_MONTH=?,DAY_NUMBER=?,BAR_PROCESSTIME=? WHERE STRATEGY_ID=?");

            tradeSize = getTrades().size();
            pstmt.setInt(1, tradeSize);

            profitableTradesRatio = Double.parseDouble(df.format(100 * profitableTrade / (profitableTrade + losingTrade)));
            formatttedValue = Double.parseDouble(df.format(profitableTradesRatio));
            pstmt.setDouble(2, formatttedValue);


            formatttedValue = Double.parseDouble(df.format(100 * netBalanceDrawDown.doubleValue() / totalProfit));
            pstmt.setDouble(3, formatttedValue);

            balanceDrawDown = balanceDrawDown.multipliedBy(series.numOf(100.0));
            formatttedValue = Double.parseDouble(df.format(balanceDrawDown.doubleValue()));
            pstmt.setDouble(4, formatttedValue);

            equityDrawDown = equityDrawDown.multipliedBy(series.numOf(100.0));
            formatttedValue = Double.parseDouble(df.format(equityDrawDown.doubleValue()));
            pstmt.setDouble(5, formatttedValue);
            pstmt.setDouble(6, maximumOpenProfit.doubleValue());
            pstmt.setDouble(7, totalProfit);

            long dayNumber = ChronoUnit.DAYS.between(series.getBar(series.getBeginIndex()).getEndTime(), series.getBar(series.getEndIndex()).getEndTime());
            double monthDivider = dayNumber / 31.0;

            totalProfitPerMonth = Double.parseDouble(df.format(totalProfit / monthDivider));
            pstmt.setDouble(8, totalProfitPerMonth);
            pstmt.setLong(9, dayNumber);
            formatttedValue = Double.parseDouble(df.format(barPerTime));
            pstmt.setDouble(10, formatttedValue);

            pstmt.setLong(11, strategyId);

            pstmt.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void updateBalance(int index) {
        Num lastBalance = balance.get(index);
        Num grossProfit = series.numOf(0);
        Num newBalance;
        for (Order entryOrder : entryOrders) {
            if (entryOrder.closeIndex < index) continue;
            for (Order exitOrder : entryOrder.exitOrders) {
                if (exitOrder.getIndex() < index) continue;
                if (entryOrder.isBuy()) {
                    grossProfit = grossProfit.plus(exitOrder.getValue().minus(entryOrder.getValue()));
                } else {
                    grossProfit = grossProfit.plus(entryOrder.getValue().minus(exitOrder.getValue()));

                }
            }
        }
        newBalance = lastBalance.plus(grossProfit);
        balance.set(index, newBalance);
        balance.add(balance.get(index));
        if (newBalance.isGreaterThanOrEqual(maximumBalance)) maximumBalance = newBalance;

        if (maximumBalance.minus(newBalance).isGreaterThan(netBalanceDrawDown)) {
            netBalanceDrawDown = maximumBalance.minus(newBalance);
//            System.out.println(index + ". maximumBalance:" + maximumBalance + "  newBalance:" + newBalance + "  netBalanceDrawDown:" + netBalanceDrawDown);
        }
        if (netBalanceDrawDown.dividedBy(maximumBalance).isGreaterThan(balanceDrawDown)) {
            balanceDrawDown = netBalanceDrawDown.dividedBy(maximumBalance);

        }

    }

    public void updateEquity(int index) {
        Num lastBalance = balance.get(index);
        Num totalOpenProfit = series.numOf(0);
        Num openProfit = series.numOf(0);
        Num closePrice = series.getBar(index).getClosePrice();
        Order entryOrder;
        for (int i = firstOpenOrderIndex; i < entryOrders.size(); i++) {
            entryOrder = entryOrders.get(i);
            if (entryOrder.isOpen()) {

                if (entryOrder.isBuy()) {
                    openProfit = closePrice.minus(entryOrder.getPricePerAsset()).multipliedBy(entryOrder.getAmount().minus(entryOrder.fulfilled));
                } else {
                    openProfit = entryOrder.getPricePerAsset().minus(closePrice).multipliedBy(entryOrder.getAmount().minus(entryOrder.fulfilled));
                }
                if (openProfit.isGreaterThan(entryOrder.maxProfit)) entryOrder.maxProfit = openProfit;
                if (openProfit.isLessThan(entryOrder.maxLoss)) entryOrder.maxLoss = openProfit;
                totalOpenProfit = totalOpenProfit.plus(openProfit);
            }
        }
        equity.set(index, lastBalance.plus(totalOpenProfit));
        equity.add(equity.get(index));

        if (totalOpenProfit.isGreaterThan(maximumOpenProfit)) maximumOpenProfit = totalOpenProfit;

        if (totalOpenProfit.isLessThan(series.numOf(0.0))) {
            if (totalOpenProfit.multipliedBy(series.numOf(-1.0)).dividedBy(lastBalance).isGreaterThan(equityDrawDown)) {
                equityDrawDown = totalOpenProfit.multipliedBy(series.numOf(-1.0)).dividedBy(lastBalance);
//                System.out.println(index+". "+"lastBalance: "+lastBalance+"  totalOpenProfit:"+totalOpenProfit+"    equityDrawDown:"+equityDrawDown);
            }
        }

    }


    public void setAutoCommit(boolean autoCommit) {
        try {
            if (autoCommit) sqlLiteConnector.con.commit();
            sqlLiteConnector.con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void storeStrategy() {

        try {
            Statement stm = sqlLiteConnector.con.createStatement();

            stm.execute("DELETE FROM STRATEGY_TRADE_HISTORY");
            stm.execute("DELETE FROM STRATEGY_EVALUATION");
            stm.execute("DELETE FROM STRATEGY_PARAMETER");


            PreparedStatement pstmt = sqlLiteConnector.con.prepareStatement("INSERT INTO STRATEGY (STRATEGY_ID,BAR_NUMBER,PERIOD,SYMBOL,DESCRIPTION,ENTRY,STOPLOSS_TAKEPROFIT) VALUES (?,?,?,?,?,?,?)");
            pstmt.setLong(1, strategyId);
            pstmt.setInt(2, endIndex);
            pstmt.setInt(3, series.getPeriod());
            pstmt.setString(4, series.getOhlcvFileName());
            pstmt.setString(5, strategy.getClass().getCanonicalName());

//            pstmt.setString(5, "BUY ENTRY: " + buyStrategy.getEntryRule().getDescription() +
//                    " - BUY EXIT: " + (buyStrategy.getExitRule() != null ? buyStrategy.getExitRule().getDescription() : "") +
//                    " / SELL ENTRY: " + sellStrategy.getEntryRule().getDescription() +
//                    " - SELL EXIT: " + (sellStrategy.getExitRule() != null ? sellStrategy.getExitRule().getDescription() : ""));
            pstmt.setString(6, getSourceContent(strategy));
            pstmt.setString(7, getSourceContent(strategy.positionManagement));

            pstmt.executeUpdate();


//            sqlLiteConnector.con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    String getSourceContent(Object object) {
        String packages[] = object.toString().split("\\.");
        String path = System.getProperty("user.dir") + "\\ta4j-core\\src\\main\\java";
        for (int i = 0; i < packages.length - 1; i++) path += "\\" + packages[i];
//        File file = new File(path+"\\"+strategyElement.getClass().getSimpleName()+".java'");
        String data = "";

        try {
            Stream<String> lines = Files.lines(Paths.get(path + "\\" + object.getClass().getSimpleName() + ".java"));
            data = lines.collect(Collectors.joining("\n"));
            lines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


    public void setDebug(boolean on) {
        debug = on;
        try {
            insertDebugInfo = sqlLiteConnector.con.prepareStatement("INSERT INTO STRATEGY_EVALUATION VALUES (?,?,?,?,?,?)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Rule rule;
        if (debug) {
            try {
                PreparedStatement pstmt = sqlLiteConnector.con.prepareStatement("INSERT INTO STRATEGY_PARAMETER (STRATEGY_ID,TYPE,ACTION,DESCRIPTION,HASHCODE) VALUES (?,?,?,?,?)");
                if (strategy.ruleForBuy != null) {
                    HashMap<Integer, String> hm = strategy.ruleForBuy.getRuleItems();
                    rule = strategy.ruleForBuy;
                    rule.getDescription();


                    for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                        pstmt.setLong(1, strategyId);
                        pstmt.setString(2, "BUY");
                        pstmt.setString(3, "ENTRY");
                        pstmt.setString(4, strategy.ruleForBuy.getRuleItems().get(ruleHashMap));
                        pstmt.setInt(5, ruleHashMap);
                        pstmt.executeUpdate();
                        //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                    }

                    if (strategy.ruleForBuy != null) {
                        rule = strategy.ruleForBuy;
                        rule.getDescription();
                        for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                            pstmt.setLong(1, strategyId);
                            pstmt.setString(2, "BUY");
                            pstmt.setString(3, "EXIT");
                            pstmt.setString(4, strategy.ruleForBuy.getRuleItems().get(ruleHashMap));
                            pstmt.setInt(5, ruleHashMap);
                            pstmt.executeUpdate();
                            //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                        }
                    }

                    for (SLTPManager takeProfitManager : strategy.positionManagement.exitLevelsForBuy) {
                        pstmt.setLong(1, strategyId);
                        pstmt.setString(2, "BUY");
                        pstmt.setString(3, "EXIT LEVEL");
                        pstmt.setString(4, takeProfitManager.getParameters());
                        pstmt.setInt(5, takeProfitManager.hashCode());
                        pstmt.executeUpdate();
                        //                    System.out.println(takeProfitManager.getParameters());
                    }

                }

                if (strategy.ruleForSell != null) {
                    rule = strategy.ruleForSell;
                    rule.getDescription();
                    for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                        pstmt.setLong(1, strategyId);
                        pstmt.setString(2, "SELL");
                        pstmt.setString(3, "ENTRY");
                        pstmt.setString(4, strategy.ruleForSell.getRuleItems().get(ruleHashMap));
                        pstmt.setInt(5, ruleHashMap);
                        pstmt.executeUpdate();
                        //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                    }

                    if (strategy.ruleForSell != null) {
                        rule = strategy.ruleForSell;
                        rule.getDescription();

                        for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                            pstmt.setLong(1, strategyId);
                            pstmt.setString(2, "SELL");
                            pstmt.setString(3, "EXIT");
                            pstmt.setString(4, strategy.ruleForSell.getRuleItems().get(ruleHashMap));
                            pstmt.setInt(5, ruleHashMap);
                            pstmt.executeUpdate();
                            //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                        }
                    }

                    for (SLTPManager takeProfitManager : strategy.positionManagement.exitLevelsForSell) {
                        pstmt.setLong(1, strategyId);
                        pstmt.setString(2, "SELL");
                        pstmt.setString(3, "EXIT LEVEL");
                        pstmt.setString(4, takeProfitManager.getParameters());
                        pstmt.setInt(5, takeProfitManager.hashCode());
                        pstmt.executeUpdate();
                        //                    System.out.println(takeProfitManager.getParameters());
                    }

                }

                for (Indicator indicator : strategy.indicitatorsToDebug) {
                    pstmt.setLong(1, strategyId);
                    pstmt.setString(2, "INDICATOR");
                    pstmt.setString(3, "INDICATOR");
                    pstmt.setString(4, indicator.getClass().getSimpleName());
                    pstmt.setInt(5, indicator.hashCode());
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


//
//        try {
//            Field[] fields = indicatorSet.getClass().getFields();
//            for (Field field: fields){
//                if (field.get(indicatorSet)!=null) {
//                    Object object=field.get(indicatorSet);
//                    if (object instanceof Indicator) {
//                        indicitatorsToDebug.add((Indicator)object);
//                    }
//                }
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void debug(int index, int orderId, Number value, int hashCode) {
//        System.out.println(backTestContext.strategyId+" "+hashCode()+" "+satisfied);
        if (value == null) return;
        try {
            insertDebugInfo.setLong(1, strategyId);
            insertDebugInfo.setInt(2, hashCode);
            insertDebugInfo.setInt(3, index);
            insertDebugInfo.setInt(4, orderId);
            insertDebugInfo.setDouble(5, value.doubleValue());
            insertDebugInfo.setString(6, series.getBar(index).getEndTime().toString());
            insertDebugInfo.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void onBarChange(){
        int i=1;
        if (strategy.ruleForSell != null && strategy.ruleForSell.isSatisfied(i)) {
            Order newEntry = entryOrder(Order.OrderType.SELL, i, series.getBar(i).getClosePrice(), tradingAmount);
            openOrders.put(newEntry.getId(), newEntry);

        }

        if (strategy.ruleForBuy != null && strategy.ruleForBuy.isSatisfied(i)) {
            Order newEntry = entryOrder(Order.OrderType.BUY, i, series.getBar(i).getClosePrice(), tradingAmount);
            openOrders.put(newEntry.getId(), newEntry);
        }

    }

    public void runBackTest() {

        long startTime = System.currentTimeMillis();
        setAutoCommit(false);

        int tempCounter = 0;
        for (int i = beginIndex; i <= endIndex; i++) {
//            if (tempCounter>100000) {
//                tempCounter=0;
//                System.out.print("#");
//            } else tempCounter++;
//            System.out.println(i);
            if (debug) {
                int index=-1;
                ZonedDateTime time;
                for (Indicator indicator : strategy.indicitatorsToDebug) {
//                    int indexForSecond=timeSeriesRepo.barIndex.get(periodForSecond);
                    if (indicator.getTimeSeries().getPeriod()==period) index=i;
                    else {
                        TimeSeriesRepo timeSeriesRepo = indicator.getTimeSeries().getTimeSeriesRepo();
                        time=series.getBar(i).getEndTime();
                        index = timeSeriesRepo.getIndex(time,indicator.getTimeSeries().getPeriod());
                    }
                    if (index>-1) {
                        Num iValue = (Num) indicator.getValue(index);
                        debug(index, 0, iValue.getDelegate(), indicator.hashCode());
                    }
                }
            }

            if (openOrders.size() > 0) {
                exitByStopLoss(i);
                exitByTakeProfit(i);
                exitByRule(i);


                setExitLevels(i);
//               setStopLoss(i);
//               setTakeProfit(i);


            }

            ZonedDateTime barTime=series.getBar(i).getEndTime();
//            System.out.println(i);
            if (strategy.ruleForSell != null && strategy.ruleForSell.isSatisfied(barTime)) {
                System.out.println(i+" SELL  "+barTime);

                Order newEntry = entryOrder(Order.OrderType.SELL, i, series.getBar(i).getClosePrice(), tradingAmount);
                openOrders.put(newEntry.getId(), newEntry);
//                return;

            }

            if (strategy.ruleForBuy != null && strategy.ruleForBuy.isSatisfied(barTime)) {
                System.out.println(i+" BUY  "+barTime);
                Order newEntry = entryOrder(Order.OrderType.BUY, i, series.getBar(i).getClosePrice(), tradingAmount);
                openOrders.put(newEntry.getId(), newEntry);
//                return;
            }

            updateBalance(i);
            updateEquity(i);

        }
        barPerTime = (double) endIndex / (System.currentTimeMillis() - startTime);
        setTrades();

        setAutoCommit(true);



    }

    public void getLastStrategyResults(int number) {
        try {
            PreparedStatement prs = sqlLiteConnector.con.prepareStatement("select * from STRATEGY order by STRATEGY_ID desc");
            ResultSet rs = prs.executeQuery();
            List<String> result = new ArrayList<>();
            int i = 0;
            while (rs.next()) {
                result.add("Trade size: " + rs.getString("TRADE_NUMBER") +
                        ", profitable: " + rs.getString("PROFITABLE_TRADES_RATIO") +
                        ", balance dd: " + rs.getString("BALANCE_DRAWDOWN") +
                        ", equity dd: " + rs.getString("EQUITY_DRAWDOWN") +
                        ", total profit/month: " + rs.getString("TOTAL_PROFIT_PER_MONTH"));
                i++;
                if (i == number) break;
            }
            rs.close();
            for (int j = result.size() - 1; j > -1; j--) {
                if (j == 0) System.out.println("");
                System.out.println(result.get(j));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getIndex(int index, int period) {
        if (this.period == period) return index;
        index = strategy.timeSeries.getTimeSeries(period).getIndex(series.getBar(index).getEndTime()) - 1;  // -1 szükségese mert különben előrelátnánk
        if (index < 0) index = 0;
        return index;
    }


    public void debugRule(int index, Rule rule, boolean satisfied) {
        if (debug) debug(index, 0, satisfied ? 1.0 : 0.0, rule.hashCode());
    }


}
