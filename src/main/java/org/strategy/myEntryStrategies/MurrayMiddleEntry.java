package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.KAMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.InSlopeRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class MurrayMiddleEntry extends Strategy {

    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    boolean buyOk = false, sellOk = false;


    public void init() {

        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathIndicator(tradeEngine.series, 256, i);
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
        ADXIndicator adxIndicator = new ADXIndicator(tradeEngine.series, 5);
        KAMAIndicator kamaIndicator = new KAMAIndicator(closePrice);

        Num maxSlopeForSell = tradeEngine.series.numOf(-0.00003);
        Num minSlopeForBuy = tradeEngine.series.numOf(0.00003);

        Num minSlopeForAdx = tradeEngine.series.numOf(4.0);
        ruleForSell = new InSlopeRule(kamaIndicator, 1, maxSlopeForSell);
        ruleForSell = ruleForSell.and(new InSlopeRule(adxIndicator, minSlopeForAdx));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(adxIndicator, 45.0));

        ruleForBuy = new InSlopeRule(kamaIndicator, minSlopeForBuy);
        ruleForBuy = ruleForBuy.and(new InSlopeRule(adxIndicator, minSlopeForAdx));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(adxIndicator, 45.0));

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        kamaIndicator.indicatorColor = Color.ORANGE;
        tradeEngine.log(kamaIndicator);

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 2.6, 54);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 2.6, 54);


        kcU.indicatorColor=Color.WHITE;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);
//
        adxIndicator.subWindowIndex = 4;
        tradeEngine.log(adxIndicator);


    }


    public void onTradeEvent(Order order) {


    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
        double currentPrice=0.0,lowerMurray,upperMurray,murrayHeight;
        if (buyOk || sellOk) {

            if (buyOk) currentPrice = tradeEngine.timeSeriesRepo.ask;
            else  currentPrice = tradeEngine.timeSeriesRepo.bid;

            for (int i = 0; i < 12; i++) {
                if (currentPrice >= murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue() && currentPrice <= murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue()) {
                    lowerMurray = murrayMathIndicators[i].getValue(tradeEngine.currentBarIndex).doubleValue();
                    upperMurray = murrayMathIndicators[i + 1].getValue(tradeEngine.currentBarIndex).doubleValue();
                    murrayHeight = upperMurray - lowerMurray;

                    if (buyOk && currentPrice >= lowerMurray + (murrayHeight * 0.5) && currentPrice <= lowerMurray + (murrayHeight * 0.65)) {
                        Order order = Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
                        order.takeProfit = upperMurray;
                        order.stopLoss = lowerMurray - murrayHeight * 0.25;
                        buyOk = false;
                        tradeEngine.onTradeEvent(order);
                    } else if (sellOk && currentPrice <= lowerMurray + (murrayHeight * 0.5) && currentPrice >= lowerMurray + (murrayHeight * 0.35)) {
                        Order order = Order.sell(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
                        order.takeProfit = lowerMurray;
                        order.stopLoss = upperMurray + murrayHeight * 0.25;
                        sellOk = false;
                        tradeEngine.onTradeEvent(order);
                    }
                    break;
                }
            }

        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
//        System.out.println("onBarChangeEvent------------- "+timeFrame);
//        try {
//            TimeUnit.SECONDS.sleep(timeFrame);
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("onBarChangeEvent------------- "+eventSeries.timeFrame+" cp: "+slowerClosePrice.getValue(timeSeriesRepo.getTimeSeries(3).getEndIndex()-1));
//        System.out.println("onBarChangeEvent rule ------------- "+eventSeries.timeFrame+" cp: "+ruleForSell.isSatisfied(series.getEndIndex()-1));
//        if (!strategy.ruleForSell.isSatisfied(eventSeries.getEndIndex())) System.out.println("NOT SELL");;
//    if (series.getEndIndex()-1==990) {
//        if (ruleForSell.isSatisfied(series.getEndTime()))
//            System.out.println("Sell Entry: " + (series.getEndIndex() - 1)+"    "+eventSeries.timeFrame);
//    }
//        System.out.println(series.getEndIndex()-1);

//        int i = series.getEndIndex();
//        System.out.println("------------ "+i);

//        System.out.println(timeFrame+" --------------------  "+series.getIndex(time) + ": " + time);
        buyOk=false;
        sellOk=false;
        if (tradeEngine.timeFrame == timeFrame) {
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();
            if (tradeEngine.currentBarIndex<256) return;
            ZonedDateTime time = tradeEngine.series.getCurrentTime();


            if (ruleForSell.isSatisfied(time)) {

                sellOk = true;
//                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            } else if (ruleForBuy.isSatisfied(time)) {
                buyOk = true;
//                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }
//
//
        }


        }


        public void onOneMinuteDataEvent () {

        }


    }