package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.*;
import org.ta4j.core.trading.rules.helpers.IsZoloSignalRule;

import java.awt.*;
import java.time.ZonedDateTime;

import static org.ta4j.core.trading.rules.helpers.IsZoloSignalRule.ReboundType.DOWN;


public class KeltnerEntry extends Strategy {

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
        kcU = new KeltnerChannelUpperIndicator(kcM, 3.4, 54);
        kcL = new KeltnerChannelLowerIndicator(kcM, 3.4, 54);

//        KeltnerChannelMiddleIndicator kcM8 = new KeltnerChannelMiddleIndicator(tradeEngine.getTimeSeries(60), 34);
//        KeltnerChannelUpperIndicator kcU8 = new KeltnerChannelUpperIndicator(kcM8, 3.4, 34);
//        KeltnerChannelLowerIndicator kcL8 = new KeltnerChannelLowerIndicator(kcM8, 3.4, 34);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 5);

//        TradeCounterIndicator tradeCounterIndicator=new TradeCounterIndicator(tradeEngine);

//        SMAIndicator shortSma = new SMAIndicator(closePrice, 2);
//        SMAIndicator longSma = new SMAIndicator(closePrice, 8);

        CCIIndicator longCci = new CCIIndicator(tradeEngine.series, 34);
        Num cciUpperLimit = tradeEngine.series.numOf(250);
        Num cciLowerLimit = tradeEngine.series.numOf(-250);

        EMAIndicator emaIndicator=new EMAIndicator(closePrice,3);

        MedianPriceIndicator medianPriceIndicator = new MedianPriceIndicator(tradeEngine.series);

        RSIIndicator rsiIndicator = new RSIIndicator(emaIndicator, 8);
        RSIIndicator rsiIndicatorFast = new RSIIndicator(emaIndicator, 4);

        ZoloIndicator zoloIndicatorUp=new ZoloIndicator(tradeEngine.series,true);
        ZoloIndicator zoloIndicatorDown=new ZoloIndicator(tradeEngine.series,false);
        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,5);

//        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,8);

//        Rule closePriceOverKeltnerUpperIn8=new OverIndicatorRule(closePrice, kcU, 8);
//        Rule closePriceOverKeltnerUpper8In3=new OverIndicatorRule(closePrice, kcU8, 3);
//        Rule closePriceOverKeltnerMiddle=new OverIndicatorRule(closePrice, kcM);
//        Rule chaikinOver0_4in8=new OverIndicatorRule(chaikinIndicator, 0.4, 8);
//        Rule noTradeOpen=new NotRule(new hasOpenOrder(tradeEngine));
//
//        ruleForSell=closePriceOverKeltnerUpperIn8.and(closePriceOverKeltnerMiddle).and(chaikinOver0_4in8).and(noTradeOpen);

//        ruleForSell = new OverIndicatorRule(closePrice, kcU, 8);
//        ruleForSell = new UnderIndicatorRule(rsiIndicator, 90.0,3);
//        ruleForSell = ruleForSell.and(new IsFallingRule(rsiIndicatorFast, 1, 1.0));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(longCci,cciUpperLimit ));

        ruleForSell=new IsZoloSignalRule(tradeEngine.series, IsZoloSignalRule.ReboundType.DOWN);

//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU, 8));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(zoloIndicatorDown, 50));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.45, 5));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 40));

        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,1));

//        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));



        ruleForBuy=new IsZoloSignalRule(tradeEngine.series, IsZoloSignalRule.ReboundType.UP);
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL, 8));
//        ruleForBuy = new OverIndicatorRule(rsiIndicator, 10.0,3);
//        ruleForSell = ruleForSell.and(new IsRisingRule(rsiIndicatorFast, 1, 1.0));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(longCci, cciLowerLimit));

//        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 2 );
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(zoloIndicatorUp, 50));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.45, 5));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 60));
//        ruleForBuy = ruleForBuy.and(new IsRisingRule(emaIndicator, 1, 1.0));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,1));
//        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
//                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        // log:




        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, 38);
        }
        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

        MurrayChangeIndicator murrayChangeIndicator = new MurrayChangeIndicator(tradeEngine.series,38);
        murrayChangeIndicator.subWindowIndex = 4;
        tradeEngine.log(murrayChangeIndicator);

        kcU.indicatorColor=Color.WHITE;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);


        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcU8);
//
//        kcU8.indicatorColor=Color.GRAY;
//        tradeEngine.log(kcL8);


//        longCci.subWindowIndex=4;
//        tradeEngine.log(longCci);

//        testIndicator1.subWindowIndex=3;
//        testIndicator1.indicatorColor=Color.RED;
//        tradeEngine.log(testIndicator1);



//        moneyFlowIndicator.subWindowIndex=5;
//        tradeEngine.log(moneyFlowIndicator);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);



        zoloIndicatorUp.subWindowIndex = 5;
        zoloIndicatorUp.indicatorColor=Color.GREEN;
        tradeEngine.log(zoloIndicatorUp);



        zoloIndicatorDown.subWindowIndex = 5;
        zoloIndicatorDown.indicatorColor=Color.RED;
        tradeEngine.log(zoloIndicatorDown);
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
//        if (tradeEngine.timeSeriesRepo.ask > buyLimit) {
//            tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));
//            buyLimit = Double.MAX_VALUE;
//        }
//
//
//        if (tradeEngine.timeSeriesRepo.bid < sellLimit) {
//            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
//            sellLimit = 0.0;
//        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception{

//        sellLimit = 0.0;
//        buyLimit = Double.MAX_VALUE;
//        if (ruleForSell.isSatisfied(tradeEngine.series.getCurrentTime())) {
//            sellLimit = kcU.getValue(tradeEngine.currentBarIndex).doubleValue();
//        }
//        else if (ruleForBuy.isSatisfied(tradeEngine.series.getCurrentTime())) {
//            buyLimit =  kcL.getValue(tradeEngine.currentBarIndex).doubleValue();
//        }


        if (ruleForBuy.isSatisfied(tradeEngine.series.getCurrentTime())) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));
            buyLimit = Double.MAX_VALUE;
        }


        if  (ruleForSell.isSatisfied(tradeEngine.series.getCurrentTime())) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
            sellLimit = 0.0;
        }


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