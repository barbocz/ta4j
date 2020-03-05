package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.helpers.WeightedCloseIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.IsNotEqualRule;
import org.ta4j.core.trading.rules.OrderConditionRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.ZonedDateTime;

// KeltnerExit-tel használni!!!
public class BollingerEntryProven extends Strategy {

    boolean buyOk = false, sellOk = false;
    double buyLimit, sellLimit;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;

    final Double murrayRange = 38.12;

    public void init() {

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);

//        MurrayMathIndicator murrayMathIndicator0= new MurrayMathIndicator(tradeEngine.series,256,0);
//        MurrayMathIndicator murrayMathIndicator12= new MurrayMathIndicator(tradeEngine.series,256,12);
//        MurrayMathIndicator murrayMathIndicator6= new MurrayMathIndicator(tradeEngine.series,256,6);


        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
         kcU = new KeltnerChannelUpperIndicator(kcM, 5.0, 89);
         kcL = new KeltnerChannelLowerIndicator(kcM, 5.0, 89);
        MoneyFlowIndicator moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 6);
        WeightedCloseIndicator weightedCloseIndicator = new WeightedCloseIndicator(tradeEngine.series);
        EMAIndicator emaIndicator = new EMAIndicator(weightedCloseIndicator, 5);

        MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        }

        PreviousValueIndicator previousMurray = new PreviousValueIndicator(murrayMathIndicators[12], 1);

        ruleForSell = new OverIndicatorRule(closePrice, kcU, 3);
        ruleForSell = ruleForSell.and(new IsNotEqualRule(previousMurray, murrayMathIndicators[12], 13));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 95.0, 4));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY, 1));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(closePrice,kcU ));

        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 3);
        ruleForBuy = ruleForBuy.and(new IsNotEqualRule(previousMurray, murrayMathIndicators[12], 13));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 5.0, 4));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL, 1));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice,kcL ));

//        LaguerreIndicator laguerreIndicator=new LaguerreIndicator(tradeEngine.series,0.6);
//        PreviousValueIndicator previousValueIndicator=new PreviousValueIndicator(laguerreIndicator);


//        ruleForSell = new OverIndicatorRule(closePrice, bollingerBandsUpperIndicator, 5);
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice,kcU,5 ));
//        ruleForSell = ruleForSell.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(1.0),3));
//        ruleForSell = ruleForSell.and(new UnderIndicatorRule(laguerreIndicator,0.95));
//
////        ruleForSell = ruleForSell.and(new IsFallingRule(kamaIndicator,true));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(minusDIIndicator,plusDIIndicator));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcU));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.42, 6));
//
//
////        ruleForSell =  ruleForSell.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(1.0),4));
//
//
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 98, 8));
////        ruleForSell = ruleForSell.and(new IsFallingRule(avgAdxIndicator, true));
////        ruleForSell = ruleForSell.and(new OverIndicatorRule(avgAdxIndicator,75.0));
//        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY,5));
//
////        ruleForSell = ruleForSell.or(new OverIndicatorRule(cciIndicator,tradeEngine.series.numOf(220)).
////                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_SELL)).and(new UnderIndicatorRule(closePrice,kcU)));
//
//
//
////        ruleForBuy = new IsRisingRule(avgIndicator, true);
//        ruleForBuy = new UnderIndicatorRule(closePrice, bollingerBandsLowerIndicator, 8);
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(closePrice, kcL,8));
//
//        ruleForBuy =  ruleForBuy.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(0.0),3));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(laguerreIndicator,0.05));
//        ruleForBuy = ruleForBuy.and(new IsEqualRule(previousValueIndicator,tradeEngine.series.numOf(0.0),4));

//        ruleForBuy = ruleForBuy.and(new IsEqualRule(laguerreIndicatorSlow,tradeEngine.series.numOf(0.0),0));

//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(plusDIIndicator, minusDIIndicator));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(longCci, cciLowerLimit));

//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL8, 3     ));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.42, 6));
////        ruleForBuy = ruleForBuy.and(new IsFallingRule(avgAdxIndicator, true));
////        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(avgAdxIndicator,75.0));
////        ruleForBuy = ruleForBuy.and(new IsRisingRule(kamaIndicator,true));
////        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 2, 8));
////        ruleForBuy = ruleForBuy.and(new IsRisingRule(avgIndicator, true));
//        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL,5));
////        ruleForBuy = ruleForBuy.or(new UnderIndicatorRule(cciIndicator,tradeEngine.series.numOf(-220)).
////                and(new hasOpenOrder(tradeEngine, hasOpenOrder.OpenedOrderType.ONLY_BUY)).and(new OverIndicatorRule(closePrice,kcL)));


//        FisherIndicator testIndicator=new FisherIndicator(tradeEngine.series);


        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        emaIndicator.indicatorColor = Color.ORANGE;
        tradeEngine.log(emaIndicator);

        kcU.indicatorColor = Color.RED;
        tradeEngine.log(kcU);

        kcM.indicatorColor = Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor = Color.GREEN;
        tradeEngine.log(kcL);


        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }
//        laguerreIndicator.subWindowIndex = 5;
//        laguerreIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(laguerreIndicator);

        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());
        if (buyOk) {
            if (tradeEngine.timeSeriesRepo.ask > buyLimit) {
                tradeEngine.onTradeEvent(Order.buy(orderAmount,  tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime()));
                buyOk = false;
            }
        }
        if (sellOk) {
            if (tradeEngine.timeSeriesRepo.bid < sellLimit) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, tradeEngine.series.getCurrentTime()));
                sellOk = false;
            }
        }

    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        buyOk = false;
        sellOk = false;
        buyLimit=Double.MAX_VALUE;
        sellLimit=0.0;
        if (tradeEngine.period == timeFrame) {
//            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            ZonedDateTime time = tradeEngine.series.getCurrentTime();


            if (ruleForSell.isSatisfied(time)) {
//                System.out.println(tradeEngine.series.getIndex(time) + ". Sell Entry: " + time);
//                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
                sellOk = true;
                sellLimit=kcU.getValue(tradeEngine.currentBarIndex).doubleValue();
            } else if (ruleForBuy.isSatisfied(time)) {
                buyOk = true;
                buyLimit=kcL.getValue(tradeEngine.currentBarIndex).doubleValue();
//                System.out.println(tradeEngine.series.getIndex(time) + ". Buy Entry: " + time);
//                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }


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