package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.IsFallingRule;
import org.ta4j.core.trading.rules.IsRisingRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class Murray1 extends Strategy {
    final Double murrayRange = 76.24;
    MoneyFlowIndicator moneyFlowIndicator, moneyFlowIndicatorSlower;
    LaguerreIndicator laguerreIndicator;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicatorSlower = new MoneyFlowIndicator(tradeEngine.series, 8);
        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);

        ruleForSell=new OverIndicatorRule(moneyFlowIndicatorSlower,88.0,2);
        ruleForSell=ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator,98.0,2));
        ruleForSell=ruleForSell.and(new OverIndicatorRule(laguerreIndicator,0.9,2));
        ruleForSell=ruleForSell.and(new OverIndicatorRule(closePrice,murrayMathIndicators[9]));
//        ruleForSell=ruleForSell.and(new IsFallingRule(moneyFlowIndicator,false).or(new IsFallingRule(laguerreIndicator,false)));

        ruleForBuy=new UnderIndicatorRule(moneyFlowIndicatorSlower,12.0,2);
        ruleForBuy=ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator,2.0,2));
        ruleForBuy=ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator,0.1,2));
        ruleForBuy=ruleForBuy.and(new UnderIndicatorRule(closePrice,murrayMathIndicators[3]));
//        ruleForBuy=ruleForBuy.and(new IsRisingRule(moneyFlowIndicator,false).or(new IsRisingRule(laguerreIndicator,false)));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }


        laguerreIndicator.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicator);


        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


        moneyFlowIndicatorSlower.subWindowIndex = 4;
        moneyFlowIndicatorSlower.indicatorColor = Color.RED;
        tradeEngine.log(moneyFlowIndicatorSlower);



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
            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            if (ruleForSell.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }
    }


    public void onOneMinuteDataEvent() {

    }




}