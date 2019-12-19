/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.indicators;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.num.Num;

import java.util.Enumeration;
import java.util.function.Function;

/**
 * Average true range indicator.
 * <p/>
 */
public class ATRIndicator extends CachedIndicator<Num> {
    public static enum PriceType{
        OPEN,
        HIGH,
        LOW,
        CLOSE,
        NONE
    }

    private final MMAIndicator averageTrueRangeIndicator;
    private double coefficient=1.0;
    private PriceType priceType=PriceType.NONE;

    public ATRIndicator(TimeSeries series, int barCount) {
        super(series);
        this.averageTrueRangeIndicator = new MMAIndicator(new TRIndicator(series), barCount);
    }

    public ATRIndicator(TimeSeries series, int barCount,double coefficient) {
        super(series);
        this.averageTrueRangeIndicator = new MMAIndicator(new TRIndicator(series), barCount);
        this.coefficient=coefficient;
    }

    public ATRIndicator(TimeSeries series, int barCount, double coefficient, PriceType priceType) {

        super(series);
        this.averageTrueRangeIndicator = new MMAIndicator(new TRIndicator(series), barCount);
        this.coefficient=coefficient;
        this.priceType=priceType;

    }


    @Override
    protected Num calculate(int index) {
        Num price=numOf(0.0);
        Num value=averageTrueRangeIndicator.getValue(index);
        value=value.multipliedBy(numOf(coefficient));
        if (!priceType.equals(PriceType.NONE)) {
            if (priceType.equals(PriceType.CLOSE)) price = getTimeSeries().getBar(index).getClosePrice();
            else if (priceType.equals(PriceType.OPEN)) price = getTimeSeries().getBar(index).getOpenPrice();
            else if (priceType.equals(PriceType.LOW)) price = getTimeSeries().getBar(index).getLowPrice();
            else if (priceType.equals(PriceType.HIGH)) price = getTimeSeries().getBar(index).getHighPrice();
            value=value.plus(price);
        }


        return value;
    }
}
