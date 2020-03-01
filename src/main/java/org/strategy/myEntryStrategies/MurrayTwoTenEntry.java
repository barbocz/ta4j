package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.KAMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MbfxTimingIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathMultiIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.IsEqualRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayTwoTenEntry extends Strategy {
    MurrayMathMultiIndicator murrayMathMultiIndicatorHigh, murrayMathMultiIndicatorExtremeHigh, murrayMathMultiIndicatorLow, murrayMathMultiIndicatorExtremeLow;
    double murrayMathMultiHigh = Double.MAX_VALUE, correctionLevelHigh = Double.MAX_VALUE;
    double murrayMathMultiLow = 0.0, correctionLevelLow = 0.0;
    double lastHighEntry = -1.0, lastLowEntry = -1.0;
    PreviousValueIndicator previousHighValueIndicator, previousLowValueIndicator;
    LowestValueIndicator lowestValueIndicator;
    HighestValueIndicator highestValueIndicator;
    ADXIndicator adxIndicator;

//    Rule orderConditionRule;

    public void init() {

        murrayMathMultiIndicatorHigh = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.HIGH);
        murrayMathMultiIndicatorLow = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.LOW);
        murrayMathMultiIndicatorExtremeHigh = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_HIGH);
        murrayMathMultiIndicatorExtremeLow = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_LOW);

        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        previousLowValueIndicator = new PreviousValueIndicator(lowPriceIndicator);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 16);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        previousHighValueIndicator = new PreviousValueIndicator(highPriceIndicator);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 16);

        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.5);
        PreviousValueIndicator previousValueIndicator=new PreviousValueIndicator(laguerreIndicator);
//        TimeSeries series60=tradeEngine.getTimeSeries(15);
        LaguerreIndicator laguerreIndicatorSlow=new LaguerreIndicator(tradeEngine.series,0.8);
        MbfxTimingIndicator mbfxTimingIndicator=new MbfxTimingIndicator(tradeEngine.series,5);



        adxIndicator = new ADXIndicator(tradeEngine.series, 3);
        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 4);

        ruleForSell = new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(1.0),2);
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(laguerreIndicator,0.95));
        ruleForSell = ruleForSell.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(1.0),0));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(laguerreIndicatorSlow,0.88));
//        ruleForSell = ruleForSell.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(1.0),0));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(mbfxTimingIndicator,90));
//        ruleForSell = ruleForSell.and(new IsFallingRule(adxIndicator,true));

        ruleForBuy = new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(0.0),2);
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(laguerreIndicator,0.05));
//        ruleForBuy = ruleForBuy.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(0.0),0));
        ruleForBuy = ruleForBuy.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(0.0),0));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(laguerreIndicatorSlow,0.12));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(mbfxTimingIndicator,10));
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(adxIndicator,true));

//        orderConditionRule=new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.NONE);
        //        Num maxSlopeForAdx = tradeEngine.series.numOf(-5.0);
//
//        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(previousHigh, 48);
////        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
//        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(previousLow, 48);


//        ruleForSell = new OverIndicatorRule(highPriceIndicator, murrayMathIndicators[10]);
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(lowPriceIndicator,  murrayMathIndicators[10]));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(highestValueIndicator,  murrayMathIndicators[10]));
////        ruleForSell = ruleForSell.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(highestValueIndicator, previousValueIndicatorHigh, 8));
//
//
//        ruleForBuy = new UnderIndicatorRule(lowPriceIndicator, murrayMathIndicators[2]);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(highPriceIndicator, murrayMathIndicators[2]));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(lowestValueIndicator, murrayMathIndicators[2]));
////        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(previousValueIndicatorLow, murrayMathIndicators[0],8));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(lowestValueIndicator, previousValueIndicatorLow, 8));


//        tradeEngine.log(ruleForSell);
//        tradeEngine.log(ruleForBuy);

//        tradeEngine.log(murrayMathIndicators[10]);
//        MurrayMathIndicator m256=new MurrayMathIndicator(tradeEngine.series, 256, 10);
//        m256.indicatorColor= Color.RED;

        KAMAIndicator kamaIndicator = new KAMAIndicator(new ClosePriceIndicator(tradeEngine.series));
        kamaIndicator.indicatorColor=Color.ORANGE;
        tradeEngine.log(kamaIndicator);

        murrayMathMultiIndicatorHigh = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.HIGH);
        tradeEngine.log(murrayMathMultiIndicatorHigh);
        murrayMathMultiIndicatorLow = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.LOW);
        murrayMathMultiIndicatorLow.indicatorColor = Color.RED;
        tradeEngine.log(murrayMathMultiIndicatorLow);

        murrayMathMultiIndicatorExtremeHigh.indicatorColor = Color.ORANGE;
        tradeEngine.log(murrayMathMultiIndicatorExtremeHigh);

        murrayMathMultiIndicatorExtremeLow.indicatorColor = Color.ORANGE;
        tradeEngine.log(murrayMathMultiIndicatorExtremeLow);
//
//        MurrayMathIndicator m512=new MurrayMathIndicator(tradeEngine.series, 64, 10);
//        m512.indicatorColor= Color.ORANGE;
//        tradeEngine.log(m512);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//

        laguerreIndicator.subWindowIndex = 4;
        tradeEngine.log(laguerreIndicator);


        laguerreIndicatorSlow.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicatorSlow);


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
        if (lastHighEntry != murrayMathMultiHigh && tradeEngine.timeSeriesRepo.ask >= murrayMathMultiHigh && tradeEngine.openedOrders.size() == 0
                && previousHighValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < murrayMathMultiHigh &&
                murrayMathMultiHigh - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > 0.0003 &&
                adxIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < 650) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, murrayMathMultiHigh, tradeEngine.series.getCurrentTime()));
            lastHighEntry = murrayMathMultiHigh;
        } else if (lastLowEntry != murrayMathMultiLow && tradeEngine.timeSeriesRepo.bid <= murrayMathMultiLow && tradeEngine.openedOrders.size() == 0 &&
                previousLowValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > murrayMathMultiLow &&
                highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathMultiLow > 0.0003 &&
                adxIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < 650) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount, murrayMathMultiLow, tradeEngine.series.getCurrentTime()));
            lastLowEntry = murrayMathMultiLow;
        } else if (tradeEngine.openedOrders.size() == 1 && tradeEngine.timeSeriesRepo.ask >= correctionLevelHigh) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, correctionLevelHigh, tradeEngine.series.getCurrentTime()));
        } else if (tradeEngine.openedOrders.size() == 1 && tradeEngine.timeSeriesRepo.bid <= correctionLevelLow) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount, correctionLevelLow, tradeEngine.series.getCurrentTime()));
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

//        if (tradeEngine.timeFrame == timeFrame) {
//            murrayMathMultiHigh = murrayMathMultiIndicatorHigh.getValue(tradeEngine.currentBarIndex).doubleValue() - 0.0001;
//            murrayMathMultiLow = murrayMathMultiIndicatorLow.getValue(tradeEngine.currentBarIndex).doubleValue() + 0.0001;
//            if (lastHighEntry != murrayMathMultiHigh) lastHighEntry = -1;
//            if (lastLowEntry != murrayMathMultiLow) lastLowEntry = -1;
//            correctionLevelHigh = (murrayMathMultiHigh + murrayMathMultiIndicatorExtremeHigh.getValue(tradeEngine.currentBarIndex).doubleValue()) / 2.0;
//            correctionLevelLow = (murrayMathMultiLow + murrayMathMultiIndicatorExtremeLow.getValue(tradeEngine.currentBarIndex).doubleValue()) / 2.0;
//        }

        if (tradeEngine.period ==timeFrame) {
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            ZonedDateTime time=tradeEngine.series.getCurrentTime();

//            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
//            ZonedDateTime zdt= ZonedDateTime.parse("2019.08.02 10:54", dateFormatter.withZone(ZoneId.systemDefault()));
//            if (tradeEngine.series.getCurrentTime().isAfter(zdt)) {
//                System.out.println("BREAK");
//            }

//            if (tradeEngine.series.getCurrentIndex()==62) {
//                System.out.println("HEREEEEEEEEEEE");
//            }


//            buyOk=ruleForBuy.isSatisfied(time);
//            sellOk=ruleForSell.isSatisfied(time);
//
//            if (buyOk) buyLimit=kcL.getValue(tradeEngine.series.getPrevIndex()).doubleValue();
//            if (sellOk) sellLimit=kcU.getValue(tradeEngine.series.getPrevIndex()).doubleValue();

            if (ruleForSell.isSatisfied(time)) {
//                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {

//                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }



        }


    }


    public void onOneMinuteDataEvent() {

    }


}