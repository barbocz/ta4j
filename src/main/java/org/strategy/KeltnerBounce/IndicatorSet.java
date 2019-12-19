package org.strategy.KeltnerBounce;

import org.strategy.CoreData;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.mt4Selection.AntiAlligatorIndicator;
import org.ta4j.core.indicators.mt4Selection.MbfxTimingIndicator;
import org.ta4j.core.indicators.mt4Selection.WaddahIndicator;

public class IndicatorSet extends CoreData {

    public KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series, 21);
    public KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM, 2.5, 34);
    public KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM, 2.5, 34);
    public AntiAlligatorIndicator antiAlligatorIndicator = new AntiAlligatorIndicator(series);

    public HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
    public LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
    public WaddahIndicator waddahIndicatorUp = new WaddahIndicator(series, WaddahIndicator.Type.TREND_UP);
    public WaddahIndicator waddahIndicatorDown = new WaddahIndicator(series, WaddahIndicator.Type.TREND_DOWN);

    public WaddahIndicator waddahIndicatorUp60 = new WaddahIndicator(series60, WaddahIndicator.Type.TREND_UP);
    public WaddahIndicator waddahIndicatorDown60 = new WaddahIndicator(series60, WaddahIndicator.Type.TREND_DOWN);
    public MbfxTimingIndicator mbfxTiming60 = new MbfxTimingIndicator(series60, 5);

    public IndicatorSet() {
        waddahIndicatorUp60.setBaseSeries(series);
        waddahIndicatorDown60.setBaseSeries(series);
        mbfxTiming60.setBaseSeries(series);
    }

}
