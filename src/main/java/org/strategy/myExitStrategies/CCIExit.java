package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.indicators.CCIIndicator;


public class CCIExit extends Strategy  {


    CCIIndicator cciIndicator ;


    public CCIExit(Integer timeFrame, TimeSeriesRepo timeSeriesRepo) {


        // log:
//        rulesForLog.add(ruleForSell);
//        rulesForLog.add(ruleForBuy);
//        indicatorsForLog.add(closePriceD);
//        indicatorsForLog.add(kcU);

//        indicatorsForLog.add(chaikinIndicator);
//        setLogOn();

    }

    public void init() {
        cciIndicator = new CCIIndicator(tradeEngine.series, 30);



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
//        System.out.println("onTickEvent------------- "+timeSeriesRepo.bid);


    }

//    @Override
//    public void onExitEvent(Order order){
//        if (order.closePhase==0 && order.getProfit()>0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.closePhase = 1;
//            if (order.type == Order.Type.BUY) {
//                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.prevIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            } else {
//                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.prevIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
//            }
//            tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.STOPLOSS, false);
//        } else order.closedAmount = order.openedAmount;
//
//    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.period == timeFrame) {

            for (Order order : tradeEngine.openedOrders) {


                    if (order.type == Order.Type.BUY) {
                        if (cciIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue()>220) {
                            tradeEngine.closeOrder(order);
                            continue;
                        }

                    } else {
                        if (cciIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue()<-220) {
                            tradeEngine.closeOrder(order);
                            continue;
                        }

                    }
//                int openIndex = tradeEngine.series.getIndex(order.openTime);
//                if (tradeEngine.currentIndex - openIndex > 4)
//                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);



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

//            double openProfit=0.0;
//            for (Order order : tradeEngine.openedOrders) {
//                openProfit+=order.getProfit(tradeEngine.timeSeriesRepo.bid);
//            }
//            if (openProfit<-2.0) {
//                for (Order order : tradeEngine.openedOrders) {
//                    tradeEngine.closeOrder(order);
//                }
//            }
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