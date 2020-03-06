package org.strategy;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.h2.jdbcx.JdbcConnectionPool;
import org.util.SqlLiteConnector;

public class LogStrategy {


    public int id;
    public boolean backtest = false;
    HashMap<String, PreparedStatement> insertPreparedStatements = new HashMap<>();
    HashMap<String, PreparedStatement> updatePreparedStatements = new HashMap<>();

    JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:~\\ta4j;", "sa", "12345");
    Connection dbConnection;

    SqlLiteConnector sqlLiteConnector = new SqlLiteConnector();
//    Connection dbConnection = sqlLiteConnector.con;

    public List<Indicator> indicatorsForLog = new ArrayList<>();
    public List<Rule> rulesForLog = new ArrayList<>();

    BufferedWriter logFileForBars, logFileForExitLevels,logFileForTradeSignals;
    String logFileNameForBars, logFileNameForExitLevels;
    TradeEngine tradeEngine;
    boolean online = false;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    DateTimeFormatter dateTimeFormatterWithSeconds = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
    DecimalFormat decimalFormatWith2Dec, decimalFormatWith5Dec;

    StringBuffer lastExitPrice = new StringBuffer("");
    StringBuffer tradeSignalsContent = new StringBuffer("");
    int tradeSignalIndex=0;

    PreparedStatement insertTrade, updateTrade, insertEvaluation, updateStrategy;

    String metaTradeFileDir,metaTradeTesterFileDir;
    Path targetDir ;

    public LogStrategy() throws Exception {
        setVariables();
    }

    void setVariables(){
        dbConnection = sqlLiteConnector.con;
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatWith2Dec = new DecimalFormat("#.00", decimalFormatSymbols);
        decimalFormatWith5Dec = new DecimalFormat("#.00000", decimalFormatSymbols);
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            metaTradeFileDir=prop.getProperty("mt4.filesDirectory");
            metaTradeTesterFileDir=prop.getProperty("mt4.testerFilesDirectory");
            targetDir = Paths.get(metaTradeFileDir);
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public LogStrategy(TradeEngine tradeEngine) throws Exception {

        ResultSet resultSet;

        setVariables();
//        dbConnection =DriverManager.getConnection("jdbc:h2:tcp://localhost/C:\\\\Users\\\\Barbocz Attila\\\\ta4j;AUTO_SERVER=TRUE;user=sa;password=12345");
//        dbConnection = DriverManager.getConnection("jdbc:h2:~\\ta4j;","sa","12345");
        this.tradeEngine = tradeEngine;
        if (tradeEngine.timeSeriesRepo.processType == TimeSeriesRepo.ProcessType.MT4) {             // MT4-es feed-elésnél a H2-be
            online = true;
            dbConnection = jdbcConnectionPool.getConnection();
        }                                                 // backtest esetén SQLite-ba


        Statement statement = dbConnection.createStatement();
        statement.executeUpdate("insert into strategy (SOURCE) values ('" + (online ? "MT4" : "FILE:" + tradeEngine.timeSeriesRepo.coreSeries.ohlcvFileName) + "')");

        if (online) resultSet = statement.executeQuery("select scope_identity()");
        else  resultSet = statement.executeQuery("SELECT last_insert_rowid() AS LAST FROM strategy");

        if (resultSet.next()) id = resultSet.getInt(1);
        else throw new Exception("Error:Id is missing");


        PreparedStatement preparedStatement = dbConnection.prepareStatement("update STRATEGY set BAR_NUMBER=?,PERIOD=?,SYMBOL=?,ENTRY_STATEGY=?,ENTRY_STRATEGY_NAME=?,EXIT_STRATEGY=?,EXIT_STRATEGY_NAME=?,START_TIME=?,FIRST_BAR_TIME=?,LAST_BAR_TIME=?,MAGIC_NUMBER=? WHERE STRATEGY_ID=?");
        preparedStatement.setInt(1, tradeEngine.series.getEndIndex());
        preparedStatement.setInt(2, tradeEngine.period);
        preparedStatement.setString(3, tradeEngine.timeSeriesRepo.symbol);
        preparedStatement.setString(4, getSourceContent(tradeEngine.entryStrategy));
        preparedStatement.setString(5, tradeEngine.entryStrategy.getClass().getName());
        preparedStatement.setString(6, getSourceContent(tradeEngine.exitStrategy));
        preparedStatement.setString(7, tradeEngine.exitStrategy.getClass().getName());
        preparedStatement.setString(8, dateTimeFormatter.format(ZonedDateTime.now()));
        if (tradeEngine.series.getEndIndex() > -1) {
            preparedStatement.setString(9, dateTimeFormatter.format(tradeEngine.series.getBar(0).getBeginTime()));
            preparedStatement.setString(10, dateTimeFormatter.format(tradeEngine.series.getBar(tradeEngine.series.getEndIndex()).getBeginTime()));
        } else {
            preparedStatement.setString(9, "");
            preparedStatement.setString(10, "");
        }
        preparedStatement.setLong(11, tradeEngine.mt4MagicNumber);
        preparedStatement.setInt(12, id);
        preparedStatement.executeUpdate();

//        updateStrategy = dbConnection.prepareStatement("INSERT INTO STRATEGY ( SOURCE, BAR_NUMBER ,PERIOD , SYMBOL ,TRADE_NUMBER , PROFITABLE_TRADES_RATIO ,EQUITY_MINIMUM ,BALANCE_DRAWDOWN ,\n" +
//                "    OPEN_MINIMUM , OPEN_MAXIMUM , TOTAL_PROFIT ,  TOTAL_PROFIT_PER_MONTH , DAY_NUMBER ,  BAR_PROCESSTIME ,    ENTRY_STATEGY ,    EXIT_STRATEGY ,    ENTRY_STRATEGY_NAME , EXIT_STRATEGY_NAME,START_TIME ,END_TIME, FIRST_BAR_TIME, LAST_BAR_TIME  \n" +
//                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        updateStrategy = dbConnection.prepareStatement("UPDATE STRATEGY SET BAR_NUMBER=? ,TRADE_NUMBER=? , PROFITABLE_TRADES_RATIO=? ,EQUITY_MINIMUM=? ,BALANCE_DRAWDOWN=? , \n" +
                "  OPEN_MINIMUM=? , OPEN_MAXIMUM=? , TOTAL_PROFIT=? ,  TOTAL_PROFIT_PER_MONTH=? , DAY_NUMBER=? ,  BAR_PROCESSTIME=? , END_TIME=? ,LAST_BAR_TIME=?,LAST_CLOSE_PRICE=? WHERE STRATEGY_ID=?");


    }

    public void init() throws Exception {

        logFileNameForBars = System.getProperty("user.dir") + "\\ta4j-core\\log\\" + id + "_bars.csv";

        if (!online) logFileForBars = new BufferedWriter(new FileWriter(logFileNameForBars));

//        insertTrade = dbConnection.prepareStatement("insert into STRATEGY_TRADE_HISTORY values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        insertTrade = dbConnection.prepareStatement("insert into STRATEGY_TRADE_HISTORY (STRATEGY_ID,ORDER_ID,TYPE,OPEN_TIME,OPEN_PRICE,OPEN_AMOUNT,COMMENT,MT4_TICKET_NUMBER,MT_4_OPEN_TIME,MT4_OPEN_PRICE) values (?,?,?,?,?,?,?,?,?,?)");

        updateTrade = dbConnection.prepareStatement("update STRATEGY_TRADE_HISTORY set CLOSE_TIME=?,CLOSE_PRICE=?,CLOSE_AMOUNT=?,CLOSE_BY=?,DURATION=?,PROFIT=?,COMMENT=?,MAX_PROFIT=?,MAX_LOSS=?,MT_4_CLOSE_TIME=?,MT4_CLOSE_PRICE=?,MT4_PROFIT=? where STRATEGY_ID=? AND ORDER_ID=?");
//        insertTrade= dbConnection.prepareStatement("insert into  STRATEGY_TRADE_HISTORY (STRATEGY_ID,ORDER_ID,TYPE,AMOUNT,OPEN_TIME,OPEN_PRICE) values (?,?,?,?,?,?)");
//        insertTrade= dbConnection.prepareStatement("insert into  STRATEGY_TRADE_HISTORY (STRATEGY_ID,ORDER_ID) values (?,?)");

        if (tradeEngine.logLevel.ordinal() > TradeEngine.LogLevel.BASIC.ordinal()) {

            insertEvaluation = dbConnection.prepareStatement("INSERT INTO STRATEGY_EVALUATION VALUES (?,?,?,?,?,?)");

            ;
            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO STRATEGY_PARAMETER (STRATEGY_ID,TYPE,ACTION,DESCRIPTION,HASHCODE,SUBWINDOW_INDEX,COLOR,BUFFER_INDEX) VALUES (?,?,?,?,?,?,?,?)");
            Set<Rule> rules = new HashSet<>(Arrays.asList(tradeEngine.entryStrategy.ruleForBuy, tradeEngine.entryStrategy.ruleForSell, tradeEngine.exitStrategy.ruleForBuy, tradeEngine.exitStrategy.ruleForSell));

            HashMap<Rule, Integer> bufferIndexesForRules = new HashMap<>();  // subWindowIndex,bufferIndex
            int subWindowIndex = 0;
            for (Rule rule : rules) {
                if (rule == null) continue;
                rule.getDescription();
                for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                    pstmt.setLong(1, id);

                    if (rule == tradeEngine.entryStrategy.ruleForBuy) {
                        pstmt.setString(2, "BUY");
                        pstmt.setString(3, "ENTRY");
                        subWindowIndex = 1;
                        if (!bufferIndexesForRules.containsKey(rule)) bufferIndexesForRules.put(rule, -1);

                    } else if (rule == tradeEngine.exitStrategy.ruleForBuy) {
                        pstmt.setString(2, "BUY");
                        pstmt.setString(3, "EXIT");
                        subWindowIndex = 1;
                        if (!bufferIndexesForRules.containsKey(rule)) bufferIndexesForRules.put(rule, 7);
                    } else if (rule == tradeEngine.entryStrategy.ruleForSell) {
                        pstmt.setString(2, "SELL");
                        pstmt.setString(3, "ENTRY");
                        subWindowIndex = 2;
                        if (!bufferIndexesForRules.containsKey(rule)) bufferIndexesForRules.put(rule, -1);

                    } else if (rule == tradeEngine.exitStrategy.ruleForSell) {
                        pstmt.setString(2, "SELL");
                        pstmt.setString(3, "EXIT");
                        subWindowIndex = 2;
                        if (!bufferIndexesForRules.containsKey(rule)) bufferIndexesForRules.put(rule, 7);
                    }
                    pstmt.setString(4, rule.getRuleItems().get(ruleHashMap));
//                    pstmt.setString(4, rule.getClass().getSimpleName());
                    pstmt.setInt(5, ruleHashMap);

                    pstmt.setInt(6, subWindowIndex);
                    pstmt.setString(7, "");
                    bufferIndexesForRules.replace(rule, bufferIndexesForRules.get(rule) + 1);
                    pstmt.setInt(8, bufferIndexesForRules.get(rule));
                    pstmt.executeUpdate();
                }
            }

            HashMap<Integer, Integer> bufferIndexes = new HashMap<>();
            for (Indicator indicator : indicatorsForLog) {
                pstmt.setLong(1, id);
                pstmt.setString(2, "INDICATOR");
                pstmt.setString(3, "INDICATOR");
                pstmt.setString(4, indicator.toString());
                pstmt.setInt(5, indicator.hashCode());
                pstmt.setInt(6, indicator.getSubWindowIndex());
                pstmt.setString(7, (indicator.getIndicatorColor().getRed() + "|" + indicator.getIndicatorColor().getGreen() + "|" + indicator.getIndicatorColor().getBlue()));
                if (!bufferIndexes.containsKey(indicator.getSubWindowIndex()))
                    bufferIndexes.put(indicator.getSubWindowIndex(), 0);
                else
                    bufferIndexes.replace(indicator.getSubWindowIndex(), bufferIndexes.get(indicator.getSubWindowIndex()) + 1);
                pstmt.setInt(8, bufferIndexes.get(indicator.getSubWindowIndex()));
                pstmt.executeUpdate();

            }

            if (tradeEngine.logLevel.ordinal() == TradeEngine.LogLevel.TOTAL.ordinal()) {
                logFileNameForExitLevels = System.getProperty("user.dir") + "\\ta4j-core\\log\\" + id + "_exitLevels.csv";
                if (!online) logFileForExitLevels = new BufferedWriter(new FileWriter(logFileNameForExitLevels));
            }
        }


    }

    public void logIndicatorValue(int timeFrame, ZonedDateTime logTime) throws Exception {
//        System.out.println("LOG INDI");


        Num indicatorValueNum;
        Object indicatorValue;
        for (Indicator indicator : indicatorsForLog) {
            TimeSeries indicatorSeries = indicator.getTimeSeries();
            if (indicatorSeries.getPeriod() != timeFrame) continue;
            int index = indicatorSeries.getIndex(logTime);
//                Num indicatorValue1 = (Num) indicator.getValue(index);
//                tradeEngine.info=timeFrame+", "+indicator.getClass().getSimpleName()+",  "+logTime.toString()+", "+index;

            if (index > -1) {
                indicatorValue = indicator.getValue(index);

                if (indicatorValue == null) {
                    System.out.println("INDICATOR VALUE IS NULL: " + timeFrame + ", " + indicator.getClass().getSimpleName() + ",  " + logTime.toString() + ", " + index);
                    continue;
                } else {
                    indicatorValueNum = (Num) indicatorValue;
                }
                insertEvaluation.setLong(1, id);
                insertEvaluation.setInt(2, indicator.hashCode());
                insertEvaluation.setInt(3, index);
                insertEvaluation.setInt(4, 0);
                insertEvaluation.setDouble(5, indicatorValueNum.doubleValue());
                insertEvaluation.setString(6, dateTimeFormatter.format(logTime));
                insertEvaluation.executeUpdate();

            }
        }


    }

    public void logRule(Rule rule, ZonedDateTime time, int index, boolean satisfied) {
        if (tradeEngine.logLevel.ordinal() > TradeEngine.LogLevel.BASIC.ordinal()) {
            try {
                insertEvaluation.setLong(1, id);
                insertEvaluation.setInt(2, rule.hashCode());
                insertEvaluation.setInt(3, index);
                insertEvaluation.setInt(4, 0);
                insertEvaluation.setDouble(5, satisfied ? 1.0 : 0.0);
                insertEvaluation.setString(6, dateTimeFormatter.format(time));
                insertEvaluation.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void logExitPrices() throws Exception {
        StringBuffer exitPrice;
        for (Order order : tradeEngine.openedOrders) {
            if (order.takeProfit > 0.0 || order.stopLoss > 0.0) {
                exitPrice = new StringBuffer("");
                exitPrice = exitPrice.append(order.id).append("|").append(order.type).append("|").append(dateTimeFormatter.format(tradeEngine.series.getCurrentTime())).append("|").append(order.takeProfit).append("|").append(order.stopLoss).append("\n");
                if (!lastExitPrice.equals(exitPrice)) {
                    if (online)
                        logFileForExitLevels = new BufferedWriter(new FileWriter(logFileNameForExitLevels, true));
                    logFileForExitLevels.write(exitPrice.toString());
                    if (online) logFileForExitLevels.close();
                    lastExitPrice = exitPrice;
                }
            }
        }
    }


//    public void setLogOn() {
//
//        islogged = true;
////        ruleForSell.setTradeEngine(this);
////        ruleForSell.getTradeEngine();
////        ruleForBuy.setTradeEngine(this);
//
//        try {
//            // logFile előkészítés az MT4-nek -> logFile4IndicatorContent
//            // header format subwindow,bufferIndex,type,name,hashcode,chartType - 0:price chart, 1:buy entry signals, 2:sell entr signals, 3.,4,5... egyéb indik
//            int bufferIndex = 0;
//            HashMap<Integer, Integer> bufferSubWindowMap = new HashMap<>();  // HashMap<SubWindow,bufferIndex>
//
//            String[] fileList = new File(metaTradeFileDir).list();
//            for (String fileName : fileList) {
//                if (fileName.contains("log4J")) {
//                    new File(metaTradeFileDir + fileName).delete();
//                }
//            }
//
//            if (dbConnection == null) {
//                SqlLiteConnector sqlLiteConnector = new SqlLiteConnector();
//                dbConnection = sqlLiteConnector.con;
//            }
////            setAutoCommit(false);
//
//            Statement stm = dbConnection.createStatement();
//            stm.execute("DELETE FROM STRATEGY_TRADE_HISTORY");
//            stm.execute("DELETE FROM STRATEGY_EVALUATION");
//            stm.execute("DELETE FROM STRATEGY_PARAMETER");
//            stm.execute("DELETE FROM STRATEGY_TRADE_HISTORY");
//            preparedStatementForEvaluation = dbConnection.prepareStatement("INSERT INTO STRATEGY_EVALUATION VALUES (?,?,?,?,?,?)");
//
//            Rule rule;
//
//
//            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO STRATEGY_PARAMETER (STRATEGY_ID,TYPE,ACTION,DESCRIPTION,HASHCODE) VALUES (?,?,?,?,?)");
//            if (entryStrategy.ruleForBuy != null) {
//                HashMap<Integer, String> hm = entryStrategy.ruleForBuy.getRuleItems();
//                rule = entryStrategy.ruleForBuy;
//                rule.getDescription();
//
//
//                StringBuffer entryBuyRuleBufferValues = new StringBuffer("");
//
//                for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
//                    pstmt.setLong(1, entryStrategy.id);
//                    pstmt.setString(2, "BUY");
//                    pstmt.setString(3, "ENTRY");
//                    pstmt.setString(4, entryStrategy.ruleForBuy.getRuleItems().get(ruleHashMap));
//                    pstmt.setInt(5, ruleHashMap);
//                    pstmt.executeUpdate();
//                    //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
//                    bufferIndexes.put(ruleHashMap, bufferIndex);
//                    subWindows.put(ruleHashMap, 1);
//                    entryBuyRuleBufferValues.append(bufferIndex).append("|BUY|").append(entryStrategy.ruleForBuy.getRuleItems().get(ruleHashMap)).append("|").append(ruleHashMap).append("\r\n");
//
//                    bufferIndex++;
//                }
//                bufferValues.put(1, entryBuyRuleBufferValues);
//
//
//            }
//
//            StringBuffer entrySellRuleBufferValues = new StringBuffer("");
//            if (entryStrategy.ruleForSell != null) {
//                rule = entryStrategy.ruleForSell;
//                rule.getDescription();
//                bufferIndex = 0;
//                for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
//                    pstmt.setLong(1, entryStrategy.id);
//                    pstmt.setString(2, "SELL");
//                    pstmt.setString(3, "ENTRY");
//                    pstmt.setString(4, entryStrategy.ruleForSell.getRuleItems().get(ruleHashMap));
//                    pstmt.setInt(5, ruleHashMap);
//                    pstmt.executeUpdate();
//                    bufferIndexes.put(ruleHashMap, bufferIndex);
//                    subWindows.put(ruleHashMap, 2);
//                    entrySellRuleBufferValues.append(bufferIndex).append("|SELL|").append(entryStrategy.ruleForSell.getRuleItems().get(ruleHashMap)).append("|").append(ruleHashMap).append("\r\n");
//
//                    bufferIndex++;
//                    //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
//                }
//                bufferValues.put(2, entrySellRuleBufferValues);
////                if (strategy.ruleForSell != null) {
////                    rule = strategy.ruleForSell;
////                    rule.getDescription();
////
////                    for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
////                        pstmt.setLong(1, strategyId);
////                        pstmt.setString(2, "SELL");
////                        pstmt.setString(3, "EXIT");
////                        pstmt.setString(4, strategy.ruleForSell.getRuleItems().get(ruleHashMap));
////                        pstmt.setInt(5, ruleHashMap);
////                        pstmt.executeUpdate();
////                        //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
////                    }
////                }
////
////                for (SLTPManager takeProfitManager : strategy.positionManagement.exitLevelsForSell) {
////                    pstmt.setLong(1, strategyId);
////                    pstmt.setString(2, "SELL");
////                    pstmt.setString(3, "EXIT LEVEL");
////                    pstmt.setString(4, takeProfitManager.getParameters());
////                    pstmt.setInt(5, takeProfitManager.hashCode());
////                    pstmt.executeUpdate();
////                    //                    System.out.println(takeProfitManager.getParameters());
////                }
//
//            }
//
//            // TODO indikátor debug-ot átgondolni!!!
//            StringBuffer bufferValue;
//            for (Indicator indicator : logStrategy.indicatorsForLog) {
//                pstmt.setLong(1, entryStrategy.id);
//                pstmt.setString(2, "INDICATOR");
//                pstmt.setString(3, "INDICATOR");
//                pstmt.setString(4, indicator.getClass().getSimpleName());
//                pstmt.setInt(5, indicator.hashCode());
//                pstmt.executeUpdate();
//
//                if (bufferSubWindowMap.containsKey(indicator.getSubWindowIndex())) {
//                    bufferIndex = bufferSubWindowMap.get(indicator.getSubWindowIndex()) + 1;
//                    bufferSubWindowMap.replace(indicator.getSubWindowIndex(), bufferIndex);
//                } else {
//                    bufferIndex = 0;
//                    bufferSubWindowMap.put(indicator.getSubWindowIndex(), bufferIndex);
//                }
//                subWindows.put(indicator.hashCode(), indicator.getSubWindowIndex());
//                if (!bufferValues.containsKey(indicator.getSubWindowIndex())) bufferValue = new StringBuffer("");
//                else bufferValue = bufferValues.get(indicator.getSubWindowIndex());
//                bufferIndexes.put(indicator.hashCode(), bufferIndex);
//                bufferValue.append(bufferIndex).append("|").append(indicator.getClass().getSimpleName()).append("|").append(indicator.hashCode()).append("|").
//                        append(indicator.getIndicatorColor().getRed()).append("|").append(indicator.getIndicatorColor().getGreen()).append("|").append(indicator.getIndicatorColor().getBlue()).append("\r\n");
//                if (!bufferValues.containsKey(indicator.getSubWindowIndex()))
//                    bufferValues.put(indicator.getSubWindowIndex(), bufferValue);
//                else bufferValues.replace(indicator.getSubWindowIndex(), bufferValue);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }


    public void logTrade(boolean isNewOrder, Order order) throws Exception {

        if (isNewOrder) {
            insertTrade.setInt(1, id);
            insertTrade.setInt(2, order.id);
            insertTrade.setString(3, order.type.toString());
            insertTrade.setString(4, dateTimeFormatter.format(order.openTime));
            insertTrade.setDouble(5, order.openPrice);
            insertTrade.setDouble(6, order.openedAmount);
            insertTrade.setString(7, "");
            // MT4
            insertTrade.setInt(8, order.mt4TicketNumber);
            if (order.mt4OpenTime!=null) insertTrade.setString(9, dateTimeFormatterWithSeconds.format(order.mt4OpenTime));
            else insertTrade.setString(9,null);
            insertTrade.setDouble(10, order.mt4OpenPrice);


//            insertTrade.setInt(1, id);
//            insertTrade.setInt(2, order.id);
//            insertTrade.setString(3, order.type.toString());
//            insertTrade.setString(4, dateTimeFormatter.format(order.openTime));
//            insertTrade.setDouble(5, order.openPrice);
//            insertTrade.setDouble(6, order.openedAmount);
//            insertTrade.setString(7, dateTimeFormatter.format(order.closeTime));
//            insertTrade.setDouble(8, order.closePrice);
//            insertTrade.setDouble(9, order.closedAmount);
//            insertTrade.setString(10, order.exitType.toString());
//            insertTrade.setInt(11, tradeEngine.series.getIndex(order.closeTime) - tradeEngine.series.getIndex(order.openTime));
//            insertTrade.setDouble(12, order.profit);
//            insertTrade.setString(13, "");
//            insertTrade.setDouble(14, order.maxProfit);
//            insertTrade.setDouble(15, order.maxLoss);
            insertTrade.executeUpdate();
        } else {
            updateTrade.setString(1, dateTimeFormatter.format(order.closeTime));
            updateTrade.setDouble(2, order.closePrice);
            updateTrade.setDouble(3, order.closedAmount);
            updateTrade.setString(4, order.exitType.toString());
            updateTrade.setInt(5, tradeEngine.series.getIndex(order.closeTime) - tradeEngine.series.getIndex(order.openTime));
            updateTrade.setDouble(6, order.profit);
            updateTrade.setString(7, "");
            updateTrade.setDouble(8, order.maxProfit);
            updateTrade.setDouble(9, order.maxLoss);

            // MT4 results
            if (order.mt4CloseTime!=null)  updateTrade.setString(10, dateTimeFormatterWithSeconds.format(order.mt4CloseTime));
            else updateTrade.setString(10,null);
            updateTrade.setDouble(11, order.mt4ClosePrice);
            updateTrade.setDouble(12, order.mt4Profit);

            updateTrade.setInt(13, id);
            updateTrade.setInt(14, order.id);
            updateTrade.executeUpdate();

        }

    }

    public void logStrategyResult() throws Exception {

//        if (tradeEngine.series.getBeginIndex() < 1) return;
        long dayNumber = ChronoUnit.DAYS.between(tradeEngine.series.getBar(tradeEngine.series.getBeginIndex()).getEndTime(), tradeEngine.series.getBar(tradeEngine.series.getEndIndex()).getEndTime());


        double monthDivider = dayNumber / 31.0;
        if (dayNumber == 0) monthDivider = 1.0;

        double profitableTradesRatio = 0.0;
        if (tradeEngine.profitableTrade + tradeEngine.losingTrade > 0)
            profitableTradesRatio = Double.parseDouble(decimalFormatWith2Dec.format(100 * tradeEngine.profitableTrade / (tradeEngine.profitableTrade + tradeEngine.losingTrade)));
        double totalProfitPerMonth = Double.parseDouble(decimalFormatWith2Dec.format((tradeEngine.balance - tradeEngine.initialBalance) / monthDivider));
//        double barPerTime = (double) series.getEndIndex() / (System.currentTimeMillis() - startTime);
        double barPerTime = 0.0;

//        System.out.println("TRADE: "+closedOrders.size()+", TOTAL PROFIT: "+totalProfit*1000.0+", BALANCE: "+backTestBalance);
//        System.out.printf("BAR_NUMBER: %s, PERIOD: %s, SYMBOL: %s, TRADE_NUMBER: %s, PROFITABLE_TRADES_RATIO: %s, EQUITY_MINIMUM: %s, BALANCE_DRAWDOWN: %s, OPEN_MIMIMUN: %s, " +
//                        "TOTAL_PROFIT: %s, TOTAL_PROFIT_PER_MONTH: %s, DAY_NUMBER: %s, BAR_PROCESSTIME: %s, ENTRY_STATEGY: %s, EXIT_STRATEGY: %s,",
//                tradeEngine.series.getEndIndex(), tradeEngine.series.getPeriod(), tradeEngine.series.getSymbol(), tradeEngine.closedOrders.size(), decimalFormat.format(profitableTradesRatio), decimalFormat.format(tradeEngine.equityMinimum), decimalFormat.format(tradeEngine.balanceDrawDown), decimalFormat.format(tradeEngine.openMinimum),
//                decimalFormat.format(tradeEngine.balance-tradeEngine.initialBalance), decimalFormat.format(totalProfitPerMonth), dayNumber, decimalFormat.format(barPerTime), tradeEngine.entryStrategy.getClass().getName(), tradeEngine.exitStrategy.getClass().getName());
//
//        System.out.println("");


        updateStrategy.setInt(1, tradeEngine.series.getEndIndex());
        updateStrategy.setInt(2, tradeEngine.closedOrders.size());
        updateStrategy.setDouble(3, Double.parseDouble(decimalFormatWith2Dec.format(profitableTradesRatio)));
        updateStrategy.setDouble(4, Double.parseDouble(decimalFormatWith2Dec.format(tradeEngine.equityMinimum)));
        updateStrategy.setDouble(5, Double.parseDouble(decimalFormatWith2Dec.format(tradeEngine.balanceDrawDown)));
        updateStrategy.setDouble(6, Double.parseDouble(decimalFormatWith2Dec.format(tradeEngine.openMinimum)));
        updateStrategy.setDouble(7, Double.parseDouble(decimalFormatWith2Dec.format(tradeEngine.openMaximum)));
        updateStrategy.setDouble(8, Double.parseDouble(decimalFormatWith2Dec.format(tradeEngine.balance - tradeEngine.initialBalance)));
        updateStrategy.setDouble(9, Double.parseDouble(decimalFormatWith2Dec.format(totalProfitPerMonth)));
        updateStrategy.setDouble(10, dayNumber);
        updateStrategy.setDouble(11, Double.parseDouble(decimalFormatWith2Dec.format(barPerTime)));
        updateStrategy.setString(12, dateTimeFormatter.format(ZonedDateTime.now()));
        updateStrategy.setString(13, dateTimeFormatter.format(tradeEngine.series.getBar(tradeEngine.series.getEndIndex()).getEndTime()));
        updateStrategy.setDouble(14,tradeEngine.series.getBar(tradeEngine.series.getEndIndex()).getClosePrice().doubleValue());

        updateStrategy.setInt(15, id);
        updateStrategy.executeUpdate();


//        writeMt4Log();


//        getLastStrategyResults(4);
    }


    public void onBarChangeEvent(int timeFrame) throws Exception {

        if (tradeEngine.series.getEndIndex() < 1) return;

//            if (tradeEngine.timeSeriesRepo.timeFramesForBarchange.size()==3) System.out.println("three "+tradeEngine.series.getCurrentTime().toString());
        if (online && tradeEngine.period ==timeFrame) {
            logStrategyResult();
            logFileForBars = new BufferedWriter(new FileWriter(logFileNameForBars, true));
        }
        if (tradeEngine.period ==timeFrame) logFileForBars.write(tradeEngine.series.toString(tradeEngine.series.getCurrentBar()) + "|" + decimalFormatWith2Dec.format(tradeEngine.balance) + "|" + decimalFormatWith2Dec.format(tradeEngine.equity) + "|" +tradeEngine.series.getIndex(tradeEngine.series.getCurrentBar().getEndTime())+"\r\n");

        if (online && tradeEngine.period ==timeFrame) logFileForBars.close();


        if (tradeEngine.logLevel.ordinal() > TradeEngine.LogLevel.BASIC.ordinal())
            logIndicatorValue(timeFrame, tradeEngine.series.getCurrentTime());
        if (tradeEngine.logLevel == TradeEngine.LogLevel.TOTAL && tradeEngine.period ==timeFrame) logExitPrices();
        if (tradeEngine.logLevel == TradeEngine.LogLevel.ANALYSE && tradeEngine.period ==timeFrame) logForAnalyzing();

//        System.out.println("++++ "+tradeEngine.series.toString(tradeEngine.series.getCurrentBar()));
    }

    void logForAnalyzing(){
        ZonedDateTime time = tradeEngine.series.getCurrentTime();
        for (Rule rule : tradeEngine.entryStrategy.ruleForSell.getRuleSet()) rule.isSatisfied(time);
        for (Rule rule : tradeEngine.entryStrategy.ruleForBuy.getRuleSet()) rule.isSatisfied(time);
        if (tradeEngine.entryStrategy.ruleForSell.isSatisfied(time)) {
            tradeSignalsContent.append(tradeSignalIndex).append("|SELL|").append(dateTimeFormatter.format(time)).append("|").append(tradeEngine.timeSeriesRepo.bid).
                    append("|").append(dateTimeFormatter.format(time)).append("|NULL|NULL\r\n");
            tradeSignalIndex++;
        }
        if (tradeEngine.entryStrategy.ruleForBuy.isSatisfied(time)) {
            tradeSignalsContent.append(tradeSignalIndex).append("|BUY|").append(dateTimeFormatter.format(time)).append("|").append(tradeEngine.timeSeriesRepo.bid).
                    append("|").append(dateTimeFormatter.format(time)).append("|NULL|NULL\r\n");
            tradeSignalIndex++;
        }

    }


    public void createPreparedStatement(String key, String command, String table, String columnList) throws Exception {
        String columns[] = columnList.split(",");
        String sql = "";
        if (command.equals("insert")) {
            sql = "insert into " + table + " (";
            for (String column : columns) {
                sql += column + (column.equals(columns[columns.length - 1]) ? ")" : ",");
            }
            sql += " VALUES (";
            for (String column : columns) {
                sql += "?" + (column.equals(columns[columns.length - 1]) ? ")" : ",");
            }
            insertPreparedStatements.put(key, dbConnection.prepareStatement(sql));
        } else if (command.equals("update")) {
            sql = "update " + table + " set ";
            for (String column : columns) {
                if (!column.equals(columns[columns.length - 1])) sql += column + "=?,";
                else sql += column + "=? ";
            }
            sql += " WHERE STRATEGY_ID=?";
            updatePreparedStatements.put(key, dbConnection.prepareStatement(sql));
        }


        System.out.println(sql);
//        PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO STRATEGY_PARAMETER (STRATEGY_ID,TYPE,ACTION,DESCRIPTION,HASHCODE) VALUES (?,?,?,?,?)");
    }

    public void insertToDatabase(String key, Object... values) throws Exception {
        int index = 1;
        PreparedStatement preparedStatement = insertPreparedStatements.get(key);
        for (Object value : values) {
            if (value instanceof String) {
                preparedStatement.setString(index, (String) value);
            } else if (value instanceof Integer) {
                preparedStatement.setLong(index, (Integer) value);
            } else if (value instanceof Double) {
                preparedStatement.setDouble(index, (Double) value);
            }
            index++;
        }
        preparedStatement.executeUpdate();
    }

    public void updateInDatabase(String key, Object... values) throws Exception {
        int index = 1;
        PreparedStatement preparedStatement = updatePreparedStatements.get(key);
        for (Object value : values) {
            if (value instanceof String) {
                preparedStatement.setString(index, (String) value);
            } else if (value instanceof Integer) {
                preparedStatement.setLong(index, (Integer) value);
            } else if (value instanceof Double) {
                preparedStatement.setDouble(index, (Double) value);
            }
            index++;
        }
        preparedStatement.setLong(index, id);
        preparedStatement.executeUpdate();
    }

    String getSourceContent(Object object) {
        String packages[] = object.toString().split("\\.");
        String path = System.getProperty("user.dir") + "\\ta4j-core\\src\\main\\java";
        for (int i = 0; i < packages.length - 1; i++) path += "\\" + packages[i];
//        File file = new File(path+"\\"+strategyElement.getClass().getSimpleName()+".java'");
        String data = "";

        try {
            Stream<String> lines = Files.lines(Paths.get(path + "\\" + object.getClass().getSimpleName() + ".java"));
            data = lines.collect(Collectors.joining("\n"));
            lines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            if (autoCommit) {
                dbConnection.commit();
                logFileForBars.close();
                if (tradeEngine.logLevel.ordinal() == TradeEngine.LogLevel.TOTAL.ordinal())
                    logFileForExitLevels.close();
            }
            dbConnection.setAutoCommit(autoCommit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLastStrategyResults(int number) {
        try {
            PreparedStatement prs = dbConnection.prepareStatement("select * from STRATEGY order by STRATEGY_ID desc");
            ResultSet rs = prs.executeQuery();
            List<String> result = new ArrayList<>();
            int i = 0;
            while (rs.next()) {
                result.add("ID: " + rs.getString("STRATEGY_ID") +
                        ", Trade number: " + rs.getString("TRADE_NUMBER") +
                        ", profitable: " + rs.getString("PROFITABLE_TRADES_RATIO") +
                        ", equity min: " + rs.getString("EQUITY_MINIMUM") +
                        ", balance dd: " + rs.getString("BALANCE_DRAWDOWN") +
                        ", open min: " + rs.getString("OPEN_MINIMUM") +
                        ", open max: " + rs.getString("OPEN_MAXIMUM") +
                        ", total profit/month: " + rs.getString("TOTAL_PROFIT_PER_MONTH"));
                i++;
                if (i == number) break;
            }
            rs.close();
            for (int j = result.size() - 1; j > -1; j--) {
                if (j == 0) System.out.println("");
                System.out.println(result.get(j));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getMT4data(int strategyId) throws Exception {

        String[] fileList = new File(metaTradeFileDir).list();
        for (String fileName : fileList) {
            if (fileName.contains("log4J")) {
                new File(metaTradeFileDir + fileName).delete();
            }
        }

        // bars + balance && equity
        Path sourceFile,targetFile;
        File barFile=new File(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_bars.csv");
        if (barFile.exists()) {
             sourceFile = Paths.get(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_bars.csv");
             targetFile = targetDir.resolve("log4J_balanceEquity.csv");
            copyFile(sourceFile, targetFile);
        }

        // exit levels
        File exitFile=new File(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_exitLevels.csv");
        if (exitFile.exists()) {
            sourceFile = Paths.get(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_exitLevels.csv");
            targetFile = targetDir.resolve("log4J_exits.csv");
            copyFile(sourceFile, targetFile);
        }

        Double closedAmount, openProfit = 0.0;
        try {
            // trades ----------------------------
            // closed trades
            double lastClosePrice=0.0;
            PreparedStatement prs;
            ResultSet rs;
            if (tradeEngine==null) {
                prs = dbConnection.prepareStatement("select LAST_CLOSE_PRICE from STRATEGY WHERE STRATEGY_ID=" + strategyId);
                rs = prs.executeQuery();
                if (rs.next()) lastClosePrice=rs.getDouble(1);
            } else {
                lastClosePrice=(tradeEngine.timeSeriesRepo.bid+ tradeEngine.timeSeriesRepo.ask)/2.0;
            }

            prs = dbConnection.prepareStatement("select * from STRATEGY_TRADE_HISTORY WHERE STRATEGY_ID=" + strategyId);
            rs = prs.executeQuery();
            StringBuffer fileContent = new StringBuffer("");
            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_trades.csv"), "UTF8"));


//            System.out.println("lastClosePrice: "+lastClosePrice);
            while (rs.next()) {
                fileContent.append(rs.getInt("ORDER_ID")).append("|").append(rs.getString("TYPE")).append("|").append(rs.getString("OPEN_TIME")).
                        append("|").append(rs.getDouble("OPEN_PRICE")).append("|").append(rs.getString("CLOSE_TIME")).append("|").append(rs.getDouble("CLOSE_PRICE")).append("|").append(rs.getDouble("CLOSE_AMOUNT")).append("\r\n");

                if ( rs.getDouble("CLOSE_AMOUNT") == 0.0) {
                    if (rs.getString("TYPE").equals("BUY"))
                        openProfit += (lastClosePrice - rs.getDouble("OPEN_PRICE")) * rs.getDouble("OPEN_AMOUNT");
                    else
                        openProfit += (rs.getDouble("OPEN_PRICE") - lastClosePrice) * rs.getDouble("OPEN_AMOUNT");
                }
            }

            rs.close();
            fileWriter.write(fileContent.toString());
            fileWriter.close();

            if (tradeEngine.logLevel== TradeEngine.LogLevel.ANALYSE && tradeSignalsContent.length()>0) {
                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_tradeSignals.csv"), "UTF8"));
                fileWriter.write(tradeSignalsContent.toString());
                fileWriter.close();
            }

            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeTesterFileDir + "log4J_trades.csv"), "UTF8"));
            fileWriter.write(fileContent.toString());
            fileWriter.close();


            // Buy Rules  ----------------------------
            prs = dbConnection.prepareStatement("select * from STRATEGY_PARAMETER WHERE TYPE='BUY' AND STRATEGY_ID=" + strategyId);
            rs = prs.executeQuery();
            fileContent = new StringBuffer("");
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_BuyRules.csv"), "UTF8"));
            while (rs.next()) {
                fileContent.append(rs.getInt("BUFFER_INDEX")).append("|").append(rs.getString("ACTION")).append("|").append(rs.getString("DESCRIPTION")).append("\r\n");
            }
            rs.close();

            prs = dbConnection.prepareStatement("select STRATEGY_PARAMETER.BUFFER_INDEX,STRATEGY_EVALUATION.BAR_TIME from STRATEGY_PARAMETER,STRATEGY_EVALUATION WHERE " +
                    "STRATEGY_PARAMETER.TYPE='BUY' AND STRATEGY_PARAMETER.STRATEGY_ID=" + strategyId + " AND STRATEGY_PARAMETER.STRATEGY_ID=STRATEGY_EVALUATION.STRATEGY_ID AND STRATEGY_PARAMETER.HASHCODE=STRATEGY_EVALUATION.HASHCODE AND VALUE=1");
            rs = prs.executeQuery();
            while (rs.next()) {
                fileContent.append(rs.getInt(1)).append("|").append(rs.getString(2)).append("\r\n");
            }

            fileWriter.write(fileContent.toString());
            fileWriter.close();

            // Sell Rules  ----------------------------
            prs = dbConnection.prepareStatement("select * from STRATEGY_PARAMETER WHERE TYPE='SELL' AND STRATEGY_ID=" + strategyId);
            rs = prs.executeQuery();
            fileContent = new StringBuffer("");
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_SellRules.csv"), "UTF8"));
            while (rs.next()) {
                fileContent.append(rs.getInt("BUFFER_INDEX")).append("|").append(rs.getString("ACTION")).append("|").append(rs.getString("DESCRIPTION")).append("\r\n");
            }
            rs.close();

            prs = dbConnection.prepareStatement("select STRATEGY_PARAMETER.BUFFER_INDEX,STRATEGY_EVALUATION.BAR_TIME from STRATEGY_PARAMETER,STRATEGY_EVALUATION WHERE " +
                    "STRATEGY_PARAMETER.TYPE='SELL' AND STRATEGY_PARAMETER.STRATEGY_ID=" + strategyId + " AND STRATEGY_PARAMETER.STRATEGY_ID=STRATEGY_EVALUATION.STRATEGY_ID AND STRATEGY_PARAMETER.HASHCODE=STRATEGY_EVALUATION.HASHCODE AND VALUE=1");
            rs = prs.executeQuery();
            while (rs.next()) {
                fileContent.append(rs.getInt(1)).append("|").append(rs.getString(2)).append("\r\n");
            }

            fileWriter.write(fileContent.toString());
            fileWriter.close();

            // indicators ----------------------------------
            prs = dbConnection.prepareStatement("select * from STRATEGY_PARAMETER WHERE TYPE='INDICATOR' AND STRATEGY_ID=" + strategyId);
            rs = prs.executeQuery();
            HashMap<Integer, StringBuffer> fileContents = new HashMap<>();
            int subWindowIndex;
            while (rs.next()) {
                subWindowIndex = rs.getInt("SUBWINDOW_INDEX");
                if (!fileContents.containsKey(subWindowIndex)) fileContents.put(subWindowIndex, new StringBuffer(""));
                fileContents.get(subWindowIndex).append(rs.getInt("BUFFER_INDEX")).append("|").append(rs.getString("DESCRIPTION")).append("|").append(rs.getString("COLOR")).append("\r\n");
            }


            prs = dbConnection.prepareStatement("select STRATEGY_PARAMETER.BUFFER_INDEX,STRATEGY_PARAMETER.SUBWINDOW_INDEX,STRATEGY_EVALUATION.BAR_TIME,STRATEGY_EVALUATION.VALUE from STRATEGY_PARAMETER,STRATEGY_EVALUATION WHERE " +
                    "STRATEGY_PARAMETER.TYPE='INDICATOR' AND STRATEGY_PARAMETER.STRATEGY_ID=" + strategyId + " AND STRATEGY_PARAMETER.STRATEGY_ID=STRATEGY_EVALUATION.STRATEGY_ID AND STRATEGY_PARAMETER.HASHCODE=STRATEGY_EVALUATION.HASHCODE");
            rs = prs.executeQuery();
            while (rs.next()) {
                fileContents.get(rs.getInt(2)).append(rs.getInt(1)).append("|").append(rs.getString(3)).append("|").append(decimalFormatWith5Dec.format(rs.getDouble(4))).append("\r\n");
            }

            for (Integer windowIndex : fileContents.keySet()) {
                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_Indicator_" + windowIndex + ".csv"), "UTF8"));
                fileWriter.write(fileContents.get(windowIndex).toString());
                fileWriter.close();
            }


            // endfile
            prs = dbConnection.prepareStatement("select * from STRATEGY WHERE STRATEGY_ID=" + strategyId);
            rs = prs.executeQuery();
            if (rs.next()) {
                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "endFile")));  //jel file direktben
                fileWriter.write(rs.getString("SYMBOL") + "|" + rs.getString("PERIOD") + "\r\n");

                try {
                    prs = dbConnection.prepareStatement("select * from STRATEGY  where strategy_id=" + strategyId);
                    rs = prs.executeQuery();
                    if (rs.next())
                        fileWriter.write(rs.getString("STRATEGY_ID") + "|" + rs.getString("TRADE_NUMBER") + "|" + rs.getString("PROFITABLE_TRADES_RATIO") + "|" + rs.getString("EQUITY_MINIMUM") + "|" + rs.getString("BALANCE_DRAWDOWN") + "|" + rs.getString("TOTAL_PROFIT_PER_MONTH") + "|" + openProfit);
                    rs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }


                fileWriter.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            StringBuffer tradeLogForMT4 = new StringBuffer("");
//            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_trades.csv"), "UTF8"));
//            closedOrders.stream().forEach((order) -> {
//                tradeLogForMT4.append(order.id).append("|").append(order.type).append("|").append(dateFormatter.format(order.openTime)).append("|").append(order.openPrice).append("|").append(dateFormatter.format(order.closeTime)).append("|").append(order.closePrice).append("\r\n");
//            });
//            fileWriter.write(tradeLogForMT4.toString());
//            fileWriter.close();
//
//            for (Integer subWindowIndex : bufferValues.keySet()) {
//                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_" + subWindowIndex + ".csv"), "UTF8"));
//                fileWriter.write(bufferValues.get(subWindowIndex).toString());
//                fileWriter.close();
//            }
//            if (detailedLogMode) {
//                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_exits.csv")));
//                fileWriter.write(exitPriceLogContent.toString());
//                fileWriter.close();
//            }
//
//            StringBuffer prefix = new StringBuffer("Balance: "); //!!!!!!!!!!!!!!
////            StringBuffer prefix = new StringBuffer("Balance: ").append(backTestBalance).
////                    append("\nB").append("|").append(dateFormatter.format(series.getBar(0).getEndTime())).append("|").append(backestOpenBalance).append("\n").
////                    append("E").append("|").append(dateFormatter.format(series.getBar(0).getEndTime())).append("|").append(backestOpenBalance).append("\n");
//            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_balanceEquity.csv")));
//            fileWriter.write(prefix.append(balanceEquityLogContent).toString());
//            fileWriter.close();
//
//            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "endFile")));
//            fileWriter.write("END");
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    void copyFile(Path sourceFile, Path targetFile) {
        try {
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.err.format("I/O Error when copying file "+sourceFile+" to "+targetFile);
        }
    }

    public void getProfitByMonth(int strategyId){

        HashMap<String ,Double> profits=new HashMap<>();
        ZonedDateTime zdt;
        String key;
        Double profit,totalProfit=0.0,totalFee=0.0;
        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from STRATEGY_TRADE_HISTORY where STRATEGY_ID="+strategyId+" order by ORDER_ID");
            while (resultSet.next()) {

                    if (resultSet.getString("CLOSE_TIME")==null) continue;
//                    System.out.println(resultSet.getString("CLOSE_TIME"));

                    zdt = ZonedDateTime.parse(resultSet.getString("CLOSE_TIME"), dateTimeFormatter.withZone(ZoneId.systemDefault()));
                    key=zdt.getYear()+" "+zdt.getMonth();
                    if (profits.containsKey(key)) {
                        profits.replace(key,profits.get(key)+resultSet.getDouble("PROFIT"));
                    } else profits.put(key, resultSet.getDouble("PROFIT"));
//                    dateFormatter.parse(ohlcv.get(firstRowIndex)[0]).toInstant()
//
//                    ZonedDateTime zdt = dateFormatter.parse(resultSet.getString("CLOSE_TIME")).toInstant();




            }

//            resultSet = statement.executeQuery("select TOTAL_PROFIT from STRATEGY where STRATEGY_ID="+strategyId);
//            if (resultSet.next()) totalProfit=resultSet.getDouble(1);
//            resultSet = statement.executeQuery("select 7*sum(CLOSE_AMOUNT)/100000 from STRATEGY_TRADE_HISTORY where STRATEGY_ID="+strategyId);
//            if (resultSet.next()) totalFee=resultSet.getDouble(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (String month: profits.keySet()){
            System.out.println(month+":  "+profits.get(month));
        }
//        System.out.println("");
//        System.out.println("Gross profit: "+totalProfit+",  total fee: "+totalFee+"  net profit: "+(totalProfit-totalFee));



    }

}
