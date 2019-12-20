package org;

import java.io.*;
import java.util.Properties;

public class Test2 {


    public static void main(String[] args) throws Exception {

        try (InputStream input = new FileInputStream("config.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            System.out.println(prop.getProperty("mt4.filesDirectory"));
//            System.out.println(prop.getProperty("db.user"));
//            System.out.println(prop.getProperty("db.password"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
