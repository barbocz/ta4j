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
package org.ta4j.core.trading.rules.helpers;

import org.strategy.Order;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.AbstractRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import java.time.ZonedDateTime;

/**
 * Indicator-under-indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} is strictly lesser than the value of the second one.
 */
public class MurrayHighLevelChangeRule extends AbstractRule {


    private final Indicator<Num> murrayMathIndicator;
    private final PreviousValueIndicator previousValueIndicator;
    private final TimeSeriesRepo timeSeriesRepo;
    private final int timeFrame;
    private final HighPriceIndicator highPriceIndicator;
    private final int shift;


    public MurrayHighLevelChangeRule(TimeSeries series, int murrayPeriod,int shift) {
        murrayMathIndicator= new MurrayMathIndicator(series,murrayPeriod,12);
        this.shift=shift;
        timeSeriesRepo=series.getTimeSeriesRepo();
        timeFrame=series.getPeriod();

        previousValueIndicator = new PreviousValueIndicator(murrayMathIndicator, 1);

        highPriceIndicator = new HighPriceIndicator(series);

    }



    @Override
    public boolean isSatisfied (ZonedDateTime time) {

        int index=timeSeriesRepo.getIndex(time,timeFrame);

        for (int i = Math.max(0, index - shift + 1); i <= index; i++) {
            if (highPriceIndicator.getValue(i).isGreaterThan(previousValueIndicator.getValue(i))) {
                tradeEngine.logStrategy.logRule(this, time, index, true);
                return true;
            }
        }
        return false;

    }

    @Override
    public String getParameters() {
        String info=getClass().getSimpleName();
        info+=" (MurrayLevelChangeRule M"+timeFrame;
        return info;
    }
}
