package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;


public class RebounceExit extends Strategy {
    final Double murrayRange = 76.24;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    MoneyFlowIndicator moneyFlowIndicator, moneyFlowIndicatorSlower;
    LaguerreIndicator laguerreIndicator;
    ClosePriceIndicator closePriceIndicator;
    double murrayHeight = 0.0;
    double signalLevel = 0; // 0: Hold, 3: Strong sell, -3 Strong buy
    int buyCounter = 0, sellCounter = 0;

    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicatorSlower = new MoneyFlowIndicator(tradeEngine.series, 8);
        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
    }


    public void onTradeEvent(Order order) {
        double murrayMultiplier = 1.0;
        if (murrayHeight < 0.001) murrayMultiplier = 2.0;
        if (order.type == Order.Type.BUY) {
//            order.takeProfit=murrayLevels[4]-0.00005;

            tradeEngine.setStopLoss(order,order.openPrice - 3*murrayMultiplier * murrayHeight);
            tradeEngine.setTakeProfit(order,order.openPrice +murrayMultiplier * murrayHeight);
            order.takeProfitTarget = order.openPrice+murrayHeight;
            tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
            order.phase = buyCounter;
            if (buyCounter > 0) {


                tradeEngine.setTakeProfit(order, order.openPrice + murrayMultiplier * murrayHeight);
//                order.takeProfit = order.openPrice + murrayMultiplier * murrayHeight;
                for (Order currentOrder : tradeEngine.openedOrders) {
                    if (currentOrder.type == Order.Type.BUY) {
                        tradeEngine.setTakeProfit(currentOrder, order.takeProfit);

                    }
                }
            }
        } else {
//            order.takeProfit=murrayLevels[8]+0.00005;
//                    if (tradeEngine.currentBarIndex>8923) {
//            System.out.println("");
//        }

            tradeEngine.setStopLoss(order,order.openPrice + 3*murrayMultiplier * murrayHeight);
            tradeEngine.setTakeProfit(order,order.openPrice - murrayMultiplier * murrayHeight);
            order.takeProfitTarget = order.openPrice-murrayHeight;
            tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
            order.phase = sellCounter;
            if (sellCounter > 0) {


//                order.takeProfit = order.openPrice - murrayMultiplier * murrayHeight;
                tradeEngine.setTakeProfit(order, order.openPrice - murrayMultiplier * murrayHeight);
                for (Order currentOrder : tradeEngine.openedOrders) {
                    if (currentOrder.type == Order.Type.SELL) {
                        tradeEngine.setTakeProfit(currentOrder, order.takeProfit);

                    }

                }
            }
        }

    }


    @Override
    public void onAfterCloseOrder(Order order) {
//        if (order.exitType == TAKEPROFIT && order.phase==0) {
//            order.phase++;
//
//            if (order.type == Order.Type.BUY) {
//
//                tradeEngine.setTakeProfit(order, 0.0);
//                tradeEngine.setStopLoss(order,order.openPrice+0.0001 );
//                order.keepItsProfit=true;
//            } else {
//                tradeEngine.setTakeProfit(order, 0.0);
//                tradeEngine.setStopLoss(order,order.openPrice-0.0001 );
//                order.keepItsProfit=true;
//            }
//
//        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
//        for (Order order : tradeEngine.openedOrders) {
//            if (order.type == Order.Type.BUY){
//
//                if (tradeEngine.timeSeriesRepo.bid>order.takeProfitTarget) {
//                    tradeEngine.setStopLoss(order,order.openPrice+0.00015 );
//                    order.takeProfitTarget=Double.MAX_VALUE;
//                }
//            } else  if (order.type == Order.Type.SELL) {
//
//                if (tradeEngine.timeSeriesRepo.ask<order.takeProfitTarget) {
//                    tradeEngine.setStopLoss(order,order.openPrice-0.00015 );
//                    order.takeProfitTarget=0.0;
//                }
//            }
//
//        }

    }

    @Override
    public void onBeforeCloseOrder(Order order) {
        order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        murrayHeight = murrayLevels[1] - murrayLevels[0];

        signalLevel = 0.0;
        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() == 0.0) signalLevel--;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue() < 15.0) signalLevel--;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < 0.1) signalLevel--;

        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() == 100.0) signalLevel++;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue() > 85.0) signalLevel++;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > 0.9) signalLevel++;
        double closePrice = closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();

        buyCounter = 0;
        sellCounter = 0;
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY) buyCounter++;
            else sellCounter++;
        }

        for (Order order : tradeEngine.openedOrders) {
            int openIndex = tradeEngine.series.getIndex(order.openTime);
            if (buyCounter==1 && order.type == Order.Type.BUY && tradeEngine.series.getCurrentIndex()-openIndex>3) tradeEngine.setTakeProfit(order,0.0);
            if (sellCounter==1 && order.type == Order.Type.SELL && tradeEngine.series.getCurrentIndex()-openIndex>3) tradeEngine.setTakeProfit(order,0.0);
//            if (tradeEngine.series.getCurrentIndex() - openIndex > 60) {
//                order.closedAmount = order.openedAmount;
//                order.closePrice = tradeEngine.timeSeriesRepo.bid;
//                order.exitType = EXITRULE;
//                tradeEngine.closeOrder(order);
//            }

            if (order.type == Order.Type.BUY && signalLevel > 0 && buyCounter==1) {
                tradeEngine.setStopLoss(order, getNextMurrayLevel(closePrice, -0.0002, false));
                tradeEngine.setTakeProfit(order, getNextMurrayLevel(closePrice, -0.00008, true));
            } else if (order.type == Order.Type.SELL && signalLevel < 0  && sellCounter==1) {
                tradeEngine.setStopLoss(order, getNextMurrayLevel(closePrice, 0.0002, true));
                tradeEngine.setTakeProfit(order, getNextMurrayLevel(closePrice, 0.00008, false));
            }

        }


        tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);


    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

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

    double getNextMurrayLevel(double currentPrice, double bias, boolean upper) {
        int murrayLevel;

//        if (tradeEngine.currentBarIndex>12850) {
//            System.out.println("");
//        }
        if (upper) {
            murrayLevel = getMurrayRange(currentPrice);
            if (murrayLevel < 0 || murrayLevel == 12) {
                if (currentPrice > murrayLevels[12]) return murrayLevels[12] + bias;
                return murrayLevels[12] + bias;
            } else {
                murrayLevel++;
                return murrayLevels[murrayLevel] + bias;
            }
        } else {
            murrayLevel = getMurrayRange(currentPrice);
            if (murrayLevel < 0) {
                if (currentPrice < murrayLevels[0]) return murrayLevels[0] - bias;
                return murrayLevels[0] + bias;
            } else return murrayLevels[murrayLevel] + bias;
        }


    }


}