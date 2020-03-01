package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.WeightedCloseIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import static org.strategy.Order.ExitType.EXITRULE;


public class MurrayMiracleExit extends Strategy {
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    RSIIndicator rsiIndicator;
    MoneyFlowIndicator moneyFlowIndicator;
    PreviousValueIndicator prevMoneyFlowIndicator;


    public void init() {
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 38.0);
        }
        WeightedCloseIndicator weightedCloseIndicator = new WeightedCloseIndicator(tradeEngine.series);
        rsiIndicator = new RSIIndicator(weightedCloseIndicator, 5);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        prevMoneyFlowIndicator = new PreviousValueIndicator(moneyFlowIndicator);

    }


    public void onTradeEvent(Order order) {
//         if (tradeEngine.currentBarIndex>7130) {
//            System.out.println("Break");
//        }
        setTakeProfitStoploss(order, order.openPrice, false);


    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
        boolean closerStoplossNeeded=false;
        if (order.exitType == Order.ExitType.TAKEPROFIT && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 1.0) {
            if (order.phase == 0) order.closedAmount = order.openedAmount / 20.0;
            else {
                if (order.type == Order.Type.BUY) {
                    if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>70) order.closedAmount = order.openedAmount/5;
                    else if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>90) {
                        order.closedAmount = order.openedAmount/2;
                        closerStoplossNeeded=true;
                    }
                    else  order.closedAmount = order.openedAmount / 10.0;

                } else {
                    if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<30) order.closedAmount = order.openedAmount/5;
                    else if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<10) {
                        order.closedAmount = order.openedAmount/2;
                        closerStoplossNeeded=true;
                    }
                    else  order.closedAmount = order.openedAmount / 10.0;

                }
            }
            order.phase++;
            setTakeProfitStoploss(order, tradeEngine.timeSeriesRepo.bid, closerStoplossNeeded);
//            if (order.type == Order.Type.BUY) {
////                tradeEngine.setExitPrice(order, order.takeProfit+murrayHeight, TradeEngine.ExitMode.TAKEPROFIT, false);
////                tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, false);
//                order.takeProfit = order.takeProfit + 0.00038;
//                order.stopLoss = order.openPrice;
//            } else {
//                order.takeProfit = order.takeProfit - 0.00038;
//                order.stopLoss = order.openPrice;
////                tradeEngine.setExitPrice(order, order.takeProfit-murrayHeight, TradeEngine.ExitMode.TAKEPROFIT, false);
////                tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, false);
//            }
        } else order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        for (Order order : tradeEngine.openedOrders) {
            int openIndex = tradeEngine.series.getIndex(order.openTime);
            if (order.phase == 0 && tradeEngine.series.getCurrentIndex() - openIndex > 8)
                tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);

//            if (order.type == Order.Type.BUY && rsiIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > 80.0) {
//                setTakeProfitStoploss(order, tradeEngine.timeSeriesRepo.bid, true);
//            }
//            if (order.type == Order.Type.SELL && rsiIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < 15.0) {
//                setTakeProfitStoploss(order, tradeEngine.timeSeriesRepo.bid, true);
//            }

            if (order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)<0.0) {
                if (order.type == Order.Type.BUY) {
                    if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).isLessThan(prevMoneyFlowIndicator.getValue(tradeEngine.currentBarIndex))) {
                        order.closedAmount = order.openedAmount;
                        order.closePrice = tradeEngine.timeSeriesRepo.bid;
                        order.exitType = EXITRULE;
                        tradeEngine.closeOrder(order);
                    }
                } else {
                    if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).isGreaterThan(prevMoneyFlowIndicator.getValue(tradeEngine.currentBarIndex))) {
                        order.closedAmount = order.openedAmount;
                        order.closePrice = tradeEngine.timeSeriesRepo.bid;
                        order.exitType = EXITRULE;
                        tradeEngine.closeOrder(order);
                    }
                }
            }

        }

//        for (Order order : tradeEngine.openedOrders) {
//            int openIndex = tradeEngine.series.getIndex(order.openTime);
//            if (tradeEngine.series.getCurrentIndex() - openIndex > 7) {
//                order.closedAmount = order.openedAmount;
//                order.closePrice = tradeEngine.timeSeriesRepo.bid;
//                order.exitType = EXITRULE;
//                tradeEngine.closeOrder(order);
//            }
//        }
//
//        tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }

    void setTakeProfitStoploss(Order order, double currentPrice, boolean closerStopNeeded) {
        if (murrayLevels[0]==null) return;
        int murrayRange;
        double range = 0.0003;
        if (closerStopNeeded) range=0.00010;
        if (order.type == Order.Type.BUY) {

            murrayRange = getMurrayRange(currentPrice - range);
            if (murrayRange < 0) order.stopLoss = currentPrice - range;
            else
                order.stopLoss = murrayMathIndicators[murrayRange].getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00018;
            if (order.phase == 1 && order.stopLoss < order.openPrice) order.stopLoss = order.openPrice + 0.00007;


            if (!closerStopNeeded) {
                murrayRange = getMurrayRange(currentPrice + 0.0002);
                if (murrayRange < 0 || murrayRange == 12) order.takeProfit = currentPrice + 0.0002;
                else
                    order.takeProfit = murrayMathIndicators[murrayRange + 1].getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00003;
            }


        } else {

            murrayRange = getMurrayRange(currentPrice + range);
            if (murrayRange < 0 || murrayRange == 12) order.stopLoss = currentPrice + range;
            else
                order.stopLoss = murrayMathIndicators[murrayRange + 1].getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00018;
            if (order.phase == 1 && order.stopLoss > order.openPrice) order.stopLoss = order.openPrice - 0.00007;


            if (!closerStopNeeded) {
                murrayRange = getMurrayRange(currentPrice - 0.0002);
                if (murrayRange < 0) order.takeProfit = currentPrice - 0.0002;
                else
                    order.takeProfit = murrayMathIndicators[murrayRange].getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00003;
            }
        }
    }

    int getMurrayRange(double value) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        }

        return -1;
    }


}