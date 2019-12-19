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
import org.ta4j.core.Order;
import org.ta4j.core.TradingRecord;

import java.time.ZonedDateTime;

/**
 * A boolean-indicator-based rule.
 * </p>
 * Satisfied when the value of the {@link Indicator indicator} is true.
 */
public class BooleanIndicatorRule extends AbstractRule {

    private final Indicator<Boolean> indicator;
    private TimeSeriesRepo timeSeriesRepo=null;


    /**
     * Constructor.
     *
     * @param indicator a boolean indicator
     */
    public BooleanIndicatorRule(Indicator<Boolean> indicator) {
        this.indicator = indicator;
        timeSeriesRepo=indicator.getTimeSeries().getTimeSeriesRepo();
        period=indicator.getTimeSeries().getPeriod();
    }



    @Override
    public boolean isSatisfied(int index) {

        final boolean satisfied = indicator.getValue(index);
        //traceIsSatisfied(index, satisfied);
        getCore().debugRule(index,this,satisfied);
        return satisfied;
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {
        int index=timeSeriesRepo.getIndex(time,period);
        if (index<0) return false;
        final boolean satisfied = indicator.getValue(index);
        //traceIsSatisfied(index, satisfied);
        getCore().debugRule(timeSeriesRepo.getIndex(time,period),this,satisfied);
        return satisfied;
    }
}
