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
import org.ta4j.core.num.Num;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Indicator-over-indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} is strictly greater than the value of the second one.
 */
public class OverIndicatorRule extends AbstractRule {

    /**
     * The first indicator
     */
    private final Indicator<Num> first;
    /**
     * The second indicator
     */
    private final Indicator<Num> second;
    private final int shift;
    private double satisfactionRatio=0.0;
    private TimeSeriesRepo timeSeriesRepo=null;
    private int periodForFirst,periodForSecond;
    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold a threshold
     */
    public OverIndicatorRule(Indicator<Num> indicator, Number threshold) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(), indicator.numOf(threshold)));

    }

    public OverIndicatorRule(Indicator<Num> indicator, Number threshold,int shift) {
        this(indicator, new ConstantIndicator<>(indicator.getTimeSeries(), indicator.numOf(threshold)),shift);
    }

    /**
     * Constructor.
     *
     * @param indicator the indicator
     * @param threshold a threshold
     */
    public OverIndicatorRule(Indicator<Num> indicator, Num threshold) {
        this(indicator, new ConstantIndicator<Num>(indicator.getTimeSeries(), threshold));

    }

    /**
     * Constructor.
     *
     * @param first  the first indicator
     * @param second the second indicator
     */
    public OverIndicatorRule(Indicator<Num> first, Indicator<Num> second) {
        this(first,second,0,0);

    }


    public OverIndicatorRule(Indicator<Num> first, Indicator<Num> second,int shift) {
        this(first,second,shift,0);
    }

    public OverIndicatorRule(Indicator<Num> first, Indicator<Num> second,int shift,double satisfactionRatio) {
        this.first = first;
        this.second = second;
        this.shift=shift;
        this.satisfactionRatio=satisfactionRatio;
        timeSeriesRepo=first.getTimeSeries().getTimeSeriesRepo();
        periodForFirst=first.getTimeSeries().getPeriod();
        periodForSecond=second.getTimeSeries().getPeriod();
    }

    @Override
    public boolean isSatisfied(int index) {
//        int indexForFirst=timeSeriesRepo.barIndex.get(periodForFirst);
//        int indexForSecond=timeSeriesRepo.barIndex.get(periodForSecond);
        int indexForFirst=index;
        int indexForSecond=index;
//        System.out.println(first.getValue(indexForFirst)+" - "+second.getValue(indexForSecond));

        boolean satisfied=false;
        if (shift==0) {
            satisfied = satisfied = first.getValue(indexForFirst).isGreaterThan(second.getValue(indexForSecond));
            //traceIsSatisfied(index, satisfied);
        } else {
            int count = 0;
            Num valueForSecond=second.getValue(indexForSecond);
            for (int i = 0; i < shift ; i++) {
                if (first.getValue(indexForFirst-i).isGreaterThan(valueForSecond)) {
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
        boolean satisfied=false;
        boolean sameTimeFrame=false;


        int indexForFirst=timeSeriesRepo.getIndex(time,periodForFirst);
        int indexForSecond=timeSeriesRepo.getIndex(time,periodForSecond);
        if (indexForFirst==indexForSecond) sameTimeFrame=true;

        if (indexForFirst>-1 && indexForSecond>-1) {
            Num valueForSecond = second.getValue(indexForSecond);
//        System.out.println(first.getValue(indexForFirst)+" - "+second.getValue(indexForSecond));

            if (shift == 0) {
                satisfied = first.getValue(indexForFirst).isGreaterThan(valueForSecond);

                //traceIsSatisfied(index, satisfied);
            } else {
                int count = 0;

                for (int i = 0; i < shift; i++) {   // shift mindig az első indikátor indexére vonatkozik

                    if (indexForFirst - i > -1) {

                        if (sameTimeFrame) valueForSecond=second.getValue(indexForSecond - i);

                        if (first.getValue(indexForFirst - i).isGreaterThan(valueForSecond)) {
                            if (satisfactionRatio == 0.0) {
                                satisfied = true;
                                break;
                            } else {
                                count++;
                            }
                        }
                    }
                }
                if (satisfactionRatio > 0.0) satisfied = (count / (double) shift) >= satisfactionRatio;
            }
        }
//        getCore().debugRule(indexForFirst,this,satisfied);
        if (satisfied && isLogNeeded)  tradeEngine.logStrategy.logRule(this,time,indexForFirst,satisfied);

        return satisfied;
    }

    @Override
    public String getParameters() {
        String info=getClass().getSimpleName();
        info+=" ("+first.getClass().getSimpleName()+" M"+first.getTimeSeries().getPeriod();
        if (second!=null) info+=", "+second.getClass().getSimpleName()+" M"+second.getTimeSeries().getPeriod()+")";
        else info+=")";
        if (shift>0) info+=" in "+shift;
        return info;
    }
}
