package org;

import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.*;
import org.strategy.myExitStrategies.*;
import org.strategy.myExitStrategies.Dummy;
import org.ta4j.core.Strategy;

import java.io.*;
//new line 2234

public class Test1 {
    public static void main(String[] args) throws Exception {


        String backTestFileName="smallBacktestEUR.csv";
//        backTestFileName="smallestBacktestEUR.csv";
        backTestFileName="backtest.csv";
//        backTestFileName="2019nov.csv";
//        backTestFileName="2018nov.csv";
//        backTestFileName="2019aug.csv";
//        backTestFileName="backtestGBP.csv";
        backTestFileName="backtestEUR.csv";

        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD",backTestFileName,"yyyy.MM.dd HH:mm");
//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD","smallBb"yyyy.MM.dd HH:mm");

//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("GBPUSD","backTestGBP.csv","yyyy.MM.dd HH:mm");

        long startTime = System.currentTimeMillis();

//            TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,21,new ReboundEntry(),new MurrayMiracleExit_M30(),null, TradeEngine.LogLevel.BASIC);
//        TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,3,new MurrayLevelChange_v2(),new MurrayLevelChangedExit_v2(),null, TradeEngine.LogLevel.TOTAL);
        TradeEngine.LogLevel logLevel;
        if (backTestFileName=="backtestEUR.csv") logLevel=TradeEngine.LogLevel.BASIC;
        else logLevel=TradeEngine.LogLevel.TOTAL;
        TradeEngine tradeEngine=new TradeEngine(timeSeriesRepo,3,new MurrayEntry(),new MurrayExit(),null, logLevel);
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
