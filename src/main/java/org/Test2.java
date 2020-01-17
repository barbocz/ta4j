package org;

import org.strategy.LogStrategy;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;

public class Test2 {


    public static void main(String[] args) throws Exception {
        String metaTradeTimeZone="CET";
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            metaTradeTimeZone=prop.getProperty("mt4.timeZone");
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.of(metaTradeTimeZone));

        System.out.println(ZonedDateTime.parse("2020.01.17 13:08", zdtFormatter).toInstant());


//        ZonedDateTime currentTime = ZonedDateTime.parse("2020.01.16 17:38", zdtFormatter);
//
//        System.out.println("zdt:"+currentTime);
//        System.out.println("instant:"+currentTime.toInstant());
//        System.out.println("instant now:"+Instant.now());


//        Instant instant = Instant.now();
//
//        System.out.println("Instant : " + instant);
//
//        // Japan = UTC+9
//        ZonedDateTime jpTime = instant.atZone(ZoneId.of("Etc/GMT-2"));
//
//        System.out.println("ZonedDateTime : " + jpTime);
//
//        System.out.println("OffSet : " + jpTime.getOffset());
//        System.out.println("Instant : " + instant.atZone(ZoneId.of("Etc/GMT-2")).toInstant());
//
//        try (ZContext context = new ZContext()) {
//            //  Socket to receive messages on
////            ZMQ.Socket receiver = context.createSocket(SocketType.PULL);
////            receiver.connect("tcp://*:5557");
//
//            //  Socket to send messages to
//
//            ZMQ.Socket sender = context.createSocket(SocketType.PUSH);
//            sender.setSndHWM(1);
//
//            sender.connect("tcp://localhost:32769");
//
//
//            String response="HALI";
//
//            sender.send(response.getBytes(ZMQ.CHARSET), 0);


//        }




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
