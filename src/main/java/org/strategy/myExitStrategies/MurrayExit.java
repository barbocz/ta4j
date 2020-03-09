package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.strategy.Order.ExitType.EXITRULE;


public class MurrayExit extends Strategy {

    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    MoneyFlowIndicator moneyFlowIndicator;
    LaguerreIndicator laguerreIndicator;
    Double murrayLevels[] = new Double[13];
    final Double murrayRange = 38.12;
    final Double murrayRangeInPip = murrayRange / 100000;
    final Double murrayRangeInPipForCorrection = 76.24 / 100000;
    boolean sellByMoneyFlow = false, buyByMoneyFlow = false;
    HighestValueIndicator highestLaguerre, highestMoneyFlow;
    LowestValueIndicator lowestLaguerre, lowestMoneyFlow;

    public void init() {
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.2);
        highestLaguerre = new HighestValueIndicator(laguerreIndicator, 3);
        lowestLaguerre = new LowestValueIndicator(laguerreIndicator, 3);
        highestMoneyFlow = new HighestValueIndicator(moneyFlowIndicator, 3);
        lowestMoneyFlow = new LowestValueIndicator(moneyFlowIndicator, 3);
        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
    }


    public void onTradeEvent(Order order) {
        int firstCorrectionLevelIndex, firstTakeProfitLevelIndex;
//        if (tradeEngine.currentBarIndex > 11261) {
//            System.out.println("");
//        }

        if (order.phase == 0) {                           // nyitó állapot
            if (order.type == Order.Type.BUY) {
                tradeEngine.setStopLoss(order,order.openPrice - 0.003);
//                order.stopLoss = order.openPrice - 0.003;//0.00264;
                firstCorrectionLevelIndex = getMurrayRange(order.openPrice - murrayRangeInPip);
                if (firstCorrectionLevelIndex < 0) firstCorrectionLevelIndex = 0;
                order.stopLossTarget = murrayLevels[firstCorrectionLevelIndex] - 0.00007;
                ;

                firstTakeProfitLevelIndex = getMurrayRange(order.openPrice + 0.0002);
                if (firstTakeProfitLevelIndex < 0 || firstTakeProfitLevelIndex == 12) firstTakeProfitLevelIndex = 12;
                else firstTakeProfitLevelIndex++;
                order.takeProfitTarget = murrayLevels[firstTakeProfitLevelIndex] - 0.00007;


            } else {
                tradeEngine.setStopLoss(order,order.openPrice + 0.003);
//                order.stopLoss = order.openPrice + 0.003;//0.00264;
                firstCorrectionLevelIndex = getMurrayRange(order.openPrice + murrayRangeInPip);
                if (firstCorrectionLevelIndex < 0 || firstCorrectionLevelIndex == 12) firstCorrectionLevelIndex = 12;
                else firstCorrectionLevelIndex++;
                order.stopLossTarget = murrayLevels[firstCorrectionLevelIndex] + 0.00007;
                ;

                firstTakeProfitLevelIndex = getMurrayRange(order.openPrice - 0.0002);
                if (firstTakeProfitLevelIndex < 0) firstTakeProfitLevelIndex = 0;
                order.takeProfitTarget = murrayLevels[firstTakeProfitLevelIndex] + 0.00007;
            }

        } else if (order.phase == 1) {                      // profit állapot
//            itt csak akkor lesz trade ha pozíciókat kezdünk építeni
        } else {                                        // loss állapot, korrekciós szintek kezelése


        }


    }


    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
//        if (tradeEngine.currentBarIndex>5638) {
//            System.out.println("");
//        }
        if (tradeEngine.openedOrders.size() > 0) {

//            List<Order> ordersToLose=new ArrayList<>();
//            for (Order order : tradeEngine.openedOrders) {
//                if (order.parentId!=0 || order.childOrders.size()>0) continue;
//                if (order.type == Order.Type.BUY) {
//                    if (sellByMoneyFlow && order.stopLoss < order.openPrice && tradeEngine.timeSeriesRepo.bid - 0.0004 > order.openPrice) {
////                        if (tradeEngine.series.getCurrentIndex() - tradeEngine.series.getIndex(order.openTime) > 3)
////                        order.stopLoss = order.openPrice + 0.0001;
////                        else ordersToLose.add(order);
//                        order.stopLoss = tradeEngine.timeSeriesRepo.bid - 0.0003;
//                    }
////                    if (tradeEngine.timeSeriesRepo.ask > order.takeProfitTarget) {
////                        order.stopLoss = order.openPrice + 0.0001;
//////                        order.takeProfitTarget=order.takeProfitTarget + 0.0001;
////                    }
//                } else {
//                    if (buyByMoneyFlow && order.stopLoss > order.openPrice && tradeEngine.timeSeriesRepo.ask + 0.0004 < order.openPrice) {
////                        if (tradeEngine.series.getCurrentIndex() - tradeEngine.series.getIndex(order.openTime) > 3)
////                            order.stopLoss = order.openPrice - 0.0001;
////                        else ordersToLose.add(order);
//                        order.stopLoss =tradeEngine.timeSeriesRepo.ask + 0.0003;
//                    }
//
////                    if (tradeEngine.timeSeriesRepo.bid < order.takeProfitTarget) {
////                        order.stopLoss = order.openPrice - 0.0001;
//////                        order.takeProfitTarget=order.takeProfitTarget - 0.0001;
////                    }
//                }
//            }

//            for (Order order: ordersToLose) {
//                order.closedAmount = order.openedAmount ;
//                if (order.type == Order.Type.BUY) order.closePrice = tradeEngine.timeSeriesRepo.bid; else order.closePrice = tradeEngine.timeSeriesRepo.ask;
//                order.exitType = EXITRULE;
//                tradeEngine.closeOrder(order);
//            }



            ZonedDateTime time = tradeEngine.series.getCurrentTime();
            Order buyStartOrder = null, sellStartOrder = null;
            for (Order order : tradeEngine.openedOrders) {
                if (order.parentId > 0) continue;
                if (order.type == Order.Type.BUY) {
                    buyStartOrder = order;
                    break;
                } else {
                    sellStartOrder = order;
                    break;
                }
            }


            if (buyStartOrder != null) {
                if (buyStartOrder.phase == 0) {
                    if (tradeEngine.timeSeriesRepo.ask > buyStartOrder.takeProfitTarget) {        // a kezdő trade simán eléri az első kijelölt target tp szintet
                        buyStartOrder.phase = 1;
//                        buyStartOrder.stopLoss = buyStartOrder.takeProfitTarget - murrayRangeInPip;
//                        buyStartOrder.stopLoss = buyStartOrder.openPrice + 0.00007;
                        tradeEngine.setStopLoss(buyStartOrder,buyStartOrder.openPrice + 0.00007);
                        buyStartOrder.takeProfitTarget = buyStartOrder.takeProfitTarget + murrayRangeInPip;


                    }
                    if (tradeEngine.timeSeriesRepo.bid < buyStartOrder.stopLossTarget) {        // a kezdő trade mínuszba megy és áttöri első kijelölt target sl szintet

                        buyStartOrder.phase = 2;
                        buyStartOrder.stopLossTarget = buyStartOrder.stopLossTarget - murrayRangeInPipForCorrection;
//                        buyStartOrder.stopLoss=buyStartOrder.openPrice - 0.003;
                        tradeEngine.setStopLoss(buyStartOrder,buyStartOrder.openPrice - 0.003);

                        Order correctionOrder = Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
//                        correctionOrder.stopLoss = buyStartOrder.stopLoss;
                        tradeEngine.setStopLoss(correctionOrder,buyStartOrder.stopLoss);
                        correctionOrder.parentId = buyStartOrder.id;
                        buyStartOrder.childOrders.add(correctionOrder);
                        tradeEngine.onTradeEvent(correctionOrder);
                        buyStartOrder.takeProfitTarget = getBreakLevel(buyStartOrder.type);

                    }
                } else if (buyStartOrder.phase == 1) {
                    if (tradeEngine.timeSeriesRepo.ask > buyStartOrder.takeProfitTarget) {        // profitban levő trade eléri a következő kijelölt target tp szintet
//                        buyStartOrder.stopLoss = buyStartOrder.takeProfitTarget - murrayRangeInPip;
                        tradeEngine.setStopLoss(buyStartOrder,buyStartOrder.takeProfitTarget - murrayRangeInPip);
                        buyStartOrder.takeProfitTarget = buyStartOrder.takeProfitTarget + murrayRangeInPip;
                    }
                } else {                                                                // korrekció management
                    if (tradeEngine.timeSeriesRepo.ask > buyStartOrder.takeProfitTarget) {        // a vesztes trade  eléri az első kijelölt korreciós target tp szintet
//                        tradeEngine.setExitPrice(buyStartOrder, buyStartOrder.takeProfitTarget - 0.0001, TradeEngine.ExitMode.ANY, true);
                        tradeEngine.setExitPrice(buyStartOrder, buyStartOrder.takeProfitTarget - 0.0001);
                        for (Order childOrder : buyStartOrder.childOrders) {
//                            tradeEngine.setExitPrice(childOrder, buyStartOrder.takeProfitTarget - 0.0001, TradeEngine.ExitMode.ANY, true);
                            tradeEngine.setExitPrice(childOrder, buyStartOrder.takeProfitTarget - 0.0001);
                        }
                    }
                    if (tradeEngine.timeSeriesRepo.bid < buyStartOrder.stopLossTarget && buyByMoneyFlow) {        // a vesztes trade  áttöri a következő kijelölt target sl szintet
                        buyStartOrder.phase++;
//                        if (buyStartOrder.phase > 2 && !buyByMoneyFlow) return;
                        buyStartOrder.stopLossTarget = buyStartOrder.stopLossTarget - murrayRangeInPipForCorrection;
//                        buyStartOrder.stopLoss=buyStartOrder.openPrice - 0.003;
                        tradeEngine.setStopLoss(buyStartOrder,buyStartOrder.openPrice - 0.003);

                        Order correctionOrder = Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
//                        correctionOrder.stopLoss = buyStartOrder.stopLoss;
//                        correctionOrder.stopLoss = tradeEngine.timeSeriesRepo.ask - 0.0002;
                        tradeEngine.setStopLoss(correctionOrder,tradeEngine.timeSeriesRepo.ask - 0.0002);
                        correctionOrder.parentId = buyStartOrder.id;
                        buyStartOrder.childOrders.add(correctionOrder);
                        tradeEngine.onTradeEvent(correctionOrder);
                        buyStartOrder.takeProfitTarget = getBreakLevel(buyStartOrder.type);
//                        if ( buyStartOrder.phase>3) System.out.println(time+"    "+buyStartOrder.phase + ". correctionLevel: " +"  loss: "+buyStartOrder.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)+ "  ttp: "+getBreakLevel(buyStartOrder.type));
                    }
                }


            }
            if (sellStartOrder != null) {

                if (sellStartOrder.phase == 0) {
                    if (tradeEngine.timeSeriesRepo.ask < sellStartOrder.takeProfitTarget) {        // a kezdő trade simán eléri az első kijelölt target tp szintet
                        sellStartOrder.phase = 1;
//                        sellStartOrder.stopLoss = sellStartOrder.takeProfitTarget + murrayRangeInPip;
//                        sellStartOrder.stopLoss = sellStartOrder.openPrice - 0.00007;
                        tradeEngine.setStopLoss(sellStartOrder,sellStartOrder.openPrice - 0.00007);
                        sellStartOrder.takeProfitTarget = sellStartOrder.takeProfitTarget - murrayRangeInPip;


                    }
                    if (tradeEngine.timeSeriesRepo.ask > sellStartOrder.stopLossTarget) {        // a kezdő trade mínuszba megy és áttöri első kijelölt target sl szintet
//                        System.out.println(sellStartOrder.phase + ". correctionLevel: " + sellStartOrder.stopLossTarget);
                        sellStartOrder.phase = 2;
                        sellStartOrder.stopLossTarget = sellStartOrder.stopLossTarget + murrayRangeInPipForCorrection;
//                        sellStartOrder.stopLoss=sellStartOrder.openPrice + 0.003;
                        tradeEngine.setStopLoss(sellStartOrder,sellStartOrder.openPrice + 0.003);

                        Order correctionOrder = Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime());
//                        correctionOrder.stopLoss = sellStartOrder.stopLoss;
                        tradeEngine.setStopLoss(correctionOrder,sellStartOrder.stopLoss);
                        correctionOrder.parentId = sellStartOrder.id;
                        sellStartOrder.childOrders.add(correctionOrder);
                        tradeEngine.onTradeEvent(correctionOrder);
                        sellStartOrder.takeProfitTarget = getBreakLevel(sellStartOrder.type);

                    }
                } else if (sellStartOrder.phase == 1) {
                    if (tradeEngine.timeSeriesRepo.ask < sellStartOrder.takeProfitTarget) {        // profitban levő trade eléri a következő kijelölt target tp szintet
//                        sellStartOrder.stopLoss = sellStartOrder.takeProfitTarget + murrayRangeInPip;
                        tradeEngine.setStopLoss(sellStartOrder,sellStartOrder.takeProfitTarget + murrayRangeInPip);
                        sellStartOrder.takeProfitTarget = sellStartOrder.takeProfitTarget - murrayRangeInPip;
                    }
                } else {                                                                // korrekció management
                    if (tradeEngine.timeSeriesRepo.ask < sellStartOrder.takeProfitTarget) {        // a vesztes trade  eléri az első kijelölt korreciós target tp szintet
//                        tradeEngine.setExitPrice(sellStartOrder, sellStartOrder.takeProfitTarget + 0.0001, TradeEngine.ExitMode.ANY, true);
                        tradeEngine.setExitPrice(sellStartOrder, sellStartOrder.takeProfitTarget + 0.0001);
                        for (Order childOrder : sellStartOrder.childOrders) {
//                            tradeEngine.setExitPrice(childOrder, sellStartOrder.takeProfitTarget + 0.0001, TradeEngine.ExitMode.ANY, true);
                            tradeEngine.setExitPrice(childOrder, sellStartOrder.takeProfitTarget + 0.0001);
                        }
                    }
                    if (tradeEngine.timeSeriesRepo.ask > sellStartOrder.stopLossTarget && sellByMoneyFlow) {        // a vesztes trade  áttöri a következő kijelölt target sl szintet
                        sellStartOrder.phase++;
//                        if (sellStartOrder.phase > 2 && !sellByMoneyFlow) return;
                        sellStartOrder.stopLossTarget = sellStartOrder.stopLossTarget + murrayRangeInPipForCorrection;
//                        sellStartOrder.stopLoss=sellStartOrder.openPrice + 0.003;
                        tradeEngine.setStopLoss(sellStartOrder,sellStartOrder.openPrice + 0.003);
                        Order correctionOrder = Order.sell(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
//                        correctionOrder.stopLoss = sellStartOrder.stopLoss;
//                        correctionOrder.stopLoss = tradeEngine.timeSeriesRepo.ask + 0.0002;
                        tradeEngine.setStopLoss(correctionOrder,tradeEngine.timeSeriesRepo.ask + 0.0002);
                        correctionOrder.parentId = sellStartOrder.id;
                        sellStartOrder.childOrders.add(correctionOrder);
                        tradeEngine.onTradeEvent(correctionOrder);
                        sellStartOrder.takeProfitTarget = getBreakLevel(sellStartOrder.type);
//                        if ( sellStartOrder.phase>3) System.out.println(time+"    "+sellStartOrder.phase + ". correctionLevel: " +"  loss: "+sellStartOrder.getCurrentProfit(tradeEngine.timeSeriesRepo.ask)+"  ttp: "+getBreakLevel(sellStartOrder.type));
                    }
                }
            }


        }


    }

    @Override
    public void onExitEvent(Order order) {
        order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        sellByMoneyFlow = false;
        buyByMoneyFlow = false;

        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();

        if (tradeEngine.period == timeFrame) {
            if (highestLaguerre.getValue(tradeEngine.currentBarIndex).doubleValue() > 0.90 || highestMoneyFlow.getValue(tradeEngine.currentBarIndex).doubleValue() > 90.0) {
                sellByMoneyFlow = true;
                double buyProfit = 0.0;
                for (Order order : tradeEngine.openedOrders) {
                    if (order.type == Order.Type.BUY)
                        buyProfit += order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid);
                }
                if (buyProfit > 0.0) {
                    for (Order order : tradeEngine.openedOrders) {
                        if (order.type == Order.Type.BUY) {
                            order.closedAmount = order.openedAmount;
                            order.closePrice = tradeEngine.timeSeriesRepo.bid;
                            order.exitType = EXITRULE;
                            tradeEngine.closeOrder(order);
                        }
                    }
                }
            }
            if (lowestLaguerre.getValue(tradeEngine.currentBarIndex).doubleValue() < 0.1 || lowestMoneyFlow.getValue(tradeEngine.currentBarIndex).doubleValue() < 10.0) {
                buyByMoneyFlow = true;
                double sellProfit = 0.0;
                for (Order order : tradeEngine.openedOrders) {
                    if (order.type == Order.Type.SELL)
                        sellProfit += order.getCurrentProfit(tradeEngine.timeSeriesRepo.ask);
                }
                if (sellProfit > 0.0) {
                    for (Order order : tradeEngine.openedOrders) {
                        if (order.type == Order.Type.SELL) {
                            order.closedAmount = order.openedAmount;
                            order.closePrice = tradeEngine.timeSeriesRepo.ask;
                            order.exitType = EXITRULE;
                            tradeEngine.closeOrder(order);
                        }
                    }
                }
            }

        }
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

    double getBreakLevel(Order.Type type) {
        int breakLevelIndex;
        double targetPrice, priceVolume = 0.0, volume = 0.0;
        for (Order order : tradeEngine.openedOrders) {
            if (order.type != type) continue;
            priceVolume += order.openPrice * order.openedAmount;
            volume += order.openedAmount;
        }

        targetPrice = priceVolume / volume;

        if (type == Order.Type.BUY) {
            targetPrice = targetPrice + 0.00007;
//            breakLevelIndex = getMurrayRange(targetPrice);
//            if (breakLevelIndex < 0 || breakLevelIndex == 12) breakLevelIndex = 12;
//            else breakLevelIndex++;
//            targetPrice = murrayLevels[breakLevelIndex] - 0.00007;;
        } else {
            targetPrice = targetPrice - 0.00007;
//            breakLevelIndex = getMurrayRange(targetPrice);
//            if (breakLevelIndex < 0) breakLevelIndex = 0;
//            targetPrice = murrayLevels[breakLevelIndex] + 0.00007;
        }


        return targetPrice;
    }


}
//        LinkedList<Double> takeProfitLevels=new LinkedList<>();
//        LinkedList<Double> correctionLevels=new LinkedList<>();
//        int firstCorrectionLevelIndex,firstTakeProfitLevelIndex;
//        double firstCorrectionLevel;
//        if (order.type == Order.Type.BUY) {
//
//                order.stopLoss = order.openPrice - 0.002;
//
//                if (order.phase==0) {
//                    order.takeProfit = order.openPrice + 0.002;
//                    firstCorrectionLevelIndex = getMurrayRange(order.openPrice - 0.0003812);
//                    if (firstCorrectionLevelIndex < -1) firstCorrectionLevelIndex = 0;
//
//                    for (int i = 0; i < 4; i++) {
//                        correctionLevels.add(i, murrayLevels[firstCorrectionLevelIndex] - i * 0.0003812);
//                    }
//                }
//
//                firstTakeProfitLevelIndex = getMurrayRange(order.openPrice + 0.0001);
//                if (firstTakeProfitLevelIndex < -1 || firstTakeProfitLevelIndex == 12) firstTakeProfitLevelIndex = 12;
//                else firstTakeProfitLevelIndex++;
//                for (int i = 0; i < 8; i++) {
//                    takeProfitLevels.add(i, murrayLevels[firstTakeProfitLevelIndex] + i * 0.0003812 - 0.00007);
//                }
//
//
//        } else {
//            if (order.phase==0) {
//                order.stopLoss = order.openPrice + 0.002;
//                if (order.phase==0) {
//                    order.takeProfit = order.openPrice - 0.002;
//
//                    firstCorrectionLevelIndex = getMurrayRange(order.openPrice + 0.0003812);
//                    if (firstCorrectionLevelIndex < -1 || firstCorrectionLevelIndex == 12)
//                        firstCorrectionLevelIndex = 12;
//                    else firstCorrectionLevelIndex++;
//
//
//                    for (int i = 0; i < 4; i++) {
//                        correctionLevels.add(i, murrayLevels[firstCorrectionLevelIndex] + i * 0.0003812);
//                    }
//                }
//
//                firstTakeProfitLevelIndex = getMurrayRange(order.openPrice - 0.0001);
//                if (firstTakeProfitLevelIndex < -1) firstTakeProfitLevelIndex = 0;
//                for (int i = 0; i < 8; i++) {
//                    takeProfitLevels.add(i, murrayLevels[firstTakeProfitLevelIndex] - i * 0.0003812 + 0.00007);
//                }
//            }
//        }
//        if (order.parentId==0) {
//            order.hashMapParameters.put("TP", takeProfitLevels);
//            order.hashMapParameters.put("CL", correctionLevels);
//        } else {
//            for (Order parentOrder: tradeEngine.openedOrders) {
//                if (parentOrder.id==order.parentId) {
//                    parentOrder.hashMapParameters.put("TP", takeProfitLevels);
//                    break;
//                }
//            }
//
//        }