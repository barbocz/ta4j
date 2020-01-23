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

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;


/**
 * Stochastic oscillator K.
 * </p>
 * Receives timeSeries and barCount and calculates the StochasticOscillatorKIndicator
 * over ClosePriceIndicator, or receives an indicator, HighPriceIndicator and
 * LowPriceIndicator and returns StochasticOsiclatorK over this indicator.
 */
public class StochasticOscillatorKIndicator extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;

    private final int barCount;
    private final int slowing=5;

    private HighPriceIndicator highPriceIndicator;

    private LowPriceIndicator lowPriceIndicator;
    private ClosePriceIndicator closePriceIndicator;
    HighestValueIndicator highestHigh;
    LowestValueIndicator lowestMin;

    public StochasticOscillatorKIndicator(TimeSeries timeSeries, int barCount) {

        this(new ClosePriceIndicator(timeSeries), barCount, new HighPriceIndicator(timeSeries), new LowPriceIndicator(
                timeSeries));
        closePriceIndicator=new ClosePriceIndicator(timeSeries);
    }

    public StochasticOscillatorKIndicator(Indicator<Num> indicator, int barCount,
                                          HighPriceIndicator highPriceIndicator, LowPriceIndicator lowPriceIndicator) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
        this.highPriceIndicator = highPriceIndicator;
        this.lowPriceIndicator = lowPriceIndicator;
        highestHigh = new HighestValueIndicator(highPriceIndicator, barCount);
        lowestMin = new LowestValueIndicator(lowPriceIndicator, barCount);

    }

    @Override
    protected Num calculate(int index) {


//        HighestValueIndicator highestHigh = new HighestValueIndicator(highPriceIndicator, barCount);
//        LowestValueIndicator lowestMin = new LowestValueIndicator(lowPriceIndicator, barCount);
//
//        Num highestHighPrice = highestHigh.getValue(index);
//        Num lowestLowPrice = lowestMin.getValue(index);
//
//        return indicator.getValue(index).minus(lowestLowPrice)
//                .dividedBy(highestHighPrice.minus(lowestLowPrice))
//                .multipliedBy(numOf(100));

        Num sumLow= DoubleNum.valueOf(0.0);
        Num sumHigh= DoubleNum.valueOf(0.0);

        Num lowestLowPrice;
        if (index<slowing) return DoubleNum.valueOf(0.0);
        for (int i = index-slowing; i <= index; i++) {

            lowestLowPrice = lowestMin.getValue(i);

            sumLow=sumLow.plus(closePriceIndicator.getValue(i).minus(lowestLowPrice));
            sumHigh=sumHigh.plus(highestHigh.getValue(i).minus(lowestLowPrice));
        }
        if (sumHigh.isEqual(DoubleNum.valueOf(0.0))) return DoubleNum.valueOf(100.0);
        else return sumLow.dividedBy(sumHigh).multipliedBy( DoubleNum.valueOf(100.0));

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
