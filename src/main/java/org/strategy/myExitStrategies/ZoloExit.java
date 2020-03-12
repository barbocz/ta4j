package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


public class ZoloExit extends Strategy  {


    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
    }


    public void onTradeEvent(Order order) {
        if (order.type == Order.Type.BUY) {
            tradeEngine.setStopLoss(order,order.openPrice - 0.00033);

        } else {
            tradeEngine.setStopLoss(order,order.openPrice + 0.00033);
        }
    }


    public void onTickEvent() {

    }

    @Override
    public void onBeforeCloseOrder(Order order){
         order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.period == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {
                if (order.type == Order.Type.BUY) {
                    if (order.stopLoss < order.openPrice) {
                        if (tradeEngine.timeSeriesRepo.ask - 0.00055 > order.openPrice) tradeEngine.setStopLoss( order,order.openPrice);
                    } else {
                        if (tradeEngine.timeSeriesRepo.ask - 0.00033 > order.stopLoss) tradeEngine.setStopLoss( order,tradeEngine.timeSeriesRepo.ask - 0.00033);
                    }
                }
                else {
                    if (order.stopLoss > order.openPrice) {
                        if (tradeEngine.timeSeriesRepo.bid + 0.00055 < order.openPrice) tradeEngine.setStopLoss( order,order.openPrice);
                    } else {
                        if (tradeEngine.timeSeriesRepo.bid + 0.00033 < order.stopLoss) tradeEngine.setStopLoss( order,tradeEngine.timeSeriesRepo.bid + 0.00033);
                    }
                }
            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}