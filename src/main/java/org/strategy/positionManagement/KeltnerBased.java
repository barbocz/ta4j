package org.strategy.positionManagement;

import org.strategy.AbstractPositionManagement;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.trading.sltp.BreakEventAfter;
import org.ta4j.core.trading.sltp.IndicatorValue;


public class KeltnerBased extends AbstractPositionManagement {

    public KeltnerBased(KeltnerChannelUpperIndicator kcU, KeltnerChannelMiddleIndicator kcM, KeltnerChannelLowerIndicator kcL) {

        exitLevelsForSell.add(new BreakEventAfter(8));
        exitLevelsForSell.add(new IndicatorValue(kcM,2));
        exitLevelsForSell.add(new IndicatorValue(kcL));

        exitLevelsForBuy.add(new BreakEventAfter(8));
        exitLevelsForBuy.add(new IndicatorValue(kcM,2));
        exitLevelsForBuy.add(new IndicatorValue(kcU));

    }



}