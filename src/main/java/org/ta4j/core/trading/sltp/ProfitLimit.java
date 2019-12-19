package org.ta4j.core.trading.sltp;

import org.ta4j.core.Order;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.AbstractSLTPManager;

public class ProfitLimit extends AbstractSLTPManager {
    private final double profit;

    public ProfitLimit(double profit){
        this.profit =profit;
    }

    @Override
    protected Num calculate(int index, Order order) {
        Num targetPrice=null;

        if (order.getType().isBuy())
            targetPrice=DoubleNum.valueOf(profit).plus(order.getPricePerAsset().multipliedBy(order.getOpenAmount())).dividedBy(order.getOpenAmount());
        else
            targetPrice=order.getPricePerAsset().multipliedBy(order.getOpenAmount()).minus(DoubleNum.valueOf(profit)).dividedBy(order.getOpenAmount());


        return targetPrice;
    }


}
