package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import static org.strategy.Order.ExitType.EXITRULE;
import static org.strategy.Order.ExitType.TAKEPROFIT;


public class MurrayTwoTenExit_v2 extends Strategy {
    final Double murrayRange = 38.12;
    final double proportionOfFirstClose = 0.2;


    final Double murrayRangeInPip = murrayRange / 100000;
    final Double stopLossInPip = 54.0 / 100000.0;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double murrayHeight = 0.0, atrValue = 0.0, atrBasedMurrayBias = 0.0,atrBasedTrailingBias = 0.0;
    MoneyFlowIndicator moneyFlowIndicator;

    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    ATRIndicator atrIndicator;
    boolean isOverBought=false,isOverSold=false;

    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);

        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        atrIndicator = new ATRIndicator(tradeEngine.series, 14);
    }


    public void onTradeEvent(Order order) {
        int firstCorrectionLevelIndex;
        if (order.type == Order.Type.BUY) {
//            if (tradeEngine.currentBarIndex>12850) {
//                System.out.println("");
//            }

            tradeEngine.setStopLoss(order, murrayLevels[0] - 0.0005);

            tradeEngine.setTakeProfit(order, getNextMurrayLevel(order.openPrice + atrValue, -1.0 * atrBasedMurrayBias, true));
//                order.stopLoss = order.openPrice - 0.003;//0.00264;


////            order.takeProfit = order.openPrice + 3 * murrayRangeInPip-0.00016;
//            order.takeProfitTarget = getNextMurrayLevel(order.openPrice+atrValue,-1.0*atrBasedBias,true);
//            tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
        } else {
            tradeEngine.setStopLoss(order, murrayLevels[12] + 0.0005);
            tradeEngine.setTakeProfit(order, getNextMurrayLevel(order.openPrice - atrValue, atrBasedMurrayBias, false));
//            tradeEngine.setStopLoss(order,order.openPrice + stopLossInPip);
////            order.takeProfit = order.openPrice - 3* murrayRangeInPip+0.00016;
//            order.takeProfitTarget = getNextMurrayLevel(order.openPrice-atrValue,atrBasedBias,true);
//            tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
        }


    }


    public void onTickEvent() throws Exception {

        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY && isOverBought && tradeEngine.timeSeriesRepo.bid - atrBasedTrailingBias>order.openPrice) {
                tradeEngine.setStopLoss(order, tradeEngine.timeSeriesRepo.bid - atrBasedTrailingBias);
            } else  if (order.type == Order.Type.SELL && isOverSold && tradeEngine.timeSeriesRepo.ask + atrBasedTrailingBias<order.openPrice) {
                tradeEngine.setStopLoss(order, tradeEngine.timeSeriesRepo.ask + atrBasedTrailingBias);
            }
        }


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


//        for (Order order : tradeEngine.openedOrders) {
//            if (order.type == Order.Type.BUY) {
//                if (order.stopLoss < order.openPrice && tradeEngine.timeSeriesRepo.ask - 0.0005 > order.openPrice) tradeEngine.setStopLoss( order,order.openPrice+ 0.0001);
//                if (tradeEngine.timeSeriesRepo.ask>order.takeProfitTarget) {
////                    order.stopLoss=order.takeProfitTarget - 0.0001;
//                    order.takeProfitTarget=order.takeProfitTarget + 0.0001;
//                    tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
//                }
//            }
//            else {
//                if (order.stopLoss > order.openPrice && tradeEngine.timeSeriesRepo.ask + 0.0005 < order.openPrice)   tradeEngine.setStopLoss( order,order.stopLoss = order.openPrice- 0.0001);
//                if (tradeEngine.timeSeriesRepo.bid<order.takeProfitTarget) {
////                    order.stopLoss=order.takeProfitTarget + 0.0001;
//                    order.takeProfitTarget=order.takeProfitTarget - 0.0001;
//                    tradeEngine.logOrderActivity(order,"TARGET","takeProfitTarget: "+order.takeProfitTarget);
//                }
//            }
//        }


    }

    @Override
    public void onBeforeCloseOrder(Order order) {
        if (order.exitType == TAKEPROFIT && order.phase==0) order.closedAmount = order.openedAmount * proportionOfFirstClose;
    }

    @Override
    public void onAfterCloseOrder(Order order) {
        if (order.exitType == TAKEPROFIT && order.phase==0) {
            order.phase++;

            if (order.type == Order.Type.BUY) {

                tradeEngine.setTakeProfit(order, 0.0);
                tradeEngine.setStopLoss(order,order.openPrice+0.0001 );
                order.keepItsProfit=true;
            } else {
                tradeEngine.setTakeProfit(order, 0.0);
                tradeEngine.setStopLoss(order,order.openPrice-0.0001 );
                order.keepItsProfit=true;
            }

        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        murrayHeight = murrayLevels[1] - murrayLevels[0];
//        if (tradeEngine.currentBarIndex>12850) {
//            System.out.println("");
//        }
        atrValue = atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        atrBasedMurrayBias = atrValue / 10.0;
        atrBasedTrailingBias= atrValue * 0.9;

        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==100.0) isOverBought=true; else isOverBought=false;
        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0) isOverSold=true; else isOverSold=false;


//        for (Order order : tradeEngine.openedOrders) {
//            int openIndex = tradeEngine.series.getIndex(order.openTime);
//            if (tradeEngine.series.getCurrentIndex() - openIndex > 7) {
//                order.closedAmount = order.openedAmount;
//                order.closePrice = tradeEngine.timeSeriesRepo.bid;
//                order.exitType = EXITRULE;
//                tradeEngine.closeOrder(order);
//            }
//        }
//        tradeEngine.openedOrders.removeIf((Order openedOrder) -> openedOrder.openedAmount == 0.0);
//        for (Order order : tradeEngine.openedOrders) {
//            int openIndex = tradeEngine.series.getIndex(order.openTime);
//            if (order.type == Order.Type.BUY) {
////                order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - 0.0001;
//                if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex-2).doubleValue() - moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>50.0) {
//                    if (tradeEngine.series.getCurrentIndex() - openIndex < 10)
//                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.00038);
////                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.00038, TradeEngine.ExitMode.ANY, true);
//                    else
//                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.0002);
////                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-0.0002, TradeEngine.ExitMode.ANY, true);
//                }
//            } else {
////                if (tradeEngine.currentBarIndex > 10302) {
////                    System.out.println("");
////                }
////                order.takeProfit = keltnerChannelMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() + 0.0001;
//                if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() - moneyFlowIndicator.getValue(tradeEngine.currentBarIndex-2).doubleValue()>50.0) {
//                    if (tradeEngine.series.getCurrentIndex() - openIndex < 10)
//                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.00038);
////                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.00038, TradeEngine.ExitMode.ANY, true);
//                    else
//                        tradeEngine.setExitPrice(order,closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.0002);
////                        tradeEngine.setExitPrice(order, closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+0.0002, TradeEngine.ExitMode.ANY, true);
//                }
//            }
//
//            if (tradeEngine.series.getCurrentIndex() - openIndex > 14) {
//
//                    if (order.type == Order.Type.BUY) {
//                        tradeEngine.setExitPrice(order,order.openPrice+0.0001);
////                        tradeEngine.setExitPrice(order, order.openPrice+0.0001, TradeEngine.ExitMode.ANY, true);
//                    } else {
//                        tradeEngine.setExitPrice(order,order.openPrice-0.0001);
////                        tradeEngine.setExitPrice(order, order.openPrice-0.0001, TradeEngine.ExitMode.ANY, true);
//                    }
//
//            }
//
//        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }

    int getMurrayRange(double value) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        }
        return -1;
    }

    double getNextMurrayLevel(double currentPrice, double bias, boolean upper) {
        int murrayLevel;

//        if (tradeEngine.currentBarIndex>12850) {
//            System.out.println("");
//        }
        if (upper) {
            murrayLevel = getMurrayRange(currentPrice);
            if (murrayLevel < 0 || murrayLevel == 12) {
                if (currentPrice > murrayLevels[12]) return murrayLevels[12] + atrValue + bias;
                return murrayLevels[12] + bias;
            } else {
                murrayLevel++;
                return murrayLevels[murrayLevel] + bias;
            }
        } else {
            murrayLevel = getMurrayRange(currentPrice);
            if (murrayLevel < 0) {
                if (currentPrice < murrayLevels[0]) return murrayLevels[0] - atrValue + bias;
                return murrayLevels[0] + bias;
            } else return murrayLevels[murrayLevel] + bias;
        }


    }


}