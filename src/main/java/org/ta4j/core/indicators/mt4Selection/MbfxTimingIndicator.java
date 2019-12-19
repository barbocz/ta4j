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

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.util.Arrays;
import java.util.List;

/**
 * Base class for price indicators
 */
public class MbfxTimingIndicator extends CachedIndicator<Num> {

    private final int length;
    private final LowPriceIndicator lowPriceIndicator;
    private final HighPriceIndicator highPriceIndicator;
    private final ClosePriceIndicator closePriceIndicator;

    public MbfxTimingIndicator(TimeSeries series, int length) {
        super(series);
        this.length = length;

        lowPriceIndicator = new LowPriceIndicator(series);
        highPriceIndicator = new HighPriceIndicator(series);
        closePriceIndicator = new ClosePriceIndicator(series);
    }


    @Override
    protected Num calculate(int index) {

        double ld_0=0.0;
         double ld_8=0.0;
        double ld_16=0.0;
        double ld_24=0.0;
        double ld_32=0.0;
        double ld_40=0.0;
        double ld_48=0.0;
        double ld_56=0.0;
        double ld_64=0.0;
        double ld_72=0.0;
        double ld_80=0.0;
        double ld_88=0.0;
        double ld_96=0.0;
        double ld_104=0.0;
        double ld_112=0.0;
        double ld_120=0.0;
        double ld_128=0.0;
        double ld_136=0.0;
        double ld_144=0.0;
        double ld_152=0.0;
        double ld_160=0.0;
        double ld_168=0.0;
        double ld_176=0.0;
        double ld_184=0.0;
        double ld_192=0.0;
        double ld_200=0.0;
        double ld_208=0.0;

        for (int i = index - length; i <= index; i++) {
            if (ld_8 == 0.0) {
                ld_8 = 1.0;
                ld_16 = 0.0;
                if (length - 1 >= 5) ld_0 = length - 1.0;
                else ld_0 = 5.0;
                ld_80 = numOf(100.0).multipliedBy((highPriceIndicator.getValue(i).plus(lowPriceIndicator.getValue(i)).plus(closePriceIndicator.getValue(i))).dividedBy(numOf(3))).doubleValue();
                ld_96 = 3.0 / (length + 2.0);
                ld_104 = 1.0 - ld_96;
            } else {
                if (ld_0 <= ld_8) ld_8 = ld_0 + 1.0;
                else ld_8 += 1.0;
                ld_88 = ld_80;
                ld_80 = numOf(100.0).multipliedBy((highPriceIndicator.getValue(i).plus(lowPriceIndicator.getValue(i)).plus(closePriceIndicator.getValue(i))).dividedBy(numOf(3))).doubleValue();
                ld_32 = ld_80 - ld_88;
                ld_112 = ld_104 * ld_112 + ld_96 * ld_32;
                ld_120 = ld_96 * ld_112 + ld_104 * ld_120;
                ld_40 = 1.5 * ld_112 - ld_120 / 2.0;
                ld_128 = ld_104 * ld_128 + ld_96 * ld_40;
                ld_208 = ld_96 * ld_128 + ld_104 * ld_208;
                ld_48 = 1.5 * ld_128 - ld_208 / 2.0;
                ld_136 = ld_104 * ld_136 + ld_96 * ld_48;
                ld_152 = ld_96 * ld_136 + ld_104 * ld_152;
                ld_56 = 1.5 * ld_136 - ld_152 / 2.0;
                ld_160 = ld_104 * ld_160 + ld_96 * Math.abs(ld_32);
                ld_168 = ld_96 * ld_160 + ld_104 * ld_168;
                ld_64 = 1.5 * ld_160 - ld_168 / 2.0;
                ld_176 = ld_104 * ld_176 + ld_96 * ld_64;
                ld_184 = ld_96 * ld_176 + ld_104 * ld_184;
                ld_144 = 1.5 * ld_176 - ld_184 / 2.0;
                ld_192 = ld_104 * ld_192 + ld_96 * ld_144;
                ld_200 = ld_96 * ld_192 + ld_104 * ld_200;
                ld_72 = 1.5 * ld_192 - ld_200 / 2.0;
                if (ld_0 >= ld_8 && ld_80 != ld_88) ld_16 = 1.0;
                if (ld_0 == ld_8 && ld_16 == 0.0) ld_8 = 0.0;
            }
            if (ld_0 < ld_8 && ld_72 > 0.0000000001) {
                ld_24 = 50.0 * (ld_56 / ld_72 + 1.0);
                if (ld_24 > 100.0) ld_24 = 100.0;
                if (ld_24 < 0.0) ld_24 = 0.0;
            } else ld_24 = 50.0;
        }
        
        return numOf(ld_24);
        

    }


}
