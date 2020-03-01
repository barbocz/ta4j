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
import org.strategy.TradeEngine;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

/**
 * Indicator-equal-indicator rule.
 * </p>
 * Satisfied when the value of the first {@link Indicator indicator} is equal to the value of the second one.
 */
public class IsRebounceCandleRule extends AbstractRule {

    public enum ReboundType {
        UP,  // a Murray szintről felpattanás várható
        DOWN  // a Murray szintről lepattanás várható
    }

    private ClosePriceIndicator closePriceIndicator;
    private OpenPriceIndicator openPriceIndicator;
    private final ReboundType reboundType;



    public IsRebounceCandleRule(TradeEngine tradeEngine,ReboundType reboundType) {

        this.reboundType=reboundType;
        closePriceIndicator=new ClosePriceIndicator(tradeEngine.series);
        openPriceIndicator=new OpenPriceIndicator(tradeEngine.series);
        period=tradeEngine.series.getPeriod();

    }


    @Override
    public boolean isSatisfied(int index) {
        return false;
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time) {
//        final boolean satisfied = first.getValue(index).isEqual(second.getValue(index));
        //traceIsSatisfied(index, satisfied);
        boolean satisfied=false;
        int index = tradeEngine.timeSeriesRepo.getIndex(time, period);

        if (reboundType == ReboundType.UP) {
            if (openPriceIndicator.getValue(index-1).doubleValue()>closePriceIndicator.getValue(index-1).doubleValue() &&
                    openPriceIndicator.getValue(index).doubleValue()<=closePriceIndicator.getValue(index).doubleValue() &&
                    Math.abs(openPriceIndicator.getValue(index-1).doubleValue()-closePriceIndicator.getValue(index-1).doubleValue())/
                    Math.abs(openPriceIndicator.getValue(index).doubleValue()-closePriceIndicator.getValue(index).doubleValue())>3.0)
             satisfied=true;
        } else {
            if (openPriceIndicator.getValue(index-1).doubleValue()<closePriceIndicator.getValue(index-1).doubleValue() &&
                    openPriceIndicator.getValue(index).doubleValue()>=closePriceIndicator.getValue(index).doubleValue() &&
                    Math.abs(openPriceIndicator.getValue(index-1).doubleValue()-closePriceIndicator.getValue(index-1).doubleValue())/
                            Math.abs(openPriceIndicator.getValue(index).doubleValue()-closePriceIndicator.getValue(index).doubleValue())>3.0)
                satisfied=true;
        }

        if (satisfied && isLogNeeded)  tradeEngine.logStrategy.logRule(this,time,index,satisfied);
        return satisfied;



    }


    @Override
    public String getParameters() {
        String info=getClass().getSimpleName()+" "+reboundType;
        return info;
    }
}
