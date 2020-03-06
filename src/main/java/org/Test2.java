package org;

import javafx.collections.transformation.SortedList;
import org.strategy.LogStrategy;
import org.strategy.TimeSeriesRepo;
import org.strategy.TradeEngine;
import org.strategy.myEntryStrategies.MurrayEntry;
import org.strategy.myExitStrategies.MurrayExit;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.mt4Selection.MurrayChangeIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathFixedIndicator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Test2 {


    public static void main(String[] args) throws Exception {

        String dateFormatPattern = "yyyy.MM.dd HH:mm";
        DateTimeFormatter zdtFormatter = DateTimeFormatter.ofPattern(dateFormatPattern);
        ZonedDateTime currentTime = ZonedDateTime.parse("2020.02.21 12:32", zdtFormatter.withZone(ZoneId.systemDefault()));
        System.out.println(currentTime);
        System.out.println(currentTime.getHour()+"--"+currentTime.getMinute());



    }



}
