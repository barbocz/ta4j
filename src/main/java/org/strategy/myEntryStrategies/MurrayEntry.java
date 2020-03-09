package org.strategy.myEntryStrategies;

import org.strategy.Order;
import org.strategy.Strategy;
import org.ta4j.core.indicators.CoppockCurveIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;
import org.ta4j.core.indicators.pivotpoints.DeMarkPivotPointIndicator;
import org.ta4j.core.indicators.pivotpoints.TimeLevel;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;
import org.ta4j.core.trading.rules.IsMurrayRebound_v2_Rule;
import org.ta4j.core.trading.rules.OrderConditionRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


public class MurrayEntry extends Strategy {

    final Double murrayRange = 38.12;
    LaguerreIndicator laguerreIndicator;

    public void init() {

//        ZoloIndicator zoloIndicatorUp = new ZoloIndicator(tradeEngine.series, true);
//        ZoloIndicator zoloIndicatorDown = new ZoloIndicator(tradeEngine.series, false);
        MoneyFlowIndicator moneyFlowIndicator = new MoneyFlowIndicator(tradeEngine.series, 5);
        MurrayChangeIndicator murrayChangeIndicator = new MurrayChangeIndicator(tradeEngine.series, murrayRange);
        laguerreIndicator = new LaguerreIndicator(tradeEngine.series, 0.2);

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(tradeEngine.series, 89);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 4.6, 89);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 4.6, 89);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(tradeEngine.series);
//        VolumeIndicator volumeIndicator=new VolumeIndicator(tradeEngine.series);


        ruleForSell = new IsMurrayRebound_v2_Rule(tradeEngine.series, IsMurrayRebound_v2_Rule.ReboundType.DOWN, murrayRange);
//        ruleForSell = ruleForSell.and(new IsNotShiftyTrendRule(tradeEngine.series, IsNotShiftyTrendRule.TrendType.DOWN));
//        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice,kcU,1));
//        ruleForSell=ruleForSell.and(new IsZoloSignalRule(tradeEngine.series, IsZoloSignalRule.ReboundType.DOWN));
        ruleForSell = ruleForSell.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_BUY, 5));


        ruleForBuy = new IsMurrayRebound_v2_Rule(tradeEngine.series, IsMurrayRebound_v2_Rule.ReboundType.UP, murrayRange);
//        ruleForBuy = ruleForBuy.and(new IsNotShiftyTrendRule(tradeEngine.series, IsNotShiftyTrendRule.TrendType.UP));
//        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(laguerreIndicator,0.1));
//        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice,kcL,1));
//        ruleForBuy=ruleForBuy.and(new IsZoloSignalRule(tradeEngine.series, IsZoloSignalRule.ReboundType.UP));
        ruleForBuy = ruleForBuy.and(new OrderConditionRule(tradeEngine, OrderConditionRule.AllowedOrderType.ONLY_SELL, 5));

        tradeEngine.log(ruleForSell);
        tradeEngine.log(ruleForBuy);

//        DeMarkPivotPointIndicator deMarkPivotPointIndicator=new DeMarkPivotPointIndicator(tradeEngine.series, TimeLevel.DAY);
//        deMarkPivotPointIndicator.indicatorColor=Color.YELLOW;
//        tradeEngine.log(deMarkPivotPointIndicator);

        MurrayMathFixedIndicator murrayMathIndicators[] = new MurrayMathFixedIndicator[13];
        for (int i = 0; i < 13; i++) {
            murrayMathIndicators[i] = new MurrayMathFixedIndicator(tradeEngine.series, i, murrayRange);
        }
        for (int i = 0; i < 13; i++) {
            if (i % 2 != 0) murrayMathIndicators[i].indicatorColor = Color.GRAY;
            if (i == 2) murrayMathIndicators[i].indicatorColor = Color.GREEN;
            if (i == 10) murrayMathIndicators[i].indicatorColor = Color.RED;
            tradeEngine.log(murrayMathIndicators[i]);
        }

        murrayChangeIndicator.subWindowIndex = 4;
        tradeEngine.log(murrayChangeIndicator);




        kcU.indicatorColor=Color.RED;
        tradeEngine.log(kcU);

        kcM.indicatorColor=Color.WHITE;
        tradeEngine.log(kcM);

        kcL.indicatorColor=Color.GREEN;
        tradeEngine.log(kcL);
//        zoloIndicatorUp.subWindowIndex = 5;
//        zoloIndicatorUp.indicatorColor = Color.GREEN;
//        tradeEngine.log(zoloIndicatorUp);
//
//        zoloIndicatorDown.subWindowIndex = 5;
//        zoloIndicatorDown.indicatorColor = Color.RED;
//        tradeEngine.log(zoloIndicatorDown);

//        MbfxTimingIndicator mbfxTimingIndicator=new MbfxTimingIndicator(tradeEngine.series,8);
//        mbfxTimingIndicator.subWindowIndex = 5;
//        mbfxTimingIndicator.indicatorColor = Color.RED;
//        tradeEngine.log(mbfxTimingIndicator);



        laguerreIndicator.subWindowIndex = 5;
        laguerreIndicator.indicatorColor = Color.RED;
        tradeEngine.log(laguerreIndicator);

        moneyFlowIndicator.subWindowIndex = 6;
        tradeEngine.log(moneyFlowIndicator);


    }


    public void onTradeEvent(Order order) {

    }

    public void onTickEvent() throws Exception {
//        System.out.println("onTickEvent------------- "+tradeEngine.series.getBid());

//        if (tradeEngine.openedOrders.size() > 0) {
//            HashMap<Integer, Double> profits = new HashMap<>();
//            List<Order> losingOrders = new ArrayList<>();
//            List<Order> profitableOrders = new ArrayList<>();
//            LinkedList<Double> takeProfitLevels = new LinkedList<>();
//
//            int id;
//            double correctionLevel, takeProfitLevel;
////            if (tradeEngine.currentBarIndex > 21440) {
////                System.out.println("");
////            }
//            for (Order order : tradeEngine.openedOrders) {
//                if (order.parentId != 0) id = order.parentId;
//                else id = order.id;
//                if (profits.containsKey(id))
//                    profits.replace(id, profits.get(id) + order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid));
//                else profits.put(id, order.getCurrentProfit(tradeEngine.timeSeriesRepo.bid));
//            }
//            for (Order order : tradeEngine.openedOrders) {
//                if (profits.containsKey(order.id)) {
//                    if (profits.get(order.id) < 0.0) losingOrders.add(order);
//                    else profitableOrders.add(order);
//                }
//            }
//
////            for (Order order : profitableOrders) {
////                takeProfitLevel=order.hashMapParameters.get("TP").get(order.phase);
////                if (order.type == Order.Type.BUY && tradeEngine.timeSeriesRepo.bid>takeProfitLevel) {
////                    if (tradeEngine.currentBarIndex > 21423) {
////                        System.out.println(tradeEngine.series.getCurrentTime()+" - takeProfitLevel: " + takeProfitLevel+"   profit: "+profits.get(order.id));
////                    }
////
////                } else {
////
////                }
////
////            }
//
//            if (losingOrders.size() > 0) {
//                LinkedList<Double> correctionLevels;
//                for (Order order : losingOrders) {
//                    correctionLevels = order.hashMapParameters.get("CL");
//                    for (Double currentCorrectionLevel : correctionLevels) {
//                        if (order.type == Order.Type.BUY) {
//                            if (tradeEngine.timeSeriesRepo.ask < currentCorrectionLevel) {
//                                if (tradeEngine.currentBarIndex > 21440) {
//                                    order.phase++;
//
//                                    System.out.println("correctionLevel: " + currentCorrectionLevel);
//
//
//                                    Order correctionOrder = Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, tradeEngine.series.getCurrentTime());
//                                    correctionOrder.parentId = order.id;
//                                    tradeEngine.onTradeEvent(correctionOrder);
//                                }
//                            }
//                        } else {
//
//                        }
//
//                    }
//
//                }
//            }
//
//        }


    }

    public void onBarChangeEvent(int timeFrame) throws Exception {
        ZonedDateTime time = tradeEngine.series.getCurrentTime();

        if (time.getHour() > 14 && time.getHour() < 17) return;
//        if (time.getHour()==15 || (time.getHour()==16 && time.getMinute()<31)) return;
        if (time.getHour() > 22 || (time.getHour() == 0 && time.getMinute() < 30)) return;
        if (ruleForSell.isSatisfied(time) ) {
            tradeEngine.onTradeEvent(Order.sell(orderAmount, tradeEngine.timeSeriesRepo.bid, time));
        } else if (ruleForBuy.isSatisfied(time) ) {
            tradeEngine.onTradeEvent(Order.buy(orderAmount, tradeEngine.timeSeriesRepo.ask, time));
        }
    }


    public void onOneMinuteDataEvent() {

    }


}