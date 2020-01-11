package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;


public class KeltnerExit_v2 extends Strategy  {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;
    ATRIndicator atrIndicator;
    ParabolicSarIndicator parabolicSarIndicator;

    public KeltnerExit_v2() {


        // log:
//        rulesForLog.add(ruleForSell);
//        rulesForLog.add(ruleForBuy);
//        indicatorsForLog.add(closePriceD);
//        indicatorsForLog.add(kcU);

//        indicatorsForLog.add(chaikinIndicator);
//        setLogOn();

    }

    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 34);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 3.4, 34);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 3.4, 34);

        HighPriceIndicator highPriceIndicator=new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator=new HighestValueIndicator(highPriceIndicator,89);


        LowPriceIndicator lowPriceIndicator=new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator=new LowestValueIndicator(lowPriceIndicator,89);

        atrIndicator=new ATRIndicator(tradeEngine.series,16);
        parabolicSarIndicator=new ParabolicSarIndicator(tradeEngine.series,tradeEngine.series.numOf(0.08),tradeEngine.series.numOf(1));





        ruleForSell = new OverIndicatorRule(closePrice, keltnerChannelUpperIndicator, 8);
    }


    public void onTradeEvent(Order order) {


//        if (order.type == Order.Type.BUY) {
//            if (order.openPrice > keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue())
//                order.stopLoss = keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue();
//        } else {
//            if (order.openPrice < keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue())
//                order.stopLoss = keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue();
//        }
    }

//    public void onExitEvent(Order order){
//        order.closedAmount=order.amount;
//    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order){
//        if (order.closePhase==0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)>0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.closePhase = 1;
//            if (order.type == Order.Type.BUY) {
//                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            } else {
//                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            }
//            tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.STOPLOSS, false);
//        } else order.closedAmount = order.openedAmount;
        order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.timeFrame == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {


                    if (order.type == Order.Type.BUY) {

//                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);

                    } else {
//                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                    }

                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (tradeEngine.series.getCurrentIndex() - openIndex > 34)
                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);



//                if (order.type == Order.Type.BUY) {
//                    if (tradeEngine.currentBar.getOpenPrice().doubleValue() > keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue()) {
//                        order.stopLoss = keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue();
//                    }
//                } else {
//                    if (tradeEngine.currentBar.getOpenPrice().doubleValue() < keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue()) {
//                        order.stopLoss = keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue();
//                    }
//                }
//            System.out.format("%s = %s - %s: open: %s -  takeProfit: %s\n", order.openTime, tradeEngine.series.getCurrentTime(), order.type, order.openPrice, order.takeProfit);
            }
        }
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


//        for (Indicator indicator : indicatorsForLog) {
//            if (indicator.getTimeSeries().getPeriod() == timeFrame) {
//                TimeSeries indicatorSeries = indicator.getTimeSeries();
//                if (indicatorSeries.getEndIndex() > -1) {
//                    Num iValue = (Num) indicator.getValue(indicatorSeries.getEndIndex() - 1);
//                    logIndicator(indicator, indicatorSeries.getEndTime(), indicatorSeries.getEndIndex() - 1, iValue.doubleValue());
//                }
//            }
//        }

//    if (preIndex==series.getEndIndex()) System.out.println("HIBA------------------------");
//        preIndex=series.getEndIndex();

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());
//        if (preZdt==series.getCurrentTime()) System.out.println("HIBA------------------------");
//        preZdt=series.getCurrentTime();
//        System.out.println(new SimpleDateFormat("mm:ss:SSS").format(new Date())+" Keltner onOneMinuteDataEvent------------- START"+eventSeries.timeFrame);
//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println(new SimpleDateFormat("mm:ss:SSS").format(new Date())+" Keltner onOneMinuteDataEvent------------- END"+eventSeries.timeFrame);

    }


}