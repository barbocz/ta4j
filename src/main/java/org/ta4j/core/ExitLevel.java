package org.ta4j.core;

import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExitLevel {

    Order order;

    public HashMap<Integer,List<ExitLevel.Property>> log = new HashMap<>();

    public void set(int index, Num level, Num amount,String description) {

//        boolean okToSet=false;
//        if (this instanceof StopLoss) {
//            Num clPrice=order.tradingRecord.series.getBar(index).getClosePrice();
//            if (order.getType().isBuy() && level.isLessThanOrEqual(order.tradingRecord.series.getBar(index).getClosePrice())) okToSet=true;
//            if (!order.getType().isBuy() && level.isGreaterThanOrEqual(order.tradingRecord.series.getBar(index).getClosePrice())) okToSet=true;
//        } else {    // Takeprofit
//            if (order.getType().isBuy() && level.isGreaterThanOrEqual(order.tradingRecord.series.getBar(index).getClosePrice())) okToSet=true;
//            if (!order.getType().isBuy() && level.isLessThanOrEqual(order.tradingRecord.series.getBar(index).getClosePrice())) okToSet=true;
//        }
//        if (okToSet) {
            if (log.containsKey(index)) {
                log.get(index).add(new ExitLevel.Property(level, amount, description));
            } else {
                List<ExitLevel.Property> firstExitLevel = new ArrayList<>();
                firstExitLevel.add(new ExitLevel.Property(level, amount, description));
                log.put(index, firstExitLevel);
            }
//        }
    }

    public List<ExitLevel.Property> get(int index) {
        return log.get(index);
    }

    public static class Property {
        public Num amount;
        public Num level;
        public String description;


        Property( Num level, Num amount,String description) {
            this.level = level;
            this.amount = amount;
            this.description = description;

        }
    }
}
