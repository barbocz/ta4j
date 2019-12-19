/*
  The MIT License (MIT)

  Copyright (c) 2014-2018 Marc de Verdelhan & respective authors (see AUTHORS)

  Permission is hereby granted, free of charge, to any person obtaining a copy of
  this software and associated documentation files (the "Software"), to deal in
  the Software without restriction, including without limitation the rights to
  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.trading.extendedRules;

import org.ta4j.core.Order;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.extendedCore.BaseTradingRecordExtended;
import org.ta4j.core.indicators.helpers.PriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.AbstractRule;
import org.ta4j.core.trading.rules.TrailingStopLossRule;

import java.util.HashMap;

/**
 * A trailing stop-loss rule
 * <p></p>
 * Satisfied when the price reaches the trailing loss threshold.
 */
public class TrailingStopLossRuleExtended extends AbstractRule {


    public enum Method {
        GROUPPED,

    }

    /**
     * The price indicator
     */
    private final PriceIndicator priceIndicator;
    /**
     * the loss ratio multiplier for buy trades eg. for lossPercentage 5% this ratio will be: 0.95
     */
    private final Num lossRatioBuyMultiplier;
    /**
     * the loss ratio multiplier for sell trades eg. for lossPercentage 5% this ratio will be: 1.05
     */
    private final Num lossRatioSellMultiplier;


    /**
     * the current price extremum
     */
    private HashMap<Integer,Num> currentExtremums=new HashMap<>();
    /**
     * the current stop loss price activation
     */
    private HashMap<Integer,Num> currentStopLossLimitActivations=new HashMap<>();



    /**
     * Constructor.
     *
     * @param priceIndicator the price indicator
     * @param lossPercentage the loss percentage
     */

    public TrailingStopLossRuleExtended(PriceIndicator priceIndicator, Num lossPercentage) {
        this.priceIndicator = priceIndicator;
        final Num hundred = lossPercentage.numOf(100);
        this.lossRatioBuyMultiplier = hundred.minus(lossPercentage).dividedBy(hundred);
        this.lossRatioSellMultiplier = hundred.plus(lossPercentage).dividedBy(hundred);
    }


    public boolean isSatisfied(int index, TradingRecord tradingRecord,Order currentOrder) {
        boolean satisfied = false;
        // No trading history or no trade opened, no loss
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Order entryOrder=currentTrade.getEntry();
                int orderId=entryOrder.getId();
                if (!currentExtremums.containsKey(orderId)) {
                    currentExtremums.put(orderId,null);
                    currentStopLossLimitActivations.put(orderId,null);
                }

                final Num currentPrice = priceIndicator.getValue(index);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = isBuySatisfied(orderId,currentPrice);
                } else {
                    satisfied = isSellSatisfied(orderId, currentPrice);
                }
            }
        }

        return satisfied;
    }

    private boolean isBuySatisfied(int orderId, Num currentPrice) {

        Num currentExtremum=currentExtremums.get(orderId);
        Num currentStopLossLimitActivation=currentStopLossLimitActivations.get(orderId);

        if (currentExtremum == null || currentPrice.isGreaterThan(currentExtremum)) {
            currentExtremums.replace(orderId,currentPrice);
            currentStopLossLimitActivation = currentPrice.multipliedBy(lossRatioBuyMultiplier);
            currentStopLossLimitActivations.replace(orderId,currentStopLossLimitActivation);
        }
        return currentPrice.isLessThanOrEqual(currentStopLossLimitActivation);
    }

    private boolean isSellSatisfied(int orderId, Num currentPrice) {
        Num currentExtremum=currentExtremums.get(orderId);
        Num currentStopLossLimitActivation=currentStopLossLimitActivations.get(orderId);

        if (currentExtremum == null || currentPrice.isLessThan(currentExtremum)) {
            currentExtremums.replace(orderId,currentPrice);
            currentStopLossLimitActivation = currentPrice.multipliedBy(lossRatioSellMultiplier);
            currentStopLossLimitActivations.replace(orderId,currentStopLossLimitActivation);
        }
        return currentPrice.isGreaterThanOrEqual(currentStopLossLimitActivation);

    }


}
