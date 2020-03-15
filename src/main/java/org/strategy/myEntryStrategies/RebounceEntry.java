package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RebounceEntry extends Strategy {
    final Double murrayRange = 76.24;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double buyUpperLimit = Double.MAX_VALUE,buyLowerLimit=0.0, sellUpperLimit = Double.MAX_VALUE, sellLowerLimit=0.0, lastTradedBuyLimit = 0.0, lastTradedSellLimit = 0.0;
    MoneyFlowIndicator moneyFlowIndicator,moneyFlowIndicatorSlower;
    ClosePriceIndicator closePriceIndicator;
    double signalLevel=0; // 0: Hold, 3: Strong sell, -3 Strong buy
    HashMap<Double,Boolean> buyCorrectionLevels=new HashMap<>();
    HashMap<Double,Boolean> sellCorrectionLevels=new HashMap<>();


    int lastTradeIndex = 0;

    LaguerreIndicator laguerreIndicator;
    //    Rule orderConditionRule;
    int m1CurrentBarIndex = 0;

    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicatorSlower=new MoneyFlowIndicator(tradeEngine.series, 8);


        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);


        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

//        zoloIndicatorUp.subWindowIndex = 4;
//        zoloIndicatorUp.indicatorColor = Color.GREEN;
//        tradeEngine.log(zoloIndicatorUp);
//
//        zoloIndicatorDown.subWindowIndex = 4;
//        zoloIndicatorDown.indicatorColor = Color.RED;
//        tradeEngine.log(zoloIndicatorDown);


        laguerreIndicator.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicator);



        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


        moneyFlowIndicatorSlower.subWindowIndex = 4;
        moneyFlowIndicatorSlower.indicatorColor=Color.RED;
        tradeEngine.log(moneyFlowIndicatorSlower);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {

        if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit || tradeEngine.timeSeriesRepo.ask<sellLowerLimit) {
            double sellLimit;
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit) sellLimit= sellUpperLimit;
                else sellLimit= sellLowerLimit;
            } else sellLimit= tradeEngine.timeSeriesRepo.ask;

            tradeEngine.onTradeEvent(Order.sell(orderAmount,sellLimit, tradeEngine.series.getCurrentTime()));

            lastTradedSellLimit = sellLimit;
            sellUpperLimit = Double.MAX_VALUE;
            sellLowerLimit=0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }
        if (tradeEngine.timeSeriesRepo.bid  > buyUpperLimit || tradeEngine.timeSeriesRepo.bid <buyLowerLimit) {
            double buyLimit;
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.bid > buyUpperLimit) buyLimit= buyUpperLimit;
                else buyLimit= buyLowerLimit;
            } else buyLimit= tradeEngine.timeSeriesRepo.bid;

            tradeEngine.onTradeEvent(Order.buy(orderAmount,buyLimit, tradeEngine.series.getCurrentTime()));

            lastTradedBuyLimit = buyLimit;
            buyUpperLimit = Double.MAX_VALUE;
            buyLowerLimit=0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        ZonedDateTime time = tradeEngine.series.getCurrentTime();
        signalLevel=0.0;
        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0) signalLevel--;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue()<15.0) signalLevel--;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<0.1) signalLevel--;

        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==100.0) signalLevel++;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue()>85.0) signalLevel++;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>0.9) signalLevel++;

//        if (time.getHour() > 14 && time.getHour() < 17) return;
////        if (time.getHour()==15 || (time.getHour()==16 && time.getMinute()<31)) return;
//        if (time.getHour() > 22 || (time.getHour() == 0 && time.getMinute() < 30)) return;

        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        double murrayHeight = murrayLevels[1] - murrayLevels[0];
        if (murrayLevels[2] != murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 21) {
            lastTradedBuyLimit = 0.0;
        }
        if (murrayLevels[10] != murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 21) {
            lastTradedSellLimit = 0.0;
        }

        int buyCounter=0,sellCounter=0;
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY) buyCounter++; else sellCounter++;
        }

        double closePrice=closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        if (buyCounter==0) {
            if (signalLevel > -2 || murrayLevels[2] == lastTradedBuyLimit  ) {
                buyUpperLimit = Double.MAX_VALUE;
                buyLowerLimit = 0.0;
            } else {
                if (closePrice >= murrayLevels[2])  buyLowerLimit = murrayLevels[2];
                else if (closePrice < murrayLevels[2] && closePrice >= murrayLevels[1]) {
                    buyUpperLimit = murrayLevels[2];
                    buyLowerLimit = murrayLevels[1];
                } else if (closePrice< murrayLevels[1]) {
                    buyUpperLimit = murrayLevels[1];
                    buyLowerLimit = murrayLevels[0];
                }
            }
        }
        if (sellCounter==0) {
            if ( signalLevel < 2 || murrayLevels[10] == lastTradedSellLimit) {
                sellUpperLimit = Double.MAX_VALUE;
                sellLowerLimit = 0.0;
            } else {
                if (closePrice <= murrayLevels[10])  sellUpperLimit = murrayLevels[10];
                else if (closePrice > murrayLevels[10] && closePrice <= murrayLevels[11]) {
                    sellUpperLimit = murrayLevels[11];
                    sellLowerLimit = murrayLevels[10];
                } else if (closePrice> murrayLevels[11]) {
                    sellUpperLimit = murrayLevels[12];
                    sellLowerLimit = murrayLevels[11];
                }
            }
        }


//
//
//
//        if (murrayLevels[2] != lastTradedBuyLimit && moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0
//                && closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>murrayLevels[2] ) buyLimit = murrayLevels[2];
//        else buyLimit=0.0;
//
//        if (murrayLevels[10] != lastTradedSellLimit && moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==100.0
//                && closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<murrayLevels[10] ) sellLimit = murrayLevels[10];
//        else sellLimit=Double.MAX_VALUE;

    }


    public void onOneMinuteDataEvent() {
//        m1CurrentBarIndex = tradeEngine.timeSeriesRepo.getIndex(tradeEngine.series.getCurrentTime(), 1);
    }


}