package org.strategy.myExitStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TradeEngine;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;


public class ThreePhaseExit extends Strategy {

    BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator;
    HighestValueIndicator highestValueIndicator;
    LowestValueIndicator lowestValueIndicator;
    ATRIndicator atrIndicator;

    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        SMAIndicator smaIndicator = new SMAIndicator(closePrice, 20);
        bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
        StandardDeviationIndicator standardDeviationIndicator = new StandardDeviationIndicator(closePrice, 20);
        bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator, 1.6);
        bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, standardDeviationIndicator, 1.6);
        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(tradeEngine.series);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 8);
        atrIndicator = new ATRIndicator(tradeEngine.series, 64);

        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(tradeEngine.series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 8);

    }


    public void onTradeEvent(Order order) {
        double atrCorrection=atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()*3.0;
        if (order.type == Order.Type.BUY) {
            tradeEngine.setExitPrice(order, bollingerBandsLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()-atrCorrection, TradeEngine.ExitMode.STOPLOSS, true);

        } else {
            tradeEngine.setExitPrice(order, bollingerBandsUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()+atrCorrection, TradeEngine.ExitMode.STOPLOSS, true);
        }


    }


    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+tradeEngine.timeSeriesRepo.bid);


    }

    @Override
    public void onBeforeCloseOrder(Order order) {
   order.closedAmount = order.openedAmount;

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.period == timeFrame) {
            double atrCorrection=atrIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()*0.1;
            for (Order order : tradeEngine.openedOrders) {

                    if (order.type == Order.Type.BUY) {
                        tradeEngine.setExitPrice(order, bollingerBandsUpperIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    } else {
                        tradeEngine.setExitPrice(order, bollingerBandsLowerIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                    }

//                if (order.closePhase ==0) tradeEngine.setExitPrice(order, bollingerBandsMiddleIndicator.getValue(tradeEngine.currentBarIndex).doubleValue(), TradeEngine.ExitMode.TAKEPROFIT, true);
                int openIndex = tradeEngine.series.getIndex(order.openTime);
                if (tradeEngine.series.getCurrentIndex() - openIndex > 6)
                {
                    if (order.type == Order.Type.BUY) atrCorrection = -1 * atrCorrection;
                    tradeEngine.setExitPrice(order, order.openPrice+atrCorrection, TradeEngine.ExitMode.ANY, true);
                }
            }
        }
    }


    public void onOneMinuteDataEvent() {
//        System.out.println(symbol+" "+series.getCurrentTime());

    }


}