package org;

import org.strategy.LogStrategy;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Test2 {


    public static void main(String[] args) throws Exception {


        LogStrategy logStrategy=new LogStrategy();
        logStrategy.getProfitByMonth(793);

//        TimeSeriesRepo timeSeriesRepo=new TimeSeriesRepo("EURUSD","EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeriesRepo.coreSeries);
//        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(timeSeriesRepo.coreSeries, 34);
//        Rule closePriceOverKeltnerUpperIn8=new OverIndicatorRule(closePrice, kcM, 8);
//
//        for(Field f : closePriceOverKeltnerUpperIn8.getClass().getFields()) {
//            System.out.println(f.getGenericType() +" "+f.getName() + " = " + f.get(closePriceOverKeltnerUpperIn8));
//        }
//        LogStrategy logStrategy = new LogStrategy();
//        logStrategy.getMT4data(1117);
//        TimeSeriesRepo ts=new TimeSeriesRepo("EURUSD","EURUSD_3MONTH.csv","yyyy.MM.dd HH:mm");
//        String logFileNameForBars = System.getProperty("user.dir") + "\\ta4j-core\\log\\" +  "_bars.csv";
//        System.out.println(ts.coreSeries.getEndIndex());
//        for (int i = 0; i < 2000; i++) {
//            BufferedWriter logFileForBars = new BufferedWriter(new FileWriter(logFileNameForBars, true));
//            logFileForBars.write(ts.coreSeries.getBar(i).getBeginTime().toString() + "|" +ts.coreSeries.getBar(i).getOpenPrice()+ "\r\n");
//            logFileForBars.close();
//    }


    }


}
