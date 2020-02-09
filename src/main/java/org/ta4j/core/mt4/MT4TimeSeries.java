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
package org.ta4j.core.mt4;

import org.strategy.TimeSeriesRepo;
import org.ta4j.core.*;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.ta4j.core.num.NaN.NaN;

/**
 * Base implementation of a {@link TimeSeries}.
 * </p>
 */
public class MT4TimeSeries implements TimeSeries {

    public TimeSeriesRepo timeSeriesRepo = null;

    private List<String> indicatorFiles = new ArrayList<>();
    public String ohlcvFileName = null;
    private HashMap<String, List<String[]>> indicators = new HashMap<>();
    public String symbol = "";

    public int period = 1;
    public TreeMap<ZonedDateTime, Integer> timeTreeMap = new TreeMap<>();

    Double openPrice = null, closePrice = null, highPrice = null, lowPrice = null, bid = null, ask = null;

    boolean doAggregate = false;

    double summaAmount = 0.0;
    public ZonedDateTime nextBarDate = null, firstBarDate = null;

    String dateFormatPattern = "yyyy.MM.dd HH:mm";
//    DateTimeFormatter simpleDateFormatter = DateTimeFormatter.ofPattern("MM.dd HH:mm");
//    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    List<Bar> sortedBars = new ArrayList<>();

    SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatPattern);
    DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);


    public ZonedDateTime currentTime = null;
    public int currentIndex=-1,prevIndex=-1;
    public Bar currentBar=null,prevBar=null;

    @Override
    public Bar getCurrentBar() {return currentBar;}

    @Override
    public void setCurrentBar(Bar bar) {
        currentBar=bar;
    }

    @Override
    public Bar getPrevBar() {return prevBar;}

    @Override
    public int getPrevIndex() {return prevIndex;}


    //----------------------------------------
    private static final long serialVersionUID = -1878027009398790126L;
    /**
     * Name for unnamed series
     */
    private static final String UNNAMED_SERIES_NAME = "unamed_series";
    /**
     * Num type function
     **/
    protected Function<Number, Num> numFunction;
    /**
     * The logger
     */
//    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Name of the series
     */
    private final String name;

    /**
     * List of bars
     */
    public List<Bar> bars;
    /**
     * Begin index of the time series
     */
    private int seriesBeginIndex;
    /**
     * End index of the time series
     */
    public int seriesEndIndex;

    public ZonedDateTime seriesEndTime;
    /**
     * Maximum number of bars for the time series
     */
    private int maximumBarCount = Integer.MAX_VALUE;
    /**
     * Number of removed bars
     */
    private int removedBarsCount = 0;
    /**
     * True if the current series is constrained (i.e. its indexes cannot change), false otherwise
     */
    private boolean constrained;

    private Duration barDuration = Duration.ofSeconds(60);

    /**
     * Constructor of an unnamed series.
     */

    public void resetBars() {
        seriesBeginIndex = -1;
        seriesEndIndex = -1;
        currentIndex=-1;
        prevIndex=-1;
        removedBarsCount = 0;
        currentTime = null;
        currentBar=null;
        prevBar=null;
        nextBarDate = null;
        bars.clear();
        sortedBars.clear();
        timeTreeMap.clear();
        closePrice = null;

    }


    public void updateBar(String message) {
        // perform any operation
//        System.out.println("updateBar: " + message);

        String priceData[] = message.split(";");
        // 0-s elem 'M' mint MINUTE data, 'T' fenntartva a tick adatnak

        Bar bar = null;

        try {

//            instantTime = dateFormatter.parse(priceData[1]).toInstant();
            currentTime = ZonedDateTime.parse(priceData[1], zdtFormatter.withZone(ZoneId.systemDefault()));

//            System.out.println("currentIndex " + currentIndex);


            if ((doAggregate && seriesBeginIndex == -1 && closePrice == null)) {
                nextBarDate = getNextBarDate(currentTime);
//                ChronoUnit cu;
//                if (period == 10080) cu = ChronoUnit.WEEKS;
//                else if (period == 1440) cu = ChronoUnit.DAYS;
//                else cu = ChronoUnit.HOURS;
//
//                firstBarDate = time.truncatedTo(cu);
//                if (cu == ChronoUnit.HOURS) {
//                    while (firstBarDate.plus(period, ChronoUnit.MINUTES).isBefore(time)) {
//                        firstBarDate = firstBarDate.plus(period, ChronoUnit.MINUTES);
//                    }
//                }
//                nextBarDate = firstBarDate.plus(period, ChronoUnit.MINUTES);

                highPrice = 0.0;
                lowPrice = Double.MAX_VALUE;
                openPrice = Double.parseDouble(priceData[2]);
                summaAmount = 0.0;

            }

//            if (this.period==1440 && priceData[1].equals("2018.05.28 23:59")) {
//                System.out.println("stop");
//            }

            if (!doAggregate) {
                bar = new BaseBar(barDuration, currentTime,
                        numFunction.apply(Double.valueOf(priceData[2])),
                        numFunction.apply(Double.valueOf(priceData[3])),
                        numFunction.apply(Double.valueOf(priceData[4])),
                        numFunction.apply(Double.valueOf(priceData[5])),
                        numFunction.apply(Double.valueOf(priceData[6])),
                        numFunction.apply(0.0));

                sortedBars.add(bar);
                timeTreeMap.put(currentTime, sortedBars.size() - 1);


//                if (!sortedBars.isEmpty()) {
//                    seriesBeginIndex = 0;
//                    seriesEndIndex = sortedBars.size() - 1;
//                }

                addBar(bar, false);

                setCurrentBar(bar);
                currentIndex = getIndex(currentTime);
                if (currentIndex>0) {
                    prevIndex=currentIndex-1;
                    prevBar=bars.get(prevIndex);
                }

                seriesEndTime = currentTime;
                if (period == 1 && timeSeriesRepo != null) {
                    timeSeriesRepo.onBarChangeEvent(this);
                }

            } else {  // AGGREGATE

                if (currentTime.isAfter(nextBarDate)) {    // tervezettől nagyobb időugrás, jellemzően adatkimaradás


                    if (summaAmount > 0.0) {  // ha volt adat
                        bar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                , numFunction.apply(summaAmount), numFunction.apply(0));

                        sortedBars.add(bar);
                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);



//                        if (!sortedBars.isEmpty()) {
//                            seriesBeginIndex = 0;
//                            seriesEndIndex = sortedBars.size() - 1;
//                        }

                        addBar(bar, false);

                        setCurrentBar(bar);
                        currentIndex = getIndex(currentTime);
                        if (currentIndex>0) {
                            prevIndex=currentIndex-1;
                            prevBar=bars.get(prevIndex);
                        }


                        if (timeSeriesRepo != null) {
                            timeSeriesRepo.onBarChangeEvent(this);
                        }
                    }
                    nextBarDate = getNextBarDate(currentTime);


                    openPrice = Double.parseDouble(priceData[2]);
                    summaAmount = 0.0;
                    highPrice = 0.0;
                    lowPrice = Double.MAX_VALUE;


                } else {
                    summaAmount += Double.parseDouble(priceData[6]);
                    closePrice = Double.parseDouble(priceData[5]);
                    if (openPrice == 0.0) openPrice = Double.parseDouble(priceData[2]);

                    if (Double.parseDouble(priceData[3]) > highPrice)
                        highPrice = Double.parseDouble(priceData[3]);
                    if (Double.parseDouble(priceData[4]) < lowPrice)
                        lowPrice = Double.parseDouble(priceData[4]);

//                        if (zd.getDayOfWeek().equals(DayOfWeek.SATURDAY) || zd.getDayOfWeek().equals(DayOfWeek.SUNDAY)) continue;
                    if (currentTime.isAfter(nextBarDate.minusSeconds(1))) {
                        bar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                , numFunction.apply(summaAmount), numFunction.apply(0));

                        sortedBars.add(bar);
                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);

//                        if (!sortedBars.isEmpty()) {
//                            seriesBeginIndex = 0;
//                            seriesEndIndex = sortedBars.size() - 1;
//                        }

                        addBar(bar, false);

                        setCurrentBar(bar);
                        currentIndex = getIndex(currentTime);
                        if (currentIndex>0) {
                            prevIndex=currentIndex-1;
                            prevBar=bars.get(prevIndex);
                        }



//                    openPrice = Double.parseDouble(priceData[2]);
//                            firstBarDate = nextBarDate;
                        //nextBarDate = time.plus(period, ChronoUnit.MINUTES);
//                    ZonedDateTime zzzz=time.plus(period, ChronoUnit.MINUTES);
                        nextBarDate = currentTime.plus(period, ChronoUnit.MINUTES);
                        summaAmount = 0.0;
                        highPrice = 0.0;
                        lowPrice = Double.MAX_VALUE;
                        openPrice = 0.0;

                        if (timeSeriesRepo != null) {

                            timeSeriesRepo.onBarChangeEvent(this);
                        }

                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }


//        Instant instantTime = dateFormatter.parse(priceData[0]).toInstant();
////                    if (priceData[0].equals("25.05.2018 20:59:00.000")) {
////                        System.out.println("stop");
////                    }
//
//        if (doAggregate) {
////                        ZonedDateTime zd=nextBarDate.atZone(ZoneId.systemDefault());
//            summaAmount += Double.parseDouble(priceData[5]);
//            closePrice = Double.parseDouble(priceData[4]);
//            if (Double.parseDouble(priceData[2]) > highPrice)
//                highPrice = Double.parseDouble(priceData[2]);
//            if (Double.parseDouble(priceData[3]) < lowPrice)
//                lowPrice = Double.parseDouble(priceData[3]);
////                        if (zd.getDayOfWeek().equals(DayOfWeek.SATURDAY) || zd.getDayOfWeek().equals(DayOfWeek.SUNDAY)) continue;
//            if (instantTime.isAfter(nextBarDate.minusSeconds(1)) || instantTime.equals(lastDate)) {
//                ZonedDateTime barTime = instantTime.atZone(ZoneId.systemDefault());
//                bar = new BaseBar(barDuration, barTime, numFunction.apply(openPrice), numFunction.apply(highPrice)
//                        , numFunction.apply(lowPrice), numFunction.apply(closePrice)
//                        , numFunction.apply(summaAmount), numFunction.apply(0));
//                if (hasIndicator) bars.put(barTime, bar);
//                else {
//                    sortedBars.add(bar);
//                    timeTreeMap.put(barTime, sortedBars.size() - 1);
//                }
//
//                openPrice = Double.parseDouble(priceData[1]);
////                            firstBarDate = nextBarDate;
//                nextBarDate = instantTime.plus(timeFrame, ChronoUnit.MINUTES);
//                summaAmount = 0.0;
//                highPrice = 0.0;
//                lowPrice = Double.MAX_VALUE;
//            }
//
//
//        } else {
//            zonedDateTime = instantTime.plusSeconds(barDurationSeconds).atZone(ZoneId.systemDefault());
//            bar = new BaseBar(barDuration, zonedDateTime, stringToNumFunction.apply(priceData[1]), stringToNumFunction.apply(priceData[2])
//                    , stringToNumFunction.apply(priceData[3]), stringToNumFunction.apply(priceData[4])
//                    , stringToNumFunction.apply(priceData[5]), stringToNumFunction.apply("0.0"));
//
//            if (hasIndicator) bars.put(zonedDateTime, bar);
//            else {
//                sortedBars.add(bar);
//                timeTreeMap.put(zonedDateTime, sortedBars.size() - 1);
//            }
//
//        }
//    }


        // check if listener is registered.
        if (timeSeriesRepo != null && getEndIndex() > -1) timeSeriesRepo.onOneMinuteDataEvent(this);


    }


    public MT4TimeSeries() {
        this(UNNAMED_SERIES_NAME);
    }


    /**
     * Constructor.
     *
     * @param name the name of the series
     */
    public MT4TimeSeries(String name) {
        this(name, new ArrayList<>());
    }

    /**
     * Constructor of an unnamed series.
     *
     * @param bars the list of bars of the series
     */
    public MT4TimeSeries(List<Bar> bars) {
        this(UNNAMED_SERIES_NAME, bars);
    }

    /**
     * Constructor.
     *
     * @param name the name of the series
     * @param bars the list of bars of the series
     */
    public MT4TimeSeries(String name, List<Bar> bars) {
        this(name, bars, 0, bars.size() - 1, false);
    }

    /**
     * Constructor.
     *
     * @param name the name of the series
     */
    public MT4TimeSeries(String name, Function<Number, Num> numFunction) {
        this(name, new ArrayList<>(), numFunction);
    }

    /**
     * Constructor.
     *
     * @param name the name of the series
     * @param bars the list of bars of the series
     */
    public MT4TimeSeries(String name, List<Bar> bars, Function<Number, Num> numFunction) {
        this(name, bars, 0, bars.size() - 1, false, numFunction);
    }

    /**
     * Constructor.<p/>
     * Creates a BaseTimeSeries with default {@link DoubleNum} as type for the data and all operations on it
     *
     * @param name             the name of the series
     * @param bars             the list of bars of the series
     * @param seriesBeginIndex the begin index (inclusive) of the time series
     * @param seriesEndIndex   the end index (inclusive) of the time series
     * @param constrained      true to constrain the time series (i.e. indexes cannot change), false otherwise
     */
    private MT4TimeSeries(String name, List<Bar> bars, int seriesBeginIndex, int seriesEndIndex, boolean constrained) {
        this(name, bars, seriesBeginIndex, seriesEndIndex, constrained, DoubleNum::valueOf);
    }


    /**
     * Constructor.
     *
     * @param name             the name of the series
     * @param bars             the list of bars of the series
     * @param seriesBeginIndex the begin index (inclusive) of the time series
     * @param seriesEndIndex   the end index (inclusive) of the time series
     * @param constrained      true to constrain the time series (i.e. indexes cannot change), false otherwise
     */
    private MT4TimeSeries(String name, List<Bar> bars, int seriesBeginIndex, int seriesEndIndex, boolean constrained,
                          Function<Number, Num> numFunction) {
        this.name = name;

        this.bars = bars;
        if (bars.isEmpty()) {
            // Bar list empty
            this.seriesBeginIndex = -1;
            this.seriesEndIndex = -1;
            this.constrained = false;
            this.numFunction = numFunction;
            return;
        }
        // Bar list not empty: take Function of first bar
        this.numFunction = bars.get(0).getClosePrice().function();
        // Bar list not empty: checking num types
        if (!checkBars(bars)) {
            throw new IllegalArgumentException(
                    String.format("Num implementation of bars: %s" +
                                    " does not match to Num implementation of time series: %s",
                            bars.get(0).getClosePrice().getClass(), numFunction));
        }
        // Bar list not empty: checking indexes
        if (seriesEndIndex < seriesBeginIndex - 1) {
            throw new IllegalArgumentException("End index must be >= to begin index - 1");
        }
        if (seriesEndIndex >= bars.size()) {
            throw new IllegalArgumentException("End index must be < to the bar list size");
        }
        this.seriesBeginIndex = seriesBeginIndex;
        this.seriesEndIndex = seriesEndIndex;
        this.constrained = constrained;
    }


    /**
     * Cuts a list of bars into a new list of bars that is a subset of it
     *
     * @param bars       the list of {@link Bar bars}
     * @param startIndex start index of the subset
     * @param endIndex   end index of the subset
     * @return a new list of bars with tick from startIndex (inclusive) to endIndex (exclusive)
     */
    private static List<Bar> cut(List<Bar> bars, final int startIndex, final int endIndex) {
        return new ArrayList<>(bars.subList(startIndex, endIndex));
    }

    /**
     * @param series a time series
     * @param index  an out of bounds bar index
     * @return a message for an OutOfBoundsException
     */
    private static String buildOutOfBoundsMessage(MT4TimeSeries series, int index) {
        return String.format("Size of series: %s bars, %s bars removed, index = %s",
                series.bars.size(), series.removedBarsCount, index);
    }

    /**
     * Returns a new BaseTimeSeries that is a subset of this BaseTimeSeries.
     * The new series holds a copy of all {@link Bar bars} between <tt>startIndex</tt> (inclusive) and <tt>endIndex</tt> (exclusive)
     * of this TimeSeries.
     * The indices of this TimeSeries and the new subset TimeSeries can be different. I. e. index 0 of the new TimeSeries will
     * be index <tt>startIndex</tt> of this TimeSeries.
     * If <tt>startIndex</tt> < this.seriesBeginIndex the new TimeSeries will start with the first available Bar of this TimeSeries.
     * If <tt>endIndex</tt> > this.seriesEndIndex+1 the new TimeSeries will end at the last available Bar of this TimeSeries
     *
     * @param startIndex the startIndex
     * @param endIndex   the endIndex (exclusive)
     * @return a new BaseTimeSeries with Bars from <tt>startIndex</tt> to <tt>endIndex</tt>-1
     */
    @Override
    public TimeSeries getSubSeries(int startIndex, int endIndex) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException
                    (String.format("the endIndex: %s must be bigger than startIndex: %s", endIndex, startIndex));
        }
        if (!bars.isEmpty()) {
            int start = Math.max(startIndex, this.seriesBeginIndex);
            int end = Math.min(endIndex, this.seriesEndIndex + 1);
            return new MT4TimeSeries(getName(), cut(bars, start, end), numFunction);
        }
        return new MT4TimeSeries(name, numFunction);

    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void setTimeSeriesRepo(TimeSeriesRepo timeSeriesRepo) {
        this.timeSeriesRepo = timeSeriesRepo;
    }

    @Override
    public TimeSeriesRepo getTimeSeriesRepo() {
        return timeSeriesRepo;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    @Override
    public Num numOf(Number number) {
        return this.numFunction.apply(number);
    }

    @Override
    public Function<Number, Num> function() {
        return numFunction;
    }

    /**
     * Checks if all {@link Bar bars} of a list fits to the {@link Num NumFunction} used by this time series.
     *
     * @param bars a List of Bar objects.
     * @return false if a Num implementation of at least one Bar does not fit.
     */
    private boolean checkBars(List<Bar> bars) {
        for (Bar bar : bars) {
            if (!checkBar(bar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the {@link Num} implementation of a {@link Bar} fits to the NumFunction used by time series.
     *
     * @param bar a Bar object.
     * @return false if another Num implementation is used than by this time series.
     * @see Num
     * @see Bar
     * @see #addBar(Duration, ZonedDateTime)
     */
    private boolean checkBar(Bar bar) {
        if (bar.getClosePrice() == null) {
            return true; // bar has not been initialized with data (uses deprecated constructor)
        }
        // all other constructors initialize at least the close price, check if Num implementation fits to numFunction
        Class<? extends Num> f = numOf(1).getClass();
        return f == bar.getClosePrice().getClass() || bar.getClosePrice().equals(NaN);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOhlcvFileName() {
        return ohlcvFileName;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }


    @Override
    public Bar getBar(int i) {
        int innerIndex = i - removedBarsCount;
        if (innerIndex < 0) {
            if (i < 0) {
                // Cannot return the i-th bar if i < 0
                throw new IndexOutOfBoundsException(buildOutOfBoundsMessage(this, i));
            }
            //log.trace("Time series `{}` ({} bars): bar {} already removed, use {}-th instead", name, bars.size(), i, removedBarsCount);
            if (bars.isEmpty()) {
                throw new IndexOutOfBoundsException(buildOutOfBoundsMessage(this, removedBarsCount));
            }
            innerIndex = 0;
        } else if (innerIndex >= bars.size()) {
            // Cannot return the n-th bar if n >= bars.size()
            throw new IndexOutOfBoundsException(buildOutOfBoundsMessage(this, i));
        }
        return bars.get(innerIndex);
    }

    @Override
    public int getBarCount() {
        if (seriesEndIndex < 0) {
            return 0;
        }
        final int startIndex = Math.max(removedBarsCount, seriesBeginIndex);
        return seriesEndIndex - startIndex + 1;
    }

    @Override
    public List<Bar> getBarData() {
        return bars;
    }

    @Override
    public int getBeginIndex() {
        return seriesBeginIndex;
    }

    @Override
    public int getEndIndex() {
        return seriesEndIndex;
    }

    @Override
    public ZonedDateTime getEndTime() {
        return seriesEndTime;
    }

    @Override
    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void setCurrentTime(ZonedDateTime time) {
        currentTime = time;
        currentIndex = getIndex(time);
        currentBar = bars.get(currentIndex);
        if (currentIndex>0) {
            prevIndex=currentIndex-1;
            prevBar=bars.get(prevIndex);
        }
    }

    @Override
    public int getMaximumBarCount() {
        return maximumBarCount;
    }

    @Override
    public void setMaximumBarCount(int maximumBarCount) {
        if (constrained) {
            throw new IllegalStateException("Cannot set a maximum bar count on a constrained time series");
        }
        if (maximumBarCount <= 0) {
            throw new IllegalArgumentException("Maximum bar count must be strictly positive");
        }
        this.maximumBarCount = maximumBarCount;
        removeExceedingBars();
    }

    @Override
    public int getRemovedBarsCount() {
        return removedBarsCount;
    }

    /**
     * @param bar the <code>Bar</code> to be added
     * @apiNote to add bar data directly use #addBar(Duration, ZonedDateTime, Num, Num, Num, Num, Num)
     */
    @Override
    public void addBar(Bar bar, boolean replace) {
        Objects.requireNonNull(bar);
        if (!checkBar(bar)) {
            throw new IllegalArgumentException(String.format("Cannot add Bar with data type: %s to series with data" +
                    "type: %s", bar.getClosePrice().getClass(), numOf(1).getClass()));
        }
        if (!bars.isEmpty()) {
            if (replace) {
                bars.set(bars.size() - 1, bar);
                return;
            }
            final int lastBarIndex = bars.size() - 1;
            ZonedDateTime seriesEndTime = bars.get(lastBarIndex).getEndTime();
            if (seriesEndTime.isAfter(bar.getEndTime())) {   // eredeti :   !bar.getEndTime().isAfter(seriesEndTime)
                throw new IllegalArgumentException(
                        String.format("Cannot add a bar with end time:%s that is <= to series end time: %s",
                                bar.getEndTime(),
                                seriesEndTime));
            }
            if (seriesEndTime.equals(bar.getEndTime())) return;
        }

        bars.add(bar);
        if (seriesBeginIndex == -1) {
            // Begin index set to 0 only if it wasn't initialized
            seriesBeginIndex = 0;
        }
        seriesEndIndex++;
        seriesEndTime = bar.getEndTime();
        removeExceedingBars();
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime) {
        this.addBar(new BaseBar(timePeriod, endTime, function()));
    }

    @Override
    public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume) {
        this.addBar(new BaseBar(endTime, openPrice, highPrice, lowPrice, closePrice, volume, numOf(0)));
    }

    @Override
    public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume, Num amount) {
        this.addBar(new BaseBar(endTime, openPrice, highPrice, lowPrice, closePrice, volume, amount));
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                       Num closePrice, Num volume) {
        this.addBar(new BaseBar(timePeriod, endTime, openPrice, highPrice, lowPrice, closePrice, volume, numOf(0)));
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                       Num closePrice, Num volume, Num amount) {
        this.addBar(new BaseBar(timePeriod, endTime, openPrice, highPrice, lowPrice, closePrice, volume, amount));
    }

    @Override
    public void addTrade(Number price, Number amount) {
        addTrade(numOf(price), numOf(amount));
    }

    @Override
    public void addTrade(String price, String amount) {
        addTrade(numOf(new BigDecimal(price)), numOf(new BigDecimal(amount)));
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        getLastBar().addTrade(tradeVolume, tradePrice);
    }

    @Override
    public void addPrice(Num price) {
        getLastBar().addPrice(price);
    }

    @Override
    public TreeMap<ZonedDateTime, Integer> getTimeTreeMap() {
        return timeTreeMap;
    }

    @Override
    public int getIndex(ZonedDateTime time) {
//        int index = -1;
//        if (seriesBeginIndex>-1) {
//            try {
//                index = timeTreeMap.get(timeTreeMap.headMap(time, false).lastKey()) + 1;
//                if (index >= getEndIndex()) index = getEndIndex();
//            } catch (NoSuchElementException e) {
//                if (time.isEqual(getBar(0).getEndTime())) index = 0;
//                else {
//                    index=-1;
//                }
//            }
//        }
//        return index;
        if (seriesEndIndex < 0) return -1;

        ZonedDateTime key = timeTreeMap.higherKey(time);
        if (key == null)
            return timeTreeMap.get(timeTreeMap.lastKey());  // csak kisebb kulcsok vannak visszaadjuk az utolsót (t-1)

        int index = timeTreeMap.get(key);
        if (index > 0) return index - 1;
        else return -1;  // aktuálisat nem adhatjuk vissza mert az még nem lehet up-to-date

    }

    /**
     * Removes the N first bars which exceed the maximum bar count.
     */
    private void removeExceedingBars() {
        int barCount = bars.size();
        if (barCount > maximumBarCount) {
            // Removing old bars
            int nbBarsToRemove = barCount - maximumBarCount;
            for (int i = 0; i < nbBarsToRemove; i++) {
                bars.remove(0);
            }
            // Updating removed bars count
            removedBarsCount += nbBarsToRemove;
            seriesEndTime = bars.get(getEndIndex()).getEndTime();
        }
    }

    public static class SeriesBuilder implements TimeSeriesBuilder {

        private static final long serialVersionUID = 111164611841087550L;
        /**
         * Default Num type function
         **/
        private static Function<Number, Num> defaultFunction = DoubleNum::valueOf;
        private List<Bar> bars;
        private String name;
        private Function<Number, Num> numFunction;
        private Function<String, Num> stringToNumFunction;
        private boolean constrained;
        private int maxBarCount;
        private int period;
        private List<String> indicatorFiles = new ArrayList<>();
        private String ohlcvFileName, ohlcvMessage;  // ohlcvMessage esetén  time;open;high;low;close;volume\n
        private String dateFormatPattern = "yyyy.MM.dd HH:mm";
        private String symbol;
        DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
        public TreeMap<ZonedDateTime, Integer> timeTreeMap = new TreeMap<>();

        public SeriesBuilder() {
            initValues();
        }

        public static void setDefaultFunction(Function<Number, Num> defaultFunction) {
            SeriesBuilder.defaultFunction = defaultFunction;
        }

        private void initValues() {
            this.bars = new ArrayList<>();
            this.name = "unnamed_series";
            this.numFunction = SeriesBuilder.defaultFunction;
            this.constrained = false;
            this.maxBarCount = Integer.MAX_VALUE;
            this.period = 0;
        }

        public MT4TimeSeries buildFromSeries(MT4TimeSeries mT4TimeSeries) {
            if (period == 0 || period % mT4TimeSeries.period != 0) {
                System.out.println(("Incompatible time periods!"));
                return null;
            }

            List<Bar> sortedBars = new ArrayList<>();
            int beginIndex = -1;
            int endIndex = -1;
            Double openPrice = null, closePrice = null, highPrice = null, lowPrice = null;
            double summaAmount = 0.0;
            ZonedDateTime nextBarDate = null, firstBarDate = null;
            ZonedDateTime lastDate = null;
            int firstRowIndex = 1;

            long barDurationSeconds = period * 60;
            Duration barDuration = Duration.ofSeconds(barDurationSeconds);
            ChronoUnit cu = null;
            ZonedDateTime firstDate = mT4TimeSeries.bars.get(0).getEndTime();
            lastDate = mT4TimeSeries.bars.get(mT4TimeSeries.bars.size() - 1).getEndTime();

            nextBarDate = getNextBarDate(firstDate);

            openPrice = mT4TimeSeries.bars.get(0).getOpenPrice().doubleValue();

            closePrice = 0.0;
            highPrice = 0.0;
            lowPrice = Double.MAX_VALUE;
            Bar newBar = null;
            ZonedDateTime lastBarTime = mT4TimeSeries.bars.get(0).getEndTime();

//            ZonedDateTime zonedDateTime=null;
//            try {
//                String dateFormatPattern = "dd.MM.yyyy HH:mm";
//                SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatPattern);
//                 zonedDateTime=dateFormatter.parse("25.05.2018 21:00:00.000").toInstant().atZone(ZoneId.systemDefault());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            for (Bar bar : mT4TimeSeries.bars) {
                ZonedDateTime instantTime = bar.getEndTime();

                if (instantTime.isAfter(nextBarDate)) {    // tervezettől nagyobb időugrás, jellemzően adatkimaradás
                    if (summaAmount > 0.0) {  // ha volt adat
                        newBar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                , numFunction.apply(summaAmount), numFunction.apply(0));

                        sortedBars.add(newBar);
                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);

                    }
                    nextBarDate = getNextBarDate(instantTime);


                    openPrice = bar.getOpenPrice().doubleValue();
                    summaAmount = 0.0;
                    highPrice = 0.0;
                    lowPrice = Double.MAX_VALUE;


                } else {

                    if (openPrice == 0.0) openPrice = bar.getOpenPrice().doubleValue();


                    summaAmount += bar.getVolume().doubleValue();
                    closePrice = bar.getClosePrice().doubleValue();
                    if (bar.getHighPrice().doubleValue() > highPrice) highPrice = bar.getHighPrice().doubleValue();
                    if (bar.getLowPrice().doubleValue() < lowPrice) lowPrice = bar.getLowPrice().doubleValue();

                    if (instantTime.isAfter(nextBarDate.minusSeconds(1)) || instantTime.equals(lastDate)) {

                        newBar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                , numFunction.apply(summaAmount), numFunction.apply(0));
                        sortedBars.add(newBar);
                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);

//                    openPrice = bar.getOpenPrice().doubleValue();
                        nextBarDate = instantTime.plus(period, ChronoUnit.MINUTES);
//                    if (nextBarDate.isBefore(instantTime)) {
////                        System.out.println("STOP");
//                        while (nextBarDate.isBefore(instantTime))
//                            nextBarDate = nextBarDate.plus(period, ChronoUnit.MINUTES);
//                    }
                        openPrice = 0.0;
                        summaAmount = 0.0;
                        highPrice = 0.0;
                        lowPrice = Double.MAX_VALUE;
                    }

                }

                lastBarTime = instantTime;
            }


            if (!sortedBars.isEmpty()) {
                beginIndex = 0;
                endIndex = sortedBars.size() - 1;
            }
            MT4TimeSeries series = new MT4TimeSeries(name, sortedBars, beginIndex, endIndex, constrained, numFunction);
            series.timeTreeMap = timeTreeMap;
            series.setMaximumBarCount(maxBarCount);
            series.setPeriod(period);
            series.ohlcvFileName = mT4TimeSeries.ohlcvFileName;
            series.seriesEndTime = series.getBar(series.getEndIndex()).getEndTime();
            series.symbol=symbol;

            initValues(); // reinitialize values for next series
            return series;
        }

        public MT4TimeSeries build() {
            int beginIndex = -1;
            int endIndex = -1;


            List<Bar> sortedBars = new ArrayList<>();


            boolean hasIndicator = false;
            HashMap<String, List<String[]>> indicators = new HashMap<>();
            for (int i = 0; i < indicatorFiles.size(); i++) {
                indicators.put(indicatorFiles.get(i), loadCsvData(indicatorFiles.get(i)));
            }
            if (indicatorFiles.size() > 0) hasIndicator = true;

            long barDurationSeconds = period * 60;
            Duration barDuration = Duration.ofSeconds(barDurationSeconds);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatPattern);
            DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);


            String date = "EMPTY";
            HashMap<ZonedDateTime, Bar> bars = new HashMap();


            ZonedDateTime lastDate = null;
            int firstRowIndex = 1;
            long dataPeriodSeconds = 60;  // defaultból 1 perces streamet várunk
            List<String[]> ohlcv = new ArrayList<>();
            ZonedDateTime firstDate = ZonedDateTime.now(ZoneId.systemDefault());
            ;
            Bar bar = null;
            ZonedDateTime zonedDateTime = null;

            Double openPrice = null, closePrice = null, highPrice = null, lowPrice = null;
            boolean doAggregate = false;
            double summaAmount = 0.0;
            ZonedDateTime nextBarDate = null, firstBarDate = null;

//            ZonedDateTime.parse(priceData[1], zdtFormatter.withZone(ZoneId.systemDefault()));

            try {
                if (ohlcvFileName != null) {
                    ohlcv = loadCsvData(ohlcvFileName);
                    for (String item : ohlcv.get(0)) {
                        if (item.matches(".*[0-9].*")) {
                            firstRowIndex = 0;
                            break;
                        }  // header kiszűrése
                    }
                    dataPeriodSeconds = ChronoUnit.SECONDS.between(dateFormatter.parse(ohlcv.get(firstRowIndex)[0]).toInstant(),
                            dateFormatter.parse(ohlcv.get(firstRowIndex + 1)[0]).toInstant());
                    if (dataPeriodSeconds > barDurationSeconds) {
                        throw new Exception("Incompatible data and timeFrame "+ohlcv.get(firstRowIndex + 1)[0]);
                    }
//                    firstDate = dateFormatter.parse(ohlcv.get(firstRowIndex)[0]).toInstant();
                    firstDate = ZonedDateTime.parse(ohlcv.get(firstRowIndex)[0], zdtFormatter.withZone(ZoneId.systemDefault()));
                    lastDate = ZonedDateTime.parse(ohlcv.get(ohlcv.size() - 1)[0], zdtFormatter.withZone(ZoneId.systemDefault()));
                    openPrice = Double.parseDouble(ohlcv.get(firstRowIndex)[1]);
                }

//            if (myString.matches(".*[0-9].*"))


                if (dataPeriodSeconds < barDurationSeconds) doAggregate = true;

                nextBarDate = getNextBarDate(firstDate);

                summaAmount = 0.0;
                highPrice = 0.0;
                lowPrice = Double.MAX_VALUE;
                if (ohlcv.size() > 0) openPrice = Double.parseDouble(ohlcv.get(firstRowIndex)[1]);


            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (ohlcvFileName != null) {
                boolean skip = true;
                try {
                    for (String[] priceData : ohlcv) {
                        if (firstRowIndex == 1 && skip) {
                            skip = false;
                            continue;
                        }
//                        Instant instantTime = dateFormatter.parse(priceData[0]).toInstant();

                        ZonedDateTime time = ZonedDateTime.parse(priceData[0], zdtFormatter.withZone(ZoneId.systemDefault()));
//                    if (priceData[0].equals("25.05.2018 20:59:00.000")) {
//                        System.out.println("stop");
//                    }

                        if (doAggregate) {
//                        ZonedDateTime zd=nextBarDate.atZone(ZoneId.systemDefault());

                            if (time.isAfter(nextBarDate)) {    // tervezettől nagyobb időugrás, jellemzően adatkimaradás
                                if (summaAmount > 0.0) {  // ha volt adat
                                    bar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                            , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                            , numFunction.apply(summaAmount), numFunction.apply(0));

                                    if (hasIndicator) bars.put(nextBarDate, bar);
                                    else {
                                        sortedBars.add(bar);
                                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);
                                    }

                                }
                                nextBarDate = getNextBarDate(time);


                                openPrice = Double.parseDouble(priceData[1]);
                                summaAmount = 0.0;
                                highPrice = 0.0;
                                lowPrice = Double.MAX_VALUE;


                            } else {


                                summaAmount += Double.parseDouble(priceData[5]);
                                closePrice = Double.parseDouble(priceData[4]);
                                if (Double.parseDouble(priceData[2]) > highPrice)
                                    highPrice = Double.parseDouble(priceData[2]);
                                if (Double.parseDouble(priceData[3]) < lowPrice)
                                    lowPrice = Double.parseDouble(priceData[3]);
//                        if (zd.getDayOfWeek().equals(DayOfWeek.SATURDAY) || zd.getDayOfWeek().equals(DayOfWeek.SUNDAY)) continue;
                                if (time.isAfter(nextBarDate.minusSeconds(1)) || time.equals(lastDate)) {
//                                ZonedDateTime barTime = instantTime.atZone(ZoneId.systemDefault());
                                    bar = new BaseBar(barDuration, nextBarDate, numFunction.apply(openPrice), numFunction.apply(highPrice)
                                            , numFunction.apply(lowPrice), numFunction.apply(closePrice)
                                            , numFunction.apply(summaAmount), numFunction.apply(0));
                                    if (hasIndicator) bars.put(nextBarDate, bar);
                                    else {
                                        sortedBars.add(bar);
                                        timeTreeMap.put(nextBarDate, sortedBars.size() - 1);
                                    }

                                    openPrice = Double.parseDouble(priceData[1]);
//                            firstBarDate = nextBarDate;
                                    nextBarDate = time.plus(period, ChronoUnit.MINUTES);
                                    summaAmount = 0.0;
                                    highPrice = 0.0;
                                    lowPrice = Double.MAX_VALUE;
                                }
                            }


                        } else {
                            zonedDateTime = time.plusSeconds(barDurationSeconds);
                            bar = new BaseBar(barDuration, zonedDateTime, stringToNumFunction.apply(priceData[1]), stringToNumFunction.apply(priceData[2])
                                    , stringToNumFunction.apply(priceData[3]), stringToNumFunction.apply(priceData[4])
                                    , stringToNumFunction.apply(priceData[5]), stringToNumFunction.apply("0.0"));

                            if (hasIndicator) bars.put(zonedDateTime, bar);
                            else {
                                sortedBars.add(bar);
                                timeTreeMap.put(zonedDateTime, sortedBars.size() - 1);
                            }


                        }
                    }

                    for (String indicatorName : indicators.keySet()) {
                        int bufferNumber = indicators.get(indicatorName).get(0).length - 1;

                        List<String[]> cc = indicators.get(indicatorName);
                        for (String[] indicatorBuffers : indicators.get(indicatorName)) {
                            zonedDateTime = dateFormatter.parse(indicatorBuffers[0]).toInstant().atZone(ZoneId.systemDefault());
                            DoubleNum[] bufferValues = new DoubleNum[bufferNumber];
                            for (int i = 0; i < bufferNumber; i++)
                                bufferValues[i] = DoubleNum.valueOf(indicatorBuffers[i + 1]);
                            if (bars.containsKey(zonedDateTime)) {
                                bar = bars.get(zonedDateTime);
                                bar.addBuffer(indicatorName, bufferValues);
                            }
                        }

                    }

                } catch (Exception e) {
                    System.out.println("DATE: " + date);
                    e.printStackTrace();
                }


                if (hasIndicator) {
                    sortedBars = bars.entrySet().stream().sorted(Map.Entry.<ZonedDateTime, Bar>comparingByKey()).map(sortedBar -> sortedBar.getValue()).collect(Collectors.toList());
                    int i = 0;
                    for (Bar cBar : sortedBars) {
                        timeTreeMap.put(cBar.getBeginTime(), i);
                        i++;
                    }
                }

                if (!sortedBars.isEmpty()) {
                    beginIndex = 0;
                    endIndex = sortedBars.size() - 1;
                }
            }
            MT4TimeSeries series = new MT4TimeSeries(name, sortedBars, beginIndex, endIndex, constrained, numFunction);
            series.timeTreeMap = timeTreeMap;
            series.setMaximumBarCount(maxBarCount);
            series.setPeriod(period);
            series.ohlcvFileName = ohlcvFileName;
            series.dateFormatPattern = dateFormatPattern;
            series.firstBarDate = firstBarDate;
            series.nextBarDate = nextBarDate;
            series.doAggregate = doAggregate;
            series.barDuration = barDuration;
            series.symbol = symbol;
            if (series.getEndIndex() > -1) series.seriesEndTime = series.getBar(series.getEndIndex()).getEndTime();
            initValues(); // reinitialize values for next series
            return series;
        }

        ZonedDateTime getNextBarDate(ZonedDateTime time) {
            ChronoUnit cu;
            if (period == 10080) cu = ChronoUnit.WEEKS;
            else if (period == 1440) cu = ChronoUnit.DAYS;
            else cu = ChronoUnit.HOURS;

            ZonedDateTime firstBarDate = time.truncatedTo(cu);
            if (cu == ChronoUnit.HOURS) {
                while (firstBarDate.plus(period, ChronoUnit.MINUTES).isBefore(time)) {
                    firstBarDate = firstBarDate.plus(period, ChronoUnit.MINUTES);
                }
            }
            return firstBarDate.plus(period, ChronoUnit.MINUTES);
        }

        public SeriesBuilder setConstrained(boolean constrained) {
            this.constrained = constrained;
            return this;
        }

        public SeriesBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public SeriesBuilder withSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public SeriesBuilder withDateFormatPattern(String dateFormatPattern) {
            this.dateFormatPattern = dateFormatPattern;
            return this;
        }

        public SeriesBuilder withPeriod(int period) {
            this.period = period;
            return this;
        }

        public SeriesBuilder withOhlcvFileName(String ohlcvFileName) {
            this.ohlcvFileName = ohlcvFileName;
            return this;
        }

        public SeriesBuilder withIndicatorFileName(String indicatorFileName) {
            indicatorFiles.add(indicatorFileName);
            return this;
        }


        public SeriesBuilder withBars(List<Bar> bars) {
            this.bars = bars;
            return this;
        }

        public SeriesBuilder withMaxBarCount(int maxBarCount) {
            this.maxBarCount = maxBarCount;
            return this;
        }

        public SeriesBuilder withNumTypeOf(Num type) {
            numFunction = type.function();
            return this;
        }

        public SeriesBuilder withNumTypeOf(Function<Number, Num> function) {
            numFunction = function;
            return this;
        }

        public SeriesBuilder withNumTypeOf(Class<? extends Num> abstractNumClass) {
            if (abstractNumClass == DoubleNum.class) {
                numFunction = DoubleNum::valueOf;
                stringToNumFunction = DoubleNum::valueOf;
                return this;
            } else if (abstractNumClass == DoubleNum.class) {
                numFunction = DoubleNum::valueOf;
                stringToNumFunction = DoubleNum::valueOf;
                return this;
            }
            numFunction = DoubleNum::valueOf;
            stringToNumFunction = DoubleNum::valueOf;
            return this;
        }

    }

    //----------------------------------------------------------
    private static List<String[]> loadCsvData(String fileName) {

        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\ta4j-main\\resources\\" + fileName));

            int index = 0;
            List<String[]> result = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);
                result.add(row);
            }
            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    ZonedDateTime getNextBarDate(ZonedDateTime time) {
        ChronoUnit cu;
        if (period == 10080) cu = ChronoUnit.WEEKS;
        else if (period == 1440) cu = ChronoUnit.DAYS;
        else cu = ChronoUnit.HOURS;

        firstBarDate = time.truncatedTo(cu);
        if (cu == ChronoUnit.HOURS) {
            while (firstBarDate.plus(period, ChronoUnit.MINUTES).isBefore(time)) {
                firstBarDate = firstBarDate.plus(period, ChronoUnit.MINUTES);
            }
        }
        return firstBarDate.plus(period, ChronoUnit.MINUTES);
    }


    public void setBid(double value) {
        bid = value;
    }

    public double getBid() {
        return bid;
    }

    public void setAsk(double value) {
        ask = value;
    }

    public double getAsk() {
        return ask;
    }

    @Override
    public String toString(Bar bar){
        StringBuffer sBuffer=new StringBuffer("");
        sBuffer.append(zdtFormatter.format(bar.getBeginTime())).append("|").append(bar.getOpenPrice()).append("|").append(bar.getHighPrice()).append("|").append(bar.getLowPrice()).append("|").append(bar.getClosePrice()).append("|").append(bar.getVolume());
        return sBuffer.toString();
    }
}
