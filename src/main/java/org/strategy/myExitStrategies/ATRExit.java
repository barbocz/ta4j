package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.num.Num;


public class ATRExit extends Strategy {


    ATRIndicator atrIndicator;
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;

    ParabolicSarIndicator parabolicSarIndicator;


    public void init() {
        atrIndicator = new ATRIndicator(tradeEngine.series, 34);
        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 2.6, 54);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 2.6, 54);
        Num sarStep = tradeEngine.series.numOf(0.02);
        Num sarMax = tradeEngine.series.numOf(1);

        parabolicSarIndicator = new ParabolicSarIndicator(tradeEngine.series, sarStep, sarMax);
    }


    public void onTradeEvent(Order order) {
        double atrCorrection = 0.3 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        if (order.type == Order.Type.BUY)
            tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - atrCorrection, TradeEngine.ExitMode.STOPLOSS, false);
        else
            tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + atrCorrection, TradeEngine.ExitMode.STOPLOSS, false);

//        if (order.type == Order.Type.BUY) {
////            order.takeProfit = order.openPrice + multiplier *atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//            order.stopLoss = order.openPrice -  multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
////            order.stopLoss =  parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//        } else {
////            order.takeProfit = order.openPrice -  multiplier *atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//            order.stopLoss = order.openPrice +  multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//        }
    }

//    public void onExitEvent(Order order){
//        order.closedAmount=order.amount;
//    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+timeSeriesRepo.bid);


    }

//    @Override
//    public void onExitEvent(Order order){
//        if (order.closePhase==0 && order.getProfit()>0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.closePhase = 1;
//            if (order.type == Order.Type.BUY) {
//                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            } else {
//                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            }
//            tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.STOPLOSS, false);
//        } else order.closedAmount = order.openedAmount;
//
//    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        if (tradeEngine.period == timeFrame) {

//            double atrValueCorrection =  atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//            double atrValueLimit = 5.0 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//            double atrStopLoss = 1 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            double multiplier = 1.5;
            double atrCorrection = 0.3 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            for (Order order : tradeEngine.openedOrders) {
                if (order.type == Order.Type.BUY)
                    tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - atrCorrection, TradeEngine.ExitMode.STOPLOSS, true);
                else
                    tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + atrCorrection, TradeEngine.ExitMode.STOPLOSS, true);


//                tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() , TradeEngine.ExitMode.STOPLOSS, true);
//                if (order.closePhase == 0) {
//                    if (order.type == Order.Type.BUY) {
////                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        if (tradeEngine.timeSeriesRepo.bid - multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>order.openPrice)
//                            tradeEngine.setExitPrice(order, tradeEngine.timeSeriesRepo.bid - multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//
//                    } else {
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        if (tradeEngine.timeSeriesRepo.ask + multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<order.openPrice)
//                            tradeEngine.setExitPrice(order, tradeEngine.timeSeriesRepo.ask + multiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.ANY, true);
//                    }
//
////                    int openIndex = tradeEngine.series.getIndex(order.openTime);
////                    if (tradeEngine.series.getCurrentIndex() - openIndex > 4)
////                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
//                }
////                else {
////                    if (order.type == Order.Type.BUY) {
////                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                    } else {
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                    }
////
////                    int openIndex = tradeEngine.series.getIndex(order.openTime);
////                    if (tradeEngine.series.getCurrentIndex() - openIndex > 21)
////                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
////
////                }

            }
        }


    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());
//        if (preZdt==series.getCurrentTime()) System.out.println("HIBA------------------------");
//        preZdt=series.getCurrentTime();
//        System.out.println(new SimpleDateFormat("mm:ss:SSS").format(new Date())+" Keltner onOneMinuteDataEvent------------- START"+eventSeries.timeFrame);
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(new SimpleDateFormat("mm:ss:SSS").format(new Date())+" Keltner onOneMinuteDataEvent------------- END"+eventSeries.timeFrame);

    }


}