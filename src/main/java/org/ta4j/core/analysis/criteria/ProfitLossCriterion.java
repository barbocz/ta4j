/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization & respective
 * authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core.analysis.criteria;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

/**
 * Profit and loss criterion.
 * </p>
 * The profit or loss over the provided {@link TimeSeries series}.
 */
public class ProfitLossCriterion extends AbstractAnalysisCriterion {

    @Override
    public Num calculate(TimeSeries series, TradingRecord tradingRecord) {
        return tradingRecord.getTrades().stream()
                .filter(Trade::isClosed)
                .map(trade -> calculate(series, trade))
                .reduce(series.numOf(0), Num::plus);
    }

    /**
     * Calculates the profit or loss on the trade.
     *
     * @param series a time series
     * @param trade  a trade
     * @return the profit or loss on the trade
     */
    @Override
    public Num calculate(TimeSeries series, Trade trade) {
        if (trade.isClosed()) {
            Num exitClosePrice = trade.getExit().getNetPrice().isNaN() ?
                    series.getBar(trade.getExit().getIndex()).getClosePrice() : trade.getExit().getNetPrice();
            Num entryClosePrice = trade.getEntry().getNetPrice().isNaN() ?
                    series.getBar(trade.getEntry().getIndex()).getClosePrice() : trade.getEntry().getNetPrice();
            // én javításom!!!
            if (trade.getEntry().isBuy()) {
                return exitClosePrice.minus(entryClosePrice).multipliedBy(trade.getExit().getAmount());
            } else {
                return entryClosePrice.minus(exitClosePrice).multipliedBy(trade.getExit().getAmount());
            }
        }
        return series.numOf(0);
    }

    @Override
    public boolean betterThan(Num criterionValue1, Num criterionValue2) {
        return criterionValue1.isGreaterThan(criterionValue2);
    }
}