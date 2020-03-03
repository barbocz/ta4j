package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class TMAIndicator extends CachedIndicator<Num> {

    private final int HalfLength=22;
    private double BandsDeviations = 2.6;
    private SMAIndicator smaIndicator;

    public enum Type {
        UP,  //
        DOWN  //
    }

    Type type;

    public TMAIndicator(TimeSeries series, Type type) {
        super(series);
        smaIndicator = new SMAIndicator(new ClosePriceIndicator(series), 1);
        this.type=type;

    }

    @Override
    protected Num calculate(int index) {
        double sum,sumw,tmpBuffer,diff,upBuffer,dnBuffer,wuBuffer,wdBuffer;
        int i,j,k;
//        Double tmBuffer[] = new Double[HalfLength];
        double FullLength = 2.0*HalfLength+1.0;

        for ( i = index - HalfLength; i <= index; i++) {
            sum  = (HalfLength+1)*smaIndicator.getValue(i).doubleValue();
            sumw=HalfLength+1;
            k=HalfLength;
            for (j = 1;  j <=HalfLength ; j++) {
                sum+= k * smaIndicator.getValue(i-j).doubleValue();
                sumw+=k;
                k--;
            }
            tmpBuffer=sum/sumw;
            diff=smaIndicator.getValue(i).doubleValue()-tmpBuffer;

        }

        return numOf(0.0);
    }


}


