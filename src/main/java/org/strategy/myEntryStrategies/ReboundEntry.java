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


public class ReboundEntry extends Strategy {

    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    boolean buyOk = false, sellOk = false;
    double sellLimit, buyLimit=Double.MAX_VALUE;

    public void init() {

        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 76);
        }
        WeightedCloseIndicator weightedCloseIndicator = new WeightedCloseIndicator(tradeEngine.series);
        RSIIndicator rsiIndicator = new RSIIndicator(weightedCloseIndicator, 5);

        MurrayChangeIndicator murrayChangeIndicator = new MurrayChangeIndicator(tradeEngine.series,76);
        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,5);
        ZoloIndicator zoloIndicatorUp=new ZoloIndicator(tradeEngine.series,true);
        ZoloIndicator zoloIndicatorDown=new ZoloIndicator(tradeEngine.series,false);

//        ruleForSell=new IsRebounceCandleRule(tradeEngine,IsRebounceCandleRule.ReboundType.DOWN);
        ruleForSell = new OverIndicatorRule(zoloIndicatorUp, 100);
        ruleForSell = ruleForSell.and(new IsFallingRule(zoloIndicatorUp, true));
        ruleForSell = ruleForSell.and(new IsRisingRule(zoloIndicatorDown, true));

//        ruleForSell = new OverIndicatorRule(rsiIndicator, 25, 2);
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(murrayChangeIndicator, -3.5, 2));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 95,3));
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(murrayChangeIndicator, -4.0,3));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,1));

//        ruleForBuy=new IsRebounceCandleRule(tradeEngine,IsRebounceCandleRule.ReboundType.UP);
        ruleForBuy = new OverIndicatorRule(zoloIndicatorDown, 100);
        ruleForBuy = ruleForBuy.and(new IsRisingRule(zoloIndicatorUp, true));
        ruleForBuy = ruleForBuy.and(new IsFallingRule(zoloIndicatorDown, true));

//        ruleForBuy = new UnderIndicatorRule(rsiIndicator, 75.0, 2);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(murrayChangeIndicator, 3.5,2));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 5,3));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(murrayChangeIndicator, 4,3));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,1));

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



        zoloIndicatorUp.subWindowIndex = 5;
        zoloIndicatorUp.indicatorColor=Color.GREEN;
        tradeEngine.log(zoloIndicatorUp);



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
        if (ruleForSell.isSatisfied(tradeEngine.series.getCurrentTime())) {
            sellLimit = tradeEngine.series.getCurrentBar().getClosePrice().doubleValue() - 0.00006;
        }
        else if (ruleForBuy.isSatisfied(tradeEngine.series.getCurrentTime())) {
            buyLimit = tradeEngine.series.getCurrentBar().getClosePrice().doubleValue() + 0.00006;
        }
    }


    public void onOneMinuteDataEvent() {

    }


}