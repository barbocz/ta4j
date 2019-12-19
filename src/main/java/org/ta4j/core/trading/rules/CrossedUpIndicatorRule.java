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
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.CrossIndicator;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Crossed-up indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} crosses-up the value of the second one.
 */
public class CrossedUpIndicatorRule extends AbstractRule {

    /** The cross indicator */
    private CrossIndicator cross;
    private int shift=0;
    private TimeSeriesRepo timeSeriesRepo=null;
    private int periodForFirst,periodForSecond;
    private final Indicator<Num> first;
    private final Indicator<Num> second;

    /**
     * Constructor.
     * @param indicator the indicator
     * @param threshold a threshold
     */
    public CrossedUpIndicatorRule(Indicator<Num> indicator, Number threshold) {
        this(indicator, indicator.numOf(threshold));
    }

    public CrossedUpIndicatorRule(Indicator<Num> indicator, Number threshold,int shift) {
        this(indicator, indicator.numOf(threshold),shift);
    }

    /**
     * Constructor.
     * @param indicator the indicator
     * @param threshold a threshold
     */
    public CrossedUpIndicatorRule(Indicator<Num> indicator, Num threshold) {
        this(indicator, new ConstantIndicator<Num>(indicator.getTimeSeries(),threshold));
    }


    /**
     * Constructor.
     * @param first the first indicator
     * @param second the second indicator
     */
    public CrossedUpIndicatorRule(Indicator<Num> first, Indicator<Num> second) {
        this.cross = new CrossIndicator(second, first);
        period=first.getTimeSeries().getPeriod();

        this.first=first;
        this.second=second;

        timeSeriesRepo=first.getTimeSeries().getTimeSeriesRepo();
        periodForFirst=first.getTimeSeries().getPeriod();
        periodForSecond=second.getTimeSeries().getPeriod();

    }

    /**
     * Constructor.
     * @param first the first indicator
     * @param second the second indicator
     * @shift
     */
    public CrossedUpIndicatorRule(Indicator<Num> first, Indicator<Num> second,int shift) {
        cross = new CrossIndicator(first, second);
        this.shift=shift;

        this.first=first;
        this.second=second;

        timeSeriesRepo=first.getTimeSeries().getTimeSeriesRepo();
        periodForFirst=first.getTimeSeries().getPeriod();
        periodForSecond=second.getTimeSeries().getPeriod();
    }

    public CrossedUpIndicatorRule(Indicator<Num> indicator, Num threshold,int shift) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(),threshold),shift);
    }



    @Override
    public boolean isSatisfied(int index) {

        boolean satisfied=false;
        if (shift==0 || index-shift<0) {
            satisfied = cross.getValue(index);
            //traceIsSatisfied(index, satisfied);
        } else {
            for (int i = 0; i < shift ; i++) {
                if (cross.getValue(index-i)) {
                    satisfied= true;
                    break;
                }
            }
        }

        getCore().debugRule(index,this,satisfied);
        return satisfied;
    }


    @Override
    public boolean isSatisfied(ZonedDateTime time) {
        // true if - a second értéke nagyobb mint a first, de az előző bar-on még nagyobb volt
        boolean satisfied = false;

        int indexForFirst = timeSeriesRepo.getIndex(time, periodForFirst);
        int indexForSecond = timeSeriesRepo.getIndex(time, periodForSecond);
        if (indexForFirst<0 || indexForSecond<0) return false;
        Num valueForSecond=second.getValue(indexForSecond);
        if (shift==0 && indexForFirst > 0) {

                if (first.getValue(indexForFirst).isGreaterThan(valueForSecond) &&
                        first.getValue(indexForFirst - 1).isLessThanOrEqual(valueForSecond)) satisfied = true;

        } else {
            for (int i = 0; i < shift ; i++) {   // shift mindig az első indikátor indexére vonatkozik
                if (indexForFirst-i>0) {
                    if (first.getValue(indexForFirst-i).isGreaterThan(valueForSecond) &&
                            first.getValue(indexForFirst - i - 1).isLessThanOrEqual(valueForSecond)) satisfied = true;
                }
            }

        }

        return satisfied;

    }

    /**
     * @return the initial lower indicator
     */
    public Indicator<Num> getLow() {
        return cross.getLow();
    }
    /**
     * @return the initial upper indicator
     */
    public Indicator<Num> getUp() {
        return cross.getUp();
    }

    @Override
    public String getParameters() {
        if (cross.getUp() instanceof ConstantIndicator) {
            ConstantIndicator constantIndicator=(ConstantIndicator)cross.getUp();
            return getClass().getSimpleName() + "(" + this.cross.getLow().getClass().getSimpleName() + "," + constantIndicator.getConstant() + "," + shift + ")";
        }
        return getClass().getSimpleName()+"("+this.cross.getLow().getClass().getSimpleName()+","+this.cross.getUp().getClass().getSimpleName()+","+shift+")";
    }



}
