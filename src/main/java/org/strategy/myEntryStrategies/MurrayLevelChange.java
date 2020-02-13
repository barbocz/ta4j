package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.Rule;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.*;
import org.ta4j.core.trading.rules.helpers.MurrayHighLevelChangeRule;
import org.ta4j.core.trading.rules.helpers.MurrayLowLevelChangeRule;


import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayLevelChange extends Strategy {

    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    double sellEntryLevel=Double.MAX_VALUE,buyEntryLevel=0.0;
    ClosePriceIndicator closePriceIndicator;

    public void init(){

        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series,i);
        }
        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 6);
        MedianPriceIndicator medianPriceIndicator=new MedianPriceIndicator(tradeEngine.series);
        SMAIndicator emaIndicator=new SMAIndicator(medianPriceIndicator,3);
        RSIIndicator rsiIndicator=new RSIIndicator(emaIndicator,8);

        StochasticOscillatorKIndicator stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(tradeEngine.series, 8);
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(longSma,1,1.0));

//        SMAIndicator longSma = new SMAIndicator(closePrice, 5);
//        PreviousValueIndicator prev = new PreviousValueIndicator(longSma, 1);
//        DifferenceIndicator diff = new DifferenceIndicator(longSma, prev);
//        Num slopeLimit = tradeEngine.series.numOf(0.0);
//
//        ParabolicSarIndicator parabolicSarIndicator=new ParabolicSarIndicator(tradeEngine.series,tradeEngine.series.numOf(0.009),tradeEngine.series.numOf(1));

        ruleForSell=new OverIndicatorRule(rsiIndicator,80.0);

//        ruleForSell = ruleForSell.and(new InSlopeRule(longSma,1,slopeLimit));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,1));


//        ruleForBuy=new MurrayLowLevelChangeRule(tradeEngine.series,128,13);
//        ruleForBuy = ruleForBuy.and(new InSlopeRule(longSma,slopeLimit));
        ruleForBuy=new UnderIndicatorRule(rsiIndicator,20.0);

        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,1));



        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if ( i % 2 != 0 ) murrayMathIndicators[i].indicatorColor=Color.GRAY;
            tradeEngine.log( murrayMathIndicators[i]);
        }

//        parabolicSarIndicator.indicatorColor= Color.WHITE;
//        tradeEngine.log(parabolicSarIndicator);


        stochasticOscillatorKIndicator.subWindowIndex=4;
        tradeEngine.log(stochasticOscillatorKIndicator);

        stochasticOscillatorDIndicator.subWindowIndex=4;
        stochasticOscillatorDIndicator.indicatorColor=Color.RED;
        tradeEngine.log(stochasticOscillatorDIndicator);

//        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.4);
        rsiIndicator.subWindowIndex=5;
        tradeEngine.log(rsiIndicator);

    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
        if (tradeEngine.timeSeriesRepo.bid>=sellEntryLevel) {
            Order order=Order.sell(orderAmount,sellEntryLevel,tradeEngine.series.getCurrentTime());
            order.parameters.put("entry",sellEntryLevel);
            tradeEngine.onTradeEvent(order);
            sellEntryLevel=Double.MAX_VALUE;

        }
        else if (tradeEngine.timeSeriesRepo.bid<=buyEntryLevel) {
            Order order=Order.buy(orderAmount,buyEntryLevel,tradeEngine.series.getCurrentTime());
            order.parameters.put("entry",buyEntryLevel);
            tradeEngine.onTradeEvent(order);
            buyEntryLevel=0.0;

        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{
//        System.out.println("onBarChangeEvent------------- "+timeFrame);

        if (tradeEngine.timeFrame==timeFrame) {
            sellEntryLevel=Double.MAX_VALUE;
            buyEntryLevel=0.0;

            ZonedDateTime time=tradeEngine.series.getCurrentTime();

//            for(Rule rule: ruleForSell.getRuleSet()) rule.isSatisfied(time);
//            for(Rule rule: ruleForBuy.getRuleSet()) rule.isSatisfied(time);


            if (ruleForSell.isSatisfied(time)) {
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
                Num closePrice=closePriceIndicator.getValue(tradeEngine.currentBarIndex);
                for (int i = 0; i < 13; i++) {
                    if ( i % 2 == 0 ) {
                        if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isGreaterThan(closePrice)) {
                            sellEntryLevel=murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
                            break;
                        }
                    }
                }
            }
            else if (ruleForBuy.isSatisfied(time)) {
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
                Num closePrice=closePriceIndicator.getValue(tradeEngine.currentBarIndex);
                for (int i = 12; i >=0; i--) {
                    if ( i % 2 == 0 ) {
                        if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isLessThan(closePrice)) {
                            buyEntryLevel=murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
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