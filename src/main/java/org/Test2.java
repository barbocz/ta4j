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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Test2 {


    public static void main(String[] args) throws Exception {
        Logger logger = LogManager.getLogger(Test2.class);
        MDC.put("strategyId",10000);

        MDC.put("action","ACTION");
        MDC.put("orderId","10");
        MDC.put("mt4TicketNumber","123456");
        MDC.put("strategyId",20000);
//        ThreadContext.put("strategyId", "10000");
//        ThreadContext.put("mt4TicketNumber", "123456");
        logger.debug("elso");
//        ThreadContext.put("strategyId", "20000");

        logger.debug("masodik");



    }



}
