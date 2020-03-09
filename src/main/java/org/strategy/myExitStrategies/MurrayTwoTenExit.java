package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;


public class MurrayTwoTenExit extends Strategy {
    final Double murrayRange = 38.12;
    final Double murrayRangeInPip = murrayRange / 100000;
    final Double stopLossInPip= 54.0 / 100000.0;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double murrayHeight = 0.0;
    MoneyFlowIndicator moneyFlowIndicator;
    ClosePriceIndicator closePriceIndicator;
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;

    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);
        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
    }


    public void onTradeEvent(Order order) {
        if (order.type == Order.Type.BUY) {
            tradeEngine.setStopLoss(order,order.openPrice - stopLossInPip);

//            order.takeProfit = order.openPrice + 3 * murrayRangeInPip-0.00016;
            order.takeProfitTarget = order.openPrice + 3 * murrayRangeInPip-0.00016;
        } else {
            tradeEngine.setStopLoss(order,order.openPrice + stopLossInPip);
//            order.takeProfit = order.openPrice - 3* murrayRangeInPip+0.00016;
            order.takeProfitTarget = order.openPrice - 3* murrayRangeInPip+0.00016;
        }


    }


    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
//        for (Order order : tradeEngine.openedOrders) {
//            if (order.phase > 1) continue;
//            if (order.type == Order.Type.BUY && tradeEngine.timeSeriesRepo.bid > order.takeProfitTarget - 0.0001) {
////                if (tradeEngine.currentBarIndex > 10414) {
////                    System.out.println("");
////                }
//                order.closedAmount = order.openedAmount / 4.0;
//                order.closePrice = tradeEngine.timeSeriesRepo.bid;
//                order.exitType = EXITRULE;
//                order.stopLoss = order.takeProfitTarget - murrayRangeInPip * 3.0 - 0.0001;
//                tradeEngine.closeOrder(order);
//                order.takeProfitTarget = order.takeProfitTarget + murrayRangeInPip;
//                order.phase++;
//
//            } else if (order.type == Order.Type.SELL && tradeEngine.timeSeriesRepo.ask < order.takeProfitTarget + 0.0001) {
//                order.closedAmount = order.openedAmount / 4.0;
//                order.closePrice = tradeEngine.timeSeriesRepo.ask;
//                order.exitType = EXITRULE;
//                order.stopLoss = order.takeProfitTarget + murrayRangeInPip * 3.0 + 0.0001;
//                tradeEngine.closeOrder(order);
//                order.takeProfitTarget = order.takeProfitTarget - murrayRangeInPip;
//                order.phase++;
//            }
//        }
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY) {
                if (order.stopLoss < order.openPrice && tradeEngine.timeSeriesRepo.ask - 0.0005 > order.openPrice) tradeEngine.setStopLoss( order,order.openPrice+ 0.0001);
                if (tradeEngine.timeSeriesRepo.ask>order.takeProfitTarget) {
//                    order.stopLoss=order.takeProfitTarget - 0.0001;
                    order.takeProfitTarget=order.takeProfitTarget + 0.0001;
                }
            }
            else {
                if (order.stopLoss > order.openPrice && tradeEngine.timeSeriesRepo.ask + 0.0005 < order.openPrice)   tradeEngine.setStopLoss( order,order.stopLoss = order.openPrice- 0.0001);
                if (tradeEngine.timeSeriesRepo.bid<order.takeProfitTarget) {
//                    order.stopLoss=order.takeProfitTarget + 0.0001;
                    order.takeProfitTarget=order.takeProfitTarget - 0.0001;
                }
            }
        }


    }

    @Override
    public void onExitEvent(Order order) {
//        if (tradeEngine.openedOrders.size()==1 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0 && order.phase ==0 ) {
//            if (order.openPrice!=order.closePrice) {
//                order.closedAmount = order.openedAmount * 0.5;
//                order.stopLoss = order.openPrice;
//                if (order.type == Order.Type.BUY) order.takeProfit = order.openPrice + murrayRange;
//                else order.takeProfit = order.openPrice - murrayRange;
////            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
//                order.phase = 1;
//            } else order.closedAmount = order.openedAmount;
//        } else
//            order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        murrayHeight = murrayLevels[1] - murrayLevels[0];
        for (Order order : tradeEngine.openedOrders) {
            int openIndex = tradeEngine.series.getIndex(order.openTime);
            if (order.type == Order.Type.BUY) {
//                order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - 0.0001;
                if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex-2).doubleValue() - moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>50.0) {
                    if (tradeEngine.series.getCurrentIndex() - openIndex < 10)
                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.00038);
//                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.00038, TradeEngine.ExitMode.ANY, true);
                    else
                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.0002);
//                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.0002, TradeEngine.ExitMode.ANY, true);
                }
            } else {
//                if (tradeEngine.currentBarIndex > 10302) {
//                    System.out.println("");
//                }
//                order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + 0.0001;
                if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - moneyFlowIndicator.getValue(tradeEngine.currentBarIndex-2).doubleValue()>50.0) {
                    if (tradeEngine.series.getCurrentIndex() - openIndex < 10)
                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.00038);
//                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.00038, TradeEngine.ExitMode.ANY, true);
                    else
                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.0002);
//                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.0002, TradeEngine.ExitMode.ANY, true);
                }
            }

            if (tradeEngine.series.getCurrentIndex() - openIndex > 14) {

                    if (order.type == Order.Type.BUY) {
                        tradeEngine.setExitPrice(order,order.openPrice+0.0001);
//                        tradeEngine.setExitPrice(order, order.openPrice+0.0001, TradeEngine.ExitMode.ANY, true);
                    } else {
                        tradeEngine.setExitPrice(order,order.openPrice-0.0001);
//                        tradeEngine.setExitPrice(order, order.openPrice-0.0001, TradeEngine.ExitMode.ANY, true);
                    }

            }

        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}