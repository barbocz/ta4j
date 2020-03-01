package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.trading.rules.NotRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import org.ta4j.core.trading.rules.OrderConditionRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class KeltnerEntryWithCCI extends Strategy {



    public KeltnerEntryWithCCI(Integer timeFrame, TimeSeriesRepo timeSeriesRepo) {




//        setLogOn();  kukucs

    }

    public void init(){




        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);

        MurrayMathIndicator murrayMathIndicator0= new MurrayMathIndicator(tradeEngine.series,256,0);
        MurrayMathIndicator murrayMathIndicator12= new MurrayMathIndicator(tradeEngine.series,256,12);
        MurrayMathIndicator murrayMathIndicator6= new MurrayMathIndicator(tradeEngine.series,256,6);

        ClosePriceIndicator closePriceD = new ClosePriceIndicator(tradeEngine.getTimeSeries(1440));

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 34);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 3.4, 34);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 3.4, 34);

        KeltnerChannelMiddleIndicator kcM8 = new KeltnerChannelMiddleIndicator(tradeEngine.getTimeSeries(8), 34);
        KeltnerChannelUpperIndicator kcU8 = new KeltnerChannelUpperIndicator(kcM8, 3.4, 34);
        KeltnerChannelLowerIndicator kcL8 = new KeltnerChannelLowerIndicator(kcM8, 3.4, 34);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 8);

//        TradeCounterIndicator tradeCounterIndicator=new TradeCounterIndicator(tradeEngine);
        CCIIndicator cciIndicator = new CCIIndicator(tradeEngine.series, 30);
        CCIIndicator cciIndicator8 = new CCIIndicator(tradeEngine.getTimeSeries(13), 30);

//        SMAIndicator shortSma = new SMAIndicator(closePrice, 2);
//        SMAIndicator longSma = new SMAIndicator(closePrice, 8);



        ruleForSell = new OverIndicatorRule(closePrice, kcU, 8);
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(longCci, minus100));

        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU8, 3));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.4, 8));
        ruleForSell = ruleForSell.and(new NotRule(new OrderConditionRule(tradeEngine)));
        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
                and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));




        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 8);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(longCci, plus100));

        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL8, 3));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.4, 8));
        ruleForBuy = ruleForBuy.and(new NotRule(new OrderConditionRule(tradeEngine)));
        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
                and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        // log:
        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);


        kcU.indicatorColor=Color.WHITE;
        tradeEngine.log(kcU);

        kcU8.indicatorColor=Color.GRAY;
        tradeEngine.log(kcU8);

        cciIndicator.subWindowIndex=3;
        tradeEngine.log(cciIndicator);

        cciIndicator8.subWindowIndex=3;
        cciIndicator8.indicatorColor=Color.RED;
        tradeEngine.log(cciIndicator8);

//        testIndicator1.subWindowIndex=3;
//        testIndicator1.indicatorColor=Color.RED;
//        tradeEngine.indicatorsForLog.add(testIndicator1);

//        chaikinIndicator.subWindowIndex=3;
//        tradeEngine.indicatorsForLog.add(chaikinIndicator);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);

        kcL8.indicatorColor=Color.GRAY;
        tradeEngine.log(kcL8);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        tradeEngine.log(murrayMathIndicator0);
        tradeEngine.log(murrayMathIndicator12);



    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() {
//        System.out.println("onTickEvent------------- "+series.getBid());

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
        if (tradeEngine.period ==timeFrame) {
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            ZonedDateTime time=tradeEngine.series.getCurrentTime();

//            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
//            ZonedDateTime zdt= ZonedDateTime.parse("2019.08.02 10:54", dateFormatter.withZone(ZoneId.systemDefault()));
//            if (tradeEngine.series.getCurrentTime().isAfter(zdt)) {
//                System.out.println("BREAK");
//            }

//            if (tradeEngine.series.getCurrentIndex()==62) {
//                System.out.println("HEREEEEEEEEEEE");
//            }
            if (ruleForSell.isSatisfied(time)) {
//                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {

//                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }
        }

//        for (Indicator indicator : indicatorsForLog) {
//            if (indicator.getTimeSeries().getPeriod() == timeFrame) {
//                TimeSeries indicatorSeries = indicator.getTimeSeries();
//                if (indicatorSeries.getEndIndex() > -1) {
//                    Num iValue = (Num) indicator.getValue(indicatorSeries.getEndIndex() - 1);
//                    logIndicator(indicator, indicatorSeries.getEndTime(), indicatorSeries.getEndIndex() - 1, iValue.doubleValue());
//                }
//            }
//        }

//    if (preIndex==series.getEndIndex()) System.out.println("HIBA------------------------");
//        preIndex=series.getEndIndex();

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