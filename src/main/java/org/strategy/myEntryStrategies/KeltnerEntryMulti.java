package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.IsFallingRule;
import org.ta4j.core.trading.rules.IsRisingRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class KeltnerEntryMulti extends Strategy {

    boolean buyOk=false,sellOk=false;
    double buyLimit,sellLimit;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;



    public void init(){






        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);

//        MurrayMathIndicator murrayMathIndicator0= new MurrayMathIndicator(tradeEngine.series,256,0);
//        MurrayMathIndicator murrayMathIndicator12= new MurrayMathIndicator(tradeEngine.series,256,12);
//        MurrayMathIndicator murrayMathIndicator6= new MurrayMathIndicator(tradeEngine.series,256,6);


        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        kcU = new KeltnerChannelUpperIndicator(kcM, 2.6, 54);
        kcL = new KeltnerChannelLowerIndicator(kcM, 2.6, 54);

        KeltnerChannelMiddleIndicator kcMBig = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        KeltnerChannelUpperIndicator kcUBig = new KeltnerChannelUpperIndicator(kcMBig, 8, 54);
        KeltnerChannelLowerIndicator kcLBig = new KeltnerChannelLowerIndicator(kcMBig, 8, 54);

//        KeltnerChannelMiddleIndicator kcM8 = new KeltnerChannelMiddleIndicator(tradeEngine.getTimeSeries(60), 34);
//        KeltnerChannelUpperIndicator kcU8 = new KeltnerChannelUpperIndicator(kcM8, 3.4, 34);
//        KeltnerChannelLowerIndicator kcL8 = new KeltnerChannelLowerIndicator(kcM8, 3.4, 34);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 5);

//        TradeCounterIndicator tradeCounterIndicator=new TradeCounterIndicator(tradeEngine);
        CCIIndicator cciIndicator = new CCIIndicator(tradeEngine.series, 30);

//        SMAIndicator shortSma = new SMAIndicator(closePrice, 2);
//        SMAIndicator longSma = new SMAIndicator(closePrice, 8);

        CCIIndicator longCci = new CCIIndicator(tradeEngine.series, 34);
        Num cciUpperLimit = tradeEngine.series.numOf(250);
        Num cciLowerLimit = tradeEngine.series.numOf(-250);

        EMAIndicator emaIndicator=new EMAIndicator(closePrice,8);

        TimeSeries series1=tradeEngine.getTimeSeries(1);
        ClosePriceIndicator closePrice1 = new ClosePriceIndicator(series1);
        EMAIndicator longSma = new EMAIndicator(closePrice1, 13);

        TimeSeries series60=tradeEngine.getTimeSeries(30);
        KeltnerChannelMiddleIndicator kcM60 = new KeltnerChannelMiddleIndicator(series60, 54);
        KeltnerChannelUpperIndicator kcU60 = new KeltnerChannelUpperIndicator(kcM60, 3.0, 54);
        KeltnerChannelLowerIndicator kcL60 = new KeltnerChannelLowerIndicator(kcM60, 3.0, 54);
        ClosePriceIndicator closePrice60 = new ClosePriceIndicator(series60);
        LowPriceIndicator lowPriceIndicator60=new LowPriceIndicator(series60);
        HighPriceIndicator highPriceIndicator60=new HighPriceIndicator(series60);

        MoneyFlowIndicator moneyFlowIndicator60=new MoneyFlowIndicator(series60,5);
        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,5);

//        ruleForSell = ruleForSell.and(new OverIndicatorRule(highPriceIndicator60,kcU60,1));
        ruleForSell = new OverIndicatorRule(closePrice, kcU, 8);
        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator60, 98,2));
//        ruleForSell = ruleForSell.and(new IsFallingRule(moneyFlowIndicator60, 1, 1.0));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(kcU,closePrice));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 90,13));
        ruleForSell = ruleForSell.and(new IsFallingRule(emaIndicator, 1, 1.0));
        ruleForSell = ruleForSell.and(new IsFallingRule(moneyFlowIndicator, 1, 1.0));

//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(lowPriceIndicator60,kcL60,1));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(kcL60,closePrice60));
        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 8);
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(kcL,closePrice));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator60, 2,2));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(moneyFlowIndicator60, 1, 1.0));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 10,13));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcLBig));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.4, 6));
        ruleForBuy = ruleForBuy.and(new IsRisingRule(emaIndicator, 1, 1.0));
        ruleForBuy = ruleForBuy.and(new IsRisingRule(moneyFlowIndicator, 1, 1.0));


//        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,8);

//        Rule closePriceOverKeltnerUpperIn8=new OverIndicatorRule(closePrice, kcU, 8);
//        Rule closePriceOverKeltnerUpper8In3=new OverIndicatorRule(closePrice, kcU8, 3);
//        Rule closePriceOverKeltnerMiddle=new OverIndicatorRule(closePrice, kcM);
//        Rule chaikinOver0_4in8=new OverIndicatorRule(chaikinIndicator, 0.4, 8);
//        Rule noTradeOpen=new NotRule(new hasOpenOrder(tradeEngine));
//
//        ruleForSell=closePriceOverKeltnerUpperIn8.and(closePriceOverKeltnerMiddle).and(chaikinOver0_4in8).and(noTradeOpen);

//        ruleForSell = new OverIndicatorRule(closePrice, kcU, 8);
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(kcU60,closePrice60));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(longCci,cciUpperLimit ));
//        ruleForSell = ruleForSell.and(new IsFallingRule(longSma, 1, 1.0));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU8, 3));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.45, 8));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator60, 98.0, 8));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 98, 8));
////        ruleForSell = ruleForSell.and(new IsFallingRule(emaIndicator, 1, 1.0));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,21));
//
////        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
////                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));
//
//
//
//
//        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 8);
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(kcL60,closePrice60));
////        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(longCci, cciLowerLimit));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(longSma, 1, 1.0));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL8, 3     ));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.45, 8));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator60, 2, 8));
////        ruleForBuy = ruleForBuy.and(new IsRisingRule(emaIndicator, 1, 1.0));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,21));
//        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        // log:




        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        emaIndicator.indicatorColor=Color.RED;
        tradeEngine.log(emaIndicator);

        kcU.indicatorColor=Color.WHITE;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);



        kcUBig.indicatorColor=Color.RED;
        tradeEngine.log(kcUBig);

        kcLBig.indicatorColor=Color.GREEN;
        tradeEngine.log(kcLBig);
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcU8);
//
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcL8);


        moneyFlowIndicator.subWindowIndex=4;
        tradeEngine.log(moneyFlowIndicator);

//        testIndicator1.subWindowIndex=3;
//        testIndicator1.indicatorColor=Color.RED;
//        tradeEngine.log(testIndicator1);



//        moneyFlowIndicator.subWindowIndex=5;
//        tradeEngine.log(moneyFlowIndicator);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);
//
//        kcL8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcL8);
//
//        kcM.indicatorColor=Color.WHITE;
//        tradeEngine.log(kcM);

//        tradeEngine.log(murrayMathIndicator0);
//        tradeEngine.log(murrayMathIndicator12);



    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
//        if (buyOk) {
//            if (tradeEngine.timeSeriesRepo.ask>buyLimit) {
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,tradeEngine.series.getCurrentTime()));
//                buyOk=false;
//            }
//        }
//        if (sellOk) {
//            if (tradeEngine.timeSeriesRepo.bid<sellLimit) {
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,tradeEngine.series.getCurrentTime()));
//                sellOk=false;
//            }
//        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{
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
        if (tradeEngine.timeFrame==timeFrame) {
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            ZonedDateTime time=tradeEngine.series.getCurrentTime();
//            for(Rule rule: ruleForSell.getRuleSet()) rule.isSatisfied(time);
//            for(Rule rule: ruleForBuy.getRuleSet()) rule.isSatisfied(time);

//            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
//            ZonedDateTime zdt= ZonedDateTime.parse("2019.08.02 10:54", dateFormatter.withZone(ZoneId.systemDefault()));
//            if (tradeEngine.series.getCurrentTime().isAfter(zdt)) {
//                System.out.println("BREAK");
//            }

//            if (tradeEngine.series.getCurrentIndex()==62) {
//                System.out.println("HEREEEEEEEEEEE");
//            }


//            buyOk=ruleForBuy.isSatisfied(time);
//            sellOk=ruleForSell.isSatisfied(time);
//
//            if (buyOk) buyLimit=kcL.getValue(tradeEngine.series.getPrevIndex()).doubleValue();
//            if (sellOk) sellLimit=kcU.getValue(tradeEngine.series.getPrevIndex()).doubleValue();

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