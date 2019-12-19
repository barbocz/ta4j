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
package org.ta4j.core.indicators.mt4Selection;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A fixed indicator.
 * @param <T> the type of returned value (Double, Boolean, etc.)
 */
public class ZigZagFixedIndicator<T> extends AbstractIndicator<T> {

    private static final long serialVersionUID = -2946691798800328858L;
    private List<Num> values = new ArrayList<>();

    int extDepth =8;
    double extDeviation =0.00005;
    int extBackstep =4;
    int lookBack=256;

    public static enum Type{
        TREND_UP,
        TREND_DOWN
    }
    private final Type type;

    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private LowestValueIndicator lowestValueIndicator;
    private HighestValueIndicator highestValueIndicator;

    Num[] ExtHighBuffer,ExtLowBuffer;


    public ZigZagFixedIndicator(TimeSeries series, Type type) {
        super(series);
        this.type=type;
        highPriceIndicator = new HighPriceIndicator(series);
        lowPriceIndicator = new LowPriceIndicator(series);
        lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, extDepth);
        highestValueIndicator = new HighestValueIndicator(highPriceIndicator, extDepth);
        values=calculate();
    }


    @Override
    public T getValue(int index) {
        return (T)values.get(index);
    }

    List<Num> calculate() {

        Num zero= DoubleNum.valueOf(0.0);
        int endIndex=getTimeSeries().getEndIndex();
        if (type==Type.TREND_DOWN) {
            ExtLowBuffer = new Num[endIndex];
            Arrays.fill(ExtLowBuffer, zero);
        }
        if (type==Type.TREND_UP) {
            ExtHighBuffer = new Num[endIndex];
            Arrays.fill(ExtHighBuffer, zero);
        }

        int    limit,counterZ,whatlookfor=0;
        int    back,pos,lasthighpos=0,lastlowpos=0;
        double extremum;
        double curlow=0.0,curhigh=0.0,lasthigh=0.0,lastlow=0.0;

        for (int i = extDepth+1; i < endIndex; i++) {
            if (type==Type.TREND_DOWN) {
                extremum = lowestValueIndicator.getValue(i).doubleValue();

                if (extremum == lastlow) extremum = 0.0;
                else {
                    //--- new last low
                    lastlow = extremum;
                    //--- discard extremum if current low is too high
                    if (lowPriceIndicator.getValue(i).doubleValue() - extremum > extDeviation)
                        extremum = 0.0;
                    else {
                        //--- clear previous extremums in backstep bars
                        for (back = i - 1; back >= i - extBackstep; back--) {
                            if (!ExtLowBuffer[back].isEqual(zero) &&
                                    ExtLowBuffer[back].isGreaterThan(DoubleNum.valueOf(extremum)))
                                ExtLowBuffer[back] = zero;
                        }
                    }
                }
                if (lowPriceIndicator.getValue(i).doubleValue() == extremum) {
                    ExtLowBuffer[i] = DoubleNum.valueOf(extremum);
                } else {
                    ExtLowBuffer[i] = zero;
                }
            }
            if (type==Type.TREND_UP) {
                extremum = highestValueIndicator.getValue(i).doubleValue();
                if (extremum == lasthigh) extremum = 0.0;
                else {
                    //--- new last low
                    lasthigh = extremum;
                    //--- discard extremum if current low is too high
                    if (extremum - highPriceIndicator.getValue(i).doubleValue() > extDeviation)
                        extremum = 0.0;
                    else {
                        //--- clear previous extremums in backstep bars
                        for (back = i - 1; back >= i - extBackstep; back--) {
                            if (!ExtHighBuffer[back].isEqual(zero) &&
                                    ExtHighBuffer[back].isLessThan(DoubleNum.valueOf(extremum)))
                                ExtHighBuffer[back] = zero;
                        }
                    }
                }
                if (highPriceIndicator.getValue(i).doubleValue() == extremum) {
                    ExtHighBuffer[i] = DoubleNum.valueOf(extremum);
                } else {
                    ExtHighBuffer[i] = zero;
                }
            }

        }

        if (type==Type.TREND_DOWN)  return Arrays.asList(ExtLowBuffer);
        if (type==Type.TREND_UP)  return Arrays.asList(ExtHighBuffer);

        return null;
    }

}
