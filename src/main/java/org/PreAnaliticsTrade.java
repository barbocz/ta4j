package org;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.util.SqlLiteConnector;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class PreAnaliticsTrade {


    public static void main(String[] args) throws Exception {
        Statement statement,statement1;
        PreparedStatement preparedStatement;
        Connection dbConnection;
        Integer prev, entry, delta, exit;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat decimalFormatWith2Dec = new DecimalFormat("#.00", decimalFormatSymbols);
        String sql = "";
        dbConnection = new SqlLiteConnector().con;
        statement = dbConnection.createStatement();
        statement1=dbConnection.createStatement();
        prev = 2;
        entry = 3;
        delta = -1;
        exit = null;
        String metaTradeFileDir="";

        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            metaTradeFileDir=prop.getProperty("mt4.filesDirectory");
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        if (prev != null) sql += " where PREV=" + prev;
        if (sql.length() == 0) sql += " WHERE ENTRY=" + entry;
        else if (entry != null && sql.length() != 0) sql += " AND ENTRY=" + entry;
        if (delta != null && sql.length() != 0) sql += " AND DELTA=" + delta;
        if (exit != null && sql.length() != 0) sql += " AND EXIT=" + exit;
        System.out.println(sql);

        ResultSet resultSet = statement.executeQuery("SELECT sum(minus+plus)  from PRETEST_RESULT group by prev,entry,delta");
        double totalResult = 0;
        int totalPlus = 0, totalMinus = 0, totalTrade = 0, index = 0;
        while (resultSet.next()) {
            totalTrade += resultSet.getDouble(1);
            index++;
        }
        double limitTradeNumber = 0.5 * totalTrade / index;
        double limitRatio = 0.75;
        System.out.println("limitTradeNumber: "+limitTradeNumber+", limitRatio: "+limitRatio);
        StringBuffer fileContent = new StringBuffer("");
        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + "matrix.csv"), "UTF8"));


        double plusRatio,minusRatio;
        resultSet = statement.executeQuery("select prev,entry,delta,sum(result),sum(plus),sum(minus),sum(minus+plus),exit  from PRETEST_RESULT group by prev,entry,delta,exit");
        while (resultSet.next()) {
            plusRatio=(resultSet.getInt(5)*1.0)/resultSet.getInt(7);
            minusRatio=(resultSet.getInt(6)*1.0)/resultSet.getInt(7);
            if (resultSet.getInt(7)*1.0>limitTradeNumber && (plusRatio>limitRatio || minusRatio>limitRatio)) {
                int exitType=resultSet.getInt(4)>0?resultSet.getInt(8):-1*resultSet.getInt(8);
                if ((exitType>0 && plusRatio>limitRatio) || (exitType<0 && minusRatio>limitRatio)) {
                    System.out.println("PREV:" + resultSet.getInt(1) + ", ENTRY:" + resultSet.getInt(2) + ", DELTA:" + resultSet.getInt(3) + ", EXIT:" + resultSet.getInt(8) +
                            "  RESULT:" + decimalFormatWith2Dec.format(resultSet.getDouble(4)) +
                            "  TOTAL:" + resultSet.getInt(7) + "  PLUS:" + decimalFormatWith2Dec.format(plusRatio) + "  MINUS:" + decimalFormatWith2Dec.format(minusRatio)+"  "+exitType);
                    fileContent = fileContent.append(resultSet.getInt(1)).append(",").append(resultSet.getInt(2)).
                            append(",").append(resultSet.getInt(3)).append(",").append(exitType).append("\r\n");
                }
            }
        }


        fileWriter.write(fileContent.toString());
        fileWriter.close();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\ta4j-core\\log\\" + "matrix.csv"));

//        List<String[]> result = new ArrayList<>();
        String[] result;
        String line="";
        MultiKeyMap results = new MultiKeyMap();
        while ((line = bufferedReader.readLine()) != null) {
//            System.out.println(line);
            result=line.split(",");
//            String[] row = Arrays.stream(line.split("|")).toArray(String[]::new);
//            System.out.println(result[0]+" "+result[3]);
            results.put(Integer.parseInt(result[0]), Integer.parseInt(result[1]), Integer.parseInt(result[2]), Integer.parseInt(result[3]));

        }

        System.out.println(results.get(-3,2,-4));
        System.out.println(results.get(12,9,40));



//        StringBuffer fileContent = new StringBuffer("");
//        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaTradeFileDir + "log4J_trades.csv"), "UTF8"));


//        ResultSet resultSet=statement.executeQuery("SELECT ID,RESULT,PLUS,MINUS from PRETEST_RESULT "+sql);
//        ResultSet resultSetOfTrades;
//        double totalResult=0;
//        int totalPlus=0,totalMinus=0;
//        while (resultSet.next()) {
//            totalResult+=resultSet.getDouble(2);
//
//            totalPlus+=resultSet.getInt(3);
//            totalMinus+=resultSet.getInt(4);
//            resultSetOfTrades=statement1.executeQuery("SELECT ID,type,OPEN_TIME,OPEN_PRICE,CLOSE_TIME,CLOSE_PRICE from PRETEST_TRADE WHERE RESULT_ID="+resultSet.getInt(1));
//            while (resultSetOfTrades.next()) {
////                System.out.println(resultSetOfTrades.getString(1)+","+resultSetOfTrades.getString(2));
//                fileContent.append(resultSetOfTrades.getInt(1)).append("|").append(resultSetOfTrades.getString(2)).append("|").append(resultSetOfTrades.getString(3)).
//                        append("|").append(resultSetOfTrades.getDouble(4)).append("|").append(resultSetOfTrades.getString(5)).append("|").append(resultSetOfTrades.getDouble(6)).append("\r\n");
//
//            }
//        }
//
//        fileWriter.write(fileContent.toString());
//        fileWriter.close();
//
//        System.out.println("Total result: "+decimalFormatWith2Dec.format(totalResult)+", total trade: "+(totalPlus+totalMinus)+
//                ", plus: "+decimalFormatWith2Dec.format(100.0*totalPlus/(totalPlus+totalMinus))+", minus: "+decimalFormatWith2Dec.format(100.0*totalMinus/(totalPlus+totalMinus)));


    }


}
