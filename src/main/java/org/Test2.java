package org;
import org.h2.jdbcx.JdbcConnectionPool;
import org.strategy.LogStrategy;
import org.strategy.Strategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.myEntryStrategies.TestEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test2 {


    public static void main(String[] args) throws Exception {

        LogStrategy logStrategy = new LogStrategy();
        logStrategy.getMT4data(1117);
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
