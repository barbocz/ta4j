package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.CoppockCurveIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.pivotpoints.DeMarkPivotPointIndicator;
import org.ta4j.core.indicators.pivotpoints.TimeLevel;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import org.ta4j.core.trading.rules.OrderConditionRule;

import java.awt.*;
import java.time.ZonedDateTime;


public class TestEntry extends Strategy {

    boolean buyOk=false,sellOk=false;
    double buyLimit,sellLimit;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;



    public void init(){






        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);

//        MurrayMathIndicator murrayMathIndicator0= new MurrayMathIndicator(tradeEngine.series,256,0);
//        MurrayMathIndicator murrayMathIndicator12= new MurrayMathIndicator(tradeEngine.series,256,12);
//        MurrayMathIndicator murrayMathIndicator6= new MurrayMathIndicator(tradeEngine.series,256,6);


        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        kcU = new KeltnerChannelUpperIndicator(kcM, 5.0, 89);
        kcL = new KeltnerChannelLowerIndicator(kcM, 5.0, 89);

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

        EMAIndicator emaIndicator=new EMAIndicator(closePrice,3);



//        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,8);

//        Rule closePriceOverKeltnerUpperIn8=new OverIndicatorRule(closePrice, kcU, 8);
//        Rule closePriceOverKeltnerUpper8In3=new OverIndicatorRule(closePrice, kcU8, 3);
//        Rule closePriceOverKeltnerMiddle=new OverIndicatorRule(closePrice, kcM);
//        Rule chaikinOver0_4in8=new OverIndicatorRule(chaikinIndicator, 0.4, 8);
//        Rule noTradeOpen=new NotRule(new hasOpenOrder(tradeEngine));
//
//        ruleForSell=closePriceOverKeltnerUpperIn8.and(closePriceOverKeltnerMiddle).and(chaikinOver0_4in8).and(noTradeOpen);

//        ruleForSell = new OverIndicatorRule(closePrice, kcU, 13);
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(longCci,cciUpperLimit ));

//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU8, 3));
        ruleForSell = new OverIndicatorRule(closePrice, kcM);
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.4, 8));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 98, 8));
//        ruleForSell = ruleForSell.and(new IsFallingRule(emaIndicator, 1, 1.0));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,1));

//        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));




//        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 13);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(longCci, cciLowerLimit));

//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL8, 3     ));
        ruleForBuy = new UnderIndicatorRule(closePrice, kcM);
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.4, 8));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 2, 8));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(emaIndicator, 1, 1.0));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,1));
//        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        // log:




        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

//        SMAIndicator smaIndicator=new SMAIndicator(closePrice,154);
//        BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator=new BollingerBandsMiddleIndicator(smaIndicator);
//        StandardDeviationIndicator standardDeviationIndicator=new StandardDeviationIndicator(closePrice,154);
//        BollingerBandsLowerIndicator bollingerBandsLowerIndicator=new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator,standardDeviationIndicator,2.6);
//        BollingerBandsUpperIndicator bollingerBandsUpperIndicator=new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator,standardDeviationIndicator,2.6);

//        emaIndicator.indicatorColor=Color.RED;
//        tradeEngine.log(emaIndicator);

        kcU.indicatorColor=Color.RED;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.GREEN;
        tradeEngine.log(kcL);

        DeMarkPivotPointIndicator deMarkPivotPointIndicator=new DeMarkPivotPointIndicator(tradeEngine.series, TimeLevel.DAY);
        deMarkPivotPointIndicator.indicatorColor=Color.YELLOW;
        tradeEngine.log(deMarkPivotPointIndicator);

//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcU8);
//
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcL8);

        CoppockCurveIndicator coppockCurveIndicator=new CoppockCurveIndicator(closePrice);

        coppockCurveIndicator.subWindowIndex=4;
        tradeEngine.log(coppockCurveIndicator);

//        testIndicator1.subWindowIndex=3;
//        testIndicator1.indicatorColor=Color.RED;
//        tradeEngine.log(testIndicator1);



//        moneyFlowIndicator.subWindowIndex=5;
//        tradeEngine.log(moneyFlowIndicator);

//        kcL.indicatorColor=Color.WHITE;
//        tradeEngine.log(kcL);
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


//            buyOk=ruleForBuy.isSatisfied(time);
//            sellOk=ruleForSell.isSatisfied(time);
//
//            if (buyOk) buyLimit=kcL.getValue(tradeEngine.series.getPrevIndex()).doubleValue();
//            if (sellOk) sellLimit=kcU.getValue(tradeEngine.series.getPrevIndex()).doubleValue();

//            if (ruleForSell.isSatisfied(time)) {
////                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
//            }
//            else if (ruleForBuy.isSatisfied(time)) {
//
////                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
//            }



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

