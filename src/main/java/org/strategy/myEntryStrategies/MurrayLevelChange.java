package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.Rule;

import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.InSlopeRule;
import org.ta4j.core.trading.rules.OrderConditionRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import org.ta4j.core.trading.rules.helpers.MurrayHighLevelChangeRule;
import org.ta4j.core.trading.rules.helpers.MurrayLowLevelChangeRule;


import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayLevelChange extends Strategy {


    public void init(){

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(longSma,1,1.0));

        SMAIndicator longSma = new SMAIndicator(closePrice, 5);
        PreviousValueIndicator prev = new PreviousValueIndicator(longSma, 1);
        DifferenceIndicator diff = new DifferenceIndicator(longSma, prev);
        Num slopeLimit = tradeEngine.series.numOf(0.0);

        ParabolicSarIndicator parabolicSarIndicator=new ParabolicSarIndicator(tradeEngine.series,tradeEngine.series.numOf(0.009),tradeEngine.series.numOf(1));

        ruleForSell=new MurrayHighLevelChangeRule(tradeEngine.series,128,13);
//        ruleForSell = ruleForSell.and(new InSlopeRule(longSma,1,slopeLimit));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,13));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(parabolicSarIndicator, closePrice));

        ruleForBuy=new MurrayLowLevelChangeRule(tradeEngine.series,128,13);
//        ruleForBuy = ruleForBuy.and(new InSlopeRule(longSma,slopeLimit));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,13));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule( parabolicSarIndicator,closePrice));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        parabolicSarIndicator.indicatorColor= Color.WHITE;
        tradeEngine.log(parabolicSarIndicator);

        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,5);
        moneyFlowIndicator.subWindowIndex=4;
        tradeEngine.log(moneyFlowIndicator);

    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());


    }

    public void onBarChangeEvent(int timeFrame) throws Exception{
//        System.out.println("onBarChangeEvent------------- "+timeFrame);

        if (tradeEngine.timeFrame==timeFrame) {


            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            for(Rule rule: ruleForSell.getRuleSet()) rule.isSatisfied(time);
            for(Rule rule: ruleForBuy.getRuleSet()) rule.isSatisfied(time);


            if (ruleForSell.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }


        }


    }


    public void onOneMinuteDataEvent() {

    }




}