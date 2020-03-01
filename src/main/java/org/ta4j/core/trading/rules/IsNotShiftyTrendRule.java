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
package org.ta4j.core.trading.rules;

import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.indicators.mt4Selection.ZoloIndicator;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Indicator-lowest-indicator rule.
 * </p>
 * Satisfied when the value of the {@link Indicator indicator} is the lowest
 * within the barCount.
 */
public class IsNotShiftyTrendRule extends AbstractRule {


    //    MurrayMathIndicator murrayMathIndicator;

    TimeSeriesRepo timeSeriesRepo;

    private final TimeSeries series;
    private final int period;
    private final TrendType trendType;
    ClosePriceIndicator closePriceIndicator;
    OpenPriceIndicator openPriceIndicator;

    public enum TrendType {
        UP,  //
        DOWN  //
    }


    public IsNotShiftyTrendRule(TimeSeries series, TrendType trendType) {
        this.series = series;
        this.trendType = trendType;

        period = series.getPeriod();
        timeSeriesRepo = series.getTimeSeriesRepo();
//        if (reboundType== ReboundType.UP) murrayMathIndicator = new MurrayMathIndicator(series, 128, 2);
//        else murrayMathIndicator = new MurrayMathIndicator(series, 128, 10);
        closePriceIndicator = new ClosePriceIndicator(series);
        openPriceIndicator = new OpenPriceIndicator(series);
    }


    private void init() {

    }

    @Override
    public boolean isSatisfied(int index) {
        return false;
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {
        int index = timeSeriesRepo.getIndex(time, period);
        if (index < 11) return false;

        boolean satisfied = false;
        int trendBarCounter=0;

        for (int i = index-1; i > index-11 ; i--) {
            if (trendType==TrendType.UP) {
               if (closePriceIndicator.getValue(i).isGreaterThan(openPriceIndicator.getValue(i))) trendBarCounter++;
            } else {
                if (openPriceIndicator.getValue(i).isGreaterThan(closePriceIndicator.getValue(i))) trendBarCounter++;
            }
            if (trendBarCounter>2) {
                satisfied=true;
                break;
            }
        }


        if (satisfied && isLogNeeded) tradeEngine.logStrategy.logRule(this, time, index, satisfied);

        return satisfied;

    }
}
