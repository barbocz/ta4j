package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.KAMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

import java.awt.*;
import java.time.ZonedDateTime;


public class StochasticEntry extends Strategy {


    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        MoneyFlowIndicator moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);

//        TimeSeries series60 = tradeEngine.getTimeSeries(34);
//        KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator60 = new KeltnerChannelMiddleIndicator(series60, 54);
//        KeltnerChannelUpperIndicator keltnerChannelUpperIndicator60 = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator60, 2.6, 54);
//        KeltnerChannelLowerIndicator keltnerChannelLowerIndicator60 = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator60, 2.6, 54);
//        KAMAIndicator kamaIndicator60 = new KAMAIndicator(new ClosePriceIndicator(series60));


        StochasticOscillatorKIndicator stochasticOscillatorKIndicator = new StochasticOscillatorKIndicator(tradeEngine.series, 8);
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(stochasticOscillatorKIndicator);

        KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        KeltnerChannelUpperIndicator keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 2.6, 54);
        KeltnerChannelLowerIndicator keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 2.6, 54);

        KAMAIndicator kamaIndicator = new KAMAIndicator(closePrice);
        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 5);

//        ruleForSell = new OverIndicatorRule(closePrice, keltnerChannelUpperIndicator, 2);
////        ruleForSell = ruleForSell.and(new UnderIndicatorRule(closePrice, keltnerChannelUpperIndicator60));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, keltnerChannelUpperIndicator60));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(kamaIndicator, keltnerChannelUpperIndicator, 3));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.43, 5));
//        ruleForSell = ruleForSell.and(new CrossedDownIndicatorRule(stochasticOscillatorKIndicator, stochasticOscillatorDIndicator, 8));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(stochasticOscillatorKIndicator, 80, 8));
        ruleForSell = new IsFallingRule(kamaIndicator,true);
        ruleForSell = ruleForSell.and(new IsFallingRule(keltnerChannelMiddleIndicator, 5,0.74));
        ruleForSell = ruleForSell.and(new IsFallingRule(stochasticOscillatorKIndicator, false));
        ruleForSell = ruleForSell.and(new CrossedDownIndicatorRule(closePrice, keltnerChannelMiddleIndicator, 2));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, keltnerChannelLowerIndicator));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY, 2));

//        ruleForBuy = new UnderIndicatorRule(closePrice, keltnerChannelLowerIndicator, 2);
////        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, keltnerChannelLowerIndicator60));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, keltnerChannelLowerIndicator60));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 2, 13));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(kamaIndicator, keltnerChannelLowerIndicator, 3));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.43, 5));
//        ruleForBuy = ruleForBuy.and(new CrossedUpIndicatorRule(stochasticOscillatorKIndicator, stochasticOscillatorDIndicator, 8));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(stochasticOscillatorKIndicator, 20, 8));
        ruleForBuy = new IsRisingRule(kamaIndicator,true);
        ruleForBuy = ruleForBuy.and(new IsRisingRule(keltnerChannelMiddleIndicator, 5,0.74));
        ruleForBuy = ruleForBuy.and(new CrossedUpIndicatorRule(closePrice, keltnerChannelMiddleIndicator, 2));
        ruleForBuy = ruleForBuy.and(new IsRisingRule(stochasticOscillatorKIndicator,false));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, keltnerChannelUpperIndicator));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL, 2));

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);


        kamaIndicator.indicatorColor = Color.ORANGE;
        tradeEngine.log(kamaIndicator);

        keltnerChannelUpperIndicator.indicatorColor = Color.WHITE;
        tradeEngine.log(keltnerChannelUpperIndicator);

        keltnerChannelMiddleIndicator.indicatorColor = Color.WHITE;
        tradeEngine.log(keltnerChannelMiddleIndicator);

        keltnerChannelLowerIndicator.indicatorColor = Color.WHITE;
        tradeEngine.log(keltnerChannelLowerIndicator);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
//        AntiAlligatorIndicator antiAlligatorIndicator=new AntiAlligatorIndicator(tradeEngine.series);
        chaikinIndicator.subWindowIndex = 4;
        tradeEngine.log(chaikinIndicator);

        stochasticOscillatorKIndicator.subWindowIndex = 5;
        stochasticOscillatorKIndicator.indicatorColor = Color.RED;
        tradeEngine.log(stochasticOscillatorKIndicator);

        stochasticOscillatorDIndicator.subWindowIndex = 5;
        stochasticOscillatorDIndicator.indicatorColor = Color.GREEN;
        tradeEngine.log(stochasticOscillatorDIndicator);


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

    if (tradeEngine.logLevel== TradeEngine.LogLevel.ANALYSE) return;
//        System.out.println("onBarChangeEvent------------- "+timeFrame);
//        try {
//            TimeUnit.SECONDS.sleep(timeFrame);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("onBarChangeEvent------------- "+eventSeries.timeFrame+" cp: "+slowerClosePrice.getValue(timeSeriesRepo.getTimeSeries(3).getEndIndex()-1));
//        System.out.println("onBarChangeEvent rule ------------- "+eventSeries.timeFrame+" cp: "+ruleForSell.isSatisfied(series.getEndIndex()-1));
//        if (!strategy.ruleForSell.isSatisfied(eventSeries.getEndIndex())) System.out.println("NOT SELL");;
//    if (series.getEndIndex()-1==990) {
//        if (ruleForSell.isSatisfied(series.getEndTime()))
//            System.out.println("Sell Entry: " + (series.getEndIndex() - 1)+"    "+eventSeries.timeFrame);
//    }
//        System.out.println(series.getEndIndex()-1);

//        int i = series.getEndIndex();
//        System.out.println("------------ "+i);

//        System.out.println(timeFrame+" --------------------  "+series.getIndex(time) + ": " + time);
        if (tradeEngine.period == timeFrame) {
            ZonedDateTime time = tradeEngine.series.getCurrentTime();
            if (ruleForSell.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, time));
            } else if (ruleForBuy.isSatisfied(time)) {

                tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, time));
            }
        }


    }


    public void onOneMinuteDataEvent() {

    }


}