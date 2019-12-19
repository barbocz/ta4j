package org.strategy;

import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.trading.SLTPManager;
import org.test.TradeCenter;
import org.util.SqlLiteConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class TradeStrategy {

    public String key;
    public int timeFrame;
    public TimeSeries series;
    public HashMap<Integer, MT4TimeSeries> timeSeries = new HashMap<>();
    public List<Integer> timeFrames = new ArrayList<>();

    public TimeSeriesRepo timeSeriesRepo;

    public ClosePriceIndicator closePrice;
    private boolean islogged = false;

    public Connection dbConnection = null;
    private PreparedStatement preparedStatementForEvaluation;
    //    public List<Rule> rulesForLog=new ArrayList<>();
    public List<Indicator> indicatorsForLog = new ArrayList<>();
    public Rule ruleForBuy, ruleForSell;

    private Long strategyId;

    private List<Order> openedOrders=new ArrayList<>(),closedOrders=new ArrayList<>();


    public TradeStrategy(String key, int timeFrame, TimeSeriesRepo timeSeriesRepo, TradeCenter controller) {
        this.key = key;
        this.timeFrame = timeFrame;
        this.timeSeriesRepo = timeSeriesRepo;

        strategyId = new Date().getTime();

        series = timeSeriesRepo.getTimeSeries(this.timeFrame);
        timeFrames.add(timeFrame);

        System.out.println(getClass() + " started " + key + " - " + this.timeFrame + "  series:" + series.getEndIndex());

        //-----------------------------------------------------------------------------------------------------
//        closePrice = new ClosePriceIndicator(series);
    }

    public abstract void initStrategy(boolean logOn);

    public abstract void onTickEvent();

    public abstract void onBarChangeEvent(int timeFrame);

    public abstract void onOneMinuteDataEvent();

    public TimeSeries getTimeSeries(int timeFrame) {
        if (!timeFrames.contains(timeFrame)) timeFrames.add(timeFrame);
        return timeSeriesRepo.getTimeSeries(timeFrame);
    }

    public void setLogOn() {

        islogged = true;
//        ruleForSell.setStrategy(this);
//        ruleForSell.getStrategy();
//        ruleForBuy.setStrategy(this);

        try {
            if (dbConnection == null) {
                SqlLiteConnector sqlLiteConnector = new SqlLiteConnector();
                dbConnection = sqlLiteConnector.con;
            }
            setAutoCommit(false);

            Statement stm = dbConnection.createStatement();
            stm.execute("DELETE FROM STRATEGY_TRADE_HISTORY");
            stm.execute("DELETE FROM STRATEGY_EVALUATION");
            stm.execute("DELETE FROM STRATEGY_PARAMETER");
            preparedStatementForEvaluation = dbConnection.prepareStatement("INSERT INTO STRATEGY_EVALUATION VALUES (?,?,?,?,?,?)");

            Rule rule;


            PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO STRATEGY_PARAMETER (STRATEGY_ID,TYPE,ACTION,DESCRIPTION,HASHCODE) VALUES (?,?,?,?,?)");
            if (ruleForBuy != null) {
                HashMap<Integer, String> hm = ruleForBuy.getRuleItems();
                rule = ruleForBuy;
                rule.getDescription();


                for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                    pstmt.setLong(1, strategyId);
                    pstmt.setString(2, "BUY");
                    pstmt.setString(3, "ENTRY");
                    pstmt.setString(4, ruleForBuy.getRuleItems().get(ruleHashMap));
                    pstmt.setInt(5, ruleHashMap);
                    pstmt.executeUpdate();
                    //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                }

//                if (strategy.ruleForBuy != null) {
//                    rule = strategy.ruleForBuy;
//                    rule.getDescription();
//                    for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
//                        pstmt.setLong(1, strategyId);
//                        pstmt.setString(2, "BUY");
//                        pstmt.setString(3, "EXIT");
//                        pstmt.setString(4, strategy.ruleForBuy.getRuleItems().get(ruleHashMap));
//                        pstmt.setInt(5, ruleHashMap);
//                        pstmt.executeUpdate();
//                        //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
//                    }
//                }

//                for (SLTPManager takeProfitManager : strategy.positionManagement.exitLevelsForBuy) {
//                    pstmt.setLong(1, strategyId);
//                    pstmt.setString(2, "BUY");
//                    pstmt.setString(3, "EXIT LEVEL");
//                    pstmt.setString(4, takeProfitManager.getParameters());
//                    pstmt.setInt(5, takeProfitManager.hashCode());
//                    pstmt.executeUpdate();
//                    //                    System.out.println(takeProfitManager.getParameters());
//                }

            }

            if (ruleForSell != null) {
                rule = ruleForSell;
                rule.getDescription();
                for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
                    pstmt.setLong(1, strategyId);
                    pstmt.setString(2, "SELL");
                    pstmt.setString(3, "ENTRY");
                    pstmt.setString(4, ruleForSell.getRuleItems().get(ruleHashMap));
                    pstmt.setInt(5, ruleHashMap);
                    pstmt.executeUpdate();
                    //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
                }

//                if (strategy.ruleForSell != null) {
//                    rule = strategy.ruleForSell;
//                    rule.getDescription();
//
//                    for (Integer ruleHashMap : rule.getRuleItems().keySet()) {
//                        pstmt.setLong(1, strategyId);
//                        pstmt.setString(2, "SELL");
//                        pstmt.setString(3, "EXIT");
//                        pstmt.setString(4, strategy.ruleForSell.getRuleItems().get(ruleHashMap));
//                        pstmt.setInt(5, ruleHashMap);
//                        pstmt.executeUpdate();
//                        //                    System.out.println(ruleHashMap+". "+buyStrategy.getEntryRule().getRuleItems().get(ruleHashMap));
//                    }
//                }
//
//                for (SLTPManager takeProfitManager : strategy.positionManagement.exitLevelsForSell) {
//                    pstmt.setLong(1, strategyId);
//                    pstmt.setString(2, "SELL");
//                    pstmt.setString(3, "EXIT LEVEL");
//                    pstmt.setString(4, takeProfitManager.getParameters());
//                    pstmt.setInt(5, takeProfitManager.hashCode());
//                    pstmt.executeUpdate();
//                    //                    System.out.println(takeProfitManager.getParameters());
//                }

            }

            for (Indicator indicator : indicatorsForLog) {
                pstmt.setLong(1, strategyId);
                pstmt.setString(2, "INDICATOR");
                pstmt.setString(3, "INDICATOR");
                pstmt.setString(4, indicator.getClass().getSimpleName());
                pstmt.setInt(5, indicator.hashCode());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void logRule(Rule rule, ZonedDateTime time, int index, boolean satisfied) {
        try {
            preparedStatementForEvaluation.setLong(1, strategyId);
            preparedStatementForEvaluation.setInt(2, rule.hashCode());
            preparedStatementForEvaluation.setInt(3, index);
            preparedStatementForEvaluation.setInt(4, 0);
            preparedStatementForEvaluation.setDouble(5, satisfied ? 1.0 : 0.0);
            preparedStatementForEvaluation.setString(6, time.toString());
            preparedStatementForEvaluation.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logIndicator(Indicator indicator, ZonedDateTime time, int index, double value) {
        try {
            preparedStatementForEvaluation.setLong(1, strategyId);
            preparedStatementForEvaluation.setInt(2, indicator.hashCode());
            preparedStatementForEvaluation.setInt(3, index);
            preparedStatementForEvaluation.setInt(4, 0);
            preparedStatementForEvaluation.setDouble(5, value);
            preparedStatementForEvaluation.setString(6, time.toString());
            preparedStatementForEvaluation.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            if (autoCommit) dbConnection.commit();
            dbConnection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


