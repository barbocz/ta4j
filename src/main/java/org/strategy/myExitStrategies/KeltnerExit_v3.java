package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;


public class KeltnerExit_v3 extends Strategy {
    KeltnerChannelMiddleIndicator keltnerChannelMiddleIndicator;
    KeltnerChannelUpperIndicator keltnerChannelUpperIndicator;
    KeltnerChannelLowerIndicator keltnerChannelLowerIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;
    ATRIndicator atrIndicator;
    ParabolicSarIndicator parabolicSarIndicator;
    ClosePriceIndicator closePrice;

    public KeltnerExit_v3() {


        // log:
//        rulesForLog.add(ruleForSell);
//        rulesForLog.add(ruleForBuy);
//        indicatorsForLog.add(closePriceD);
//        indicatorsForLog.add(kcU);

//        indicatorsForLog.add(chaikinIndicator);
//        setLogOn();

    }

    public void init() {
         closePrice = new ClosePriceIndicator(tradeEngine.series);


        keltnerChannelMiddleIndicator = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        keltnerChannelUpperIndicator = new KeltnerChannelUpperIndicator(keltnerChannelMiddleIndicator, 3.0, 54);
        keltnerChannelLowerIndicator = new KeltnerChannelLowerIndicator(keltnerChannelMiddleIndicator, 3.0, 54);

        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 89);


        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 89);
        atrIndicator=new ATRIndicator(tradeEngine.series,16);


    }


    public void onTradeEvent(Order order) {

    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onExitEvent(Order order) {
         order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.timeFrame == timeFrame) {
            for (Order order : tradeEngine.openedOrders) {


                    if (order.type == Order.Type.BUY) {

                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, order.openPrice - 3.0* atrIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);

                    } else {
                        tradeEngine.setExitPrice(order, keltnerChannelLowerIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
//                        tradeEngine.setExitPrice(order, keltnerChannelUpperIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                        tradeEngine.setExitPrice(order, order.openPrice + 3.0* atrIndicator.getValue(tradeEngine.series.getPrevIndex()).doubleValue(), TradeEngine.ExitMode.STOPLOSS, true);
                    }

            }
        }

    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}