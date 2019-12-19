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
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.num.Num;

/**
 * Simple moving average (SMA) indicator.
 * </p>
 */
public class AlligatorIndicator extends CachedIndicator<Num> {

//    input int InpJawsPeriod=13; // Jaws Period
//    input int InpTeethPeriod=8; // Teeth Period
//    input int InpLipsPeriod=5;  // Lips Period

    private static final long serialVersionUID = 653601631245729997L;

    private SMMAIndicator smmaIndicator;
    private MedianPriceIndicator medianPriceIndicator;
    private CachedIndicator workIndicator;
    private final int period;
    private final int shift;

    public static enum MaType {
        SMMA,
        SMA
    }


    public AlligatorIndicator(TimeSeries series, int period,int shift,MaType maType) {
        super(series);
        this.period = period;
        this.shift = shift;
        medianPriceIndicator= new MedianPriceIndicator(series);
        if (maType==MaType.SMMA)  workIndicator=new SMMAIndicator(medianPriceIndicator,period);
        if (maType==MaType.SMA)  workIndicator=new SMAIndicator(medianPriceIndicator,period);
    }

    @Override
    protected Num calculate(int index) {
//        Num sum = getTimeSeries().numOf(0);
//
//        for (int i = Math.max(0, index - timeFrame + 1); i <= index; i++) {
//            sum = sum.plus(smmaIndicator.getValue(i));
//        }
//
//        final int realBarCount = Math.min(timeFrame, index + 1);
//        return sum.dividedBy(getTimeSeries().numOf(realBarCount));
        return (Num)workIndicator.getValue(index-shift);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " timeFrame: " + period;
    }

}
