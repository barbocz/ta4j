package org.strategy.KeltnerBounce;

import org.ta4j.core.Rule;
import org.ta4j.core.num.NaN;
import org.ta4j.core.trading.rules.*;

public class Entry extends IndicatorSet {

    public Entry(){


//        ruleForSell = new OverIndicatorRule(mbfxTiming, 90.0,3);
        ruleForSell =new CrossedDownIndicatorRule(mbfxTiming60, series.numOf(80.0),3);
        ruleForSell =  ruleForSell.and(new OverIndicatorRule(closePrice, kcU, 5));
        ruleForSell = ruleForSell.and(new OverIndicatorRule(closePrice, kcM));
        ruleForSell = ruleForSell.and(new UnderIndicatorRule(antiAlligatorIndicator, 0.0, 2));
        ruleForSell = ruleForSell.and(new IsEqualRule(waddahIndicatorUp, NaN.NaN,1));

//        ruleForSell = ruleForSell.and(new IsEqualRule(waddahIndicatorUp60, NaN.NaN,1));


//        ruleForBuy = new UnderIndicatorRule(mbfxTiming, 10.0, 3);
        ruleForBuy = new CrossedUpIndicatorRule(mbfxTiming60, series.numOf(20.0),3);
        ruleForBuy = ruleForBuy.and(new UnderIndicatorRule(closePrice, kcL, 5));
        ruleForBuy = ruleForBuy.and(new NotRule(new OverIndicatorRule(closePrice, kcM)));
        ruleForBuy = ruleForBuy.and(new OverIndicatorRule(antiAlligatorIndicator, 0.0, 2));
        ruleForBuy = ruleForBuy.and(new IsEqualRule(waddahIndicatorDown, NaN.NaN,1));

//        ruleForBuy = ruleForBuy.and(new IsEqualRule(waddahIndicatorDown60, NaN.NaN,1));
    }

}
