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
 * Indicator-equal-indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} is equal to the value of the second one.
 */
public class IsEqualRule extends AbstractRule {

    /**
     * The first indicator
     */
    private final Indicator<Num> first;
    /**
     * The second indicator
     */
    private final Indicator<Num> second;
    private int shift=0;

    private double satisfactionRatio=0.0;
    private TimeSeriesRepo timeSeriesRepo=null;
    private int periodForFirst,periodForSecond;

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param value     the value to check
     */
    public IsEqualRule(Indicator<Num> indicator, Number value,int shift) {
        this(indicator, indicator.numOf(value),shift);

    }

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param value     the value to check
     */
    public IsEqualRule(Indicator<Num> indicator, Num value,int shift) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(), value));
        this.shift=shift;
        periodForFirst=indicator.getTimeSeries().getPeriod();
        timeSeriesRepo=indicator.getTimeSeries().getTimeSeriesRepo();
    }

    public IsEqualRule(Indicator<Num> indicator, Num value,int shift,double satisfactionRatio) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(), value));
        this.shift=shift;
        this.satisfactionRatio=satisfactionRatio;
        periodForFirst=indicator.getTimeSeries().getPeriod();
        timeSeriesRepo=indicator.getTimeSeries().getTimeSeriesRepo();

    }

    /**
     * Constructor.
     *
     * @param first  the first indicator
     * @param second the second indicator
     */
    public IsEqualRule(Indicator<Num> first, Indicator<Num> second) {
        this.first = first;
        this.second = second;
        periodForFirst=first.getTimeSeries().getPeriod();
        periodForSecond=second.getTimeSeries().getPeriod();
        timeSeriesRepo=first.getTimeSeries().getTimeSeriesRepo();
    }

    @Override
    public boolean isSatisfied(int index) {
//        final boolean satisfied = first.getValue(index).isEqual(second.getValue(index));
        //traceIsSatisfied(index, satisfied);

        int indexForPeriod=getCore().getIndex(index,periodForFirst);
        boolean satisfied=false;
        if (shift==0 || indexForPeriod-shift<0) {
            satisfied= first.getValue(indexForPeriod).isEqual(second.getValue(indexForPeriod));
            //traceIsSatisfied(index, satisfied);
        } else {
            int count = 0;
            for (int i = 0; i < shift ; i++) {
                if (first.getValue(indexForPeriod-i).isEqual(second.getValue(indexForPeriod-i))) {
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
//        final boolean satisfied = first.getValue(index).isEqual(second.getValue(index));
        //traceIsSatisfied(index, satisfied);


        boolean satisfied=false;
        int indexForFirst=timeSeriesRepo.getIndex(time,periodForFirst);
        int indexForSecond=timeSeriesRepo.getIndex(time,periodForSecond);
        if (indexForFirst<0 || indexForSecond<0) return false;
        Num valueForSecond=second.getValue(indexForSecond);

        if (shift==0) {
            satisfied= first.getValue(indexForFirst).isEqual(valueForSecond);
            //traceIsSatisfied(index, satisfied);
        } else {
            int count = 0;
            for (int i = 0; i < shift ; i++) {
                if (indexForFirst-i>-1) {
                    if (first.getValue(indexForFirst - i).isEqual(valueForSecond)) {
                        if (satisfactionRatio == 0.0) {
                            satisfied = true;
                            break;
                        } else {
                            count++;
                        }
                    }
                }
            }

            if (satisfactionRatio>0.0) satisfied = (count / (double) shift) >= satisfactionRatio;
        }

        getCore().debugRule(indexForFirst,this,satisfied);
        return satisfied;



    }


    @Override
    public String getParameters() {
        return getClass().getSimpleName()+"("+first.getClass().getSimpleName()+","+second.getClass().getSimpleName()+")";
    }
}
