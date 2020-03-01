package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;


public class MurrayLevelChangedExit_v4 extends Strategy {

    ATRIndicator atrIndicator;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    LowestValueIndicator lowestValueIndicator;
    HighestValueIndicator highestValueIndicator;

    public void init() {


        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 38.0);
        }
        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);


    }


    public void onTradeEvent(Order order) {
//        double entryLevel = tradeEngine.series.numOf(order.parameters.get("entry")).doubleValue();
        double takeProfit = 0.0, stopLoss = 0.0;

//        if (tradeEngine.currentBarIndex>8949) {
//            System.out.println("Break");
//        }
        if (order.type == Order.Type.BUY) {
            for (int i = 12; i >= 1; i--) {
                if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() < order.openPrice) {
                    if (murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice < 0.0003) {
                        if (i == 11)
                            takeProfit = order.openPrice + (order.openPrice - lowestValueIndicator.getValue(order.barIndex).doubleValue());
                        else
                            takeProfit = murrayMathIndicators[i + 2].getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00005;
                    } else
                        takeProfit = murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00005;
                    break;
                }
            }
            stopLoss = lowestValueIndicator.getValue(order.barIndex).doubleValue() - 0.00008;
        } else {
            for (int i = 1; i < 13; i++) {
                if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() > order.openPrice) {
                    if (order.openPrice - murrayMathIndicators[i - 1].getValue(tradeEngine.currentBarIndex).doubleValue() < 0.0003) {
                        if (i > 1)
                            takeProfit = murrayMathIndicators[i - 2].getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00005;
                        else
                            takeProfit = order.openPrice - (highestValueIndicator.getValue(order.barIndex).doubleValue() - order.openPrice);
                    } else
                        takeProfit = murrayMathIndicators[i - 1].getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00005;
                    break;
                }
            }
            stopLoss = highestValueIndicator.getValue(order.barIndex).doubleValue() + 0.00008;
        }
        order.takeProfit = takeProfit;
        order.stopLoss = stopLoss;

//            tradeEngine.setExitPrice(order, takeProfit, TradeEngine.ExitMode.TAKEPROFIT, true);
//            tradeEngine.setExitPrice(order, stopLoss, TradeEngine.ExitMode.STOPLOSS, true);


    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
        //if (order.phase==0 && ((order.type == Order.Type.BUY && order.takeProfit>order.openPrice) || (order.type == Order.Type.SELL && order.takeProfit<order.openPrice))) {
//        if (order.phase==0 && order.exitType==Order.ExitType.TAKEPROFIT && order.profit>0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.phase = 100;
//            double murrayHeight = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();
//            if (order.type == Order.Type.BUY) {
////                tradeEngine.setExitPrice(order, order.takeProfit+murrayHeight, TradeEngine.ExitMode.TAKEPROFIT, false);
////                tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, false);
//                order.takeProfit=order.takeProfit + 0.00038;
//                order.stopLoss=order.openPrice;
//            } else {
//                order.takeProfit=order.takeProfit - 0.00038;
//                order.stopLoss=order.openPrice;
////                tradeEngine.setExitPrice(order, order.takeProfit-murrayHeight, TradeEngine.ExitMode.TAKEPROFIT, false);
////                tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, false);
//            }
//
//        } else order.closedAmount = order.openedAmount;
        order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.period == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {


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

                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (order.phase == 0 && tradeEngine.series.getCurrentIndex() - openIndex > 21)
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