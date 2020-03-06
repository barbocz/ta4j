package org;

import org.h2.jdbcx.JdbcConnectionPool;
import org.strategy.TradeEngine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

public class GetStrategyLog {
    public static void main(String[] args) throws Exception {
        int strategyId = 2627;

        JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create("jdbc:h2:~\\ta4j;", "sa", "12345");
        Connection dbConnection;
        dbConnection = jdbcConnectionPool.getConnection();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        DateTimeFormatter dateTimeFormatterWithSeconds = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        DecimalFormat decimalFormatWith2Dec, decimalFormatWith5Dec;

        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatWith2Dec = new DecimalFormat("#.00", decimalFormatSymbols);
        decimalFormatWith5Dec = new DecimalFormat("#.00000", decimalFormatSymbols);

        InputStream input = new FileInputStream("config.properties");
        Properties prop = new Properties();
        prop.load(input);
        String metaTradeFileDir = prop.getProperty("mt4.filesDirectory");
        Path targetDir = Paths.get(metaTradeFileDir);

        String[] fileList = new File(metaTradeFileDir).list();
        for (String fileName : fileList) {
            if (fileName.contains("log4J")) {
                new File(metaTradeFileDir + fileName).delete();
            }
        }

        // bars + balance && equity
        Path sourceFile, targetFile;
        File barFile = new File(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_bars.csv");
        if (barFile.exists()) {
            sourceFile = Paths.get(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_bars.csv");
            targetFile = targetDir.resolve("log4J_balanceEquity.csv");
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // exit levels
        File exitFile = new File(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_exitLevels.csv");
        if (exitFile.exists()) {
            sourceFile = Paths.get(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + strategyId + "_exitLevels.csv");
            targetFile = targetDir.resolve("log4J_exits.csv");
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        Double closedAmount, openProfit = 0.0;

        // trades ----------------------------
        // closed trades
        double lastClosePrice = 0.0;
        PreparedStatement prs;
        ResultSet rs;

        prs = dbConnection.prepareStatement("select LAST_CLOSE_PRICE from STRATEGY WHERE STRATEGY_ID=" + strategyId);
        rs = prs.executeQuery();
        if (rs.next()) lastClosePrice = rs.getDouble(1);


        prs = dbConnection.prepareStatement("select * from STRATEGY_TRADE_HISTORY WHERE STRATEGY_ID=" + strategyId);
        rs = prs.executeQuery();
        StringBuffer fileContent = new StringBuffer("");
        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_trades.csv"), "UTF8"));


//            System.out.println("lastClosePrice: "+lastClosePrice);
        while (rs.next()) {
            fileContent.append(rs.getInt("ORDER_ID")).append("|").append(rs.getString("TYPE")).append("|").append(rs.getString("OPEN_TIME")).
                    append("|").append(rs.getDouble("OPEN_PRICE")).append("|").append(rs.getString("CLOSE_TIME")).append("|").append(rs.getDouble("CLOSE_PRICE")).append("|").append(rs.getDouble("CLOSE_AMOUNT")).append("\r\n");

            if (rs.getDouble("CLOSE_AMOUNT") == 0.0) {
                if (rs.getString("TYPE").equals("BUY"))
                    openProfit += (lastClosePrice - rs.getDouble("OPEN_PRICE")) * rs.getDouble("OPEN_AMOUNT");
                else
                    openProfit += (rs.getDouble("OPEN_PRICE") - lastClosePrice) * rs.getDouble("OPEN_AMOUNT");
            }
        }

        rs.close();
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


    }

}
