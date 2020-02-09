package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.InSlopeRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

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
        ADXIndicator adxIndicator = new ADXIndicator(tradeEngine.series, 5);
        Num maxSlopeForAdx = tradeEngine.series.numOf(-5.0);
        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);
        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);


        ruleForSell = new OverIndicatorRule(adxIndicator, 50.0, 5);
        ruleForSell = ruleForSell.and(new InSlopeRule(adxIndicator, 1, maxSlopeForAdx));
//        ruleForSell = ruleForSell.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(highestValueIndicator, previousValueIndicatorHigh, 8));


        ruleForBuy = new OverIndicatorRule(adxIndicator, 50.0, 5);
        ruleForBuy = ruleForBuy.and(new InSlopeRule(adxIndicator, 1, maxSlopeForAdx));
//        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(lowestValueIndicator, previousValueIndicatorLow, 8));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        tradeEngine.log(murrayMathIndicators[10]);
        MurrayMathIndicator m256=new MurrayMathIndicator(tradeEngine.series, 256, 10);
        m256.indicatorColor= Color.RED;
        tradeEngine.log(m256);

        MurrayMathIndicator m512=new MurrayMathIndicator(tradeEngine.series, 64, 10);
        m512.indicatorColor= Color.ORANGE;
        tradeEngine.log(m512);

        tradeEngine.log(murrayMathIndicators[2]);
        MurrayMathIndicator m256L=new MurrayMathIndicator(tradeEngine.series, 256, 2);
        m256L.indicatorColor= Color.RED;
        tradeEngine.log(m256L);

        MurrayMathIndicator m512L=new MurrayMathIndicator(tradeEngine.series, 64, 2);
        m512L.indicatorColor= Color.ORANGE;
        tradeEngine.log(m512L);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
        adxIndicator.subWindowIndex = 4;
        tradeEngine.log(adxIndicator);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 4);
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

        if (tradeEngine.timeFrame == timeFrame) {
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