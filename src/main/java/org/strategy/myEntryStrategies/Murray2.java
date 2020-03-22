package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Arrays;


public class Murray2 extends Strategy {
    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    Double murrayLevels[] = new Double[13];
    Double prevMurrayLevels[] = new Double[13];
    LowPriceIndicator lowPriceIndicator;
    HighPriceIndicator highPriceIndicator;
    int lastBuyIndex=0,lastSellIndex=0;
    double sellUpperLimit=Double.MAX_VALUE,sellLowerLimit=0.0;
    double buyUpperLimit=Double.MAX_VALUE,buyLowerLimit=0.0;
    ClosePriceIndicator closePrice;
    MoneyFlowIndicator moneyFlowIndicator;
    double murrayHeight=0.0;

    boolean buyOk=false,sellOk=false;

    public void init() {

        closePrice = new ClosePriceIndicator(tradeEngine.series);
        lowPriceIndicator=new LowPriceIndicator(tradeEngine.series);
        highPriceIndicator=new HighPriceIndicator(tradeEngine.series);

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 256, i);

        Arrays.fill(murrayLevels, 0.0);
        Arrays.fill(prevMurrayLevels, 0.0);

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 9) murrayMathIndicators[i].indicatorColor = Color.YELLOW;
            if (i == 8) murrayMathIndicators[i].indicatorColor = Color.RED;
            if (i == 6) murrayMathIndicators[i].indicatorColor = Color.BLUE;
            if (i == 4) murrayMathIndicators[i].indicatorColor = Color.RED;
            if (i == 3) murrayMathIndicators[i].indicatorColor = Color.YELLOW;
            tradeEngine.log(murrayMathIndicators[i]);
        }



        MoneyFlowIndicator moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        moneyFlowIndicator.subWindowIndex = 5;
        tradeEngine.log(moneyFlowIndicator);

        ruleForBuy=new UnderIndicatorRule(moneyFlowIndicator,1.0,2);
        ruleForSell=new OverIndicatorRule(moneyFlowIndicator,99.0,2);

//        emaIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(emaIndicator);
//
//
//        longCci.subWindowIndex = 4;
//        tradeEngine.log(longCci);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
        double openPrice;
        if ((tradeEngine.timeSeriesRepo.ask > sellUpperLimit || tradeEngine.timeSeriesRepo.ask<sellLowerLimit)) {
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.ask > sellUpperLimit) openPrice=sellUpperLimit; else openPrice=sellLowerLimit;
            } else openPrice=tradeEngine.timeSeriesRepo.ask;

            Order order=Order.sell(orderAmount, openPrice, tradeEngine.series.getCurrentTime());
            tradeEngine.onTradeEvent(order);

            if (murrayHeight>0.0003)  tradeEngine.setTakeProfit(order,openPrice-murrayHeight+murrayHeight*0.1);
            else tradeEngine.setTakeProfit(order,openPrice-2*murrayHeight+murrayHeight*0.1);
            order.takeProfitTarget=openPrice-murrayHeight+murrayHeight*0.1;


            lastSellIndex=Integer.MAX_VALUE;
            sellUpperLimit=Double.MAX_VALUE;
            sellLowerLimit=0.0;
        }
        if ((tradeEngine.timeSeriesRepo.bid > buyUpperLimit || tradeEngine.timeSeriesRepo.bid<buyLowerLimit)) {
            if (tradeEngine.backtestMode) {
                if (tradeEngine.timeSeriesRepo.bid > buyUpperLimit) openPrice=buyUpperLimit; else openPrice=buyLowerLimit;
            } else openPrice=tradeEngine.timeSeriesRepo.bid;

            Order order=Order.buy(orderAmount, openPrice, tradeEngine.series.getCurrentTime());
            tradeEngine.onTradeEvent(order);
            if (murrayHeight>0.0003) tradeEngine.setTakeProfit(order,openPrice+murrayHeight-murrayHeight*0.1);
            else tradeEngine.setTakeProfit(order,openPrice+2*murrayHeight-murrayHeight*0.1);
            order.takeProfitTarget=openPrice+murrayHeight-murrayHeight*0.1;


            lastBuyIndex=Integer.MAX_VALUE;
            buyUpperLimit=Double.MAX_VALUE;
            buyLowerLimit=0.0;
        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        ZonedDateTime time = tradeEngine.series.getCurrentTime();
        int currentMurrayLevel;

        for (int m = 0; m < 13; m++)
            murrayLevels[m] = murrayMathIndicators[m].getValue(tradeEngine.currentBarIndex).doubleValue();



        buyOk=ruleForBuy.isSatisfied(time);
        sellOk=ruleForSell.isSatisfied(time);

        if (tradeEngine.currentBarIndex-lastSellIndex>6) {
            lastSellIndex=Integer.MAX_VALUE;
            sellUpperLimit=Double.MAX_VALUE;
            sellLowerLimit=0.0;
        }

        if (tradeEngine.currentBarIndex-lastBuyIndex>6) {
            lastBuyIndex=Integer.MAX_VALUE;
            buyUpperLimit=Double.MAX_VALUE;
            buyLowerLimit=0.0;
        }

        murrayHeight=murrayLevels[1]-murrayLevels[0];



        if (!murrayLevels[8].equals(prevMurrayLevels[8]) && murrayLevels[8].equals(prevMurrayLevels[12]) && highPriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>murrayLevels[8]
                && highPriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<murrayLevels[9]) {

            lastSellIndex=tradeEngine.currentBarIndex;
            currentMurrayLevel=getMurrayLevel(closePrice.getValue(tradeEngine.currentBarIndex).doubleValue());
//            sellUpperLimit=murrayLevels[currentMurrayLevel+1];
            sellLowerLimit=murrayLevels[currentMurrayLevel];
//            if (currentMurrayLevel<8) {
//                sellLowerLimit=0.0;
//                sellUpperLimit=Double.MAX_VALUE;
//            }

//            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, time));
        }
            if (tradeEngine.currentBarIndex>24311) {
                System.out.println();
            }


        if (!murrayLevels[4].equals(prevMurrayLevels[4]) && murrayLevels[4].equals(prevMurrayLevels[12]) && lowPriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()<murrayLevels[4] &&
                lowPriceIndicator.getValue(tradeEngine.currentBarIndex).doubleValue()>murrayLevels[3]) {
//            murrayHeight=Math.min(murrayLevels[1]-murrayLevels[0],prevMurrayLevels[1]-prevMurrayLevels[0]);
            lastBuyIndex=tradeEngine.currentBarIndex;
            currentMurrayLevel=getMurrayLevel(closePrice.getValue(tradeEngine.currentBarIndex).doubleValue());
            buyUpperLimit=murrayLevels[currentMurrayLevel+1];

//            buyLowerLimit=murrayLevels[currentMurrayLevel];
//            if (currentMurrayLevel>4) {
//                buyUpperLimit=Double.MAX_VALUE;
//                buyLowerLimit=0.0;
//            }
//            tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, time));
        }

        prevMurrayLevels = Arrays.copyOf(murrayLevels, 13);


    }


    public void onOneMinuteDataEvent() {

    }

    int getMurrayLevel(double value) {
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