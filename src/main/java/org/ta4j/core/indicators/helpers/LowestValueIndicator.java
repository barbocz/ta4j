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
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * Lowest value indicator.
 * </p>
 */
public class LowestValueIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> indicator;

    private final int barCount;
    private final int barShift;

    public LowestValueIndicator(Indicator<Num> indicator, int barCount, int barShift) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
        this.barShift =barShift;
    }

    public LowestValueIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
        barShift=0;
    }

    @Override
    protected Num calculate(int index) {
        if (indicator.getValue(index).isNaN() && barCount != 1) {
            return new LowestValueIndicator(indicator, barCount - 1).getValue(index - 1);
        }
        int end = Math.max(0, index - barCount + 1);
        Num lowest = indicator.getValue(index-barShift);
        for (int i = index -barShift; i >= end; i--) {
            if (lowest.isGreaterThan(indicator.getValue(i))) {
                lowest = indicator.getValue(i);
            }
        }
        return lowest;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
