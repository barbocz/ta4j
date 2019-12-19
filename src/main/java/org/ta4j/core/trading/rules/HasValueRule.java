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
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Indicator-equal-indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} is equal to the value of the second one.
 */
public class HasValueRule extends AbstractRule {

    /**
     * The first indicator
     */
    private final Indicator<Num> ref;
    /**
     * The second indicator
     */
    private int shift=0;

    private double satisfactionRatio=0.0;
    private TimeSeriesRepo timeSeriesRepo=null;


    public HasValueRule(Indicator<Num> indicator,  int shift) {
        this(indicator,shift,0.0);
    }

    public HasValueRule(Indicator<Num> indicator,  int shift, double satisfactionRatio) {
        this.ref=indicator;
        this.shift=shift;
        this.satisfactionRatio=satisfactionRatio;
        timeSeriesRepo=ref.getTimeSeries().getTimeSeriesRepo();
        period=ref.getTimeSeries().getPeriod();
    }


    @Override
    public boolean isSatisfied(int index) {
//        final boolean satisfied = first.getValue(index).isEqual(second.getValue(index));
        //traceIsSatisfied(index, satisfied);

        int indexForPeriod=getCore().getIndex(index,period);
        boolean satisfied=false;
        if (shift==0 || indexForPeriod-shift<0) {
            satisfied= !ref.getValue(indexForPeriod).isEqual(NaN.NaN);
            //traceIsSatisfied(index, satisfied);
        } else {
            int count = 0;
            for (int i = 0; i < shift ; i++) {
                if (!ref.getValue(indexForPeriod-i).isEqual(NaN.NaN)) {
                    if (satisfactionRatio==0.0) {
                        satisfied = true;
                        break;
                    } else {
                        count++;
                    }
                }
            }

            if (satisfactionRatio>0.0) satisfied = (count / (double) shift) >= satisfactionRatio;
        }

        getCore().debugRule(index,this,satisfied);
        return satisfied;

    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {

        boolean satisfied = false;

        int indexForRef = timeSeriesRepo.getIndex(time, period);
        if (indexForRef<0) return false;
        if (shift==0 && indexForRef>-1) {
            satisfied= !ref.getValue(indexForRef).isEqual(NaN.NaN);
            //traceIsSatisfied(index, satisfied);
        } else {
            int count = 0;
            for (int i = 0; i < shift ; i++) {
                if (!ref.getValue(indexForRef-i).isEqual(NaN.NaN)) {
                    if (satisfactionRatio==0.0) {
                        satisfied = true;
                        break;
                    } else {
                        count++;
                    }
                }
            }

            if (satisfactionRatio>0.0) satisfied = (count / (double) shift) >= satisfactionRatio;
        }

        getCore().debugRule(indexForRef,this,satisfied);

        return  satisfied;
    }

    @Override
    public String getParameters() {
        return getClass().getSimpleName()+"("+ref.getClass().getSimpleName()+")";
    }
}
