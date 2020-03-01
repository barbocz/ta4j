package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.*;

import java.util.ArrayList;
import java.util.List;

import static org.strategy.Order.ExitType.EXITRULE;


public class TestExit extends Strategy {

    List<Order> ordersToClose = new ArrayList<>();
    double barNumberLetOpen=32.0;


    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);



    }


    public void onTradeEvent(Order order) {
//        double atrValue = 5.0 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
//        if (order.type == Order.Type.BUY) {
//
//            tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//            if (order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < atrValue)
//                tradeEngine.setExitPrice(order, order.openPrice - atrValue, TradeEngine.ExitMode.STOPLOSS, true);
//            else
//                tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//
//        } else {
//            tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//            if (highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice < atrValue)
//                tradeEngine.setExitPrice(order, order.openPrice + atrValue, TradeEngine.ExitMode.STOPLOSS, true);
//            else
//                tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//        }

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
//        if (order.closePhase == 0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.closePhase = 1;
//
////            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.STOPLOSS, false);
//        } else order.closedAmount = order.openedAmount;
        order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.period == timeFrame) {
//            for (Order order : tradeEngine.openedOrders) {
//                int openIndex = tradeEngine.series.getIndex(order.openTime);
//                if (tradeEngine.series.getCurrentIndex() - openIndex > 7)
//                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
//            }
            ordersToClose.clear();

//            for (Order order : tradeEngine.openedOrders) {
//                order.phase++;
//                order.closedAmount = tradeEngine.initialAmount / barNumberLetOpen;
//                order.closePrice=tradeEngine.timeSeriesRepo.bid;
//                order.exitType = EXITRULE;
//                tradeEngine.closeOrder(order);
//            }

            for (Order order : tradeEngine.openedOrders) {
                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.series.getCurrentIndex() - openIndex > 7) {
                    order.closedAmount = order.openedAmount;
                    order.closePrice = tradeEngine.timeSeriesRepo.bid;
                    order.exitType = EXITRULE;
                    tradeEngine.closeOrder(order);
                }
            }


            tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);

        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}