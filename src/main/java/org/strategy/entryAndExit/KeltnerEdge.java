package org.strategy.entryAndExit;


import org.strategy.AbstractStrategy;

import org.strategy.TimeSeriesRepo;
import org.strategy.positionManagement.KeltnerBased;
import org.strategy.positionManagement.LastLowHigh;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.AntiAlligatorIndicator;
import org.ta4j.core.indicators.mt4Selection.MbfxTimingIndicator;
import org.ta4j.core.indicators.mt4Selection.PvaIndicator;
import org.ta4j.core.indicators.mt4Selection.WaddahIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.trading.rules.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeltnerEdge extends AbstractStrategy {


    public KeltnerEdge(TimeSeriesRepo timeSeries) {
        period = 5;
        series=timeSeries.getTimeSeries(period);

        //-----------------------------------------------------------------------------------------------------
        ClosePriceIndicator closePrice=new ClosePriceIndicator(series);

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series, 21);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 2.5, 34);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 2.5, 34);
        AntiAlligatorIndicator antiAlligatorIndicator = new AntiAlligatorIndicator(series);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
        WaddahIndicator waddahIndicatorUp = new WaddahIndicator(series, WaddahIndicator.Type.TREND_UP);
        WaddahIndicator waddahIndicatorDown = new WaddahIndicator(series, WaddahIndicator.Type.TREND_DOWN);
        WaddahIndicator waddahIndicatorExplosion = new WaddahIndicator(series, WaddahIndicator.Type.EXPLOSION);

//        WaddahIndicator waddahIndicatorUpDaily = new WaddahIndicator(timeSeries.get(1440), WaddahIndicator.Type.TREND_UP);
//        WaddahIndicator waddahIndicatorDownDaily = new WaddahIndicator(timeSeries.get(1440), WaddahIndicator.Type.TREND_DOWN);
//        WaddahIndicator waddahIndicatorExplosionDaily = new WaddahIndicator(timeSeries.get(1440), WaddahIndicator.Type.EXPLOSION);
//        MbfxTimingIndicator mbfxTimingDaily = new MbfxTimingIndicator(timeSeries.get(1440), 5);
        MbfxTimingIndicator mbfxTiming = new MbfxTimingIndicator(series, 5);

        PvaIndicator pvaIndicatorUp=new PvaIndicator(series, PvaIndicator.Type.TREND_UP);
        PvaIndicator pvaIndicatorDown=new PvaIndicator(series, PvaIndicator.Type.TREND_DOWN);

//        debugIndicator(pvaIndicatorUp);

        //-----------------------------------------------------------------------------------------------------
//        ruleForSell = new OverIndicatorRule(mbfxTimingDaily, 95.0, 8);
//        ruleForSell = new IsFallingRule(mbfxTimingDaily,1);
        ruleForSell = new CrossedDownIndicatorRule(mbfxTiming, 90, 5);
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU, 5));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(antiAlligatorIndicator, 0.0, 2));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown,0.001,2));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown,waddahIndicatorExplosion,1));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDownDaily,waddahIndicatorExplosionDaily,2));

//        ruleForSell = ruleForSell.and(new NotRule(new IsEqualRule(pvaIndicatorDown, NaN.NaN, 3)));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown60,0.0001,2));
        //ruleForSell = ruleForSell.and(new IsEqualRule(waddahIndicatorUp, NaN.NaN, 0));


//        ruleForBuy = new UnderIndicatorRule(mbfxTimingDaily, 5.0, 8);
//        ruleForBuy = new IsRisingRule(mbfxTimingDaily,1);
        ruleForBuy = new CrossedUpIndicatorRule(mbfxTiming, 10, 5);
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL, 5));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(antiAlligatorIndicator, 0.0, 2));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUp,waddahIndicatorExplosion,1));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUpDaily,waddahIndicatorExplosionDaily,2));
//       c ruleForBuy = ruleForBuy.and(new NotRule(new IsEqualRule(pvaIndicatorUp, NaN.NaN, 3)));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUp,0.001,2));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUp60,0.0001,2));
//        ruleForBuy = ruleForBuy.and(new IsEqualRule(waddahIndicatorDown, NaN.NaN, 0));

        //-----------------------------------------------------------------------------------------------------

//        ruleForSell = new StopLossRule(closePrice,-200.0);
//        ruleForBuy = new StopLossRule(closePrice,-200.0);

        positionManagement=new KeltnerBased(kcU,kcM,kcL);
//        positionManagement=new LastLowHigh(series,kcU,kcL);

    }
}