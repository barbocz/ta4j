package org.strategy;

import javafx.application.Platform;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.test.TradeCenter;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeSeriesRepo implements TimeSeriesRepository {
    public MT4TimeSeries coreSeries = null;
    public TreeMap<Integer, MT4TimeSeries> timeSeries = new TreeMap<>();
    public HashMap<Integer, Integer> barIndex = new HashMap<>();   //HashMap<timeFrame, index>
    public String symbol = null;
    public String coreMessage = "";
    public int portNumber;
    public Double bid, ask;
    public String metaTradeTimeZone="CET";
    public DateTimeFormatter zdtFormatter,zdtFormatterWithSeconds;
    //    private TradeCenter tradeCenter;
    public List<TradeEngine> tradeEngines = new ArrayList<>();


    public CountDownLatch tickLatch, barChangeLatch, oneMinuteDataLatch;
    //    public List<String> onEventStrategies = new ArrayList<>();
    List<Integer> timeFramesForBarchange = new ArrayList<>();

    Instant lastMinuteBarTime = null;

    public enum ProcessType {
        MT4,
        FILE
    }

    public ProcessType processType = ProcessType.FILE;

    public TimeSeriesRepo(String symbol, int portNumber, TradeCenter tradeCenter) {
        this.symbol = symbol;
        this.portNumber = portNumber;
//        this.tradeCenter = tradeCenter;

        System.out.println("TimeSeriesRepo init: " + portNumber + "  - " + symbol);

//        if (processType == ProcessType.MT4) System.out.println("processType:");

        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            metaTradeTimeZone=prop.getProperty("mt4.timeZone");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.of(metaTradeTimeZone));
        zdtFormatterWithSeconds = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.of(metaTradeTimeZone));

//        coreSeries = new MT4TimeSeries.SeriesBuilder().
//                withName(symbol).
//                withPeriod(1).
//                withDateFormatPattern("dd.MM.yyyy HH:mm").
//                withNumTypeOf(DoubleNum.class).
//                build();
//        timeSeries.put(1, coreSeries);
//        coreSeries.registerOnEventListener(this);

        Runnable myRunnable =
                new Runnable() {
                    public void run() {
                        try (ZContext context = new ZContext()) {
                            // Socket to talk to clients
                            System.out.println(symbol + " price feeding started on port " + portNumber);
                            ZMQ.Socket TimeSeriesSocket = context.createSocket(SocketType.REP);
                            TimeSeriesSocket.bind("tcp://*:" + portNumber);

                            int requestedBarNumber = 3000;

                            while (!Thread.currentThread().isInterrupted()) {
                                byte[] reply = TimeSeriesSocket.recv(0);
                                String message = new String(reply, ZMQ.CHARSET);
                                final String s;
                                long ellapsedMinuteSinceLastMessage = 0;
                                String response = "ok";


//                                System.out.println("TimeSeriesRepo message: "+message);
                                String items[] = message.split(";");
//                                System.out.println(message);

                                // MT4 üzenet típusok
                                // T;2010.01.20 20:15;1.12345;1.23456  - Tick esemény a Bid,Ask értékek küldésére
                                // P;2010.01.20 20:15 - MT4 terminál->this ping
                                // M;2020.01.13 08:09;1.11262;1.11262;1.11251;1.11259;45.0 - Percenkénti OHLCV adatok küldése

                                coreMessage = message;


                                if (lastMinuteBarTime != null) {
                                    try {
                                        ellapsedMinuteSinceLastMessage = ChronoUnit.MINUTES.between(lastMinuteBarTime, ZonedDateTime.parse(items[1], zdtFormatter).toInstant());
                                        if (ellapsedMinuteSinceLastMessage > 1)
                                        {
//                                            System.out.println("MISSED BARS " + ellapsedMinuteSinceLastMessage);
                                            requestedBarNumber=(int)ellapsedMinuteSinceLastMessage;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


//                                dataPeriodSeconds = ChronoUnit.SECONDS.between(dateFormatter.parse(ohlcv.get(firstRowIndex)[0]).toInstant(),
//                                        dateFormatter.parse(ohlcv.get(firstRowIndex + 1)[0]).toInstant());

                                if (items[0].equals("T")) {
                                    bid = Double.valueOf(items[2]);
                                    ask = Double.valueOf(items[3]);
                                    onTickEvent();


//                                } else if (items[0].equals("init")) {
//                                    processType=ProcessType.MT4;
//                                    for (MT4TimeSeries timeSeries : timeSeries.values()) {
//                                        timeSeries.resetBars();
//                                    }
//
//                                    coreMessage = "initiated";
                                    response="ok";

                                } else if (items[0].equals("M")) {
//                                    System.out.println(message);
                                    timeFramesForBarchange.clear();
                                    for (Integer timeFrame : timeSeries.keySet()) {

//                                            if (timeFrame==timeSeries.lastKey()) locked=false; // csak akkor indítsa az onOneMinuteDataEvent és onBarChangeEvent eseményeket ha elért az utolsó timeFrame-hez
                                        timeSeries.get(timeFrame).updateBar(message);


                                    }
                                    onBarChangeEventProcess();  // be kell várni az összes timeFramehez szükséges bar képzését
                                    try {
                                        lastMinuteBarTime=ZonedDateTime.parse(items[1], zdtFormatter).toInstant();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else if (items[0].equals("P")) {
//                                    System.out.println("PINGED: "+message);

                                    coreMessage = "pinged";
                                    response = Integer.toString(requestedBarNumber);
                                }


                                TimeSeriesSocket.send(response.getBytes(ZMQ.CHARSET), 0);

                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {

                                        tradeCenter.updateTimeSeriesRepo(portNumber, coreMessage);
                                    }
                                });

//                                try {
//                                    Thread.sleep(1000); //  Do some 'work'
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }
                        }
                    }
                };


        Thread thread = new Thread(myRunnable);
        thread.start();

    }


    public TimeSeriesRepo() {
        this("EURUSD_TEST");
    }

    public TimeSeriesRepo(String ohlcvFile) {
        coreSeries = new MT4TimeSeries.SeriesBuilder().
                withName(ohlcvFile).
                withPeriod(1).
                withOhlcvFileName(ohlcvFile).
                withDateFormatPattern("dd.MM.yyyy HH:mm:ss.SSS").
                withNumTypeOf(DoubleNum.class).
                build();
        setTimeSeries(1);
    }

    public TimeSeriesRepo(String ohlcvFile, String dateFormatPattern) {
        coreSeries = new MT4TimeSeries.SeriesBuilder().
                withName(ohlcvFile).
                withPeriod(1).
                withOhlcvFileName(ohlcvFile).
                withDateFormatPattern(dateFormatPattern).
                withNumTypeOf(DoubleNum.class).
                build();
        setTimeSeries(1);
    }

    public TimeSeriesRepo(String symbol, String ohlcvFile, String dateFormatPattern) {
        this.symbol = symbol;
        coreSeries = new MT4TimeSeries.SeriesBuilder().
                withName(ohlcvFile).
                withPeriod(1).
                withSymbol(symbol).
                withOhlcvFileName(ohlcvFile).
                withDateFormatPattern(dateFormatPattern).
                withNumTypeOf(DoubleNum.class).
                build();

        setTimeSeries(1);
    }

    public TimeSeriesRepo(MT4TimeSeries MT4series) {
        coreSeries = MT4series;
    }

    public void setTimeSeries(Integer timeFrame) {
        if (!timeSeries.containsKey(timeFrame)) {
            MT4TimeSeries mt4TimeSeries = null;
            if (coreSeries == null) {

                mt4TimeSeries = new MT4TimeSeries.SeriesBuilder().
                        withName(symbol).
                        withPeriod(timeFrame).
                        withDateFormatPattern("dd.MM.yyyy HH:mm").
                        withNumTypeOf(DoubleNum.class).
                        build();
//                if (timeFrame==1) coreSeries=mt4TimeSeries;
            } else {
                if (timeFrame == 1) mt4TimeSeries = coreSeries;
                else
                    mt4TimeSeries = new MT4TimeSeries.SeriesBuilder().withPeriod(timeFrame).withSymbol(coreSeries.getSymbol()).buildFromSeries(coreSeries);
            }
            mt4TimeSeries.setTimeSeriesRepo(this);  // callback miatt
            timeSeries.put(timeFrame, mt4TimeSeries);


//            timeSeries.put(timeFrame, new MT4TimeSeries.SeriesBuilder().withPeriod(timeFrame).buildFromSeries(coreSeries));

//            System.out.println("**** "+Thread.currentThread().getStackTrace()[2]);
//            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
//            for (int i = 1; i < stElements.length; i++) {
//                StackTraceElement ste = stElements[i];
//
//                System.out.println(i + ". " + ste.getClassName());
//
//            }
        }
    }

    public TimeSeries getTimeSeries(Integer timeFrame) {
//        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
//        for (int i=1; i<stElements.length; i++) {
//            StackTraceElement ste = stElements[i];
//
//                System.out.println(i+". "+ste.getClassName());
//
//        }

        if (!timeSeries.containsKey(timeFrame)) setTimeSeries(timeFrame);
        return timeSeries.get(timeFrame);


//        for (Integer tf : onEventStrategies.keySet()) {
//            System.out.println(tf + ":::::::::");
//            for (TradeEngine st : onEventStrategies.get(tf)) {
//                System.out.println(st.getClass().getName());
//            }
//        }
//        System.out.println("");

    }

//    void registerStrategyOnEvent(Integer timeFrame, MT4TimeSeries mt4TimeSeries) {
//
//        List<TradeEngine> registeredStrategies = new ArrayList<>();
//        TradeEngine currentStrategy;
//        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
//        for (int i = 1; i < stElements.length; i++) {
//            String symbol = stElements[i].getClassName();
//            if (strategies.containsKey(symbol)) {
//                currentStrategy = strategies.get(symbol);
//                if (onEventStrategies.containsKey(timeFrame)) {
//                    registeredStrategies = onEventStrategies.get(timeFrame);
//                    if (registeredStrategies.contains(currentStrategy)) break;
//                    else {
//                        registeredStrategies.add(currentStrategy);
//                        onEventStrategies.replace(timeFrame, registeredStrategies);
//                    }
//                } else {
//                    registeredStrategies.add(currentStrategy);
//                    onEventStrategies.put(timeFrame, registeredStrategies);
//                }
//                break;
//            }
//        }
//
//    }

    public void onTickEvent() {
//        System.out.println("TimeSeriesRepo onTickEvent------------- " );
        List<Callable<Integer>> tasks = new ArrayList<>();

        int latchCounter = 0;
        for (TradeEngine tradeEngine : tradeEngines) {

            latchCounter++;
            Callable<Integer> task = () -> {
                try {
                    tradeEngine.onTickEvent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tickLatch.countDown();
                return 1;
            };
            tasks.add(task);

        }
        if (tasks.size() == 0) return;
        tickLatch = new CountDownLatch(latchCounter);

        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        for (Callable<Integer> task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        try {
            tickLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onBarChangeEventProcess() {
//        System.out.println("onBarChangeEventProcess "+timeFramesForBarchange.size()+" - "+tradeCenter.tradeEngines.size());
        if (timeFramesForBarchange.size() == 0 || tradeEngines.size() == 0) return;

        List<Callable<Integer>> tasks = new ArrayList<>();
        int latchCounter = 0;
        for (Integer changedTimeFrame : timeFramesForBarchange) {
            for (TradeEngine tradeEngine : tradeEngines) {
                if (tradeEngine.timeFrames.contains(changedTimeFrame)) {
                    latchCounter++;
                    Callable<Integer> task = () -> {
                        try {
                            tradeEngine.onBarChangeEvent(changedTimeFrame);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        barChangeLatch.countDown();
                        return 1;
                    };
                    tasks.add(task);
                }
            }
        }
        if (tasks.size() == 0) return;

        barChangeLatch = new CountDownLatch(latchCounter);


        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        for (Callable<Integer> task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        try {
            barChangeLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println("END");

    }

    public void onBarChangeEvent(MT4TimeSeries eventSeries) {


        timeFramesForBarchange.add(eventSeries.period);


    }


    public void onOneMinuteDataEvent(MT4TimeSeries eventSeries) {
//        if (true) return;
//        System.out.println("TimeSeriesRepo onOneMinuteDataEvent------------- " );

        List<Callable<Integer>> tasks = new ArrayList<>();
        if (tradeEngines.size() == 0) return;

        int latchCounter = 0;
        for (TradeEngine tradeEngine : tradeEngines) {

            latchCounter++;
            Callable<Integer> task = () -> {
                try {
                    tradeEngine.onOneMinuteDataEvent();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                oneMinuteDataLatch.countDown();
                return 1;
            };
            tasks.add(task);

        }
        if (tasks.size() == 0) return;
        oneMinuteDataLatch = new CountDownLatch(latchCounter);

        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        for (Callable<Integer> task : tasks) {
            executor.submit(task);
        }

        executor.shutdown();
        try {
            oneMinuteDataLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setBarIndex(ZonedDateTime time) {
        for (Integer timeFrame : timeSeries.keySet()) {
            if (barIndex.containsKey(timeFrame)) {
                barIndex.replace(timeFrame, timeSeries.get(timeFrame).getIndex(time));
            } else barIndex.put(timeFrame, timeSeries.get(timeFrame).getIndex(time));
        }
    }

    public int getIndex(ZonedDateTime time, int timeFrame) {
        return timeSeries.get(timeFrame).getIndex(time);  // -1 szükségese mert különben előrelátnánk
    }

    public void toString(int timeFrame) {
        TimeSeries series = timeSeries.get(timeFrame);

        for (int i = 0; i < series.getEndIndex(); i++) {
            Bar bar = series.getBar(i);
            System.out.println(i + ". " + bar.getEndTime() + ":  " + bar.getOpenPrice() + ", " + bar.getHighPrice() + ", " + bar.getLowPrice() + ", " + bar.getClosePrice() + ",    " + bar.getVolume());

        }


    }

    public void setBid(double value) {
        bid = value;
        onTickEvent();
    }

    public void setAsk(double value) {
        ask = value;
        onTickEvent();
    }


}
