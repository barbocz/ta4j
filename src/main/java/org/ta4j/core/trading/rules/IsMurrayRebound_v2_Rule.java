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
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.LaguerreIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndicator;

import java.time.ZonedDateTime;

/**
 * Indicator-lowest-indicator rule.
 * </p>
 * Satisfied when the value of the {@link Indicator indicator} is the lowest
 * within the barCount.
 */
public class IsMurrayRebound_v2_Rule extends AbstractRule {

    //    MurrayMathIndicator murrayMathIndicator;

    TimeSeriesRepo timeSeriesRepo;
    LaguerreIndicator laguerreIndicator;
    MoneyFlowIndicator moneyFlowIndicator;
    HighestValueIndicator highestMurrayChange, highestLaguerre, highestMoneyFlow;
    LowestValueIndicator lowestMurrayChange, lowestLaguerre, lowestMoneyFlow;

    private MurrayChangeIndicator murrayChangeIndicator;

    private final TimeSeries series;
    private final int period;
    private final ReboundType reboundType;

    public enum ReboundType {
        UP,  // a Murray szintről felpattanás várható
        DOWN  // a Murray szintről lepattanás várható
    }


    public IsMurrayRebound_v2_Rule(TimeSeries series, ReboundType reboundType, double murrayRange) {
        this.series = series;
        this.reboundType = reboundType;

        period = series.getPeriod();
        timeSeriesRepo = series.getTimeSeriesRepo();
        laguerreIndicator = new LaguerreIndicator(series, 0.2);
        moneyFlowIndicator = new MoneyFlowIndicator(series, 5);

        murrayChangeIndicator = new MurrayChangeIndicator(series, murrayRange);
        highestMurrayChange = new HighestValueIndicator(murrayChangeIndicator, 4);
        lowestMurrayChange = new LowestValueIndicator(murrayChangeIndicator, 4);
        highestLaguerre = new HighestValueIndicator(laguerreIndicator, 3);
        lowestLaguerre = new LowestValueIndicator(laguerreIndicator, 3);
        highestMoneyFlow = new HighestValueIndicator(moneyFlowIndicator, 3);
        lowestMoneyFlow = new LowestValueIndicator(moneyFlowIndicator, 3);

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
        if (index < 257) return false;
//        if (index>4329) {
//            System.out.println("break");
//        }
        boolean satisfied = false;

        if (reboundType == ReboundType.UP) {
            if (highestMurrayChange.getValue(index).doubleValue() > 3.0) {
                satisfied = (laguerreIndicator.getValue(index).doubleValue() > laguerreIndicator.getValue(index - 1).doubleValue() ||
                        moneyFlowIndicator.getValue(index).doubleValue() > moneyFlowIndicator.getValue(index - 1).doubleValue()) &&
                        lowestLaguerre.getValue(index).doubleValue() < 0.1 && lowestMoneyFlow.getValue(index).doubleValue() < 15.0 ;
            }
        } else {
            if (lowestMurrayChange.getValue(index).doubleValue() < -3.0) {
                satisfied = (laguerreIndicator.getValue(index).doubleValue() < laguerreIndicator.getValue(index - 1).doubleValue() ||
                        moneyFlowIndicator.getValue(index).doubleValue() < moneyFlowIndicator.getValue(index - 1).doubleValue()) &&
                        highestLaguerre.getValue(index).doubleValue() > 0.9 && highestMoneyFlow.getValue(index).doubleValue() > 85.0  ;
            }
        }

        if (satisfied && isLogNeeded) tradeEngine.logStrategy.logRule(this, time, index, satisfied);

        return satisfied;

    }
}
