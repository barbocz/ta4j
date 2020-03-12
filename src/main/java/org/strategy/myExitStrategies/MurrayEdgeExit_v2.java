package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;

import static org.strategy.Order.ExitType.EXITRULE;


public class MurrayEdgeExit_v2 extends Strategy {


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
//        Num entryLevel = tradeEngine.series.numOf(order.parameters.get("entry"));
        double takeProfit = 0.0, stopLoss = 0.0;

//        if (tradeEngine.currentBarIndex > 8402) {
//            System.out.println("Break");
//        }
//        Num entryLevel=tradeEngine.series.numOf(order.openPrice);
//        entryLevel=tradeEngine.series.numOf(order.openPrice+0.00020);
//
//        for (int i = 0; i < 13; i++) {
//            if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isGreaterThan(entryLevel)) {
//                if (order.type == Order.Type.BUY) takeProfit = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
//                else stopLoss = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
//                break;
//            }
//        }
//        entryLevel=tradeEngine.series.numOf(order.openPrice-0.00020);
//        for (int i = 12; i >=0; i--) {
//            if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).isLessThan(entryLevel)) {
//                if (order.type == Order.Type.BUY) stopLoss = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
//                else takeProfit = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
//                break;
//            }
//        }
//        double height=murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathIndicators[0].getValue(tradeEngine.currentBarIndex).doubleValue();
////        double l1=murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue();
////        double l0=murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue();
//        if (stopLoss == 0.0) {
//            if (order.type == Order.Type.BUY) stopLoss=entryLevel.doubleValue()-height;
//            else stopLoss=entryLevel.doubleValue()+height;
//        }
//        if (takeProfit == 0.0) {
//            if (order.type == Order.Type.BUY) takeProfit=entryLevel.doubleValue()+height;
//            else takeProfit=entryLevel.doubleValue()-height;
//        }
//        order.takeProfit=takeProfit;
//        order.stopLoss=stopLoss;
        if (order.type == Order.Type.BUY) {
//            order.takeProfit = murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex).doubleValue()-0.000010;
            order.takeProfit = 0.0;
//            order.stopLoss = murrayMathIndicators[1].getValue(tradeEngine.currentBarIndex).doubleValue()-0.00015;
//            order.stopLoss = order.openPrice - 0.00015;
            order.stopLoss =lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.00005;
            for (Order openOrder : tradeEngine.openedOrders) {
                if (openOrder.type == Order.Type.SELL) {
                    openOrder.closedAmount = openOrder.openedAmount;
                    openOrder.closePrice = tradeEngine.timeSeriesRepo.bid;
                    openOrder.exitType = EXITRULE;
                    try {
                        tradeEngine.closeOrder(openOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } else {
//            order.takeProfit = murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex).doubleValue()+0.000010;
            order.takeProfit = 0.0;
//            order.stopLoss = murrayMathIndicators[11].getValue(tradeEngine.currentBarIndex).doubleValue()+0.00015;
//            order.stopLoss = order.openPrice + 0.00015;
            order.stopLoss = highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.00005;
            for (Order openOrder : tradeEngine.openedOrders) {
                if (openOrder.type == Order.Type.BUY) {
                    openOrder.closedAmount = openOrder.openedAmount;
                    openOrder.closePrice = tradeEngine.timeSeriesRepo.bid;
                    openOrder.exitType = EXITRULE;
                    try {
                        tradeEngine.closeOrder(openOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
        double takeProfit = 0.0, stopLoss = 0.0, correction = 0.0;
//        if (tradeEngine.currentBarIndex>8402) {
//            System.out.println("Break");
//        }
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.SELL) {

                for (int i = 1; i < 11; i++) {
                    if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() > tradeEngine.timeSeriesRepo.bid) {
                        takeProfit = murrayMathIndicators[i - 1].getValue(tradeEngine.currentBarIndex).doubleValue() + 0.00003;
                        if ((takeProfit < order.takeProfit || order.takeProfit == 0.0) && order.openPrice - 0.00010 > takeProfit)
//                                tradeEngine.setExitPrice(order, murrayMathIndicators[i -1].getValue(tradeEngine.currentBarIndex).doubleValue()+0.00008, TradeEngine.ExitMode.TAKEPROFIT, false);
                            order.takeProfit = takeProfit;


                        if (order.stopLoss != murrayMathIndicators[i + 2].getValue(tradeEngine.currentBarIndex).doubleValue()) {

                            if (murrayMathIndicators[i + 2].getValue(tradeEngine.currentBarIndex).doubleValue() > order.openPrice)
                                continue;
                            if (murrayMathIndicators[i + 2].getValue(tradeEngine.currentBarIndex).doubleValue() - tradeEngine.timeSeriesRepo.bid > 0.0007)
                                correction = 0.0003;
                            if (order.phase==100) tradeEngine.setExitPrice(order, murrayMathIndicators[i + 2].getValue(tradeEngine.currentBarIndex).doubleValue() + correction, TradeEngine.ExitMode.STOPLOSS, true);

                        }
                        break;
                    }
                }
                if (order.stopLoss > order.openPrice) {
//                    if (order.openPrice - tradeEngine.timeSeriesRepo.ask > 0.00015 && order.stopLoss >= order.openPrice) order.stopLoss = order.openPrice;
                }
            } else {

                for (int i = 11; i >= 2; i--) {
                    if (murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() < tradeEngine.timeSeriesRepo.bid) {
                        takeProfit = murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue() - 0.00003;
                        if (takeProfit > order.takeProfit && order.openPrice + 0.00010 < takeProfit) {
//                                tradeEngine.setExitPrice(order, murrayMathIndicators[i+1].getValue(tradeEngine.currentBarIndex).doubleValue()-0.00008, TradeEngine.ExitMode.TAKEPROFIT, false);
                            order.takeProfit = takeProfit;
                        }
                        if (order.stopLoss != murrayMathIndicators[i - 2].getValue(tradeEngine.currentBarIndex).doubleValue()) {

                            if (murrayMathIndicators[i - 2].getValue(tradeEngine.currentBarIndex).doubleValue() < order.openPrice)
                                continue;
                            if (tradeEngine.timeSeriesRepo.bid - murrayMathIndicators[i - 2].getValue(tradeEngine.currentBarIndex).doubleValue() > 0.0007)
                                correction = 0.0003;
                            if (order.phase==100) tradeEngine.setExitPrice(order, murrayMathIndicators[i - 2].getValue(tradeEngine.currentBarIndex).doubleValue() - correction, TradeEngine.ExitMode.STOPLOSS, true);

                        }
                        break;
                    }
                }
                if (order.stopLoss < order.openPrice) {
//                    if (tradeEngine.timeSeriesRepo.bid - order.openPrice > 0.00015  && order.stopLoss <= order.openPrice) order.stopLoss = order.openPrice;
                }
            }
        }


    }

    @Override
    public void onBeforeCloseOrder(Order order) {
        if (order.exitType == Order.ExitType.TAKEPROFIT) {
            if (order.doubleParameters.get("lastTP") != null && order.takeProfit == order.doubleParameters.get("lastTP")) {
                order.closedAmount = 0.0;
                tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.ANY, false);
                return;
            }
            if (order.openedAmount<30000) {
                order.closedAmount = order.openedAmount;
                order.phase=100;
            }
            else order.closedAmount = order.openedAmount / 2.0;

            order.doubleParameters.put("lastTP", order.takeProfit);
//            order.closePhase = 1;
//            if (order.type == Order.Type.BUY) {
//                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            } else {
//                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            }
            tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.ANY, true);
        } else order.closedAmount = order.openedAmount;
//        order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        double totalProfit=0.0;
        boolean bigLoss=false;
        int openedSince=0;
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

//                int openIndex = tradeEngine.series.getIndex(order.openTime);
//                if (tradeEngine.series.getCurrentIndex() - openIndex > 4)
//                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);

                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.series.getCurrentIndex() - openIndex > 64)
                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
                totalProfit+=order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid);
                if (order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)<-10.0) bigLoss=true;
                openedSince+=tradeEngine.series.getCurrentIndex()-tradeEngine.series.getIndex(order.openTime);


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




        if (bigLoss && totalProfit>1.0 || (openedSince>500 && totalProfit>0.0)) {
            for (Order order : tradeEngine.openedOrders) {
                order.closedAmount = order.openedAmount;
                order.closePrice = tradeEngine.timeSeriesRepo.bid;
                order.exitType = EXITRULE;
                tradeEngine.closeOrder(order);

            }
            tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);
        }

//            for (Order order : tradeEngine.openedOrders) {
//                if (order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)<-300.0) {
//                    order.closedAmount = order.openedAmount;
//                    order.closePrice = tradeEngine.timeSeriesRepo.bid;
//                    order.exitType = EXITRULE;
//                    tradeEngine.closeOrder(order);
//                }
//
//            }
//            tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);

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