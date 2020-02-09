package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.AntiAlligatorIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.WaddahIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.*;

import java.awt.*;
import java.time.ZonedDateTime;

// KeltnerExit-tel használni!!!
public class BollingerEntryProven extends Strategy {

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


        SMAIndicator smaIndicator=new SMAIndicator(closePrice,20);
        BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator=new BollingerBandsMiddleIndicator(smaIndicator);
        StandardDeviationIndicator standardDeviationIndicator=new StandardDeviationIndicator(closePrice,20);
        BollingerBandsLowerIndicator bollingerBandsLowerIndicator=new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator,standardDeviationIndicator,1.6);
        BollingerBandsUpperIndicator bollingerBandsUpperIndicator=new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator,standardDeviationIndicator,1.6);

        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(tradeEngine.series, 4);
        MinusDIIndicator minusDIIndicator = new MinusDIIndicator(tradeEngine.series, 4);
        ADXIndicator adxIndicator = new ADXIndicator(tradeEngine.series, 5);


//        KeltnerChannelMiddleIndicator kcM60 = new KeltnerChannelMiddleIndicator(tradeEngine.getTimeSeries(60), 8);
//        KeltnerChannelUpperIndicator kcU60 = new KeltnerChannelUpperIndicator(kcM60, 1.3, 8);
//        KeltnerChannelLowerIndicator kcL60 = new KeltnerChannelLowerIndicator(kcM60, 1.3, 8);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 6);
//        ADXIndicator adxIndicator = new ADXIndicator(tradeEngine.series, 3);
//        AvgIndicator avgAdxIndicator=new AvgIndicator(adxIndicator,8);

//        TradeCounterIndicator tradeCounterIndicator=new TradeCounterIndicator(tradeEngine);
        CCIIndicator cciIndicator = new CCIIndicator(tradeEngine.series, 30);

//        SMAIndicator shortSma = new SMAIndicator(closePrice, 2);
//        SMAIndicator longSma = new SMAIndicator(closePrice, 8);

        CCIIndicator longCci = new CCIIndicator(tradeEngine.series, 34);
        Num cciUpperLimit = tradeEngine.series.numOf(250);
        Num cciLowerLimit = tradeEngine.series.numOf(-250);

        EMAIndicator emaIndicator=new EMAIndicator(closePrice,8);

//        KAMAIndicator kamaIndicator = new KAMAIndicator(closePrice,5,2,30);

//        MedianPriceIndicator medianPriceIndicatorUpper=new MedianPriceIndicator(kcU,kcM);
//        MedianPriceIndicator medianPriceIndicatorLower=new MedianPriceIndicator(kcL,kcM);

//        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,8);

//        Rule closePriceOverKeltnerUpperIn8=new OverIndicatorRule(closePrice, kcU, 8);
//        Rule closePriceOverKeltnerUpper8In3=new OverIndicatorRule(closePrice, kcU8, 3);
//        Rule closePriceOverKeltnerMiddle=new OverIndicatorRule(closePrice, kcM);
//        Rule chaikinOver0_4in8=new OverIndicatorRule(chaikinIndicator, 0.4, 8);
//        Rule noTradeOpen=new NotRule(new hasOpenOrder(tradeEngine));
//
//        ruleForSell=closePriceOverKeltnerUpperIn8.and(closePriceOverKeltnerMiddle).and(chaikinOver0_4in8).and(noTradeOpen);

        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.6);
        PreviousValueIndicator previousValueIndicator=new PreviousValueIndicator(laguerreIndicator);


        ruleForSell = new OverIndicatorRule(closePrice, bollingerBandsUpperIndicator, 5);
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice,kcU,5 ));
        ruleForSell = ruleForSell.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(1.0),3));
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(laguerreIndicator,0.95));

//        ruleForSell = ruleForSell.and(new IsFallingRule(kamaIndicator,true));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(minusDIIndicator,plusDIIndicator));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.42, 6));


//        ruleForSell =  ruleForSell.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(1.0),4));


//        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 98, 8));
//        ruleForSell = ruleForSell.and(new IsFallingRule(avgAdxIndicator, true));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(avgAdxIndicator,75.0));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,5));

//        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));



//        ruleForBuy = new IsRisingRule(avgIndicator, true);
        ruleForBuy = new UnderIndicatorRule(closePrice, bollingerBandsLowerIndicator, 8);
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL,8));

        ruleForBuy =  ruleForBuy.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(0.0),3));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(laguerreIndicator,0.05));
//        ruleForBuy = ruleForBuy.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(0.0),4));

//        ruleForBuy = ruleForBuy.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(0.0),0));

//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(plusDIIndicator, minusDIIndicator));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(longCci, cciLowerLimit));

//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL8, 3     ));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.42, 6));
//        ruleForBuy = ruleForBuy.and(new IsFallingRule(avgAdxIndicator, true));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(avgAdxIndicator,75.0));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(kamaIndicator,true));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 2, 8));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(avgIndicator, true));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,5));
//        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        // log:


        bollingerBandsMiddleIndicator.indicatorColor=Color.ORANGE;
        tradeEngine.log(bollingerBandsMiddleIndicator);

        bollingerBandsLowerIndicator.indicatorColor=Color.RED;
        tradeEngine.log(bollingerBandsLowerIndicator);

        bollingerBandsUpperIndicator.indicatorColor=Color.GREEN;
        tradeEngine.log(bollingerBandsUpperIndicator);



        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

//        emaIndicator.indicatorColor=Color.RED;
//        tradeEngine.log(emaIndicator);
//
//        kcU.indicatorColor=Color.WHITE;
//        tradeEngine.log(kcU);
//
//        kcM.indicatorColor=Color.WHITE;
//        tradeEngine.log(kcM);
//
//        kcL.indicatorColor=Color.WHITE;
//        tradeEngine.log(kcL);

//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcU8);
//
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcL8);

        LaguerreIndicator laguerreIndicatorSlow=new LaguerreIndicator(tradeEngine.series,0.6);
        chaikinIndicator.subWindowIndex=4;
        tradeEngine.log(chaikinIndicator);

//        plusDIIndicator.subWindowIndex=5;
//        plusDIIndicator.indicatorColor=Color.GREEN;
//        tradeEngine.log(plusDIIndicator);
//
//        minusDIIndicator.subWindowIndex=5;
//        minusDIIndicator.indicatorColor=Color.RED;
//        tradeEngine.log(minusDIIndicator);

        AntiAlligatorIndicator antiAlligatorIndicator=new AntiAlligatorIndicator(tradeEngine.series);
        antiAlligatorIndicator.subWindowIndex=5;
        antiAlligatorIndicator.indicatorColor=Color.ORANGE;
        tradeEngine.log(antiAlligatorIndicator);


//        testIndicator1.subWindowIndex=3;
//        testIndicator1.indicatorColor=Color.RED;
//        tradeEngine.log(testIndicator1);
//
//        WaddahIndicator waddahIndicatorUp=new WaddahIndicator(tradeEngine.series, WaddahIndicator.Type.TREND_UP);
//        WaddahIndicator waddahIndicatorDown=new WaddahIndicator(tradeEngine.series, WaddahIndicator.Type.TREND_DOWN);
//        WaddahIndicator waddahIndicatorExp=new WaddahIndicator(tradeEngine.series, WaddahIndicator.Type.EXPLOSION);

//
//        waddahIndicatorUp.subWindowIndex=4;
//        waddahIndicatorUp.indicatorColor=Color.GREEN;
//        tradeEngine.log(waddahIndicatorUp);
//
//        waddahIndicatorDown.subWindowIndex=4;
//        waddahIndicatorDown.indicatorColor=Color.RED;
//        tradeEngine.log(waddahIndicatorDown);
//
//        waddahIndicatorExp.subWindowIndex=4;
//        waddahIndicatorExp.indicatorColor=Color.ORANGE;
//        tradeEngine.log(waddahIndicatorExp);

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