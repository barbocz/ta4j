package org.ta4j.core.trading.sltp;

import org.ta4j.core.Indicator;
import org.ta4j.core.Order;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.CrossIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.AbstractSLTPManager;

public class IndicatorValue extends AbstractSLTPManager {


    private final Indicator indicator;
    private final Indicator biasIndicator;
    private final int afterBar;
    final double biasMultiplier;

    public IndicatorValue(Indicator<Num> indicator) {
        this.indicator=indicator;
        this.biasIndicator=null;
        afterBar=0;
        biasMultiplier=1.0;
    }

    public IndicatorValue(Indicator<Num> indicator,int afterBar) {
        this.indicator=indicator;
        this.biasIndicator=null;
        this.afterBar=afterBar;
        biasMultiplier=1.0;
    }

    public IndicatorValue(Indicator<Num> indicator,Indicator<Num> biasIndicator, double biasMultiplier, int afterBar) {
        this.indicator=indicator;
        this.biasIndicator=biasIndicator;
        this.afterBar=afterBar;
        this.biasMultiplier=biasMultiplier;
    }



    @Override
    protected Num calculate(int index, Order order) {
        if (afterBar>0) {
            if (index - order.getIndex()<afterBar) return null;
        }
        if (biasIndicator!=null) {
            Num indicatorValue=(Num)indicator.getValue(index);
            Num biasIndicatorValue=(Num)biasIndicator.getValue(index);
            return indicatorValue.plus(biasIndicatorValue.multipliedBy(DoubleNum.valueOf(biasMultiplier)));

        } else  return (Num)indicator.getValue(index);
    }

    @Override
    public String getParameters() {
        String info=getClass().getSimpleName();
        info+=" ("+indicator.getClass().getSimpleName();
        if (biasIndicator!=null) info+=", "+biasIndicator.getClass().getSimpleName()+")";
        else info+=")";
        return info;
    }
}
