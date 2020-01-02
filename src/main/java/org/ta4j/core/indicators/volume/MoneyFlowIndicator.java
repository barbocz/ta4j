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
package org.ta4j.core.indicators.volume;


import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;

/**
 * Chaikin Money Flow (CMF) indicator.
 * </p>
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chaikin_money_flow_cmf">
 *     http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:chaikin_money_flow_cmf"</a>
 * @see <a href="http://www.fmlabs.com/reference/default.htm?url=ChaikinMoneyFlow.htm">
 *     http://www.fmlabs.com/reference/default.htm?url=ChaikinMoneyFlow.htm</a>
 */
public class MoneyFlowIndicator extends CachedIndicator<Num> {

    private CloseLocationValueIndicator clvIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private AmountIndicator amountIndicator;

    private VolumeIndicator volumeIndicator;

    private int barCount;

    public MoneyFlowIndicator(TimeSeries series, int barCount) {
        super(series);
        this.barCount = barCount;
//        this.clvIndicator = new CloseLocationValueIndicator(series);
//        this.volumeIndicator = new VolumeIndicator(series, barCount);

        this.highPriceIndicator=new HighPriceIndicator(series);
        this.lowPriceIndicator=new LowPriceIndicator(series);
        this.closePriceIndicator=new ClosePriceIndicator(series);
        this.amountIndicator=new AmountIndicator(series);


    }

    @Override
    protected Num calculate(int index) {

        int end = Math.max(0, index - barCount);
        Num dPreviousTP;
        Num dPositiveMF=numOf(0.0);
        Num dNegativeMF=numOf(0.0);
        Num dCurrentTP=highPriceIndicator.getValue(index).plus(lowPriceIndicator.getValue(index).plus(closePriceIndicator.getValue(index))).dividedBy(numOf(3.0));

        for (int i = index - 1; i >= end; i--) {

            dPreviousTP=highPriceIndicator.getValue(i).plus(lowPriceIndicator.getValue(i).plus(closePriceIndicator.getValue(i))).dividedBy(numOf(3.0));
            if (dCurrentTP.isGreaterThan(dPreviousTP)) dPositiveMF=dPositiveMF.plus(amountIndicator.getValue(i).multipliedBy(dCurrentTP));
            else if (dCurrentTP.isLessThan(dPreviousTP)) dNegativeMF=dNegativeMF.plus(amountIndicator.getValue(i).multipliedBy(dCurrentTP));
            dCurrentTP=dPreviousTP;
        }

        if (!dNegativeMF.isEqual(numOf(0.0))) return numOf(100.0).minus(numOf(100.0).dividedBy(numOf(1.0).plus(dPositiveMF).dividedBy(dNegativeMF)) );
        else return numOf(100.0);


//        int startIndex = Math.max(0, index - barCount + 1);
//        Num sumOfMoneyFlowVolume = numOf(0);
//        for (int i = startIndex; i <= index; i++) {
//            sumOfMoneyFlowVolume = sumOfMoneyFlowVolume.plus(getMoneyFlowVolume(i));
//        }
//        Num sumOfVolume = volumeIndicator.getValue(index);
//
//        return sumOfMoneyFlowVolume.dividedBy(sumOfVolume);
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
