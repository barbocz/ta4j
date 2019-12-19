package org.strategy.positionManagement;

import org.strategy.AbstractPositionManagement;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.trading.sltp.BreakEventAfter;
import org.ta4j.core.trading.sltp.IndicatorValue;


public class LastLowHigh extends AbstractPositionManagement {

    public LastLowHigh(TimeSeries series, KeltnerChannelUpperIndicator kcU, KeltnerChannelLowerIndicator kcL) {

        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(new LowPriceIndicator(series), 12);
        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(new HighPriceIndicator(series), 12);
        ATRIndicator atrIndicator=new ATRIndicator(series,14);

        exitLevelsForSell.add(new BreakEventAfter(8));
        exitLevelsForSell.add(new IndicatorValue(highestValueIndicator,atrIndicator,1.0,0));
        IndicatorValue takeProfitValueSell=new IndicatorValue(kcL);
        takeProfitValueSell.setTrail(true);
        exitLevelsForSell.add(takeProfitValueSell);
        //----------------------------------------------------------------------------------------

        exitLevelsForBuy.add(new BreakEventAfter(8));
        exitLevelsForBuy.add(new IndicatorValue(lowestValueIndicator,atrIndicator,-1.0,0));

        IndicatorValue takeProfitValueBuy=new IndicatorValue(kcU);
        takeProfitValueBuy.setTrail(true);
        exitLevelsForBuy.add(takeProfitValueBuy);


    }



}