package org.strategy;

import org.ta4j.core.Rule;

import java.util.Date;

public abstract class Strategy {
    public Long id=new Date().getTime();;

    public TradeEngine tradeEngine;
    public Rule ruleForBuy,ruleForSell;

    public double orderAmount=1;

    public abstract void init();
    public abstract void onTickEvent();
    public abstract void onTradeEvent(Order order);
    public abstract void onBarChangeEvent(int timeFrame) throws Exception;
    public abstract void onOneMinuteDataEvent();

    public void onExitEvent(Order order){
        order.closedAmount=order.openedAmount;
    }




}
