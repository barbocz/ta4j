package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayEdgeEntry extends Strategy {
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];

    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 128, i);
        }
        PreviousValueIndicator previousValueIndicatorLow = new PreviousValueIndicator(murrayMathIndicators[0]);
        PreviousValueIndicator previousValueIndicatorHigh = new PreviousValueIndicator(murrayMathIndicators[12]);

        StochasticOscillatorKIndicator stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(tradeEngine.series, 13);
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);

        MedianPriceIndicator medianPriceIndicator = new MedianPriceIndicator(tradeEngine.series);
        EMAIndicator emaIndicator = new EMAIndicator(medianPriceIndicator, 3);
        RSIIndicator rsiIndicator = new RSIIndicator(emaIndicator, 8);
        RSIIndicator rsiIndicatorFast = new RSIIndicator(emaIndicator, 4);
//        ADXIndicator adxIndicator = new ADXIndicator(tradeEngine.series, 5);
//        Num maxSlopeForAdx = tradeEngine.series.numOf(-5.0);
//        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
//        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);
//        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
//        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);

//        ruleForSell= new IsMurrayReboundRule(tradeEngine.series, IsMurrayReboundRule.ReboundType.DOWN, IsMurrayReboundRule.MethodType.CHAIKIN);
        ruleForSell = ruleForSell.and(new OverIndicatorRule(stochasticOscillatorDIndicator, stochasticOscillatorKIndicator));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,1));

//        ruleForSell = new CrossedDownIndicatorRule(stochasticOscillatorKIndicator, 92.0,13);
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(rsiIndicator, 85));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(rsiIndicator, 85.0,8));
//        ruleForSell = ruleForSell.and(new InSlopeRule(adxIndicator, 1, maxSlopeForAdx));
////        ruleForSell = ruleForSell.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(highestValueIndicator, previousValueIndicatorHigh, 8));

//        ruleForBuy= new IsMurrayReboundRule(tradeEngine.series, IsMurrayReboundRule.ReboundType.UP, IsMurrayReboundRule.MethodType.CHAIKIN);
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule( stochasticOscillatorKIndicator,stochasticOscillatorDIndicator));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(rsiIndicator, 20.0));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,1));

//        ruleForBuy = new CrossedUpIndicatorRule(stochasticOscillatorKIndicator, 8.0,13);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(rsiIndicator, 15));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(rsiIndicator, 15.0,8));
//        ruleForBuy = ruleForBuy.and(new InSlopeRule(adxIndicator, 1, maxSlopeForAdx));
////        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(lowestValueIndicator, previousValueIndicatorLow, 8));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        stochasticOscillatorKIndicator.subWindowIndex=4;
        tradeEngine.log(stochasticOscillatorKIndicator);

        stochasticOscillatorDIndicator.subWindowIndex=4;
        stochasticOscillatorDIndicator.indicatorColor=Color.RED;
        tradeEngine.log(stochasticOscillatorDIndicator);

        MurrayMathFixedIndicator murrayMathIndicatorF[] = new MurrayMathFixedIndicator[13];
        for (int i = 0; i < 13; i++) {
            murrayMathIndicatorF[i] = new MurrayMathFixedIndicator(tradeEngine.series,  i, 38.0);
        }
        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicatorF[i].indicatorColor = Color.GRAY;
            tradeEngine.log(murrayMathIndicatorF[i]);
        }

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
//        adxIndicator.subWindowIndex = 4;
//        tradeEngine.log(adxIndicator);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 5);
        chaikinIndicator.subWindowIndex = 5;
        tradeEngine.log(chaikinIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
//        if (buyOk) {
//            if (tradeEngine.timeSeriesRepo.ask>buyLimit) {
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,tradeEngine.series.getCurrentTime()));
//                buyOk=false;
//            }
//        }
//        if (sellOk) {
//            if (tradeEngine.timeSeriesRepo.bid<sellLimit) {
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,tradeEngine.series.getCurrentTime()));
//                sellOk=false;
//            }
//        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.period == timeFrame) {
            ZonedDateTime time = tradeEngine.series.getCurrentTime();
            if (ruleForSell.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, time));
            } else if (ruleForBuy.isSatisfied(time)) {

                tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, time));
            }
        }


    }


    public void onOneMinuteDataEvent() {

    }


}