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
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Indicator-between-indicators rule.
 * </p>
 * Satisfied when the value of the {@link Indicator indicator} is between the values of the boundary (up/down) indicators.
 */
public class InPipeRule extends AbstractRule {

    /** The upper indicator */
    private Indicator<Num> upper;
    /** The lower indicator */
    private Indicator<Num> lower;
    /** The evaluated indicator */
    private Indicator<Num> ref;

    private TimeSeriesRepo timeSeriesRepo=null;
    private int periodForUpper,periodForLower;
    /**
     * Constructor.
     * @param ref the reference indicator
     * @param upper the upper threshold
     * @param lower the lower threshold
     */
    public InPipeRule(Indicator<Num> ref, Number upper, Number lower) {
        this(ref, ref.numOf(upper), ref.numOf(lower));
    }

    /**
     * Constructor.
     * @param ref the reference indicator
     * @param upper the upper threshold
     * @param lower the lower threshold
     */
    public InPipeRule(Indicator<Num> ref, Num upper, Num lower) {
        this(ref, new ConstantIndicator<>(ref.getTimeSeries(), upper), new ConstantIndicator<>(ref.getTimeSeries(), lower));
    }

    /**
     * Constructor.
     * @param ref the reference indicator
     * @param upper the upper indicator
     * @param lower the lower indicator
     */
    public InPipeRule(Indicator<Num> ref, Indicator<Num> upper, Indicator<Num> lower) {
        this.upper = upper;
        this.lower = lower;
        this.ref = ref;
        timeSeriesRepo=ref.getTimeSeries().getTimeSeriesRepo();
        period=ref.getTimeSeries().getPeriod();
        periodForUpper=upper.getTimeSeries().getPeriod();
        periodForLower=lower.getTimeSeries().getPeriod();
    }

    @Override
    public boolean isSatisfied(int index) {
        int indexForPeriod=getCore().getIndex(index,period);
        final boolean satisfied = ref.getValue(indexForPeriod).isLessThanOrEqual(upper.getValue(indexForPeriod))
                && ref.getValue(indexForPeriod).isGreaterThanOrEqual(lower.getValue(indexForPeriod));
        //traceIsSatisfied(index, satisfied);
        getCore().debugRule(index,this,satisfied);
        return satisfied;
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {
        int indexForUpper = timeSeriesRepo.getIndex(time, periodForUpper);
        int indexForLower = timeSeriesRepo.getIndex(time, periodForLower);
        if (indexForUpper<0 || indexForLower<0) return false;
        int indexForRef= timeSeriesRepo.getIndex(time, period);


        final boolean satisfied = ref.getValue(indexForRef).isLessThanOrEqual(upper.getValue(indexForUpper))
                && ref.getValue(indexForRef).isGreaterThanOrEqual(lower.getValue(indexForLower));
        //traceIsSatisfied(index, satisfied);
        getCore().debugRule(indexForRef,this,satisfied);
        return satisfied;
    }
}
