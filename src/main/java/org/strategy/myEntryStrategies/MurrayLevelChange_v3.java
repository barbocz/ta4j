package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathMultiIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.IsFallingRule;
import org.ta4j.core.trading.rules.IsRisingRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayLevelChange_v3 extends Strategy {
    RSIIndicator rsiIndicator;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    double sellEntryLevel = Double.MAX_VALUE, buyEntryLevel = 0.0;
    ClosePriceIndicator closePriceIndicator;
    int buyCorrectionNumber = 0, sellCorrectionNumber = 0;    // 0: még nincs order, 1: korrekciós fázis (- 1 levelnél rányitunk, ha a korrekciós order visszajön az eredeti szintjére zárunk)  2: profitrelizálás (1 level profit megvan, fele lezás, maradék breakevent-be)
    // order.phase 0: új order 1: korrekciós order 2: profitrealizálásra megmaradt
    double murrayHeight = 0.0;
    final int maxCorrectionNumber=1;
    int lastBuyOrderIndex=0,lastSellOrderIndex=0;

    public void init() {

        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i);
        }
        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 6);
        MedianPriceIndicator medianPriceIndicator = new MedianPriceIndicator(tradeEngine.series);
        EMAIndicator emaIndicator = new EMAIndicator(medianPriceIndicator, 3);
        rsiIndicator = new RSIIndicator(emaIndicator, 8);
        RSIIndicator rsiIndicatorFast = new RSIIndicator(emaIndicator, 4);
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(longSma,1,1.0));

//        SMAIndicator longSma = new SMAIndicator(closePrice, 5);
//        PreviousValueIndicator prev = new PreviousValueIndicator(longSma, 1);
//        DifferenceIndicator diff = new DifferenceIndicator(longSma, prev);
//        Num slopeLimit = tradeEngine.series.numOf(0.0);
//
//        ParabolicSarIndicator parabolicSarIndicator=new ParabolicSarIndicator(tradeEngine.series,tradeEngine.series.numOf(0.009),tradeEngine.series.numOf(1));

        LaguerreIndicator laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.54);

        ruleForSell = new OverIndicatorRule(rsiIndicator, 75.0);
        ruleForSell = ruleForSell.and(new OverIndicatorRule(rsiIndicatorFast, 70.0));
        ruleForSell = ruleForSell.and(new IsFallingRule(laguerreIndicator, false));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.5, 5));
//        ruleForSell = ruleForSell.and(new IsNotEqualRule(laguerreIndicator, tradeEngine.series.numOf(1.0), 0));
//        ruleForSell = ruleForSell.and(new InSlopeRule(longSma,1,slopeLimit));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,13));


//        ruleForBuy=new MurrayLowLevelChangeRule(tradeEngine.series,128,13);
//        ruleForBuy = ruleForBuy.and(new InSlopeRule(longSma,slopeLimit));
        ruleForBuy = new UnderIndicatorRule(rsiIndicator, 15.0);
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(rsiIndicatorFast, 30.0));
        ruleForBuy = ruleForBuy.and(new IsRisingRule(laguerreIndicator, false));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.5, 5));
//        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(laguerreIndicator, tradeEngine.series.numOf(0.0), 0));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,13));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        MurrayMathMultiIndicator murrayMathMultiIndicatorH=new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.HIGH);
        MurrayMathMultiIndicator murrayMathMultiIndicatorEH=new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_HIGH);

        MurrayMathMultiIndicator murrayMathMultiIndicatorL=new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.LOW);
        MurrayMathMultiIndicator murrayMathMultiIndicatorEL=new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_LOW);

        tradeEngine.log(murrayMathMultiIndicatorH);
        tradeEngine.log(murrayMathMultiIndicatorEH);
        tradeEngine.log(murrayMathMultiIndicatorL);
        tradeEngine.log(murrayMathMultiIndicatorEL);

//        for (int i = 0; i < 13; i++) {
//            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
//            tradeEngine.log(murrayMathIndicators[i]);
//        }

//        parabolicSarIndicator.indicatorColor= Color.WHITE;
//        tradeEngine.log(parabolicSarIndicator);


        laguerreIndicator.subWindowIndex = 4;
        tradeEngine.log(laguerreIndicator);

//        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.4);
        rsiIndicator.subWindowIndex = 5;
        tradeEngine.log(rsiIndicator);

    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
        if (tradeEngine.timeSeriesRepo.ask >= sellEntryLevel && sellCorrectionNumber<=maxCorrectionNumber && lastSellOrderIndex!=tradeEngine.currentBarIndex) {

            sellCorrectionNumber = 0;
            for (Order currentOrder : tradeEngine.openedOrders) {
                if (currentOrder.type == Order.Type.SELL) sellCorrectionNumber++;
            }
            Order order = Order.sell(orderAmount, sellEntryLevel, tradeEngine.series.getCurrentTime());
            order.parameters.put("entry", sellEntryLevel);
//            order.parameters.put("correctionLevel",sellEntryLevel+murrayHeight);

            order.phase = sellCorrectionNumber;
//            if (sellPhase==1) order.parameters.put("correctionLevel",sellEntryLevel+murrayHeight);
            tradeEngine.onTradeEvent(order);
            sellEntryLevel = sellEntryLevel + 0.00038;
            lastSellOrderIndex=tradeEngine.currentBarIndex;


        } else if (tradeEngine.timeSeriesRepo.bid <= buyEntryLevel && buyCorrectionNumber<=maxCorrectionNumber && lastBuyOrderIndex!=tradeEngine.currentBarIndex) {

            buyCorrectionNumber = 0;
            for (Order currentOrder : tradeEngine.openedOrders) {
                if (currentOrder.type == Order.Type.BUY) buyCorrectionNumber++;
            }
            Order order = Order.buy(orderAmount, buyEntryLevel, tradeEngine.series.getCurrentTime());
            order.parameters.put("entry", buyEntryLevel);

            order.phase = buyCorrectionNumber;
            tradeEngine.onTradeEvent(order);
            buyEntryLevel = buyEntryLevel - 0.00038;
            lastBuyOrderIndex=tradeEngine.currentBarIndex;

        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
//        System.out.println("onBarChangeEvent------------- "+timeFrame);

        if (tradeEngine.timeFrame == timeFrame) {

            ZonedDateTime time = tradeEngine.series.getCurrentTime();

//            if (tradeEngine.series.getIndex(time)>1263) {
//                System.out.println("break currentBarIndex:"+tradeEngine.currentBarIndex+" cb time: "+
//                        tradeEngine.series.getBar(tradeEngine.currentBarIndex).getBeginTime()+" rsi:"+rsiIndicator.getValue(tradeEngine.currentBarIndex)+" ml: "+murrayMathIndicators[12].getValue(tradeEngine.currentBarIndex));
//            }
            murrayHeight = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();

            buyCorrectionNumber = 0;
            sellCorrectionNumber = 0;
            for (Order order : tradeEngine.openedOrders) {
                if (order.type == Order.Type.BUY) buyCorrectionNumber++;
                else sellCorrectionNumber++;
            }

//            for(Rule rule: ruleForSell.getRuleSet()) rule.isSatisfied(time);
//            for(Rule rule: ruleForBuy.getRuleSet()) rule.isSatisfied(time);

            if (buyCorrectionNumber==0) buyEntryLevel = 0.0;
            if (sellCorrectionNumber==0) sellEntryLevel = Double.MAX_VALUE;



            if (sellCorrectionNumber == 0 && ruleForSell.isSatisfied(time)) {
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
                Num closePrice = closePriceIndicator.getValue(tradeEngine.currentBarIndex);
                for (int i = 0; i < 12; i++) {
                    if (i % 2 == 0) {
                        if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isGreaterThan(closePrice)) {
                            sellEntryLevel = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
                            break;
                        }
                    }
                }
            } else if (buyCorrectionNumber == 0 && ruleForBuy.isSatisfied(time)) {
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
                Num closePrice = closePriceIndicator.getValue(tradeEngine.currentBarIndex);
                for (int i = 12; i >= 0; i--) {
                    if (i % 2 == 0) {
                        if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isLessThan(closePrice)) {
                            buyEntryLevel = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
                            break;
                        }
                    }
                }
            }


        }


    }


    public void onOneMinuteDataEvent() {

    }




}