package org.ta4j.core.trading.sltp;

import org.ta4j.core.Order;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.AbstractSLTPManager;

public class BreakEventAfter extends AbstractSLTPManager {
    private final int breakEventAfter;

    public BreakEventAfter(int breakEventAfter){
        this.breakEventAfter=breakEventAfter;
//        strict=true;
    }



    @Override
    protected Num calculate(int index, Order order) {
        if (index - order.getIndex()>=breakEventAfter) {
            return order.getPricePerAsset();
        }
        else return null;

    }

    @Override
    public String getParameters() {
        return getClass().getSimpleName()+"("+breakEventAfter+")";
    }
}
