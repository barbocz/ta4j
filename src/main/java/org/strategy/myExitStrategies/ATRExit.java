package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.CCIIndicator;


public class ATRExit extends Strategy  {


    ATRIndicator atrIndicator;



    public void init() {
        atrIndicator = new ATRIndicator(tradeEngine.series, 34);
    }


    public void onTradeEvent(Order order) {


        if (order.type == Order.Type.BUY) {
            order.takeProfit = order.openPrice +  2.6 *atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            order.stopLoss = order.openPrice -  2.6 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        } else {
            order.takeProfit = order.openPrice -  2.6 *atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
            order.stopLoss = order.openPrice +  2.6 * atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();
        }
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