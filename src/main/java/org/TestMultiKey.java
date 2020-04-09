package org;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.util.SqlLiteConnector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMultiKey {
    public static void main(String ... args){

        try {
            Connection dbConnection;

            dbConnection = new SqlLiteConnector().con;
            dbConnection.setAutoCommit(false);
            Statement statement = dbConnection.createStatement();
            statement.execute("DELETE FROM  PRETEST_RESULT");
            statement.executeUpdate("insert into PRETEST_RESULT (PREV,RESULT) values (22,2.50)");
            dbConnection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }


//        List<Integer> list = Arrays.asList(20, 5, 30);
//
//        for (Integer v:list) {
//            System.out.println(v);
//        }
//        System.out.println();
//
//        list.remove(0);
//        for (Integer v:list) {
//            System.out.println(v);
//        }

//        MultiKeyMap multiKeyMap=new MultiKeyMap();
//        multiKeyMap.put(1,2,3,100.0);
//        multiKeyMap.put(2,4,6,"hali246");
//
//        if (multiKeyMap.containsKey(1,2,3)) multiKeyMap.put(1,2,3,(Double)multiKeyMap.get(1,2,3)+10);
////        multiKeyMap.put(1,2,3,"hali-123");
//        System.out.println(multiKeyMap.get(1,2,3));



//        Object o=multiKeyMap.mapIterator().getKey();
//        System.out.println(o);
//        multiKeyMap.put("EN","Label_Name","Name");
//        multiKeyMap.put("EN","Label_Address","Address");
//        multiKeyMap.put("EN","Label_Age","Age");
//
//        multiKeyMap.put("GJ","Label_Name","નામ");
//        multiKeyMap.put("GJ","Label_Address","સરનામું");
//        multiKeyMap.put("GJ","Label_Age","ઉંમર");
//
//        multiKeyMap.put("JA","Label_Name","名");
//        multiKeyMap.put("JA","Label_Address","住所");
//        multiKeyMap.put("JA","Label_Age","年齢");
//
//
//        System.out.println("Name label in english: " + multiKeyMap.get("EN","Label_Name"));
//        System.out.println("Name label in Gujarati: " + multiKeyMap.get("GJ","Label_Name"));
//        System.out.println("Name label in Japanese: " + multiKeyMap.get("JA","Label_Name"));


    }
}
