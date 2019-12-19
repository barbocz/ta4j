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
package org.ta4j.core.extendedCore;

import org.ta4j.core.*;
import org.ta4j.core.Order.OrderType;
import org.ta4j.core.cost.CostModel;
import org.ta4j.core.cost.ZeroCostModel;

/**
 * A manager for {@link TimeSeries} objects.
 * </p>
 * Used for backtesting.
 * Allows to run a {@link Strategy trading strategy} over the managed time series.
 */
public class TimeSeriesManagerExtended {

    /**
     * The logger
     */
//    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The managed time series
     */
    private TimeSeries timeSeries;

    /**
     * The trading cost models
     */
    private CostModel transactionCostModel;
    private CostModel holdingCostModel;



    /**
     * Constructor.
     */
    public TimeSeriesManagerExtended() {
        this(null, new ZeroCostModel(), new ZeroCostModel());
    }

    /**
     * Constructor.
     *
     * @param timeSeries the time series to be managed
     */
    public TimeSeriesManagerExtended(TimeSeries timeSeries) {
        this(timeSeries, new ZeroCostModel(), new ZeroCostModel());
    }



//    public TimeSeriesManagerExtended(TimeSeries timeSeries,BaseStrategyExtended buyStrategy, BaseStrategyExtended sellStrategy) {
//        BackTestContext backTestContext=BackTestContext.getInstance();
//        backTestContext.set(timeSeries,buyStrategy,sellStrategy);
//
//        this.timeSeries = backTestContext.timeSeries;
//        this.transactionCostModel = backTestContext.transactionCostModel;
//        this.holdingCostModel = backTestContext.holdingCostModel;
//
//        this.backTestContext = backTestContext;
//    }


    /**
     * Constructor.
     *
     * @param timeSeries           the time series to be managed
     * @param transactionCostModel the cost model for transactions of the asset
     * @param holdingCostModel     the cost model for holding asset (e.g. borrowing)
     */
    public TimeSeriesManagerExtended(TimeSeries timeSeries, CostModel transactionCostModel, CostModel holdingCostModel) {
        this.timeSeries = timeSeries;
        this.transactionCostModel = transactionCostModel;
        this.holdingCostModel = holdingCostModel;
    }

    /**
     * @param timeSeries the time series to be managed
     */
    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    /**
     * @return the managed time series
     */
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    /**
     * Runs the provided strategy over the managed series.
     * <p>
     * Opens the trades with {@link OrderType} BUY order.
     *
     * @return the trading record coming from the run
     */


    /**
     * Runs the provided strategy over the managed series (from startIndex to finishIndex).
     * <p>
     * Opens the trades with {@link OrderType} BUY order.
     *
     * @param startIndex  the start index for the run (included)
     * @param finishIndex the finish index for the run (included)
     * @return the trading record coming from the run
     */



    /**
     * Runs the provided strategy over the managed series.
     * <p>
     *
     * @param amount the amount used to open/close the trades
     * @return the trading record coming from the run
     */




    /**
     * Runs the provided strategy over the managed series (from startIndex to finishIndex).
     * <p>
     *
     * @param amount      the amount used to open/close the trades
     * @param startIndex  the start index for the run (included)
     * @param finishIndex the finish index for the run (included)
     * @return the trading record coming from the run
     */


//    public TradingRecord runMy(StrategyTester strategy,  Num amount, int startIndex, int finishIndex) {

//        int runBeginIndex = Math.max(startIndex, timeSeries.getBeginIndex());
//        int runEndIndex = Math.min(finishIndex, timeSeries.getEndIndex());
//
//
//        TradingRecord tradingRecord = new BaseTradingRecord(orderType, transactionCostModel, holdingCostModel);
//        for (int i = runBeginIndex; i <= runEndIndex; i++) {
//            // For each bar between both indexes...
////            if (tradingRecord.getCurrentTrade().isOpened()) System.out.println(i+". "+timeSeries.getBar(i).getClosePrice()+" profit "+tradingRecord.getCurrentTrade().getProfit());
//            if (strategy.shouldOperate(i, tradingRecord)) {
//                tradingRecord.operate(i, timeSeries.getBar(i).getClosePrice(), amount);
////                System.out.println(i+". "+timeSeries.getBar(i).getClosePrice());
//            }
//        }
//
//        if (!tradingRecord.isClosed()) {
//            // If the last trade is still opened, we search out of the run end index.
//            // May works if the end index for this run was inferior to the actual number of bars
//            int seriesMaxSize = Math.max(timeSeries.getEndIndex() + 1, timeSeries.getBarData().size());
//            for (int i = runEndIndex + 1; i < seriesMaxSize; i++) {
//                // For each bar after the end index of this run...
//                // --> Trying to close the last trade
//                if (strategy.shouldOperate(i, tradingRecord)) {
//                    tradingRecord.operate(i, timeSeries.getBar(i).getClosePrice(), amount);
//                    break;
//                }
//            }
//        }
//        return tradingRecord;
//    }


}
