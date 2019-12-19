package org.strategy.KeltnerBounce;

import org.ta4j.core.Rule;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

public class Entry extends IndicatorSet {

    public Entry(){
        ruleForSell = new CrossedDownIndicatorRule(mbfxTiming, series.numOf(95.0), 8);
        ruleForSell = ruleForSell.and(new CrossedUpIndicatorRule(closePrice, kcU, 9));
        ruleForSell = ruleForSell.and(new CrossedDownIndicatorRule(antiAlligatorIndicator, series.numOf(0.0), 2));

        ruleForBuy = new CrossedUpIndicatorRule(mbfxTiming, series.numOf(5.0), 8);
        ruleForBuy = ruleForBuy.and(new CrossedDownIndicatorRule(closePrice, kcL, 9));
        ruleForBuy = ruleForBuy.and(new CrossedUpIndicatorRule(antiAlligatorIndicator, series.numOf(0.0), 2));
    }

}
