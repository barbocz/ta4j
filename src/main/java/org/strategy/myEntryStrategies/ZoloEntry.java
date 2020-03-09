package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class ZoloEntry extends Strategy {


    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        TimeSeries seriesH3=tradeEngine.getTimeSeries(180);
        TimeSeries seriesH1=tradeEngine.getTimeSeries(60);

        ZoloIndicator zoloIndicatorUpH3=new ZoloIndicator(seriesH3,9,3,true);
        ZoloIndicator zoloIndicatorDownH3=new ZoloIndicator(seriesH3,9,3,false);

        ZoloIndicator zoloIndicator1UpH1=new ZoloIndicator(seriesH1,9,3,true);
        ZoloIndicator zoloIndicator1DownH1=new ZoloIndicator(seriesH1,9,3,false);

        ZoloIndicator zoloIndicator2UpH1=new ZoloIndicator(seriesH1,3,9,true);
        ZoloIndicator zoloIndicator2DownH1=new ZoloIndicator(seriesH1,3,9,false);

        ZoloIndicator zoloIndicatorUp=new ZoloIndicator(tradeEngine.series,3,3,true);
        ZoloIndicator zoloIndicatorDown=new ZoloIndicator(tradeEngine.series,3,3,false);

        ruleForSell=new CrossedDownIndicatorRule(zoloIndicatorUp,800.0);
        ruleForSell=ruleForSell.and(new OverIndicatorRule(zoloIndicator1UpH1,zoloIndicator1DownH1));
        ruleForSell=ruleForSell.and(new OverIndicatorRule(zoloIndicator2UpH1,zoloIndicator2DownH1));
        ruleForSell=ruleForSell.and(new OverIndicatorRule(zoloIndicatorDownH3,zoloIndicatorUpH3));

        ruleForBuy=new CrossedDownIndicatorRule(zoloIndicatorDown,800.0);
        ruleForBuy=ruleForBuy.and(new OverIndicatorRule(zoloIndicator1DownH1,zoloIndicator1UpH1));
        ruleForBuy=ruleForBuy.and(new OverIndicatorRule(zoloIndicator2DownH1,zoloIndicator2UpH1));
        ruleForBuy=ruleForBuy.and(new OverIndicatorRule(zoloIndicatorUpH3,zoloIndicatorDownH3));


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
        zoloIndicatorUp.subWindowIndex = 4;
        zoloIndicatorUp.indicatorColor= Color.GREEN;
        tradeEngine.log(zoloIndicatorUp);

        zoloIndicatorDown.subWindowIndex = 4;
        zoloIndicatorDown.indicatorColor= Color.RED;
        tradeEngine.log(zoloIndicatorDown);

        zoloIndicator1UpH1.subWindowIndex = 5;
        zoloIndicator1UpH1.indicatorColor= Color.GREEN;
        tradeEngine.log(zoloIndicator1UpH1);

        zoloIndicator1DownH1.subWindowIndex = 5;
        zoloIndicator1DownH1.indicatorColor= Color.RED;
        tradeEngine.log(zoloIndicator1DownH1);

        zoloIndicator2UpH1.subWindowIndex = 6;
        zoloIndicator2UpH1.indicatorColor= Color.GREEN;
        tradeEngine.log(zoloIndicator2UpH1);

        zoloIndicator2DownH1.subWindowIndex = 6;
        zoloIndicator2DownH1.indicatorColor= Color.RED;
        tradeEngine.log(zoloIndicator2DownH1);

        zoloIndicatorUpH3.subWindowIndex = 7;
        zoloIndicatorUpH3.indicatorColor= Color.GREEN;
        tradeEngine.log(zoloIndicatorUpH3);

        zoloIndicatorDownH3.subWindowIndex = 7;
        zoloIndicatorDownH3.indicatorColor= Color.RED;
        tradeEngine.log(zoloIndicatorDownH3);


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