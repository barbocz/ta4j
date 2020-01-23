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

import org.strategy.TradeEngine;
import org.ta4j.core.Core;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An abstract trading {@link Rule rule}.
 */
public abstract class AbstractRule implements Rule {

    public  Core core;
    public int period=1;
    public TradeEngine tradeEngine;


    public String simpleName;


    private HashMap<Integer, String> ruleItems;
    private String ruleItem;
    private List<Rule> ruleSet;
    protected boolean isLogNeeded=true;


    private TimeSeries series;

    /** The logger */
//    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The class name
     */
    private final String className = getClass().getSimpleName();

    /**
     * Traces the isSatisfied() method calls.
     *
     * @param index       the bar index
     * @param isSatisfied true if the rule is satisfied, false otherwise
     */
    protected void traceIsSatisfied(int index, boolean isSatisfied) {
        //log.trace("{}#isSatisfied({}): {}", className, index, isSatisfied);
    }

    public String getDescription() {
        ruleItem = "";
        ruleItems = new HashMap<>();
        ruleSet = new ArrayList<>();
        return getDescription(this);
    }







    public String getDescription(Rule workRule) {
//        System.out.println(workRule.toString());
//        Rule sellRule=r1.and(r2).or(r3.and(r4));
//        workRule.setStrategyId(strategyId);
        if (!(workRule instanceof AndRule) && !(workRule instanceof OrRule) && !(workRule instanceof NotRule)) {
            ruleItem = " " + workRule.getParameters();
            ruleItems.put(workRule.hashCode(), workRule.getParameters());
            ruleSet.add(workRule);
            workRule.setCore(core);
//            System.out.println(" - "+workRule.getParameters()+" - "+workRule.hashCode());
        } else {
            if (workRule instanceof NotRule) {
                NotRule notRule = (NotRule) workRule;
                Rule rule1 = notRule.getRuleToNegate();

//                System.out.println(andRule.getRule1().getClass().getSimpleName()+" AND "+andRule.getRule2().getClass().getSimpleName());

                ruleItem = " NOT " + getDescription(rule1);
            }
            if (workRule instanceof AndRule) {
                AndRule andRule = (AndRule) workRule;
                Rule rule1 = andRule.getRule1();
                Rule rule2 = andRule.getRule2();
//                System.out.println(andRule.getRule1().getClass().getSimpleName()+" AND "+andRule.getRule2().getClass().getSimpleName());

                ruleItem = getDescription(rule1) + " AND" + getDescription(rule2);
            }
            if (workRule instanceof OrRule) {
                OrRule orRule = (OrRule) workRule;
                Rule rule1 = orRule.getRule1();
                Rule rule2 = orRule.getRule2();
//                System.out.println(orRule.getRule1().getClass().getSimpleName()+" OR "+orRule.getRule2().getClass().getSimpleName());

                ruleItem = getDescription(rule1) + " OR" + getDescription(rule2);
//                System.out.println(processRule(rule1)+" OR "+processRule(rule2));
            }
        }
        return ruleItem;
    }


    @Override
    public void setPeriod(int period) {
        this.period=period;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    public String getParameters() {
        return getClass().getSimpleName();
    }


    public HashMap<Integer, String> getRuleItems() {
        return ruleItems;
    }

    public List<Rule> getRuleSet() {
        return ruleSet;
    }


    @Override
    public void setCore(Core core){
        this.core=core;
    }

    @Override
    public Core getCore(){
        return core;
    }


    public void setTradeEngineForAllRule(Rule workRule){


        if (!(workRule instanceof AndRule) && !(workRule instanceof OrRule) && !(workRule instanceof NotRule)) {

            workRule.setTradeEngine(tradeEngine);
//            System.out.println(" - "+workRule.getParameters()+" - "+workRule.hashCode());
        } else {
            if (workRule instanceof NotRule) {
                NotRule notRule = (NotRule) workRule;
                Rule rule1 = notRule.getRuleToNegate();
                rule1.setTradeEngine(tradeEngine);
                setTradeEngineForAllRule(rule1);
            }
            if (workRule instanceof AndRule) {
                AndRule andRule = (AndRule) workRule;
                Rule rule1 = andRule.getRule1();
                rule1.setTradeEngine(tradeEngine);
                Rule rule2 = andRule.getRule2();
                rule2.setTradeEngine(tradeEngine);
                setTradeEngineForAllRule(rule1);
                setTradeEngineForAllRule(rule2);
            }
            if (workRule instanceof OrRule) {
                OrRule orRule = (OrRule) workRule;
                Rule rule1 = orRule.getRule1();
                rule1.setTradeEngine(tradeEngine);
                Rule rule2 = orRule.getRule2();
                rule2.setTradeEngine(tradeEngine);
                setTradeEngineForAllRule(rule1);
                setTradeEngineForAllRule(rule2);

            }
        }

    }

    @Override
    public TradeEngine getTradeEngine(){
        return tradeEngine;
    }

    @Override
    public void setTradeEngine(TradeEngine tradeEngine){
        this.tradeEngine =tradeEngine;
//        setStrategyForRule(this);
    }

    @Override
    public boolean isSatisfied(ZonedDateTime time, boolean isLogNeeded) {
        this.isLogNeeded=isLogNeeded;
        boolean isSatisfied=isSatisfied(time);
        this.isLogNeeded=true;
        return false;
    }

//    public void setTradeEngine(TradeEngine tradeEn){
//        this.tradeEn=tradeEn;
//    }


}
