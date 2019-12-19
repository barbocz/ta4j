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

import org.strategy.Order;
import org.strategy.TradeEngine;
import org.ta4j.core.Bar;

import java.time.ZonedDateTime;

/**
 * A {@link org.ta4j.core.Rule} which waits for a number of {@link Bar} after an order.
 * </p>
 * Satisfied after a fixed number of bars since the last order.
 */
public class BreakAfterLosingTrade extends AbstractRule {

    /**
     * The type of the order since we have to wait for
     */

    /**
     * The number of bars to wait for
     */

    TradeEngine tradeEngine;
    int barNumber;

    public BreakAfterLosingTrade(TradeEngine tradeEngine,int barNumber) {
                this.tradeEngine=tradeEngine;
                this.barNumber=barNumber;
    }

    @Override
    public boolean isSatisfied (ZonedDateTime time) {
//        final boolean satisfied = !getCore().hasOpenOrders();
//        getCore().debugRule(index,this,satisfied);

        int currentIndex=tradeEngine.series.getCurrentIndex();
        for (Order order: tradeEngine.closedOrders){
            if (currentIndex-order.barIndex<barNumber) return false;
        }
        return true;
    }
}
