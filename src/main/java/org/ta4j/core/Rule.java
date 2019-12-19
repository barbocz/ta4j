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
package org.ta4j.core;

import org.strategy.TradeEngine;
import org.ta4j.core.trading.rules.AndRule;
import org.ta4j.core.trading.rules.NotRule;
import org.ta4j.core.trading.rules.OrRule;
import org.ta4j.core.trading.rules.XorRule;

import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * A rule for strategy building.
 * </p>
 * A trading rule may be composed of a combination of other rules.
 *
 * A {@link Strategy trading strategy} is a pair of complementary (entry and exit) rules.
 */
public interface Rule {

    default void setCore(Core core){}
    default Core getCore(){return null;}

    default void setTradeEngine(TradeEngine tradeEngine){}
    default TradeEngine getTradeEngine(){return null;}
    public void setTradeEngineForAllRule(Rule workRule);

    default void setPeriod(int period) {}
    default int getPeriod(){return -1;}


    default String getDescription() {return "";}

    default String getParameters() {return "";}

    default HashMap<Integer,String> getRuleItems() {return new HashMap<>();}

    /**
     * @param rule another trading rule
     * @return a rule which is the AND combination of this rule with the provided one
     */
    default Rule and(Rule rule) {
    	return new AndRule(this, rule);
    }

    /**
     * @param rule another trading rule
     * @return a rule which is the OR combination of this rule with the provided one
     */
    default Rule or(Rule rule) {
    	return new OrRule(this, rule);
    }

    /**
     * @param rule another trading rule
     * @return a rule which is the XOR combination of this rule with the provided one
     */
    default Rule xor(Rule rule) {
    	return new XorRule(this, rule);
    }

    /**
     * @return a rule which is the logical negation of this rule
     */
    default Rule negation() {
    	return new NotRule(this);
    }

    /**
     * @param index the bar index
     * @return true if this rule is satisfied for the provided index, false otherwise
     */
//    default boolean isSatisfied(int index) {
//    	return isSatisfied(index, null);
//    }



//    default boolean isSatisfied(int index,TradingRecord tradingRecord,Order currentOrder) {
//        index=getCore().getRuleIndex(index,this);
//
//        boolean isSatisfied=isSatisfied(getCore().getRuleIndex(index,this));
////        if (isDebug()) System.out.println(index+" - "+isSatisfied+" "+getParameters());
//
//        if (tradingRecord.isDebug())    tradingRecord.debug(index,currentOrder==null?0:currentOrder.getId(),isSatisfied?1.0:0.0,hashCode());
//
//
//        return isSatisfied;
//    }


    default boolean isSatisfied(int index) {return false;}

    default boolean isSatisfied(ZonedDateTime time) {return false;}
}
