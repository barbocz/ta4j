package org.strategy.myExitStrategies;

import org.strategy.*;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;


public class KeltnerExit extends Strategy  {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;


    public KeltnerExit() {


    }

    public void init() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);


        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 34);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 3.4, 34);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 3.4, 34);

        HighPriceIndicator highPriceIndicator=new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator=new HighestValueIndicator(highPriceIndicator,89);


        LowPriceIndicator lowPriceIndicator=new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator=new LowestValueIndicator(lowPriceIndicator,89);



    }


    public void onTradeEvent(Order order) {

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order){
        if (order.closePhase==0 && order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid)>0.0) {
            order.closedAmount = order.openedAmount / 2.0;
            order.closePhase = 1;
            if (order.type == Order.Type.BUY) {
                tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            } else {
                tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, false);
            }
            tradeEngine.setExitPrice(order, order.openPrice , TradeEngine.ExitMode.STOPLOSS, false);
        } else order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

        if (tradeEngine.timeFrame == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {

                if (order.closePhase==0) {
                    if (order.type == Order.Type.BUY) {

                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, lowestValueIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue() , TradeEngine.ExitMode.STOPLOSS, true);

                    } else {
                        tradeEngine.setExitPrice(order, keltnerChannelMiddleIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, highestValueIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                    }

                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (tradeEngine.series.getCurrentIndex() - openIndex > 12)
                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);
                } else {
                    if (order.type == Order.Type.BUY) {
                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    } else {
                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    }

                    int openIndex = tradeEngine.series.getIndex(order.openTime);
                    if (tradeEngine.series.getCurrentIndex() - openIndex > 12)
                        tradeEngine.setExitPrice(order, order.openPrice, TradeEngine.ExitMode.ANY, true);

                }

            }
        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}