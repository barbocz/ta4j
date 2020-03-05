package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;


public class BollingerExit extends Strategy {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;

    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;

    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    final Double murrayRange = 30.0;
    final Double murrayRangeInPip = murrayRange / 100000.0;


    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);


        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);


        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);
        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);


    }


    public void onTradeEvent(Order order) {
        int murrayStopLossLevel;
        if (order.type == Order.Type.BUY) {
            murrayStopLossLevel = getMurrayRange(order.openPrice - murrayRangeInPip);
            if (murrayStopLossLevel < 0) murrayStopLossLevel = 0;
            order.stopLoss = murrayLevels[murrayStopLossLevel] - 0.00015;;

            order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00007;


        } else {

            order.stopLoss = order.openPrice + 0.00328;
            murrayStopLossLevel = getMurrayRange(order.openPrice + murrayRangeInPip);
            if (murrayStopLossLevel < 0 || murrayStopLossLevel == 12) murrayStopLossLevel = 12;
            else murrayStopLossLevel++;
            order.stopLoss = murrayLevels[murrayStopLossLevel]+ 0.00015;;

            order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00007;
        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
//        for (Order order : tradeEngine.openedOrders) {
//            if (order.type == Order.Type.BUY && order.stopLoss<order.openPrice && order.openPrice+0.0005<tradeEngine.timeSeriesRepo.bid) order.stopLoss=order.openPrice;
//            if (order.type == Order.Type.SELL && order.stopLoss>order.openPrice && order.openPrice-0.0005>tradeEngine.timeSeriesRepo.ask) order.stopLoss=order.openPrice;
//        }

    }

    @Override
    public void onExitEvent(Order order) {
     order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY) order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00007;
            else order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00007;

            int openIndex = tradeEngine.series.getIndex(order.openTime);
            if (tradeEngine.series.getCurrentIndex() - openIndex > 14) tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);

        }


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


}