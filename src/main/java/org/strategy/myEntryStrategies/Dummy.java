package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.OrderConditionRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class Dummy extends Strategy {


    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
//        longCci.subWindowIndex = 4;
//        tradeEngine.log(longCci);


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
//        if (tradeEngine.timeFrame==timeFrame) {
////            ZonedDateTime time=tradeEngine.series.getCurrentTime();
//
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();
//
//
//            if (ruleForSell.isSatisfied(time)) {
////                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
//            }
//            else if (ruleForBuy.isSatisfied(time)) {
//
////                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
//            }
//
//
//    }




    }


    public void onOneMinuteDataEvent() {

    }




}