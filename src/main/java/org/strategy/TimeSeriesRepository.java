package org.strategy;

import org.ta4j.core.TimeSeries;

public interface TimeSeriesRepository {
    public void setTimeSeries(Integer timeFrame);
    public TimeSeries getTimeSeries(Integer timeFrame);
    public void toString(int timeFrame);
}
