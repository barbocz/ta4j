package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.helpers.WeightedCloseIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayLevelChange_v4 extends Strategy {
    RSIIndicator rsiIndicator;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    double sellEntryLevel = Double.MAX_VALUE, buyEntryLevel = 0.0;
    ClosePriceIndicator closePriceIndicator;
    int buyCorrectionNumber = 0, sellCorrectionNumber = 0;    // 0: még nincs order, 1: korrekciós fázis (- 1 levelnél rányitunk, ha a korrekciós order visszajön az eredeti szintjére zárunk)  2: profitrelizálás (1 level profit megvan, fele lezás, maradék breakevent-be)
    // order.phase 0: új order 1: korrekciós order 2: profitrealizálásra megmaradt
    double murrayHeight = 0.0;
    final int maxCorrectionNumber=2;
    int lastBuyOrderIndex=0,lastSellOrderIndex=0;

    public void init() {

        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 38.0);
        }
        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 6);
        MedianPriceIndicator medianPriceIndicator = new MedianPriceIndicator(tradeEngine.series);
        EMAIndicator emaIndicator = new EMAIndicator(medianPriceIndicator, 3);

        WeightedCloseIndicator weightedCloseIndicator=new WeightedCloseIndicator(tradeEngine.series);
        rsiIndicator = new RSIIndicator(emaIndicator, 8);
        RSIIndicator rsiIndicatorFast = new RSIIndicator(weightedCloseIndicator, 5);

        MurrayChangeIndicator murrayChangeIndicator=new MurrayChangeIndicator(tradeEngine.series,38);
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(longSma,1,1.0));

//        SMAIndicator longSma = new SMAIndicator(closePrice, 5);
//        PreviousValueIndicator prev = new PreviousValueIndicator(longSma, 1);
//        DifferenceIndicator diff = new DifferenceIndicator(longSma, prev);
//        Num slopeLimit = tradeEngine.series.numOf(0.0);
//
//        ParabolicSarIndicator parabolicSarIndicator=new ParabolicSarIndicator(tradeEngine.series,tradeEngine.series.numOf(0.009),tradeEngine.series.numOf(1));

        LaguerreIndicator laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.54);

        ruleForSell = new OverIndicatorRule(rsiIndicatorFast, 85.0,2) ;
        ruleForSell = ruleForSell.and(new OverIndicatorRule(murrayChangeIndicator, 4.0,2));
//        ruleForSell = ruleForSell.and(new IsFallingRule(rsiIndicatorFast, true));
//        ruleForSell = ruleForSell.and(new IsMurrayReboundRule(tradeEngine.series, IsMurrayReboundRule.ReboundType.DOWN, IsMurrayReboundRule.MethodType.CHAIKIN));
////        ruleForSell = ruleForSell.and(new IsFallingRule(laguerreIndicator, false));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.5, 5));
////        ruleForSell = ruleForSell.and(new IsNotEqualRule(laguerreIndicator, tradeEngine.series.numOf(1.0), 0));
////        ruleForSell = ruleForSell.and(new InSlopeRule(longSma,1,slopeLimit));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,13));


//        ruleForBuy=new MurrayLowLevelChangeRule(tradeEngine.series,128,13);
//        ruleForBuy = ruleForBuy.and(new InSlopeRule(longSma,slopeLimit));

        ruleForBuy = new UnderIndicatorRule(rsiIndicatorFast, 15.0,2);
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(murrayChangeIndicator, 4.0,2));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(rsiIndicatorFast, true));

//        ruleForBuy = ruleForBuy.and(new IsMurrayReboundRule(tradeEngine.series, IsMurrayReboundRule.ReboundType.UP, IsMurrayReboundRule.MethodType.CHAIKIN));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(rsiIndicatorFast, 30.0));
////        ruleForBuy = ruleForBuy.and(new IsRisingRule(laguerreIndicator, false));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.5, 5));
////        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(laguerreIndicator, tradeEngine.series.numOf(0.0), 0));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,13));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i==2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i==10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

//        parabolicSarIndicator.indicatorColor= Color.WHITE;
//        tradeEngine.log(parabolicSarIndicator);


        murrayChangeIndicator.subWindowIndex = 4;
        tradeEngine.log(murrayChangeIndicator);

//        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.4);
        rsiIndicator.subWindowIndex = 5;
        tradeEngine.log(rsiIndicator);
        rsiIndicatorFast.subWindowIndex = 5;
        rsiIndicatorFast.indicatorColor=Color.GREEN;
        tradeEngine.log(rsiIndicatorFast);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
//        if (tradeEngine.timeSeriesRepo.ask >= sellEntryLevel && sellCorrectionNumber<=maxCorrectionNumber && lastSellOrderIndex!=tradeEngine.currentBarIndex) {
//
//            sellCorrectionNumber = 0;
//            for (Order currentOrder : tradeEngine.openedOrders) {
//                if (currentOrder.type == Order.Type.SELL) sellCorrectionNumber++;
//            }
//            Order order = Order.sell(orderAmount, sellEntryLevel, tradeEngine.series.getCurrentTime());
//            order.parameters.put("entry", sellEntryLevel);
////            order.parameters.put("correctionLevel",sellEntryLevel+murrayHeight);
//
//            order.phase = sellCorrectionNumber;
////            if (sellPhase==1) order.parameters.put("correctionLevel",sellEntryLevel+murrayHeight);
//            tradeEngine.onTradeEvent(order);
//            sellEntryLevel = sellEntryLevel + 0.00038;
//            lastSellOrderIndex=tradeEngine.currentBarIndex;
//
//
//        } else if (tradeEngine.timeSeriesRepo.bid <= buyEntryLevel && buyCorrectionNumber<=maxCorrectionNumber && lastBuyOrderIndex!=tradeEngine.currentBarIndex) {
//
//            buyCorrectionNumber = 0;
//            for (Order currentOrder : tradeEngine.openedOrders) {
//                if (currentOrder.type == Order.Type.BUY) buyCorrectionNumber++;
//            }
//            Order order = Order.buy(orderAmount, buyEntryLevel, tradeEngine.series.getCurrentTime());
//            order.parameters.put("entry", buyEntryLevel);
//
//            order.phase = buyCorrectionNumber;
//            tradeEngine.onTradeEvent(order);
//            buyEntryLevel = buyEntryLevel - 0.00038;
//            lastBuyOrderIndex=tradeEngine.currentBarIndex;
//
//        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
//        System.out.println("onBarChangeEvent------------- "+timeFrame);

        if (tradeEngine.period == timeFrame) {

            ZonedDateTime time = tradeEngine.series.getCurrentTime();

            if (ruleForSell.isSatisfied(time)) {
//                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {

//                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
                tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, time));
            }

        }


    }


    public void onOneMinuteDataEvent() {

    }




}