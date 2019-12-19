package org.strategy;

import org.ta4j.core.Core;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AbstractStrategy  {

    public int period;

    public TimeSeriesRepo timeSeries;
    public TimeSeries series;

    public AbstractEntry entry;
    public AbstractExit exit;
    public AbstractPositionManagement positionManagement;

    public Rule ruleForSell=null,ruleForBuy=null;

    public List<Indicator> indicitatorsToDebug=new ArrayList<>();


    protected void debugIndicator(Indicator... indicators) {
        for (Indicator indicator : indicators)  indicitatorsToDebug.add(indicator);
    }





}
