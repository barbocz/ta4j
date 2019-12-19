package org.ta4j.core.trading;


import org.ta4j.core.Order;
import org.ta4j.core.extendedCore.BaseTradingRecordExtended;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.util.HashMap;

public abstract class AbstractSLTPManager implements SLTPManager{
    /** The logger */
//    protected final Logger log = LoggerFactory.getLogger(getClass());

    public BaseTradingRecordExtended core=null;

    public HashMap<Integer,Num> lastValues=new HashMap<>();
    /** The class name */
    public final String name = getClass().getSimpleName();

    public boolean trail=false;    // alapból csak első alkalommal képez értéket, trail=true esetén minden barChange-nél
    public boolean strict=false;  // true esetén csak akkor érvényesül, ha az sl vagy tp értéke bent van a hi-lo range-ben, false esetén a következő nyitónál kilép ha a feltétel teljesül

    public void setTrail(boolean trail){
        this.trail=trail;
    }
    public boolean getTrail() {return trail;}

    public String getParameters() {return getClass().getSimpleName();}

    public String getName() {return name;}

    protected abstract Num calculate(int index, Order order);

    @Override
    public void process(int index,Order.OrderType orderType){
        Num closePrice=core.series.getBar(index).getClosePrice();
        Num takeProfit,stopLoss;
        boolean takeProfitModified,stopLossModified;
        for (Order order : core.openOrders.values()) {
            if (orderType!=order.getType()) continue;
            if (!trail && lastValues.containsKey(order.getId())) continue;
            Num exitLevel=calculate( index,  order);
            if (exitLevel==null) continue;
            stopLossModified=false;
            takeProfitModified=false;
            if (order.getType()==Order.OrderType.BUY) {
                if (order.takeProfit==null) takeProfit= DoubleNum.valueOf(Double.MAX_VALUE);
                else takeProfit=order.takeProfit;
                if (order.stopLoss==null) stopLoss= DoubleNum.valueOf(Double.MIN_VALUE);
                else stopLoss=order.stopLoss;

                if (exitLevel.isGreaterThanOrEqual(closePrice) && exitLevel.isLessThan(takeProfit)) {
                    order.takeProfit=exitLevel;
                    takeProfitModified=true;
                }
                else if (exitLevel.isLessThan(closePrice) && exitLevel.isGreaterThan(stopLoss)) {
                    order.stopLoss=exitLevel;
                    stopLossModified=true;
                }
            } else {
                if (order.takeProfit==null) takeProfit= DoubleNum.valueOf(Double.MIN_VALUE);
                else takeProfit=order.takeProfit;
                if (order.stopLoss==null) stopLoss= DoubleNum.valueOf(Double.MAX_VALUE);
                else stopLoss=order.stopLoss;

                if (exitLevel.isLessThanOrEqual(closePrice) && exitLevel.isGreaterThan(takeProfit)) {
                    order.takeProfit=exitLevel;
                    takeProfitModified=true;
                }
                else if (exitLevel.isGreaterThan(closePrice) && exitLevel.isLessThan(stopLoss))  {
                    order.stopLoss=exitLevel;
                    stopLossModified=true;
                }
            }
            if (core.debug)  {
                core.debug(index, order.getId(), exitLevel.getDelegate(), hashCode());
                if (stopLossModified) core.debug(index, order.getId(), order.stopLoss.getDelegate(), 1);
                if (takeProfitModified) core.debug(index, order.getId(), order.takeProfit.getDelegate(), 2);
            }

        }


//        Num value=calculate( index,  order);
//        if (value!=null) lastValues.put(order.getId(),value);
//        return value;
    }

    @Override
    public void setCore(BaseTradingRecordExtended core) {
        this.core=core;
    }




}
