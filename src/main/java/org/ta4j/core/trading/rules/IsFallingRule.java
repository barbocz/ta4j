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
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Indicator-falling-indicator rule.
 * </p>
 * Satisfied when the values of the {@link Indicator indicator} decrease
 * within the barCount.
 */
public class IsFallingRule extends AbstractRule {

    /**
     * The actual indicator
     */
    private final Indicator<Num> ref;
    /**
     * The barCount
     */
    private final int barCount;
    /**
     * The minimum required strenght of the falling
     */
    private double minStrenght;
    private TimeSeriesRepo timeSeriesRepo = null;
    private boolean considerEqualAsFalling = false;


    /**
     * Constructor.
     *
     * @param ref      the indicator
     * @param barCount the time frame
     */
    public IsFallingRule(Indicator<Num> ref, int barCount) {
        this(ref, barCount, 0.99);
    }

    public IsFallingRule(Indicator<Num> ref, boolean considerEqualAsFalling) {
        this(ref, 1, 0.99);
        this.considerEqualAsFalling = considerEqualAsFalling;
    }


    /**
     * Constructor.
     *
     * @param ref         the indicator
     * @param barCount    the time frame
     * @param minStrenght the minimum required falling strength (between '0' and '1', e.g. '1' for strict falling)
     */
    public IsFallingRule(Indicator<Num> ref, int barCount, double minStrenght) {
        this.ref = ref;
        period = ref.getTimeSeries().getPeriod();
        timeSeriesRepo = ref.getTimeSeries().getTimeSeriesRepo();
        this.barCount = barCount;
        this.minStrenght = minStrenght;
    }

    @Override
    public boolean isSatisfied(int index) {
        int indexForPeriod = getCore().getIndex(index, period);
        if (minStrenght >= 1) {
            minStrenght = 0.99;
        }

        int count = 0;


        for (int i = Math.max(0, indexForPeriod - barCount + 1); i <= indexForPeriod; i++) {
            if (considerEqualAsFalling) {
                if (ref.getValue(i).isLessThanOrEqual(ref.getValue(Math.max(0, i - 1)))) count += 1;
            } else if (ref.getValue(i).isLessThan(ref.getValue(Math.max(0, i - 1)))) count += 1;

        }

        double ratio = count / (double) barCount;

        final boolean satisfied = ratio >= minStrenght;
        //traceIsSatisfied(index, satisfied);
        getCore().debugRule(index, this, satisfied);
        return satisfied;
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {
        int index = timeSeriesRepo.getIndex(time, period);
        if (index < 0) return false;


        if (minStrenght >= 1) {
            minStrenght = 0.99;
        }

        int count = 0;


        for (int i = Math.max(0, index - barCount + 1); i <= index; i++) {
            if (considerEqualAsFalling) {
                if (ref.getValue(i).isLessThanOrEqual(ref.getValue(Math.max(0, i - 1)))) count += 1;
            } else if (ref.getValue(i).isLessThan(ref.getValue(Math.max(0, i - 1)))) count += 1;
        }

        double ratio = count / (double) barCount;

        final boolean satisfied = ratio >= minStrenght;
        //traceIsSatisfied(index, satisfied);
//		getCore().debugRule(index,this,satisfied);
        if (satisfied && isLogNeeded) tradeEngine.logStrategy.logRule(this, time, index, satisfied);
        return satisfied;
    }

    @Override
    public String getParameters() {
        String info = getClass().getSimpleName();
        info += " (" + ref.getClass().getSimpleName() + " M" + ref.getTimeSeries().getPeriod() + ")";
        if (barCount > 0) info += " in " + barCount;
        return info;
    }
}
