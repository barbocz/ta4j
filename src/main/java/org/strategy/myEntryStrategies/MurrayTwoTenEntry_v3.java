package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.awt.*;


public class MurrayTwoTenEntry_v3 extends Strategy {
    final Double murrayRange = 38.12;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double buyLimit = 0.0, sellLimit = Double.MAX_VALUE, lastTradedBuyLimit = 0.0, lastTradedSellLimit = 0.0;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;
    MoneyFlowIndicator moneyFlowIndicator, moneyFlowIndicatorM1;
    ClosePriceIndicator closePriceIndicator;
    ZoloIndicator zoloIndicatorUp,ZoloIndicator,zoloIndicatorDown;

    int lastTradeIndex = 0;
//    Rule orderConditionRule;

    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        kcU = new KeltnerChannelUpperIndicator(kcM, 4.6, 89);
        kcL = new KeltnerChannelLowerIndicator(kcM, 4.6, 89);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);


        kcU.indicatorColor = Color.RED;
        tradeEngine.log(kcU);

        kcM.indicatorColor = Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor = Color.GREEN;
        tradeEngine.log(kcL);


        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

        zoloIndicatorUp = new ZoloIndicator(tradeEngine.series, 8, 8, true);
        zoloIndicatorDown = new ZoloIndicator(tradeEngine.series, 8, 8, false);

        zoloIndicatorUp.subWindowIndex = 4;
        zoloIndicatorUp.indicatorColor = Color.GREEN;
        tradeEngine.log(zoloIndicatorUp);

        zoloIndicatorDown.subWindowIndex = 4;
        zoloIndicatorDown.indicatorColor = Color.RED;
        tradeEngine.log(zoloIndicatorDown);

        LaguerreIndicator laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);
        laguerreIndicator.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicator);

        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);

        ATRIndicator atrIndicator = new ATRIndicator(tradeEngine.series, 14);
        atrIndicator.subWindowIndex = 7;
        tradeEngine.log(atrIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
        double volumeSum = 0.0;

        int index = tradeEngine.timeSeriesRepo.getIndex(tradeEngine.series.getCurrentTime(), 1);
        if (tradeEngine.timeSeriesRepo.ask > sellLimit) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
            lastTradedSellLimit = sellLimit;
            sellLimit = Double.MAX_VALUE;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }
        if (tradeEngine.timeSeriesRepo.bid < buyLimit) {
                tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));
            lastTradedBuyLimit = buyLimit;
            buyLimit = 0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

//        if (tradeEngine.currentBarIndex > 1953) {
////
////                        int index = tradeEngine.timeSeriesRepo.getIndex(tradeEngine.series.getCurrentTime(), 1);
////                        double d=volumeIndicatorM1.getValue(index).doubleValue();
//            System.out.println(tradeEngine.series.getCurrentTime());
//        }
        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        double murrayHeight = murrayLevels[1] - murrayLevels[0];


        if (murrayLevels[2] != murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex - 1).doubleValue()) {
            if (tradeEngine.currentBarIndex - lastTradeIndex > 8) lastTradedBuyLimit = 0.0;
//            return;
        }
        if (murrayLevels[10] != murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex - 1).doubleValue()) {
            if (tradeEngine.currentBarIndex - lastTradeIndex > 8) lastTradedSellLimit = 0.0;
//            return;
        }

//        if (murrayLevels[2]!=murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex-2).doubleValue()) return;
//        if (murrayLevels[10]!=murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex-2).doubleValue()) return;
        boolean tightUpDown=false;
        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()>zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()) {
            if (zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()/zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()>0.95) tightUpDown=true;
        } else {
            if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()/zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()>0.95) tightUpDown=true;
        }

        buyLimit = 0.0;
        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0 || tightUpDown) buyLimit = murrayLevels[2] + 0.00003;
        if (buyLimit == lastTradedBuyLimit) buyLimit = 0.0; // &&

        sellLimit = Double.MAX_VALUE;
        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0 || tightUpDown) sellLimit = murrayLevels[10] - 0.00003;
        if (sellLimit == lastTradedSellLimit) sellLimit = Double.MAX_VALUE; //&&


//kcU.getValue(tradeEngine.currentBarIndex).doubleValue()>sellLimit ||


    }


    public void onOneMinuteDataEvent() {

    }


}