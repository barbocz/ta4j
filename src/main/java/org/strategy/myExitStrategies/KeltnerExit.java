package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;


public class KeltnerExit extends Strategy {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;
    ATRIndicator atrIndicator;



    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 34);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 3.4, 34);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 3.4, 34);
        atrIndicator = new ATRIndicator(tradeEngine.series, 34);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);

        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);


    }


    public void onTradeEvent(Order order) {
//        double atrValue = 5.0 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//        if (order.type == Order.Type.BUY) {
//
//            tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//            if (order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < atrValue)
//                tradeEngine.setExitPrice(order, order.openPrice - atrValue, TradeEngine.ExitMode.STOPLOSS, true);
//            else
//                tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//
//        } else {
//            tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//            if (highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice < atrValue)
//                tradeEngine.setExitPrice(order, order.openPrice + atrValue, TradeEngine.ExitMode.STOPLOSS, true);
//            else
//                tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onBeforeCloseOrder(Order order) {
        if (order.phase == 0 ) {
            order.closedAmount = order.openedAmount / 2.0;
            order.phase = 1;
            if (order.type == Order.Type.BUY) {
                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            } else {
                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            }
            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.STOPLOSS, false);
        } else order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.period == timeFrame) {
            double atrValueCorrection =  atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            double atrValueLimit = 5.0 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            double atrStopLoss = 1.0 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            for (Order order : tradeEngine.openedOrders) {

                if (order.phase == 0) {
                    if (order.type == Order.Type.BUY) {

                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        if (order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < atrValueCorrection || order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > atrValueLimit)
                            tradeEngine.setExitPrice(order, order.openPrice - atrStopLoss, TradeEngine.ExitMode.STOPLOSS, true);
                        else
                            tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - atrValueCorrection, TradeEngine.ExitMode.STOPLOSS, true);

                    } else {
                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        if (highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice < atrValueCorrection || highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice>atrValueLimit)
                            tradeEngine.setExitPrice(order, order.openPrice + atrStopLoss, TradeEngine.ExitMode.STOPLOSS, true);
                        else
                            tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+ atrValueCorrection, TradeEngine.ExitMode.STOPLOSS, true);

                    }

                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (tradeEngine.series.getCurrentIndex() - openIndex > 14)
                    {
                        if (order.type == Order.Type.BUY) atrValueCorrection = 0.3 * atrValueCorrection;
                        else atrValueCorrection = -0.3 * atrValueCorrection;
                        tradeEngine.setExitPrice(order, order.openPrice+atrValueCorrection, TradeEngine.ExitMode.ANY, true);
                    }

                } else {
                    if (order.type == Order.Type.BUY) {
                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    } else {
                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    }

                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (tradeEngine.series.getCurrentIndex() - openIndex >14)
                    {
                        if (order.type == Order.Type.BUY) atrValueCorrection = 0.3 * atrValueCorrection;
                        else atrValueCorrection = -0.3 * atrValueCorrection;
                        tradeEngine.setExitPrice(order, order.openPrice+atrValueCorrection, TradeEngine.ExitMode.ANY, true);
                    }

                }

            }
        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}