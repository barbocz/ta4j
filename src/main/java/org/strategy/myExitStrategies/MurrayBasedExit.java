package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;


public class MurrayBasedExit extends Strategy {

    ATRIndicator atrIndicator;
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];


    public MurrayBasedExit() {


        // log:
//        rulesForLog.add(ruleForSell);
//        rulesForLog.add(ruleForBuy);
//        indicatorsForLog.add(closePriceD);
//        indicatorsForLog.add(kcU);

//        indicatorsForLog.add(chaikinIndicator);
//        setLogOn();

    }

    public void init() {


        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 128, i);
        }

        atrIndicator = new ATRIndicator(tradeEngine.series, 16);

    }


    public void onTradeEvent(Order order) {
        double murrayLevelMultiplier = 1.0, atrMultiplier = 0.0, riskReward=1.0;

        int lowerIndex=-1, upperIndex=-1;
        double lowerPrice, upperPrice,stopLoss=0.0,takeProfit=0.0;
        double height = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();

        lowerPrice = order.openPrice - atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - murrayLevelMultiplier * height;

        upperPrice = order.openPrice + atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + murrayLevelMultiplier * height;

        if (riskReward!=1.0) {
            if (order.type == Order.Type.SELL) lowerPrice = order.openPrice - atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - riskReward * murrayLevelMultiplier * height;
            if (order.type == Order.Type.BUY) upperPrice = order.openPrice + atrMultiplier * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() +  riskReward * murrayLevelMultiplier * height;
        }

        for (int i = 0; i < 12; i++) {
//            double ml=murrayMathIndicators[murrayRange].getValue(tradeEngine.currentBarIndex).doubleValue();
//            double mu=murrayMathIndicators[murrayRange+1].getValue(tradeEngine.currentBarIndex).doubleValue();
            if (lowerPrice >= murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() && lowerPrice <= murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue())
                lowerIndex=i;
            if (upperPrice >= murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() && upperPrice <= murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue()) {
                upperIndex = i+1;
                break;
            }
        }

        if (lowerIndex<0) {
            if (order.type == Order.Type.BUY) stopLoss=lowerPrice;
            else takeProfit=lowerPrice;
        } else {
            if (order.type == Order.Type.BUY) stopLoss=murrayMathIndicators[lowerIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
            else takeProfit=murrayMathIndicators[lowerIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
        }

        if (upperIndex<0) {
            if (order.type == Order.Type.BUY) takeProfit=upperPrice;
            else stopLoss=upperPrice;
        } else {
            if (order.type == Order.Type.BUY) takeProfit=murrayMathIndicators[upperIndex].getValue(tradeEngine.currentBarIndex).doubleValue();
            else stopLoss=murrayMathIndicators[upperIndex].getValue(tradeEngine.currentBarIndex).doubleValue();;

        }

        if (order.type == Order.Type.BUY) {
            takeProfit=takeProfit - atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            stopLoss=stopLoss - atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        } else {
            takeProfit=takeProfit + atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            stopLoss=stopLoss + atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        }

        tradeEngine.setExitPrice(order, takeProfit, TradeEngine.ExitMode.TAKEPROFIT, true);
        tradeEngine.setExitPrice(order, stopLoss, TradeEngine.ExitMode.STOPLOSS, true);



    }

//    public void onExitEvent(Order order){
//        order.closedAmount=order.amount;
//    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
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

    public void onBarChangeEvent(int timeFrame) throws Exception {

//        if (tradeEngine.timeFrame == timeFrame) {
//            for (Order order : tradeEngine.openedOrders) {
//
//
//                    if (order.type == Order.Type.BUY) {
//
////                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                        tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//
//                    } else {
////                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                        tradeEngine.setExitPrice(order, parabolicSarIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                    }
//
//                    int openIndex = tradeEngine.series.getIndex(order.openTime);
//                    if (tradeEngine.series.getCurrentIndex() - openIndex > 34)
//                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
//
//
//
////                if (order.type == Order.Type.BUY) {
////                    if (tradeEngine.currentBar.getOpenPrice().doubleValue() > keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue()) {
////                        order.stopLoss = keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue();
////                    }
////                } else {
////                    if (tradeEngine.currentBar.getOpenPrice().doubleValue() < keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue()) {
////                        order.stopLoss = keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue();
////                    }
////                }
////            System.out.format("%s = %s - %s: open: %s -  takeProfit: %s\n", order.openTime, tradeEngine.series.getCurrentTime(), order.type, order.openPrice, order.takeProfit);
//            }
//        }
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