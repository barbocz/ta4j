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

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Base class for price indicators
 */
public class Mt4Indicator extends CachedIndicator<Num> {

    private final Function<Bar, HashMap<String, Num[]>> priceFunction;
    public HashMap<String, Num[]> indicators;
    public String indicatorName=null;
    public int bufferIndex=0;

    public Mt4Indicator(TimeSeries series, String indicatorName) {
        super(series);
        this.priceFunction = Bar::getMt4Indicator;
        this.indicatorName=indicatorName;
    }

    public Mt4Indicator(TimeSeries series, String indicatorName,int bufferIndex) {
        super(series);
        this.priceFunction = Bar::getMt4Indicator;
        this.indicatorName=indicatorName;
        this.bufferIndex=bufferIndex;
    }

    @Override
    protected Num calculate(int index) {
        final Bar bar = getTimeSeries().getBar(index);
        indicators=priceFunction.apply(bar);
        if (indicators.size()==0) return NaN.NaN;
        Num[] buffers=indicators.get(indicatorName);
        if (buffers==null || buffers.length==0) return NaN.NaN;
        return buffers[bufferIndex];
    }

    public Num getBufferValue(int buffer,int index){
        final Bar bar = getTimeSeries().getBar(index);
        indicators=priceFunction.apply(bar);
        Num[] buffers=indicators.get(indicatorName);
        return buffers[buffer];

    }

}
