package org.strategy.myEntryStrategies;


import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayTwoTenEntry extends Strategy {
    final Double murrayRange = 38.12;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double buyLimit = 0.0, sellLimit = Double.MAX_VALUE, lastTradedBuyLimit = 0.0, lastTradedSellLimit = 0.0;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;
    MoneyFlowIndicator moneyFlowIndicator,moneyFlowIndicatorSlower,moneyFlowIndicatorFast;
    ClosePriceIndicator closePriceIndicator;

    VolumeIndicator volumeIndicator;
    int lastTradeIndex = 0;
    double signalLevel=0; // 0: Hold, 3: Strong sell, -3 Strong buy

    LaguerreIndicator laguerreIndicator;
    //    Rule orderConditionRule;
    int m1CurrentBarIndex = 0;

    boolean moneyFlowIndicatorFastBuyOk=false,moneyFlowIndicatorFastSellOk=false;
    boolean weightedCloseBuyOk=false,weightedCloseSellOk=false;
    WeightedCloseIndicator weightedCloseIndicator;
    double weightedClose=0.0,prevWeightedClose=0.0;
    LowPriceIndicator lowPriceIndicator;
    HighPriceIndicator highPriceIndicator;
    double lowPriceValue=0.0,highPriceValue=Double.MAX_VALUE;
    public void init() {




        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        kcU = new KeltnerChannelUpperIndicator(kcM, 2.6, 89);
        kcL = new KeltnerChannelLowerIndicator(kcM, 2.6, 89);
//        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        volumeIndicator = new VolumeIndicator(tradeEngine.series);
        moneyFlowIndicatorFast=new MoneyFlowIndicator(tradeEngine.timeSeriesRepo.getTimeSeries(1), 3);

        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);

//        weightedCloseIndicator=new WeightedCloseIndicator(tradeEngine.timeSeriesRepo.getTimeSeries(1));
        lowPriceIndicator=new LowPriceIndicator(tradeEngine.timeSeriesRepo.getTimeSeries(1));
        highPriceIndicator=new HighPriceIndicator(tradeEngine.timeSeriesRepo.getTimeSeries(1));



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

//        zoloIndicatorUp.subWindowIndex = 4;
//        zoloIndicatorUp.indicatorColor = Color.GREEN;
//        tradeEngine.log(zoloIndicatorUp);
//
//        zoloIndicatorDown.subWindowIndex = 4;
//        zoloIndicatorDown.indicatorColor = Color.RED;
//        tradeEngine.log(zoloIndicatorDown);


//        laguerreIndicator.subWindowIndex = 4;
//        tradeEngine.log(laguerreIndicator);

        MoneyFlowIndicator moneyFlowIndicatorFast = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicatorFast.subWindowIndex = 4;
        moneyFlowIndicatorFast.indicatorColor=Color.GREEN;
        tradeEngine.log(moneyFlowIndicatorFast);

        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        moneyFlowIndicator.subWindowIndex = 5;
        tradeEngine.log(moneyFlowIndicator);

        moneyFlowIndicatorSlower = new MoneyFlowIndicator(tradeEngine.series, 8);
        moneyFlowIndicatorSlower.subWindowIndex = 6;
        moneyFlowIndicatorSlower.indicatorColor=Color.RED;
        tradeEngine.log(moneyFlowIndicatorSlower);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
        Order order = null;


        if (tradeEngine.timeSeriesRepo.ask > sellLimit && moneyFlowIndicatorFastSellOk) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));

            lastTradedSellLimit = sellLimit;
            sellLimit = Double.MAX_VALUE;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }
        if (tradeEngine.timeSeriesRepo.bid < buyLimit && moneyFlowIndicatorFastBuyOk ) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
            lastTradedBuyLimit = buyLimit;
            buyLimit = 0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

       if (tradeEngine.period ==timeFrame) {
           ZonedDateTime time = tradeEngine.series.getCurrentTime();

           if (time.getHour() > 14 && time.getHour() < 17) {
               buyLimit = 0.0;
               sellLimit = Double.MAX_VALUE;
               return;
           }
//        if (time.getHour()==15 || (time.getHour()==16 && time.getMinute()<31)) return;
           if (time.getHour() > 22 || (time.getHour() == 0 && time.getMinute() < 30)) {
               buyLimit = 0.0;
               sellLimit = Double.MAX_VALUE;
               return;
           }


           for (int i = 0; i < 13; i++)
               murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
           double murrayHeight = murrayLevels[1] - murrayLevels[0];
           if (murrayLevels[2] != murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 32) {
               lastTradedBuyLimit = 0.0;
           }
           if (murrayLevels[10] != murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 32) {
               lastTradedSellLimit = 0.0;
           }

//        boolean tightUpDown=false;
//        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()>zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()) {
//            if (zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()/zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()>0.95) tightUpDown=true;
//        } else {
//            if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()/zoloIndicatorDown.getValue(tradeEngine.currentBarIndex).doubleValue()>0.95) tightUpDown=true;
//        }
//
//        buyLimit = 0.0;
//        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0 || tightUpDown) buyLimit = murrayLevels[2] + 0.00003;
//        if (buyLimit == lastTradedBuyLimit) buyLimit = 0.0; // &&
//
//        sellLimit = Double.MAX_VALUE;
//        if (zoloIndicatorUp.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0 || tightUpDown) sellLimit = murrayLevels[10] - 0.00003;
//        if (sellLimit == lastTradedSellLimit) sellLimit = Double.MAX_VALUE; //&&

           buyLimit = murrayLevels[2] + 0.00008;
           if (buyLimit == lastTradedBuyLimit) //kcL.getValue(tradeEngine.currentBarIndex).doubleValue() < buyLimit ||
               buyLimit = 0.0; // &&


           sellLimit = murrayLevels[10] - 0.00008;


           if (sellLimit == lastTradedSellLimit) //kcU.getValue(tradeEngine.currentBarIndex).doubleValue() > sellLimit ||
               sellLimit = Double.MAX_VALUE; //&&

//           if (tradeEngine.currentBarIndex>11320) {
//               System.out.println();
//           }
       }

    }


    public void onOneMinuteDataEvent() {
//        if (tradeEngine.currentBarIndex<2) return;
        m1CurrentBarIndex = tradeEngine.timeSeriesRepo.getIndex(tradeEngine.series.getCurrentTime(),1);
//        lowPriceValue=lowPriceIndicator.getValue(m1CurrentBarIndex).doubleValue();
//        highPriceValue=highPriceIndicator.getValue(m1CurrentBarIndex).doubleValue();

        moneyFlowIndicatorFastBuyOk=false;
        moneyFlowIndicatorFastSellOk=false;
        if (moneyFlowIndicatorFast.getValue(m1CurrentBarIndex).doubleValue()>=moneyFlowIndicatorFast.getValue(m1CurrentBarIndex-1).doubleValue() )  moneyFlowIndicatorFastBuyOk=true;
        if (moneyFlowIndicatorFast.getValue(m1CurrentBarIndex).doubleValue()<=moneyFlowIndicatorFast.getValue(m1CurrentBarIndex-1).doubleValue())  moneyFlowIndicatorFastSellOk=true;
//
//        weightedClose=weightedCloseIndicator.getValue(m1CurrentBarIndex).doubleValue();
//        if (weightedClose>prevWeightedClose) weightedCloseBuyOk=true; else weightedCloseBuyOk=false;
//        if (weightedClose<prevWeightedClose) weightedCloseSellOk=true; else weightedCloseSellOk=false;
//        prevWeightedClose=weightedClose;

    }


}