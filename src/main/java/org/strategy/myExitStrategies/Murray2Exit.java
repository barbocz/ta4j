package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;

import java.util.ArrayList;
import java.util.List;

import static org.strategy.Order.ExitType.EXITRULE;


public class Murray2Exit extends Strategy {

    List<Order> ordersToClose = new ArrayList<>();
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    Double murrayLevels[] = new Double[13];
    double murrayHeight;



    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 256, i);


    }


    public void onTradeEvent(Order order) {



            if (order.type == Order.Type.BUY) {
//                tradeEngine.setTakeProfit(order,order.openPrice+murrayHeight-murrayHeight*0.125);
                tradeEngine.setStopLoss(order,order.openPrice-murrayHeight*3);
            } else {
//                tradeEngine.setTakeProfit(order,order.openPrice-murrayHeight+murrayHeight*0.125);
                tradeEngine.setStopLoss(order,order.openPrice+murrayHeight*3);
            }


    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);
//        for (Order order : tradeEngine.openedOrders) {
//            if (order.type == Order.Type.BUY) {
//                if (tradeEngine.timeSeriesRepo.ask>order.takeProfitTarget) {
//                    tradeEngine.setExitPrice(order,order.openPrice+0.0001);
//                    order.takeProfitTarget=Double.MAX_VALUE;
//                }
//            } else {
//                if (tradeEngine.timeSeriesRepo.bid<order.takeProfitTarget) {
//                    tradeEngine.setExitPrice(order,order.openPrice-0.0001);
//                    order.takeProfitTarget=0.0;
//                }
//            }
//        }


    }

    @Override
    public void onBeforeCloseOrder(Order order) {
//        if (order.closePhase == 0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid) > 0.0) {
//            order.closedAmount = order.openedAmount / 2.0;
//            order.closePhase = 1;
//
////            tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.STOPLOSS, false);
//        } else order.closedAmount = order.openedAmount;
        order.closedAmount = order.openedAmount;
    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        for (int m = 0; m < 13; m++)
            murrayLevels[m] = murrayMathIndicators[m].getValue(tradeEngine.currentBarIndex).doubleValue();
        murrayHeight=murrayLevels[1]-murrayLevels[0];

        if (tradeEngine.period == timeFrame) {
//            for (Order order : tradeEngine.openedOrders) {
//                int openIndex = tradeEngine.series.getIndex(order.openTime);
//                if (tradeEngine.series.getCurrentIndex() - openIndex > 7)
//                    tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
//            }
            for (Order order : tradeEngine.openedOrders) {
                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.currentBarIndex - openIndex > 54) tradeEngine.setExitPrice(order,order.openPrice);
            }



        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }

    double getMurrayLevel(double value,int bias) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (murrayLevels[i+bias]);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (murrayLevels[i+bias]);
            }
        }
        return -1;
    }


}