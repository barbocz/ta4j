package org;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.strategy.TimeSeriesRepo;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.indicators.mt4Selection.MurrayMathIndicator;
import org.util.SqlLiteConnector;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PreAnalitics {


    TimeSeriesRepo timeSeriesRepo;
    TimeSeries series;

    MurrayMathIndicator murrayMathIndicators[] = new MurrayMathIndicator[13];
    Double murrayLevels[] = new Double[13];
    Double prevMurrayLevels[] = new Double[13];
    MurrayMathIndicator murrayMathIndicator;

    HighPriceIndicator highPriceIndicator;
    HighestValueIndicator highestValueIndicator;

    LowPriceIndicator lowPriceIndicator;
    LowestValueIndicator lowestValueIndicator;


    HashMap<Integer, Double> upperEntryLevels = new HashMap<>();
    HashMap<Integer, Double> lowerEntryLevels = new HashMap<>();
    List<Integer> entryLevelsToRemove = new ArrayList<>();
    List<Integer> exitLevelsToRemove = new ArrayList<>();


    Bar bar;
    int lastBarIndex = -1, currentMurrayLevel, prevMurrayLevel;

    HashMap<Integer, Double> buyEntries = new HashMap<>();

    int prevUpperMurray = 0, preLowerMurray = 0, upperMurray = 0, lowerMurray = 0, murrayDelta = 0;

    int lookForwardAfterMurrayChange = 21, lookForwardAfterEntry = 34;

    HashMap<Integer, Double> upperExitLevels = new HashMap<>();
    HashMap<Integer, Double> lowerExitLevels = new HashMap<>();
    double murrayHeight, entryLevelValue,exitLevelValue;
    int entryLevel;
    MultiKeyMap results = new MultiKeyMap();
    MultiKeyMap plusResultCounter = new MultiKeyMap();
    MultiKeyMap minusResultCounter = new MultiKeyMap();

    Statement statement;
    PreparedStatement preparedStatement;
    Connection dbConnection;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    String openTime="";

    public static void main(String[] args) throws Exception {
        PreAnalitics preTest = new PreAnalitics();
    }


    PreAnalitics() {
        long startTime = System.currentTimeMillis();
        timeSeriesRepo = new TimeSeriesRepo("EURUSD", "backtestEUR.csv", "yyyy.MM.dd HH:mm");
        series = timeSeriesRepo.getTimeSeries(3);
        Arrays.fill(murrayLevels, 0.0);
        Arrays.fill(prevMurrayLevels, 0.0);


//        HighPriceIndicator highPriceIndicator = new HighPriceIndicator(series);
//        HighestValueIndicator highestValueIndicator = new HighestValueIndicator(highPriceIndicator, 4);
//
//        LowPriceIndicator lowPriceIndicator = new LowPriceIndicator(series);
//        LowestValueIndicator lowestValueIndicator = new LowestValueIndicator(lowPriceIndicator, 4);

        for (int i = 0; i < 13; i++)
            murrayMathIndicators[i] = new MurrayMathIndicator(series, 256, i);
        Bar minuteBar;
        double openPrice, highPrice, lowPrice, closePrice;
        int index;
        boolean checkUpper, checkLower;
        int starBar = 256;

        for (int m = 0; m < 13; m++)
            prevMurrayLevels[m] = murrayMathIndicators[m].getValue(starBar).doubleValue();

        try {


            dbConnection = new SqlLiteConnector().con;
            dbConnection.setAutoCommit(false);
            statement = dbConnection.createStatement();
            statement.execute("DELETE FROM  PRETEST_RESULT");
            statement.execute("DELETE FROM  PRETEST_TRADE");

            for (int i = starBar; i < series.getEndIndex(); i++) {
                bar = series.getBar(i);
                ZonedDateTime time = bar.getBeginTime();


                // tick
                openPrice = bar.getOpenPrice().doubleValue();
                highPrice = bar.getHighPrice().doubleValue();
                lowPrice = bar.getLowPrice().doubleValue();
                closePrice = bar.getClosePrice().doubleValue();

                for (int m = 0; m < 13; m++)
                    murrayLevels[m] = murrayMathIndicators[m].getValue(i).doubleValue();

                murrayHeight = murrayLevels[1] - murrayLevels[0];

                currentMurrayLevel = getMurrayLevel(closePrice);
                prevMurrayLevel = getPrevMurrayLevel(closePrice);

                if (prevMurrayLevel == -1 || prevMurrayLevel == 12 || !prevMurrayLevels[currentMurrayLevel].equals(murrayLevels[currentMurrayLevel]) ||
                        !prevMurrayLevels[currentMurrayLevel + 1].equals(murrayLevels[currentMurrayLevel + 1])) {
//                murrayDelta = getMurrayDelta();
                    System.out.println(i + ". " + time);
//                if (i==5458) {
//                    System.out.println();
//                }

                    setEntryLevels();
                    chekEntry(i);
                }

                prevMurrayLevels = Arrays.copyOf(murrayLevels, 13);


            }

            System.out.println();
            dbConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        results.forEach((key1,key2)->System.out.println("Keys set" + key1 + " : Values "+key2));
        System.out.println("Executed in "+ (System.currentTimeMillis()-startTime) + " ms ");

    }


    int getMurrayLevel(double value) {
        if (murrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (murrayLevels[i] <= value && murrayLevels[i + 1] > value) return (i);
            }
            return 12;
        }
        return -1;
    }

    int getPrevMurrayLevel(double value) {
        double murrayHeight=prevMurrayLevels[1]-prevMurrayLevels[0];
        int eIndex;
        if (prevMurrayLevels[6] > value) {
            for (int i = 0; i < 6; i++) {
                if (prevMurrayLevels[i] <= value && prevMurrayLevels[i + 1] > value) return (i);
            }
        } else {
            for (int i = 6; i < 12; i++) {
                if (prevMurrayLevels[i] <= value && prevMurrayLevels[i + 1] > value) return (i);
            }
            eIndex=0;
            while (prevMurrayLevels[12]+murrayHeight*eIndex<value) eIndex++;
            return 11+eIndex;
        }
        eIndex=0;
        while (prevMurrayLevels[0]-murrayHeight*eIndex>value) eIndex++;
        return -1+(-1*eIndex);

    }

    int getMurrayDelta() {

        int entryLevel;

        if (this.entryLevel > 12) entryLevel = 12;
        else if (this.entryLevel < 0) entryLevel = 0;
        else entryLevel = this.entryLevel;


        double murrayHeight = prevMurrayLevels[1] - prevMurrayLevels[0] > murrayLevels[1] - murrayLevels[0] ? murrayLevels[1] - murrayLevels[0] : prevMurrayLevels[1] - prevMurrayLevels[0];

        return (int) ((murrayLevels[entryLevel] - prevMurrayLevels[entryLevel]) / murrayHeight);

    }

    void setEntryLevels() {

        upperEntryLevels.clear();
        lowerEntryLevels.clear();

        lowerEntryLevels.put(currentMurrayLevel, murrayLevels[currentMurrayLevel]);
        lowerEntryLevels.put(currentMurrayLevel - 1, murrayLevels[currentMurrayLevel] - murrayHeight);
        lowerEntryLevels.put(currentMurrayLevel - 2, murrayLevels[currentMurrayLevel] - 2 * murrayHeight);
        upperEntryLevels.put(currentMurrayLevel + 1, murrayLevels[currentMurrayLevel + 1]);
        upperEntryLevels.put(currentMurrayLevel + 2, murrayLevels[currentMurrayLevel + 1] + murrayHeight);

    }

    void chekEntry(int barIndex) {
        entryLevelsToRemove.clear();

        for (int i = barIndex + 1; i < Math.min(barIndex + lookForwardAfterMurrayChange, series.getEndIndex()); i++) {
            for (int entryLevel : upperEntryLevels.keySet()) {
                this.entryLevel = entryLevel;
                entryLevelValue = upperEntryLevels.get(entryLevel);
                if (series.getBar(i).getHighPrice().doubleValue() > entryLevelValue) {
                    openTime=dateTimeFormatter.format(series.getBar(i).getBeginTime());
                    entryLevelsToRemove.add(entryLevel);
                    prevMurrayLevel = getPrevMurrayLevel(entryLevelValue);
                    murrayDelta = getMurrayDelta();
//                    System.out.println(" " + i + ". " + series.getBar(i).getBeginTime() + ",  entry upper level: " + entryLevel + ", prev level: " + prevMurrayLevel + ", delta: " + murrayDelta);
                    setExitLevels();
                    checkExit(i);
                }
            }
            for (Integer levelToRemove : entryLevelsToRemove) upperEntryLevels.remove(levelToRemove);

            entryLevelsToRemove.clear();

            for (int entryLevel : lowerEntryLevels.keySet()) {
                this.entryLevel = entryLevel;
                entryLevelValue = lowerEntryLevels.get(entryLevel);
                if (series.getBar(i).getLowPrice().doubleValue() < entryLevelValue) {
                    openTime=dateTimeFormatter.format(series.getBar(i).getBeginTime());
                    entryLevelsToRemove.add(entryLevel);
                    prevMurrayLevel = getPrevMurrayLevel(entryLevelValue);
                    murrayDelta = getMurrayDelta();
//                    System.out.println(" " + i + ". " + series.getBar(i).getBeginTime() + ",  entry lower level: " + entryLevel + ", prev level: " + prevMurrayLevel + ", delta: " + murrayDelta);
                    setExitLevels();
                    checkExit(i);
                }
            }
            for (Integer levelToRemove : entryLevelsToRemove) lowerEntryLevels.remove(levelToRemove);

        }

    }


    void checkExit(int barIndex) {
        exitLevelsToRemove.clear();
//        if (barIndex==5676) {
//            System.out.println();
//        }
        for (int i = barIndex; i < Math.min(barIndex + lookForwardAfterEntry, series.getEndIndex()); i++) {
            for (Integer index : upperExitLevels.keySet()) {
                if (series.getBar(i).getHighPrice().doubleValue() > upperExitLevels.get(index) ||
                        series.getBar(i).getLowPrice().doubleValue() < lowerExitLevels.get(index)) {
                    exitLevelsToRemove.add(index);
                    double result = 0.0;
                    if (series.getBar(i).getHighPrice().doubleValue() > upperExitLevels.get(index)) {
                        exitLevelValue=upperExitLevels.get(index);
                    } else {
                        exitLevelValue=lowerExitLevels.get(index);
                    }
                    result=(exitLevelValue - entryLevelValue) * 100000.0;
                    try {

//                    System.out.println("      " + series.getBar(i).getBeginTime() + ",  upper exit: " + upperExitLevels.get(index) + " - lower exit: " + lowerExitLevels.get(index) + "  result: " + result);
//                    System.out.println("      " + prevMurrayLevel + ", " + currentMurrayLevel + ", " + murrayDelta + ", " + entryLevel + ", " + index);
//                    if (prevMurrayLevel==8 && entryLevel==4 && murrayDelta==4 && index==2) {
//                        System.out.println("+++++++++++++++++++++++++ "+result);
//                    }
//
//                    if (results.containsKey(prevMurrayLevel, entryLevel, murrayDelta,  index)) {
//                        results.put(prevMurrayLevel, entryLevel, murrayDelta, index, (Double) results.get(prevMurrayLevel, entryLevel, murrayDelta, index) + result);
//                    } else
//                        results.put(prevMurrayLevel, entryLevel, murrayDelta, index, result);


                        preparedStatement = dbConnection.prepareStatement("select id,result,plus,minus from PRETEST_RESULT WHERE PREV=? AND ENTRY=? AND DELTA=? AND EXIT=?");
                        preparedStatement.setInt(1,prevMurrayLevel);
                        preparedStatement.setInt(2,entryLevel);
                        preparedStatement.setInt(3,murrayDelta);
                        preparedStatement.setInt(4,index);
                        ResultSet resultSet=preparedStatement.executeQuery();
                        int id=0;
                        String type="";
                        if (resultSet.next()) {
                            id=resultSet.getInt(1);
                            double totalResult=resultSet.getDouble(2);
                            int plus=resultSet.getInt(3);
                            int minus=resultSet.getInt(4);
                            preparedStatement = dbConnection.prepareStatement("UPDATE PRETEST_RESULT SET RESULT=?,PLUS=?,MINUS=? WHERE ID=?");
                            preparedStatement.setDouble(1,totalResult+result);
                            if (result>0.0) {
                                type="BUY";
                                plus++;
                                preparedStatement.setInt(2,plus);
                                preparedStatement.setInt(3,minus);
                            } else {
                                type="SELL";
                                minus++;
                                preparedStatement.setInt(2,plus);
                                preparedStatement.setInt(3,minus);
                            }
                            preparedStatement.setInt(4,id);
                            preparedStatement.executeUpdate();
                        } else {
                            preparedStatement = dbConnection.prepareStatement("INSERT INTO PRETEST_RESULT (PREV, ENTRY, DELTA, EXIT, RESULT, PLUS,MINUS) VALUES (?,?,?,?,?,?,?)");
                            preparedStatement.setInt(1,prevMurrayLevel);
                            preparedStatement.setInt(2,entryLevel);
                            preparedStatement.setInt(3,murrayDelta);
                            preparedStatement.setInt(4,index);
                            preparedStatement.setDouble(5,result);
                            if (result>0.0) {
                                type="BUY";
                                preparedStatement.setInt(6,1);
                                preparedStatement.setInt(7,0);
                            } else {
                                type="SELL";
                                preparedStatement.setInt(6,0);
                                preparedStatement.setInt(7,1);
                            }
                            preparedStatement.executeUpdate();
                            resultSet = statement.executeQuery("SELECT last_insert_rowid() AS LAST FROM PRETEST_RESULT");

                            if (resultSet.next()) id = resultSet.getInt(1);
                        }

                        preparedStatement = dbConnection.prepareStatement("INSERT INTO PRETEST_TRADE (RESULT_ID, TYPE, OPEN_TIME, OPEN_PRICE, CLOSE_TIME, CLOSE_PRICE) VALUES (?,?,?,?,?,?)");
                        preparedStatement.setInt(1,id);
                        preparedStatement.setString(2,type);
                        preparedStatement.setString(3,openTime);
                        preparedStatement.setDouble(4,entryLevelValue);
                        preparedStatement.setString(5,dateTimeFormatter.format(series.getBar(i).getBeginTime()));
                        preparedStatement.setDouble(6,exitLevelValue);
                        preparedStatement.executeUpdate();


                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (Integer levelToRemove : exitLevelsToRemove) upperExitLevels.remove(levelToRemove);

        }

    }

    void setExitLevels() {
        upperExitLevels.clear();
        lowerExitLevels.clear();
        int index = 0;
        for (int i = 1; i < 3; i++) {
            for (int j = 1; j < 3; j++) {
                upperExitLevels.put(index, entryLevelValue + i * murrayHeight);
                lowerExitLevels.put(index, entryLevelValue - j * murrayHeight);
                index++;
            }
        }

    }


}
