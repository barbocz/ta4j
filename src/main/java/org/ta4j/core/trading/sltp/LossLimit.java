package org.ta4j.core.trading.sltp;

import org.ta4j.core.Order;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.AbstractSLTPManager;


public class LossLimit extends AbstractSLTPManager {
    private final double loss;

    public LossLimit(double loss){
        this.loss=loss;
    }

    @Override
    protected Num calculate(int index, Order order) {
        Num targetPrice=null;
        if (order.getType().isBuy())
            targetPrice=DoubleNum.valueOf(loss).plus(order.getPricePerAsset().multipliedBy(order.getOpenAmount())).dividedBy(order.getOpenAmount());
        else
            targetPrice=order.getPricePerAsset().multipliedBy(order.getOpenAmount()).minus(DoubleNum.valueOf(loss)).dividedBy(order.getOpenAmount());

        return targetPrice;
    }

    @Override
    public String getParameters() {
        return getClass().getSimpleName()+"("+loss+")";
    }
}
