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
package org.ta4j.core.indicators.helpers;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

/**
 * Sum indicator.
 * </p>
 * I.e.: operand0 + operand1 + ... + operandN
 */
public class ExtremeIndicator extends CachedIndicator<Num> {

    public static enum MethodType {
        MIN,
        MAX,
        AVG
    }

    private final Num refValue;
    private final MethodType methodType;
    private final Indicator<Num>[] operands;

    /**
     * Constructor.
     * (operand0 plus operand1 plus ... plus operandN)
     * @param refValue ennél az értéknel csak kisebb minimumot keressük
     * @param methodType MIN a kapott értékek közül a legkisebbet adja vissza, AVG az átlagát, MAX a legnagyobbat
     * @param operands indikátorok felsorolása amik közt keressük a legkisebb értéket
     *
     *
     */
    @SafeVarargs
    public ExtremeIndicator(Num refValue, MethodType methodType, Indicator<Num>... operands) {
        // TODO: check if first series is equal to the other ones
        super(operands[0]);
        this.operands = operands;

        if (refValue==null || refValue.equals(NaN.NaN)) this.refValue=numOf(Double.MAX_VALUE);
        else this.refValue=refValue;
        this.methodType=methodType;
    }

    @Override
    protected Num calculate(int index) {
        Num minValue=numOf(Double.MAX_VALUE);
        Num maxValue=numOf(Double.MIN_VALUE);
        Num value=NaN.NaN;
        Num avgSum=numOf(0.0);
        int counter=0;

        for (Indicator<Num> operand : operands) {
            if (operand.getValue(index).isLessThanOrEqual(refValue)) {
                if (methodType.equals(MethodType.MAX)) {
                    if (operand.getValue(index).isGreaterThanOrEqual(maxValue)) {
                        value=operand.getValue(index);
                        maxValue=value;
                    }
                } else if (methodType.equals(MethodType.MIN)) {
                    if (operand.getValue(index).isLessThanOrEqual(minValue)) {
                        value=operand.getValue(index);
                        minValue=value;
                    }
                } else if (methodType.equals(MethodType.AVG)) {
                    avgSum=avgSum.plus(operand.getValue(index));
                    counter++;
                }

            }
        }
        if (methodType.equals(MethodType.AVG)) value=avgSum.dividedBy(numOf(counter));
        return value;
    }
}
