package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.KAMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MbfxTimingIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathMultiIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.IsEqualRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayTwoTenEntry extends Strategy {
    final Double murrayRange = 38.12;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double buyLimit=0.0, sellLimit=Double.MAX_VALUE,lastTradedBuyLimit=0.0,lastTradedSellLimit=0.0;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;
    MoneyFlowIndicator moneyFlowIndicator;
    ClosePriceIndicator closePriceIndicator;
    VolumeIndicator volumeIndicator;
    int lastTradeIndex=0;
//    Rule orderConditionRule;

    public void init() {

        for (int i = 0; i < 13; i++)  murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        kcU = new KeltnerChannelUpperIndicator(kcM, 4.6, 89);
        kcL = new KeltnerChannelLowerIndicator(kcM, 4.6, 89);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        volumeIndicator=new VolumeIndicator(tradeEngine.series);

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

        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.13);
        laguerreIndicator.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicator);

         moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
        if (tradeEngine.timeSeriesRepo.ask>sellLimit) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
            lastTradedSellLimit=sellLimit;
            sellLimit=Double.MAX_VALUE;
            lastTradeIndex=tradeEngine.currentBarIndex;
        }
        if (tradeEngine.timeSeriesRepo.bid<buyLimit) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount,  tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));
            lastTradedBuyLimit=buyLimit;
            buyLimit=0.0;
            lastTradeIndex=tradeEngine.currentBarIndex;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {


        for (int i = 0; i < 13; i++)   murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        double murrayHeight = murrayLevels[1] - murrayLevels[0];
        if (murrayLevels[2]!=murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex-1).doubleValue() && tradeEngine.currentBarIndex-lastTradeIndex>8) {
            lastTradedBuyLimit=0.0;
        }
        if (murrayLevels[10]!=murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex-1).doubleValue() && tradeEngine.currentBarIndex-lastTradeIndex>8) {
            lastTradedSellLimit=0.0;
        }
        buyLimit=murrayLevels[2]+0.00008;
        if (kcL.getValue(tradeEngine.currentBarIndex).doubleValue()<buyLimit || buyLimit==lastTradedBuyLimit ) buyLimit=0.0; // &&
        sellLimit=murrayLevels[10]-0.00008;


        if (kcU.getValue(tradeEngine.currentBarIndex).doubleValue()>sellLimit || sellLimit==lastTradedSellLimit ) sellLimit=Double.MAX_VALUE; //&&

    }


    public void onOneMinuteDataEvent() {

    }


}