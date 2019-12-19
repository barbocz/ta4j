package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

public class PvaIndicator extends CachedIndicator<Num> {

    public static enum Type{
        TREND_UP,
        TREND_DOWN
    }

    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private OpenPriceIndicator openPriceIndicator;
    private VolumeIndicator volumeIndicator;

    private Type type;

    public PvaIndicator(TimeSeries series, Type type) {
        super(series);
        this.type=type;
        closePriceIndicator = new ClosePriceIndicator(series);
        highPriceIndicator = new HighPriceIndicator(series);
        lowPriceIndicator = new LowPriceIndicator(series);
        openPriceIndicator = new OpenPriceIndicator(series);
        volumeIndicator = new VolumeIndicator(series);
    }

    @Override
    protected Num calculate(int index) {
        Num high=highPriceIndicator.getValue(index);
        Num low=lowPriceIndicator.getValue(index);
        Num open=openPriceIndicator.getValue(index);
        Num close=closePriceIndicator.getValue(index);
        Num volume=volumeIndicator.getValue(index);


        Num av=numOf(0.0);
        int end = Math.max(0, index - 10);
        for (int i = index - 1; i >= end; i--) av=av.plus(volumeIndicator.getValue(i));
        av=av.dividedBy(numOf(10.0));

        Num range=high.minus(low);
        Num value2=volume.multipliedBy(range);
        Num hiValue2=numOf(0.0);
        Num tempv2;
        for (int i = index - 1; i >= end; i--) {
           tempv2= volumeIndicator.getValue(i).multipliedBy(highPriceIndicator.getValue(i).minus(lowPriceIndicator.getValue(i)));
           if (tempv2.isGreaterThanOrEqual(hiValue2)) hiValue2=tempv2;
        }
        if (value2.isGreaterThanOrEqual(hiValue2) ||
                volume.isGreaterThanOrEqual(av.multipliedBy(numOf(2.0)))) {
            if (type==Type.TREND_UP) {
               if (close.isGreaterThan(open)) return volume;
            }
            if (type==Type.TREND_DOWN) {
                if (close.isLessThan(open)) return volume;
            }
        }

        return  NaN.NaN;

    }


}


