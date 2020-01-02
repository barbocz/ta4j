package org;

import org.strategy.LogStrategy;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.KeltnerEntry;
import org.strategy.myEntryStrategies.KeltnerEntryWithCCI;
import org.strategy.myEntryStrategies.TestEntry;
import org.strategy.myExitStrategies.CCIExit;
import org.strategy.myExitStrategies.KeltnerExit;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import java.io.*;
//new line 2234

public class Test1 {
    public static void main(String[] args) throws Exception {

//        File srcFile=new File(System.getProperty("user.dir") + "\\ta4j-core\\log\\85_bars.csv");
//        File dstFile=new File("C:\\Users\\Barbocz Attila\\AppData\\Roaming\\MetaQuotes\\Terminal\\294B6FCE6F709DE82DA4C87FDBF1DE36\\MQL4\\Files\\85_bars.csv");
//        try (FileInputStream fis = new FileInputStream(srcFile);
//             FileOutputStream fos = new FileOutputStream(dstFile)) {
//            int len;
//            byte[] buffer = new byte[4096];
//            while ((len = fis.read(buffer)) > 0) {
//                fos.write(buffer, 0, len);
//            }
//        } catch (IOException e) {
//            // ... handle IO exception
//            e.printStackTrace();
//        }


//
        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD","backTest.csv","yyyy.MM.dd HH:mm");

        Strategy entry=new KeltnerEntry(3,timeSeriesRepo);
        Strategy exit=new KeltnerExit(3,timeSeriesRepo);

        long startTime = System.currentTimeMillis();
        TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,3,entry,exit,null, TradeEngine.LogLevel.EXTENDED);
        System.out.println(tradeEngine.series.getSymbol());
//
        tradeEngine.initStrategy();
        tradeEngine.runBackTest();
        System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + " ms ");

//        LogStrategy logStrategy=new LogStrategy();
//        logStrategy.getMT4data(1117);
//        logStrategy.getMT4data(1153);



//        logStrategy.createPreparedStatement("basic","insert","strategy","process_type,bar_number,open_minimum");
//        logStrategy.insertToDatabase("basic","LIVE",100,123.123456);
//        logStrategy.createPreparedStatement("basicu","update","strategy","bar_number,period,EQUITY_MINIMUM");
//        logStrategy.updateInDatabase("basicu",300,15,523.123456);
//        logStrategy.createPreparedStatement("basic","insert","STRATEGY_EVALUATION","HASHCODE,VALUE");

//        BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\ta4j-core\\log\\test.csv" ));
        String fileName = System.getProperty("user.dir") + "\\ta4j-core\\log\\test.csv";

//
//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
//        System.out.println(timeSeriesRepo.getTimeSeries(3).getEndIndex());
//        TimeSeries ts=timeSeriesRepo.getTimeSeries(3);
//        long startTime = System.currentTimeMillis();
////        File file = new File(fileName);
//        BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
//        for (int i = 0; i < ts.getEndIndex(); i++) {
//            ts.setCurrentTime(ts.getBar(i).getBeginTime());
////            addStringToFile(fileName, ts.getBar(i).getBeginTime().toString()+";"+ts.getBar(i).getOpenPrice()+"\r\n");
////            Bar bar=ts.getBar(i);
////            output.write(bar.getBeginTime().toString()+"|"+ts.getBar(i).getOpenPrice()+"|"+ts.getBar(i).getHighPrice()+"|"+ts.getBar(i).getLowPrice()+"|"+ts.getBar(i).getClosePrice()+"|"+ts.getBar(i).getVolume()+"\r\n");
//            output.write(ts.toString(ts.getCurrentBar()));
//        }
//
//
//        output.close();
//        System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + " ms ");
////        addStringToFile(fileName, "ezt\r\n");
////        doSomething("id",2,"type","LIVE","ratio",100.0);
    }

    public static void addStringToFile(File file, String content) throws Exception {
//        BufferedWriter output;
//
//        if (!file.exists())
//            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF8"));
//        else
//            output = new BufferedWriter(new FileWriter(fileName, true));
//        output.write(content);
//
//        output.close();
    }

    public static void doSomething(Object... parameters)
    {

        for (Object i : parameters)
        {
            System.out.println(i);
            if (i instanceof String) System.out.println("Double");
        }
    }
}
