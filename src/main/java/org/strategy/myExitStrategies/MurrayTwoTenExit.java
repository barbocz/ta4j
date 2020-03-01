package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathMultiIndicator;


public class MurrayTwoTenExit extends Strategy  {
    MurrayMathMultiIndicator murrayMathMultiIndicatorHigh,murrayMathMultiIndicatorExtremeHigh,murrayMathMultiIndicatorLow,murrayMathMultiIndicatorExtremeLow;
    double murrayRange =0;
    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        murrayMathMultiIndicatorHigh = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.HIGH);
        murrayMathMultiIndicatorLow = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.LOW);
        murrayMathMultiIndicatorExtremeHigh = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_HIGH);
        murrayMathMultiIndicatorExtremeLow = new MurrayMathMultiIndicator(tradeEngine.series, MurrayMathMultiIndicator.LevelType.EXTREME_LOW);


    }


    public void onTradeEvent(Order order) {


        if (tradeEngine.openedOrders.size()==1) {
            if (order.type == Order.Type.BUY) {
                order.takeProfit = order.openPrice + murrayRange *0.5;
                order.stopLoss = order.openPrice - 1.3 * murrayRange;
            } else {
                order.takeProfit = order.openPrice - murrayRange *0.5;
                order.stopLoss = order.openPrice + 1.3 * murrayRange;
            }
        } else {
            Order firstOrder=tradeEngine.openedOrders.get(0);
            order.takeProfit=firstOrder.openPrice;
            order.stopLoss=firstOrder.stopLoss;
            firstOrder.takeProfit=firstOrder.openPrice;
            order.phase =1;
        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order){
        if (tradeEngine.openedOrders.size()==1 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0 && order.phase ==0 ) {
            if (order.openPrice!=order.closePrice) {
                order.closedAmount = order.openedAmount * 0.5;
                order.stopLoss = order.openPrice;
                if (order.type == Order.Type.BUY) order.takeProfit = order.openPrice + murrayRange;
                else order.takeProfit = order.openPrice - murrayRange;
//            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
                order.phase = 1;
            } else order.closedAmount = order.openedAmount;
        } else order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.period == timeFrame) {
            murrayRange =murrayMathMultiIndicatorExtremeHigh.getValue(tradeEngine.currentBarIndex).doubleValue() - murrayMathMultiIndicatorHigh.getValue(tradeEngine.currentBarIndex).doubleValue();
            for (Order order : tradeEngine.openedOrders) {

//                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                        tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue() , TradeEngine.ExitMode.STOPLOSS, true);
                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.series.getCurrentIndex() - openIndex > 7 && tradeEngine.openedOrders.size()==1)
                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);


            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}