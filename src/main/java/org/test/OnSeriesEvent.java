package org.test;

import org.ta4j.core.mt4.MT4TimeSeries;

public interface OnSeriesEvent {
    void onTickEvent(MT4TimeSeries eventSeries);
    void onOneMinuteDataEvent(MT4TimeSeries eventSeries);
    void onBarChangeEvent(MT4TimeSeries eventSeries);
}
