package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.volume.ChaikinMoneyFlowIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.*;

import java.awt.*;
import java.time.ZonedDateTime;


public class KeltnerEntry_v3 extends Strategy {

    boolean buyOk=false,sellOk=false;
    double buyLimit,sellLimit;
    KeltnerChannelUpperIndicator kcU;
    KeltnerChannelLowerIndicator kcL;



    public void init(){

        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 54);
        kcU = new KeltnerChannelUpperIndicator(kcM, 3.0, 54);
        kcL = new KeltnerChannelLowerIndicator(kcM, 3.0, 54);

        ChaikinMoneyFlowIndicator chaikinIndicator = new ChaikinMoneyFlowIndicator(tradeEngine.series, 8);
        MoneyFlowIndicator moneyFlowIndicator=new MoneyFlowIndicator(tradeEngine.series,8);

        SMAIndicator longSma = new SMAIndicator(closePrice, 8);

        ruleForSell = new OverIndicatorRule(closePrice, kcU, 8);

        ruleForSell = ruleForSell.and(new IsFallingRule(longSma,1,1.0));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(chaikinIndicator, 0.4, 8));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(moneyFlowIndicator, 90,8));

        ruleForBuy = new UnderIndicatorRule(closePrice, kcL, 8);
        ruleForBuy = ruleForBuy.and(new IsRisingRule(longSma,1,1.0));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcM));
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(chaikinIndicator, -0.4, 8));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(moneyFlowIndicator, 10,8));

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

        kcU.indicatorColor=Color.WHITE;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.WHITE;
        tradeEngine.log(kcL);

    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());


    }

    public void onBarChangeEvent(int timeFrame) throws Exception{
//        System.out.println("onBarChangeEvent------------- "+timeFrame);

        if (tradeEngine.timeFrame==timeFrame) {


            ZonedDateTime time=tradeEngine.series.getCurrentTime();

            if (ruleForSell.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.sell(orderAmount,tradeEngine.timeSeriesRepo.bid,time));
            }
            else if (ruleForBuy.isSatisfied(time)) {
                tradeEngine.onTradeEvent(Order.buy(orderAmount,tradeEngine.timeSeriesRepo.ask,time));
            }



        }


    }


    public void onOneMinuteDataEvent() {

    }




}

