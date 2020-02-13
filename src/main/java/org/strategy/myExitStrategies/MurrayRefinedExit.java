package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;


public class MurrayRefinedExit extends Strategy {
    ATRIndicator atrIndicator;
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    ClosePriceIndicator closePrice;
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;

    public void init() {

        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 128, i);
        }

        atrIndicator = new ATRIndicator(tradeEngine.series, 54);

        closePrice = new ClosePriceIndicator(tradeEngine.series);
        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 2.6, 54);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 2.6, 54);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);

        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);


    }


    public void onTradeEvent(Order order) {

        if (order.type == Order.Type.BUY)
            tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
        else
            tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);

        if (order.type == Order.Type.BUY)
            tradeEngine.setExitPrice(order, order.openPrice + (order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()), TradeEngine.ExitMode.TAKEPROFIT, true);
        else
            tradeEngine.setExitPrice(order, order.openPrice - (highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice), TradeEngine.ExitMode.TAKEPROFIT, true);
//        double murrayLevelMultiplier = 1.6, atrMultiplier = 1.0;
//
//        int lowerIndex=-1, upperIndex=-1;
//        double lowerPrice, upperPrice,stopLoss=0.0,takeProfit=0.0;
//        double height = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();
//
//        lowerPrice = order.openPrice - atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - murrayLevelMultiplier * height;
//
//        upperPrice = order.openPrice + atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + murrayLevelMultiplier * height;
//
//
//        for (int i = 0; i < 12; i++) {
////            double ml=murrayMathIndicators[murrayRange].getValue(tradeEngine.currentBarIndex).doubleValue();
////            double mu=murrayMathIndicators[murrayRange+1].getValue(tradeEngine.currentBarIndex).doubleValue();
//            if (lowerPrice >= murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() && lowerPrice <= murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue())
//                lowerIndex=i;
//            if (upperPrice >= murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() && upperPrice <= murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue()) {
//                upperIndex = i+1;
//                break;
//            }
//        }
//
//        if (lowerIndex<0) {
//            if (order.type == Order.Type.BUY) stopLoss=lowerPrice;
//            else takeProfit=lowerPrice;
//        } else {
//            if (order.type == Order.Type.BUY) stopLoss=murrayMathIndicators[lowerIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
//            else takeProfit=murrayMathIndicators[lowerIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
//        }
//
//        if (upperIndex<0) {
//            if (order.type == Order.Type.BUY) takeProfit=upperPrice;
//            else stopLoss=upperPrice;
//        } else {
//            if (order.type == Order.Type.BUY) takeProfit=murrayMathIndicators[upperIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
//            else stopLoss=murrayMathIndicators[upperIndex].getValue(tradeEngine.currentBarIndex).doubleValue();;
//
//        }
//
////        if (order.type == Order.Type.BUY) {
////            takeProfit=takeProfit - atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
////            stopLoss=stopLoss - atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
////        } else {
////            takeProfit=takeProfit + atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
////            stopLoss=stopLoss + atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
////        }
//
////        tradeEngine.setExitPrice(order, takeProfit, TradeEngine.ExitMode.TAKEPROFIT, true);
//        tradeEngine.setExitPrice(order, stopLoss, TradeEngine.ExitMode.STOPLOSS, true);
//        tradeEngine.setExitPrice(order, takeProfit, TradeEngine.ExitMode.TAKEPROFIT, true);

//        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);


    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
        if (order.phase == 0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0) {
            order.closedAmount = order.openedAmount / 2.0;
            order.phase = 1;
            if (order.type == Order.Type.BUY) {
                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            } else {
                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            }
            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.STOPLOSS, false);
        }

//        order.closedAmount = order.openedAmount;
        else if (order.phase == 1 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0) {
            order.closedAmount = order.openedAmount / 2.0;
            order.phase = 2;

            if (order.type == Order.Type.BUY) {
                tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                tradeEngine.setExitPrice(order, 100000.0, TradeEngine.ExitMode.TAKEPROFIT, false);
              } else {
                tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                tradeEngine.setExitPrice(order, 0.0, TradeEngine.ExitMode.TAKEPROFIT, false);
            }
        } else order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.timeFrame == timeFrame) {
            double height = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();
            for (Order order : tradeEngine.openedOrders) {
                if (order.phase == 0)
                    tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                if (order.phase == 2) {
                    tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                    if (order.type == Order.Type.BUY)  tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                    else tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                }
                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.series.getCurrentIndex() - openIndex > 21) {
                    if (order.type == Order.Type.BUY) height = height / 8.0;
                    else height = height / -8.0;
                    tradeEngine.setExitPrice(order, order.openPrice + height, TradeEngine.ExitMode.ANY, true);
                }

//                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                        tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue() , TradeEngine.ExitMode.STOPLOSS, true);

            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}