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
import org.ta4j.core.trading.SLTPManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Base implementation of a {@link Strategy}.
 */
public class BaseStrategyExtended implements Strategy {

    /** The logger */
//    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** The class name */
    private final String className = getClass().getSimpleName();

    /** Name of the strategy */
    private String name;

    /** The entry rule */
    private Rule entryRule;

    /** The exit rule */
    private Rule exitRule;

    public Order.OrderType orderType=Order.OrderType.BUY;

    public List<SLTPManager> exitLevelManagers=new ArrayList<>();


    /**
     * The unstable timeFrame (number of bars).<br>
     * During the unstable timeFrame of the strategy any order placement will be cancelled.<br>
     * I.e. no entry/exit signal will be fired before index == unstablePeriod.
     */
    private int unstablePeriod;

    /**
     * Constructor.
     * @param entryRule the entry rule
     * @param exitRule the exit rule
     */
    public BaseStrategyExtended(Rule entryRule, Rule exitRule) {
        this(null, entryRule, exitRule, 0);
    }

    public BaseStrategyExtended(Order.OrderType orderType, Rule entryRule, Rule exitRule) {
        this(null, entryRule, exitRule, 0);
        this.orderType=orderType;

    }

    public BaseStrategyExtended(Order.OrderType orderType, Rule entryRule, Rule exitRule, SLTPManager exitLevelManager) {
        this(null, entryRule, exitRule, 0);
        this.orderType=orderType;
        if (exitLevelManager!=null) exitLevelManagers.add(exitLevelManager);
    }

    public BaseStrategyExtended(Order.OrderType orderType, Rule entryRule, Rule exitRule,List<SLTPManager> exitLevelManagers) {
        this(null, entryRule, exitRule, 0);
        this.orderType=orderType;
        if (exitLevelManagers!=null)  this.exitLevelManagers=exitLevelManagers;
        else exitLevelManagers=new ArrayList<>();
    }


     /**
     * Constructor.
     * @param entryRule the entry rule
     * @param exitRule the exit rule
     * @param unstablePeriod strategy will ignore possible signals at <code>index</code> < <code>unstablePeriod</code>
     */
    public BaseStrategyExtended(Rule entryRule, Rule exitRule, int unstablePeriod) {
        this(null, entryRule, exitRule, unstablePeriod);
    }

    /**
     * Constructor.
     * @param name the name of the strategy
     * @param entryRule the entry rule
     * @param exitRule the exit rule
     */
    public BaseStrategyExtended(String name, Rule entryRule, Rule exitRule) {
        this(name, entryRule, exitRule, 0);
    }

    /**
     * Constructor.
     * @param name the name of the strategy
     * @param entryRule the entry rule
     * @param exitRule the exit rule
     * @param unstablePeriod strategy will ignore possible signals at <code>index</code> < <code>unstablePeriod</code>
     */
    public BaseStrategyExtended(String name, Rule entryRule, Rule exitRule, int unstablePeriod) {
        if (entryRule == null ) {
            throw new IllegalArgumentException("Rules cannot be null");
        }
        if (unstablePeriod < 0) {
        	throw new IllegalArgumentException("Unstable timeFrame bar count must be >= 0");
        }
        this.name = name;
        this.entryRule = entryRule;
        this.exitRule = exitRule;
        this.unstablePeriod = unstablePeriod;
    }

    @Override
    public String getName() {
    	return name;
    }

    @Override
    public Rule getEntryRule() {
//        entryRule.getDescription();
    	return entryRule;
    }

    @Override
    public Rule getExitRule() {
//        if (exitRule!=null) exitRule.getDescription();
    	return exitRule;
    }

    @Override
    public int getUnstablePeriod() {
    	return unstablePeriod;
    }

    @Override
    public void setUnstablePeriod(int unstablePeriod) {
        this.unstablePeriod = unstablePeriod;
    }

    @Override
    public boolean isUnstableAt(int index) {
        return index < unstablePeriod;
    }




    public  boolean shouldOperate(int index, BaseTradingRecordExtended tradingRecord) {

//        Trade trade = tradingRecord.getCurrentTrade();
//        if (trade.isNew()) {
//            return shouldEnter(index, tradingRecord);
//        } else if (trade.isOpened()) {
//            return shouldExit(index, tradingRecord);
//        }

//        if (shouldEnter(index, tradingRecord)) {
//            tradingRecord.enter(index,orderType);
//        }
//        else {
//            if (shouldExit(index, tradingRecord)) {
//                tradingRecord.exit(index,orderType);
//            }
//        }
        return false;
    }

    public void shouldCloseByTakeProfit(){};


    public boolean shouldEnter(int index) {
        return Strategy.super.shouldEnter(index);
    }


    public boolean shouldExit(int index) {
        if (getExitRule()!=null) return getExitRule().isSatisfied(index);
        return false;
    }

    @Override
    public Strategy and(Strategy strategy) {
        String andName = "and(" + name + "," + strategy.getName() + ")";
        int unstable = Math.max(unstablePeriod, strategy.getUnstablePeriod());
        return and(andName, strategy, unstable);
    }

    @Override
    public Strategy or(Strategy strategy) {
        String orName = "or(" + name + "," + strategy.getName() + ")";
        int unstable = Math.max(unstablePeriod, strategy.getUnstablePeriod());
        return or(orName, strategy, unstable);
    }

    @Override
    public Strategy opposite() {
        return new BaseStrategyExtended("opposite(" + name + ")", exitRule, entryRule, unstablePeriod);
    }

    @Override
    public Strategy and(String name, Strategy strategy, int unstablePeriod) {
        return new BaseStrategyExtended(name, entryRule.and(strategy.getEntryRule()), exitRule.and(strategy.getExitRule()), unstablePeriod);
    }

    @Override
    public Strategy or(String name, Strategy strategy, int unstablePeriod) {
        return new BaseStrategyExtended(name, entryRule.or(strategy.getEntryRule()), exitRule.or(strategy.getExitRule()), unstablePeriod);
    }

    /**
     * Traces the shouldEnter() method calls.
     * @param index the bar index
     * @param enter true if the strategy should enter, false otherwise
     */
    protected void traceShouldEnter(int index, boolean enter) {
        //log.trace(">>> {}#shouldEnter({}): {}", className, index, enter);
    }

    /**
     * Traces the shouldExit() method calls.
     * @param index the bar index
     * @param exit true if the strategy should exit, false otherwise
     */
    protected void traceShouldExit(int index, boolean exit) {
        //log.trace(">>> {}#shouldExit({}): {}", className, index, exit);
    }


}
