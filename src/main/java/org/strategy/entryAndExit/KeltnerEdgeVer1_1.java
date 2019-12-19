package org.strategy.entryAndExit;


import org.strategy.AbstractStrategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TimeSeriesRepository;
import org.strategy.positionManagement.KeltnerBased;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.*;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.trading.rules.*;

public class KeltnerEdgeVer1_1 extends AbstractStrategy {


    public KeltnerEdgeVer1_1(TimeSeriesRepository timeSeries, int corePeriod) {

        period = corePeriod;
        series=timeSeries.getTimeSeries(corePeriod);


        //-----------------------------------------------------------------------------------------------------
        ClosePriceIndicator closePrice=new ClosePriceIndicator(series);
        LowPriceIndicator lowPrice=new LowPriceIndicator(series);
        HighPriceIndicator highPrice=new HighPriceIndicator(series);
        ClosePriceIndicator closePriceD=new ClosePriceIndicator(timeSeries.getTimeSeries(1440));

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series, 21);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 2.5, 34);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 2.5, 34);

        KeltnerChannelMiddleIndicator kcMD = new KeltnerChannelMiddleIndicator(timeSeries.getTimeSeries(1440), 21);
        KeltnerChannelUpperIndicator kcUD = new KeltnerChannelUpperIndicator(kcMD, 2.5, 34);
        KeltnerChannelLowerIndicator kcLD = new KeltnerChannelLowerIndicator(kcMD, 2.5, 34);

        AntiAlligatorIndicator antiAlligatorIndicator = new AntiAlligatorIndicator(series);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
        WaddahIndicator waddahIndicatorUp = new WaddahIndicator(series, WaddahIndicator.Type.TREND_UP);
        WaddahIndicator waddahIndicatorDown = new WaddahIndicator(series, WaddahIndicator.Type.TREND_DOWN);
        WaddahIndicator waddahIndicatorExplosion = new WaddahIndicator(series, WaddahIndicator.Type.EXPLOSION);

        WaddahIndicator waddahIndicatorUpDaily = new WaddahIndicator(timeSeries.getTimeSeries(1440), WaddahIndicator.Type.TREND_UP);
        WaddahIndicator waddahIndicatorDownDaily = new WaddahIndicator(timeSeries.getTimeSeries(1440), WaddahIndicator.Type.TREND_DOWN);
        WaddahIndicator waddahIndicatorExplosionDaily = new WaddahIndicator(timeSeries.getTimeSeries(1440), WaddahIndicator.Type.EXPLOSION);
        MbfxTimingIndicator mbfxTimingDaily = new MbfxTimingIndicator(timeSeries.getTimeSeries(1440), 5);
        MbfxTimingIndicator mbfxTiming = new MbfxTimingIndicator(series, 5);

        MurrayMathIndicator murrayHigh=new MurrayMathIndicator(series,256,11);
        MurrayMathIndicator murrayLow=new MurrayMathIndicator(series,256,1);

        ChaikinMoneyFlowIndicator chaikinIndicator=new ChaikinMoneyFlowIndicator(series,8);

//        PvaIndicator pvaIndicatorUp=new PvaIndicator(series, PvaIndicator.Type.TREND_UP);
//        PvaIndicator pvaIndicatorDown=new PvaIndicator(series, PvaIndicator.Type.TREND_DOWN);

        debugIndicator(murrayLow);

        //-----------------------------------------------------------------------------------------------------
//        ruleForSell = new OverIndicatorRule(mbfxTimingDaily, 95.0, 8);
//        ruleForSell = new IsFallingRule(mbfxTimingDaily,1);
        ruleForSell = new OverIndicatorRule(highPrice, murrayHigh,8);
        ruleForSell = ruleForSell.and(new CrossedDownIndicatorRule(mbfxTiming, 80, 8));
//        ruleForSell = ruleForSell.and(new CrossedDownIndicatorRule(mbfxTimingDaily, 80, 8));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU, 8));
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(closePrice, kcU));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, murrayHigh,18));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.15,2));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePriceD, kcUD));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePriceD, kcMD));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(antiAlligatorIndicator, 0.0, 5));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown,0.001,2));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown,waddahIndicatorExplosion,1));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDownDaily,0.0,1));

//        ruleForSell = ruleForSell.and(new NotRule(new IsEqualRule(pvaIndicatorDown, NaN.NaN, 2)));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(waddahIndicatorDown60,0.0001,2));
        //ruleForSell = ruleForSell.and(new IsEqualRule(waddahIndicatorUp, NaN.NaN, 0));


//        ruleForBuy = new UnderIndicatorRule(mbfxTimingDaily, 5.0, 8);
//        ruleForBuy = new IsRisingRule(mbfxTimingDaily,1);
        ruleForBuy = new UnderIndicatorRule(lowPrice, murrayLow,8);
        ruleForBuy = ruleForBuy.and(new CrossedUpIndicatorRule(mbfxTiming, 10, 8));
//        ruleForBuy = ruleForBuy.and(new CrossedUpIndicatorRule(mbfxTimingDaily, 20, 8));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL, 8));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.15,2));


//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePriceD, kcLD));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePriceD, kcMD));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(antiAlligatorIndicator, 0.0, 5));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUp,waddahIndicatorExplosion,1));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(waddahIndicatorUpDaily,0.0,1));
//        ruleForBuy = ruleForBuy.and(new NotRule(new IsEqualRule(pvaIndicatorUp, NaN.NaN, 2)));
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