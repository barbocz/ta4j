package org;

import javafx.collections.transformation.SortedList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.apache.log4j.MDC;

import org.strategy.LogStrategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.MurrayEntry;
import org.strategy.myExitStrategies.MurrayExit;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;

public class Test2 {


    public static void main(String[] args) throws Exception {
        DateTimeFormatter zdtFormatter,zdtFormatterWithSeconds;
        String metaTradeTimeZone="CET";
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            metaTradeTimeZone=prop.getProperty("mt4.timeZone");
            System.out.println(metaTradeTimeZone);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.systemDefault());
        zdtFormatterWithSeconds = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.of(metaTradeTimeZone));

        System.out.println(ZonedDateTime.parse("2020.03.09 17:35", zdtFormatter));
        System.out.println(ZonedDateTime.parse("2020.03.09 17:35", zdtFormatter).toInstant());
        System.out.println(Instant.now());

        System.out.println(ChronoUnit.MINUTES.between(ZonedDateTime.parse("2020.03.09 17:35", zdtFormatter).toInstant(), Instant.now()));


    }



}
