package org.strategy;

import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.trading.SLTPManager;

import java.util.ArrayList;
import java.util.List;

public class CoreData {

    public List<SLTPManager> sellStopLoss=new ArrayList<>();
    public List<SLTPManager> buyStopLoss=new ArrayList<>();
    public List<SLTPManager> sellTakeProfit=new ArrayList<>();
    public List<SLTPManager> buyTakeProfit=new ArrayList<>();

    public Rule ruleForSell=null,ruleForBuy=null;


    public static MT4TimeSeries series = new MT4TimeSeries.SeriesBuilder().
                                            withName("EURUSD").
                                            withPeriod(1).
                                            withOhlcvFileName("EURUSD_1_MONTH.csv").
                                            withDateFormatPattern("dd.MM.yyyy HH:mm").
                                            withNumTypeOf(DoubleNum.class).
                                            build();

    public static MT4TimeSeries series60 = new MT4TimeSeries.SeriesBuilder().withPeriod(1440).buildFromSeries(series);

    public static ClosePriceIndicator closePrice = new ClosePriceIndicator(series);




}
