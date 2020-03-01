package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.*;


public class Dummy extends Strategy  {


    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
    }


    public void onTradeEvent(Order order) {

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order){
         order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.period == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {
//                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
////                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
//                        tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue() , TradeEngine.ExitMode.STOPLOSS, true);

            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}