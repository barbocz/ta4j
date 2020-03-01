package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.WeightedCloseIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

import java.awt.*;


public class MurrayMiracleEntry extends Strategy {

    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    boolean buyOk = false, sellOk = false;
    double sellLimit, buyLimit=Double.MAX_VALUE;

    public void init() {

        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 38.0);
        }
        WeightedCloseIndicator weightedCloseIndicator = new WeightedCloseIndicator(tradeEngine.series);
        RSIIndicator rsiIndicator = new RSIIndicator(weightedCloseIndicator, 5);

        MurrayChangeIndicator murrayChangeIndicator = new MurrayChangeIndicator(tradeEngine.series,38);
        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,5);

        ruleForSell = new OverIndicatorRule(rsiIndicator, 25, 2);
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(murrayChangeIndicator, -3.5, 2));
        ruleForSell = ruleForSell.and(new IsFallingRule(moneyFlowIndicator, false));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(murrayChangeIndicator, 5.5));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,8));

        ruleForBuy = new UnderIndicatorRule(rsiIndicator, 75.0, 2);
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(murrayChangeIndicator, 3.5,2));
        ruleForBuy = ruleForBuy.and(new IsRisingRule(moneyFlowIndicator, false));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(murrayChangeIndicator, 5.5));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,8));

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

        moneyFlowIndicator.subWindowIndex = 4;
        tradeEngine.log(moneyFlowIndicator);

        ZoloIndicator zoloIndicatorUp=new ZoloIndicator(tradeEngine.series,true);

        zoloIndicatorUp.subWindowIndex = 5;
        zoloIndicatorUp.indicatorColor=Color.GREEN;
        tradeEngine.log(zoloIndicatorUp);

        ZoloIndicator zoloIndicatorDown=new ZoloIndicator(tradeEngine.series,false);

        zoloIndicatorDown.subWindowIndex = 5;
        zoloIndicatorDown.indicatorColor=Color.RED;
        tradeEngine.log(zoloIndicatorDown);

        murrayChangeIndicator.subWindowIndex=6;
        tradeEngine.log(murrayChangeIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {


        if (tradeEngine.timeSeriesRepo.ask > buyLimit) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount,buyLimit, tradeEngine.series.getCurrentTime()));
            buyLimit = Double.MAX_VALUE;
        }


        if (tradeEngine.timeSeriesRepo.bid < sellLimit) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, sellLimit, tradeEngine.series.getCurrentTime()));
            sellLimit = 0.0;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        sellLimit = 0.0;
        buyLimit = Double.MAX_VALUE;
        if (ruleForSell.isSatisfied(tradeEngine.series.getCurrentTime()) && tradeEngine.series.getCurrentBar().getVolume().doubleValue()<700) {
            sellLimit = tradeEngine.series.getCurrentBar().getClosePrice().doubleValue() - 0.00006;
        }
        else if (ruleForBuy.isSatisfied(tradeEngine.series.getCurrentTime()) && tradeEngine.series.getCurrentBar().getVolume().doubleValue()<700) {
            buyLimit = tradeEngine.series.getCurrentBar().getClosePrice().doubleValue() + 0.00006;
        }
    }


    public void onOneMinuteDataEvent() {

    }


}