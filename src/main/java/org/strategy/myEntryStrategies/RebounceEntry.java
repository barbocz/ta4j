package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.HashMap;


public class RebounceEntry extends Strategy {
    final Double murrayRange = 76.24;
    MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
    Double murrayLevels[] = new Double[13];
    double buyUpperLimit = Double.MAX_VALUE, buyLowerLimit = 0.0, sellUpperLimit = Double.MAX_VALUE, sellLowerLimit = 0.0, lastTradedBuyLimit = 0.0, lastTradedSellLimit = 0.0;
    MoneyFlowIndicator moneyFlowIndicator, moneyFlowIndicatorSlower;
    ClosePriceIndicator closePriceIndicator;
    double signalLevel = 0; // 0: Hold, 3: Strong sell, -3 Strong buy
    HashMap<Double, Boolean> buyEntryLevels = new HashMap<>();
    HashMap<Double, Boolean> sellEntryLevels = new HashMap<>();
    int buyCounter = 0, sellCounter = 0;
    double murrayHeight;


    int lastTradeIndex = 0;

    LaguerreIndicator laguerreIndicator;
    //    Rule orderConditionRule;
    int m1CurrentBarIndex = 0;

    public void init() {

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 3);
        moneyFlowIndicatorSlower = new MoneyFlowIndicator(tradeEngine.series, 8);


        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.13);
        closePriceIndicator = new ClosePriceIndicator(tradeEngine.series);


        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

//        zoloIndicatorUp.subWindowIndex = 4;
//        zoloIndicatorUp.indicatorColor = Color.GREEN;
//        tradeEngine.log(zoloIndicatorUp);
//
//        zoloIndicatorDown.subWindowIndex = 4;
//        zoloIndicatorDown.indicatorColor = Color.RED;
//        tradeEngine.log(zoloIndicatorDown);


        laguerreIndicator.subWindowIndex = 5;
        tradeEngine.log(laguerreIndicator);


        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


        moneyFlowIndicatorSlower.subWindowIndex = 4;
        moneyFlowIndicatorSlower.indicatorColor = Color.RED;
        tradeEngine.log(moneyFlowIndicatorSlower);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {

        if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit || tradeEngine.timeSeriesRepo.ask < sellLowerLimit) {
            if (sellCounter == 0 && tradeEngine.timeSeriesRepo.bid >= murrayLevels[12]) return;
            double sellLimit;
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit) sellLimit = sellUpperLimit;
                else sellLimit = sellLowerLimit;
            } else sellLimit = tradeEngine.timeSeriesRepo.ask;

            tradeEngine.onTradeEvent(Order.sell(orderAmount, sellLimit, tradeEngine.series.getCurrentTime()));

            if (sellCounter == 0) {
                sellEntryLevels.clear();
                int currentMurrayLevel = getClosestMurrayLevel(sellLimit);
//                if (currentMurrayLevel<0) {
//                    System.out.println("");
//                }
                for (int i = 1; i < 4; i++)
                    sellEntryLevels.put(murrayLevels[currentMurrayLevel] + i * murrayHeight, true);

//                if (tradeEngine.currentBarIndex>14440) for(Double v: sellEntryLevels.keySet()) System.out.println(tradeEngine.series.getCurrentTime()+" - "+ v);
            } else {
                if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit) sellLimit = sellUpperLimit;
                else sellLimit = sellLowerLimit;
                sellEntryLevels.put(sellLimit,false);
            }

            lastTradedSellLimit = sellLimit;
            sellUpperLimit = Double.MAX_VALUE;
            sellLowerLimit = 0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }
        if (tradeEngine.timeSeriesRepo.bid > buyUpperLimit || tradeEngine.timeSeriesRepo.bid < buyLowerLimit) {
            if (buyCounter == 0 && tradeEngine.timeSeriesRepo.bid <= murrayLevels[0]) return;
            double buyLimit;
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.bid > buyUpperLimit) buyLimit = buyUpperLimit;
                else buyLimit = buyLowerLimit;
            } else buyLimit = tradeEngine.timeSeriesRepo.bid;

            tradeEngine.onTradeEvent(Order.buy(orderAmount, buyLimit, tradeEngine.series.getCurrentTime()));

            if (buyCounter == 0) {
                buyEntryLevels.clear();
                int currentMurrayLevel = getClosestMurrayLevel(buyLimit);
                for (int i = 1; i < 4; i++)
                    buyEntryLevels.put(murrayLevels[currentMurrayLevel] - i * murrayHeight, true);

//                if (tradeEngine.currentBarIndex>14440) for(Double v: buyEntryLevels.keySet()) System.out.println(tradeEngine.series.getCurrentTime()+" - "+ v);
            } else {
                if (tradeEngine.timeSeriesRepo.bid > buyUpperLimit) buyLimit = buyUpperLimit;
                else buyLimit = buyLowerLimit;
                buyEntryLevels.put(buyLimit,false);
            }

            lastTradedBuyLimit = buyLimit;
            buyUpperLimit = Double.MAX_VALUE;
            buyLowerLimit = 0.0;
            lastTradeIndex = tradeEngine.currentBarIndex;
        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        ZonedDateTime time = tradeEngine.series.getCurrentTime();
        signalLevel = 0.0;

        if (time.getHour() > 14 && time.getHour() < 17) return;
//        if (time.getHour()==15 || (time.getHour()==16 && time.getMinute()<31)) return;
        if (time.getHour() > 22 || (time.getHour() == 0 && time.getMinute() < 30)) return;

        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() == 0.0) signalLevel--;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue() < 15.0) signalLevel--;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() < 0.1) signalLevel--;

        if (moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() == 100.0) signalLevel++;
        if (moneyFlowIndicatorSlower.getValue(tradeEngine.currentBarIndex).doubleValue() > 85.0) signalLevel++;
        if (laguerreIndicator.getValue(tradeEngine.currentBarIndex).doubleValue() > 0.9) signalLevel++;



        for (int i = 0; i < 13; i++)
            murrayLevels[i] = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
        murrayHeight = murrayLevels[1] - murrayLevels[0];
        if (murrayLevels[2] != murrayMathIndicators[2].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 34) {
            lastTradedBuyLimit = 0.0;
        }
        if (murrayLevels[10] != murrayMathIndicators[10].getValue(tradeEngine.currentBarIndex - 1).doubleValue() && tradeEngine.currentBarIndex - lastTradeIndex > 34) {
            lastTradedSellLimit = 0.0;
        }

        buyCounter = 0;
        sellCounter = 0;
        for (Order order : tradeEngine.openedOrders) {
            if (order.type == Order.Type.BUY) buyCounter++;
            else sellCounter++;
        }

        double closePrice = closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue();

        if (buyCounter == 0) {
            if (signalLevel > -1 || murrayLevels[2] == lastTradedBuyLimit) {
                buyUpperLimit = Double.MAX_VALUE;
                buyLowerLimit = 0.0;
            } else {
                if (closePrice >= murrayLevels[2]) buyLowerLimit = murrayLevels[2];
                else if (closePrice < murrayLevels[2] && closePrice >= murrayLevels[1]) {
                    buyUpperLimit = murrayLevels[2];
                    buyLowerLimit = murrayLevels[1];
                } else if (closePrice < murrayLevels[1]) {
                    buyUpperLimit = murrayLevels[1];
                    buyLowerLimit = murrayLevels[0];
                }
            }
        } else {
            buyUpperLimit = Double.MAX_VALUE;
            buyLowerLimit = 0.0;
            if (signalLevel < 0) {
                for (Double buyEntryLevel : buyEntryLevels.keySet()) {
                    if (buyEntryLevels.get(buyEntryLevel) && closePrice <= buyEntryLevel && buyEntryLevel < buyUpperLimit) buyUpperLimit = buyEntryLevel;
                    if (buyEntryLevels.get(buyEntryLevel) && closePrice > buyEntryLevel && buyEntryLevel > buyLowerLimit) buyLowerLimit = buyEntryLevel;

                }
            }
        }

        if (sellCounter == 0) {
            if (signalLevel < 1 || murrayLevels[10] == lastTradedSellLimit) {
                sellUpperLimit = Double.MAX_VALUE;
                sellLowerLimit = 0.0;
            } else {
                if (closePrice <= murrayLevels[10]) sellUpperLimit = murrayLevels[10];
                else if (closePrice > murrayLevels[10] && closePrice <= murrayLevels[11]) {
                    sellUpperLimit = murrayLevels[11];
                    sellLowerLimit = murrayLevels[10];
                } else if (closePrice > murrayLevels[11]) {
                    sellUpperLimit = murrayLevels[12];
                    sellLowerLimit = murrayLevels[11];
                }
            }
        } else {
            sellUpperLimit = Double.MAX_VALUE;
            sellLowerLimit = 0.0;
            if (signalLevel > 0) {
                for (Double sellEntryLevel : sellEntryLevels.keySet()) {
                    if (sellEntryLevels.get(sellEntryLevel) && closePrice <= sellEntryLevel && sellEntryLevel < sellUpperLimit) sellUpperLimit = sellEntryLevel;
                    if (sellEntryLevels.get(sellEntryLevel) && closePrice > sellEntryLevel && sellEntryLevel > sellLowerLimit) sellLowerLimit = sellEntryLevel;

                }
            }

        }


//
//
//
//        if (murrayLevels[2] != lastTradedBuyLimit && moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==0.0
//                && closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>murrayLevels[2] ) buyLimit = murrayLevels[2];
//        else buyLimit=0.0;
//
//        if (murrayLevels[10] != lastTradedSellLimit && moneyFlowIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()==100.0
//                && closePriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<murrayLevels[10] ) sellLimit = murrayLevels[10];
//        else sellLimit=Double.MAX_VALUE;

    }


    public void onOneMinuteDataEvent() {
//        m1CurrentBarIndex = tradeEngine.timeSeriesRepo.getIndex(tradeEngine.series.getCurrentTime(), 1);
    }

    int getClosestMurrayLevel(double currentPrice) {

        int murrayLevel = getMurrayRange(currentPrice);
        if (murrayLevel < 0) return -1;
        if (Math.abs(currentPrice - murrayLevels[murrayLevel]) <= Math.abs(currentPrice - murrayLevels[murrayLevel + 1]))
            return murrayLevel;
        else return murrayLevel + 1;

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


}