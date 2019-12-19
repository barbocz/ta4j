package org;

import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.KeltnerEntry;
import org.strategy.myEntryStrategies.KeltnerEntryWithCCI;
import org.strategy.myEntryStrategies.TestEntry;
import org.strategy.myExitStrategies.CCIExit;
import org.strategy.myExitStrategies.KeltnerExit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Test {



    public static void main(String[] args) throws Exception {
//        File f = null;
//        String[] paths;
//        long startTime = System.currentTimeMillis();
//        try {
//
//            // create new file
//            f = new File("C:\\Users\\Barbocz Attila\\AppData\\Roaming\\MetaQuotes\\Terminal\\294B6FCE6F709DE82DA4C87FDBF1DE36\\MQL4\\Files\\");
//
//            // array of files and directory
//            paths = f.list();
//
//            // for each name in the path array
//            for(String path:paths) {
//
//                // prints filename and directory name
//               if (path.contains("PVA")) {
//                   File fd = new File("C:\\Users\\Barbocz Attila\\AppData\\Roaming\\MetaQuotes\\Terminal\\294B6FCE6F709DE82DA4C87FDBF1DE36\\MQL4\\Files\\"+path);
//                   System.out.println(path);
//                   fd.delete();
//               }
//            }
//
//        } catch(Exception e) {
//            // if any error occurs
//            e.printStackTrace();
//        }
//        System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + " ms ");

//        try (ZContext context = new ZContext()) {
//            // Socket to talk to clients
//            ZMQ.Socket TimeSeriesSocket = context.createSocket(SocketType.REP);
//            TimeSeriesSocket.bind("tcp://*:5001" );
//
//            while (!Thread.currentThread().isInterrupted()) {
//                byte[] reply = TimeSeriesSocket.recv(0);
//                String message = new String(reply, ZMQ.CHARSET);
//                final String s;
//
//
//
//                                System.out.println("TimeSeriesRepo message: "+message.length()+"  "+message);
//                System.out.println("size: "+message.length());
////                String items[] = message.split(";");
//
//
//                String response = "response msg";
//                TimeSeriesSocket.send(response.getBytes(ZMQ.CHARSET), 0);
//
//
////                                try {
////                                    Thread.sleep(1000); //  Do some 'work'
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                                }
//            }
//        }





        long startTime = System.currentTimeMillis();
//
//
        StringBuffer content=new StringBuffer("");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
        Strategy keltnerEntry=new KeltnerEntry(3,timeSeriesRepo);
        Strategy keltnerEntryWithCCI=new KeltnerEntryWithCCI(3,timeSeriesRepo);
        Strategy testEntry=new TestEntry(3,timeSeriesRepo);

//        TimeSeries ts=timeSeriesRepo.getTimeSeries(3);
//
//        for (int i = 0; i < ts.getEndIndex() ; i++) {
//            content.append(dateFormatter.format(ts.getBar(i).getBeginTime())).append(",").append(ts.getBar(i).getOpenPrice()).
//                    append(",").append(ts.getBar(i).getHighPrice()).
//                    append(",").append(ts.getBar(i).getLowPrice()).
//                    append(",").append(ts.getBar(i).getClosePrice()).
//                    append(",").append(ts.getBar(i).getVolume()).
//                    append("\r\n");
//        }
//
//
//        BufferedWriter exportFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\Barbocz Attila\\AppData\\Roaming\\MetaQuotes\\Terminal\\294B6FCE6F709DE82DA4C87FDBF1DE36\\MQL4\\Files\\eurusd3.csv"), "UTF8"));
//        exportFile.write(content.toString());
//        exportFile.close();

        Strategy keltnerExit=new KeltnerExit(3,timeSeriesRepo);
        Strategy cciExit=new CCIExit(3,timeSeriesRepo);
//        TradeEngine tradeEngine=new TradeEngine("test",keltnerEntry,keltnerExit,null);


            TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,3,testEntry,keltnerExit,null);
        tradeEngine.initStrategy();

        tradeEngine.runBackTest();
        System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + " ms ");





//        String dateFormatPattern = "dd.MM.yyyy HH:mm:ss.SSS";
//        List<Bar> sortedBars = new ArrayList<>();
//        DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
//        DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
////
//        Instant instantTime = dateFormatter.parse("2018.05.25 00:04").toInstant();
//        ZonedDateTime zdt = instantTime.atZone(ZoneId.systemDefault());
////
////        System.out.println(instantTime);
//        ZonedDateTime zdt= ZonedDateTime.parse("05.06.2018 00:57:00.000", zdtFormatter.withZone(ZoneId.systemDefault()));
//        System.out.println(dateFormatter.format(zdt));
//
//        System.out.println(getNextBarDate(8,zdt));

//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_1_TEST.csv");
//        timeSeriesRepo.setTimeSeries(1440);
////        timeSeriesRepo.toString(1440);
//        MT4TimeSeries ts=(MT4TimeSeries)timeSeriesRepo.getTimeSeries(1440);
//        System.out.println(ts.getIndex(zdt));
//        for (ZonedDateTime z: ts.timeTreeMap.keySet()) System.out.println(z+" "+ts.timeTreeMap.get(z));
////        for (ZonedDateTime z: ts.timeTreeMap.headMap(zdt, true).keySet()) System.out.println(z+" "+ts.timeTreeMap.get(z));
////        System.out.println(ts.timeTreeMap.headMap(zdt, true).lastKey());
//        System.out.println("");
//        ZonedDateTime key=ts.timeTreeMap.higherKey(zdt);
//        if (key==null) {
//            System.out.println("key is not found! "+ts.timeTreeMap.get(ts.timeTreeMap.lastKey()));
//            return;
//        }
//        Integer index=ts.timeTreeMap.get(key);
//        if (index>0) index--;
//        System.out.println(index);
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
//

//        Duration duration = Duration.of(1, ChronoUnit.DAYS);
//        System.out.println(zdt.truncatedTo(ChronoUnit.DAYS).plus(34,ChronoUnit.MINUTES));
//        TreeMap<Integer, String> mapCompTypes = new TreeMap();
//        mapCompTypes.put(1,"egy");
//        mapCompTypes.put(3,"három");
//        mapCompTypes.put(2,"kettő");
//        System.out.println(mapCompTypes.lastEntry()+" "+mapCompTypes.lastKey());
//
//        for (Integer i: mapCompTypes.keySet()) System.out.println(i);



//        System.out.println(new SimpleDateFormat("mm:ss:SSS").format(new Date()));

//        MT4TimeSeries series= new MT4TimeSeries.SeriesBuilder().
//                withName("*").
//                withPeriod(1).
//                withOhlcvFileName("EURUSD_1_YEAR.csv").
//                withDateFormatPattern("dd.MM.yyyy HH:mm").
//                withNumTypeOf(DoubleNum.class).
//                build();
//
//
//        MT4TimeSeries series1= new MT4TimeSeries.SeriesBuilder().
//                withName("*").
//                withPeriod(30).
//                withOhlcvFileName("EURUSD_1_YEAR.csv").
//                withDateFormatPattern("dd.MM.yyyy HH:mm").
//                withNumTypeOf(DoubleNum.class).
//                build();



//
//        ZonedDateTime zonedDateTime=dateFormatter.parse("25.05.2018 21:00:00.000").toInstant().atZone(ZoneId.systemDefault());

//        MT4TimeSeries coreSeries = new MT4TimeSeries.SeriesBuilder().
//                withName("").
//                withPeriod(1).
//                withDateFormatPattern("dd.MM.yyyy HH:mm").
//                withNumTypeOf(DoubleNum.class).
//                build();
//        System.out.println(coreSeries.getEndIndex());

//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_1_MONTH.csv");
//        KeltnerEdge strategy=new KeltnerEdge(timeSeriesRepo);
//        ZigZagFixedIndicator zz=new ZigZagFixedIndicator(strategy.series, ZigZagFixedIndicator.Type.TREND_UP);
//        for (int i = 0; i <strategy.series.getEndIndex(); i++) {
//           System.out.println(i+" "+strategy.series.getBar(i).getEndTime()+" "+zz.getValue(i));
//        }
//        TimeSeries series=strategy.series;

//        for (int i = 5320; i < 5330; i++) {
//
//            System.out.println(i+" "+series.getBar(i).getEndTime()+" "+series.getBar(i).getClosePrice()+" v: "+series.getBar(i).getVolume());
//        }
//
//        PvaIndicator pvaIndicator=new PvaIndicator(series, PvaIndicator.Type.TREND_UP);
//        VolumeIndicator volumeIndicator = new VolumeIndicator(series);
//
//
//        for (int i = 0; i <series.getEndIndex(); i++) {
//            if (!pvaIndicator.getValue(i).isNaN()) System.out.println(series.getBar(i).getEndTime()+"  "+pvaIndicator.getValue(i));
//
//        }

//        KeltnerEdge strategy=new KeltnerEdge();
//
//        BaseTradingRecordExtended record = new BaseTradingRecordExtended(strategy);




//        MbfxTimingIndicator mbfxTiming = new MbfxTimingIndicator(strategy.timeSeries.get(1440), 5);
//        Rule ruleForSell =new CrossedDownIndicatorRule(mbfxTiming,90.0,2);
//        ruleForSell.setCore(record);
//        for (int i = 0; i < strategy.series.getEndIndex(); i++) {
//            if (ruleForSell.isSatisfied(i)) System.out.println(i);
//        }



//         MT4TimeSeries series = new MT4TimeSeries.SeriesBuilder().
//                withName("EURUSD").
//                withPeriod(1).
//                withOhlcvFileName("EURUSD_1_MONTH.csv").
////                withOhlcvFileName("GBPUSD_1_MONTH.csv").
//        withDateFormatPattern("dd.MM.yyyy HH:mm").
////                withDateFormatPattern("yyyy.MM.dd HH:mm").
//        withNumTypeOf(DoubleNum.class).
//                        build();
//
//
//        MT4TimeSeries series60 = new MT4TimeSeries.SeriesBuilder().withPeriod(60).buildFromSeries(series);
//        MbfxTimingIndicator mbfxTiming = new MbfxTimingIndicator(series60, 5);
//        mbfxTiming.setBaseSeries(series);




//        Rule sellRule =new CrossedDownIndicatorRule(indicatorSet.mbfxTiming60, CoreData.series.numOf(90.0),2);
//        Rule buyRule =new CrossedUpIndicatorRule(indicatorSet.mbfxTiming60, series.numOf(10.0),3);
//        indicatorSet.mbfxTiming60.setBaseSeries(null);
//        for (int i = 1; i < CoreData.series60.getEndIndex(); i++) {
//            System.out.println(i+". "+indicatorSet.mbfxTiming60.getValue(i)+" - "+indicatorSet.mbfxTiming60.getValue(i-1));
//        }
//        MbfxTimingIndicator mbfxTiming60 = new MbfxTimingIndicator(strategy.timeSeries.get(1440), 5);
//        Rule sellRule =new IsFallingRule(mbfxTiming60, 1);
//

//
////        Rule buyRule=new IsRisingRule(indicatorSet.mbfxTiming60, 1);
//        for (int i = 0; i < strategy.series.getEndIndex(); i++) {
//            if (sellRule.isFullySatisfied(i)) System.out.println(i+". 1");;
////            if (buyRule.isSatisfied(i)) System.out.println(i+". 0");
//
//        }






//        MbfxTimingIndicator mbfxTimingIndicatorCopy=(MbfxTimingIndicator)mbfxTiming.clone();
//        mbfxTimingIndicatorCopy.setBaseSeries(null);




//        Double formatttedValue;
//        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
//        otherSymbols.setDecimalSeparator('.');
//        DecimalFormat df = new DecimalFormat("#.00", otherSymbols);
//
//        double d=1234.56789;
//        System.out.println(df.format(d));

//        WaddahIndicator waddahIndicatorUp = new WaddahIndicator(series, WaddahIndicator.Type.TREND_UP);
//        WaddahIndicator waddahIndicatorUp60 = new WaddahIndicator(series60, WaddahIndicator.Type.TREND_UP);


//        mbfxTiming.setBaseSeries(series);
//
//        for (int i = 0; i < series60.getEndIndex(); i++) {
////            int i60=series60.getIndex(series.getBar(i).getEndTime());
//            System.out.println(mbfxTiming.getValue(i));
////           System.out.println(i+". "+series.getBar(i).getEndTime()+"  serier60- "+
////                   i60+". "+series60.getBar(i60).getEndTime());
//
//        }

//        System.out.println(series60.timeTreeMap.lastKey());
////        int index=-1;
//        ZonedDateTime time=series.getBar(59).getEndTime();
//        System.out.println(time);
//        System.out.println(series60.getIndex(time));
//        System.out.println(series60.timeTreeMap.headMap(time, false).lastKey());
//        System.out.println(series60.timeTreeMap.headMap(time, true).lastKey());

//        try {
//            index=series60.timeTreeMap.get(series60.timeTreeMap.headMap(time, true).lastKey());
//        } catch (NoSuchElementException e) {
//            System.out.println("HERE");
//            if (time.isBefore(series60.getBar(0).getEndTime())) index=0;
//            else e.printStackTrace();
//        }

//        System.out.println(index);


//        System.out.println(series.getEndIndex()+"  "+series60.getEndIndex());
    }

    public static ZonedDateTime getNextBarDate(int period, ZonedDateTime time){
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
}
