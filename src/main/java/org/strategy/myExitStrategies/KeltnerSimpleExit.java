package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.strategy.Order.ExitType.EXITRULE;


public class KeltnerSimpleExit extends Strategy {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    List<Order> ordersToClose = new ArrayList<>();

    public void init() {

        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 5.6, 54);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 5.6, 54);
    }


    public void onTradeEvent(Order order) {

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
        if (order.phase == 0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0) {
            order.closedAmount = order.openedAmount / 2.0;
            order.phase = 1;
            order.takeProfit=0.0;
            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, false);
        } else order.closedAmount = order.openedAmount;


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.timeFrame == timeFrame) {
            int strongBuy = 0;

            if (tradeEngine.openedOrders.size() > 0) {
                ordersToClose.clear();

                ZonedDateTime currentTime = tradeEngine.series.getCurrentTime();
                for (Rule rule : tradeEngine.entryStrategy.ruleForSell.getRuleSet()) {
                    if (rule.isSatisfied(currentTime)) strongBuy--;
                }
                for (Rule rule : tradeEngine.entryStrategy.ruleForBuy.getRuleSet()) {
                    if (rule.isSatisfied(currentTime)) strongBuy++;
                }
//                System.out.println("order id: " + order.id + "  time: " + currentTime + "   strong:" + strongBuy);


                for (Order order : tradeEngine.openedOrders) {
                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (order.phase ==0) {
                        if (order.type == Order.Type.BUY) {
                            if (strongBuy < -1 && tradeEngine.series.getCurrentIndex() - openIndex > 4)
                                ordersToClose.add(order);
                            else {
                                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                            }
//                    if (order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < atrValueCorrection || order.openPrice - lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > atrValueLimit)
//                        tradeEngine.setExitPrice(order, order.openPrice - atrStopLoss, TradeEngine.ExitMode.STOPLOSS, true);
//                    else
//                        tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - atrValueCorrection, TradeEngine.ExitMode.STOPLOSS, true);

                        } else {
                            if (strongBuy > 1 && tradeEngine.series.getCurrentIndex() - openIndex >4)
                                ordersToClose.add(order);
                            else {
                                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                            }
//                    if (highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice < atrValueCorrection || highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - order.openPrice>atrValueLimit)
//                        tradeEngine.setExitPrice(order, order.openPrice + atrStopLoss, TradeEngine.ExitMode.STOPLOSS, true);
//                    else
//                        tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+ atrValueCorrection, TradeEngine.ExitMode.STOPLOSS, true);

                        }
                    }
                    if (order.phase ==1) {
                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                    }
                    if (tradeEngine.series.getCurrentIndex() - openIndex > 24)
                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
                }

                for (Order order : ordersToClose) {
                    order.closedAmount = order.openedAmount;
                    order.exitType = EXITRULE;
                    tradeEngine.closeOrder(order);
                }
                if (ordersToClose.size() > 0)
                    tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);
            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}