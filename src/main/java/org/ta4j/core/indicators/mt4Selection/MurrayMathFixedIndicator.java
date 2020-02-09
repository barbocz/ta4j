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
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for price indicators
 * <p>
 * csak a 2-es (LOWER) és 10-es (UPPER) szinteket számolja
 * murrayPeriod --nak veszi a 64,128,256,512 értékeket
 * csak azokat a murrayPeriod-okat veszi figyelembe ahol a murray height>=38 és <=76
 * ha több period murray height-ja teljesülés akkor azok közül csak az marad ami legalább egy felsőbb period-dal egyezik
 * és igaz rá hogy az összes period 6-os szintje alatt van (LOWER) vagy felett (UPPER) esetén
 * ha ezek után is több marad akkor az alsóbb period élvez prioritást
 */
public class MurrayMathFixedIndicator extends CachedIndicator<Num> {


    private HashMap<Integer, HighestValueIndicator> highestIndicators = new HashMap<>();
    private HashMap<Integer, LowestValueIndicator> lowestIndicators = new HashMap<>();

    Set<Integer> murrayPeriods = new HashSet<>(Arrays.asList(64, 128, 256, 512));
    //    List<Integer> murrayPeriods = Arrays.asList(128);
    private final int level;
    private Num[] buffer;


    public MurrayMathFixedIndicator(TimeSeries series, int level) {
        super(series);
        this.level = level;
        buffer = new Num[13];
        for (Integer murrayPeriod : murrayPeriods) {
            highestIndicators.put(murrayPeriod, new HighestValueIndicator(new HighPriceIndicator(series), murrayPeriod));
            lowestIndicators.put(murrayPeriod, new LowestValueIndicator(new LowPriceIndicator(series), murrayPeriod));
        }


    }


    @Override
    protected Num calculate(int index) {

        if (index < 512) return NaN.NaN;

//        if (index==3785) {
//            System.out.println("break");
//        }
        Arrays.fill(buffer, NaN.NaN);
        for (Integer murrayPeriod : murrayPeriods) {
            preCalculate(lowestIndicators.get(murrayPeriod).getValue(index).doubleValue(), highestIndicators.get(murrayPeriod).getValue(index).doubleValue());
            if (!buffer[0].isEqual(NaN.NaN)) break;
        }

        return buffer[level];

    }

    void preCalculate(double lowestValue, double highestValue) {


        double mx, fractal = 0.0, x1 = 0.0, x2 = 0.0, x3 = 0.0, x4 = 0.0, x5 = 0.0, x6 = 0.0, y1 = 0.0, y2 = 0.0, y3 = 0.0, y4 = 0.0, y5 = 0.0, y6 = 0.0;
        if (highestValue <= 1.5625 && highestValue > 0.390625)
            fractal = 1.5625;
        else if (highestValue <= 0.390625 && highestValue > 0)
            fractal = 0.1953125;
        else if (highestValue <= 3.125 && highestValue > 1.5625)
            fractal = 3.125;
        else if (highestValue <= 6.25 && highestValue > 3.125)
            fractal = 6.25;
        else if (highestValue <= 250000 && highestValue > 25000)
            fractal = 100000;
        else if (highestValue <= 25000 && highestValue > 2500)
            fractal = 10000;
        else if (highestValue <= 2500 && highestValue > 250)
            fractal = 1000;
        else if (highestValue <= 250 && highestValue > 25)
            fractal = 100;
        else if (highestValue <= 25 && highestValue > 12.5)
            fractal = 12.5;
        else if (highestValue <= 12.5 && highestValue > 6.25)
            fractal = 12.5;

        double range = (highestValue - lowestValue);
        if (range == 0) return;


        double sum = Math.floor(Math.log(fractal / range) / Math.log(2));
        double octave = fractal * (Math.pow(0.5, sum));

        if (octave == 0) return;

        double mn = Math.floor(lowestValue / octave) * octave;
        if ((mn + octave) > highestValue) mx = mn + octave;
        else mx = mn + (2 * octave);

// calculating xx

        if ((lowestValue >= (3 * (mx - mn) / 16 + mn)) && (highestValue <= (9 * (mx - mn) / 16 + mn)))
            x2 = mn + (mx - mn) / 2;
        else x2 = 0;

        if ((lowestValue >= (mn - (mx - mn) / 8)) && (highestValue <= (5 * (mx - mn) / 8 + mn)) && (x2 == 0))
            x1 = mn + (mx - mn) / 2;
        else x1 = 0;

        if ((lowestValue >= (mn + 7 * (mx - mn) / 16)) && (highestValue <= (13 * (mx - mn) / 16 + mn)))
            x4 = mn + 3 * (mx - mn) / 4;
        else x4 = 0;

        if ((lowestValue >= (mn + 3 * (mx - mn) / 8)) && (highestValue <= (9 * (mx - mn) / 8 + mn)) && (x4 == 0))
            x5 = mx;
        else x5 = 0;


        if ((lowestValue >= (mn + (mx - mn) / 8)) && (highestValue <= (7 * (mx - mn) / 8 + mn)) && (x1 == 0) && (x2 == 0) && (x4 == 0) && (x5 == 0))
            x3 = mn + 3 * (mx - mn) / 4;
        else x3 = 0;

        if ((x1 + x2 + x3 + x4 + x5) == 0) x6 = mx;
        else x6 = 0;

        double finalH = x1 + x2 + x3 + x4 + x5 + x6;


// calculating yy
        if (x1 > 0) y1 = mn;
        else y1 = 0;

        if (x2 > 0) y2 = mn + (mx - mn) / 4;
        else y2 = 0;

        if (x3 > 0) y3 = mn + (mx - mn) / 4;
        else y3 = 0;

        if (x4 > 0) y4 = mn + (mx - mn) / 2;
        else y4 = 0;

        if (x5 > 0) y5 = mn + (mx - mn) / 2;
        else y5 = 0;

        if ((finalH > 0) && ((y1 + y2 + y3 + y4 + y5) == 0)) y6 = mn;
        else y6 = 0;


        double finalL = y1 + y2 + y3 + y4 + y5 + y6;

        double dmml = (finalH - finalL) / 8;

        if (dmml > 0.00038 && dmml < 0.00039 || dmml>0.00076 && dmml<0.00077) {
            buffer[0] = numOf(finalL - dmml * 2); //-2/8
            for (int i = 1; i < 13; i++) buffer[i] = buffer[i - 1].plus(numOf(dmml));
        }

    }


}
