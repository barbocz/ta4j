package org;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JSON_Test {
    private static final Logger logger = LogManager.getLogger(JSON_Test.class);
    public static void main(String[] args) {

        logger.info("na ezt találd meg és");
        logger.error("hiba");
        logger.warn("warn");
                 String json =  "[{\"action\": \"TRADE_OPEN\", \"magicNumber\": 12345, \"ticketNumber\": 138583296, \"openTime\": \"2020.01.17 11:21:30\", \"openPrice\": 1.11331000]}";
        json =  "[{\"action\": \"TRADE_OPEN\", \"magicNumber\": 12345, \"ticketNumber\": 138583440, \"openTime\": \"2020.01.17 11:26:15\", \"openPrice\": 1.11328000}]";

        json="[{\"action\": \"TRADE_CLOSE_ALL\", \"orders\": { \"closePrice\": 1.11271, \"lot\": 0.10, \"closeTime\": \"2020.01.17 11:57\", \"profit\": -5.70}, { \"closePrice\": 1.11271, \"lot\": 0.20, \"closeTime\": \"2020.01.17 11:57\", \"profit\": -12.00}, { \"closePrice\": 1.11271, \"lot\": 0.20, \"closeTime\": \"2020.01.17 11:57\", \"profit\": -16.00}, { \"closePrice\": 1.11271, \"lot\": 0.20, \"closeTime\": \"2020.01.17 11:57\", \"profit\": -18.80}}}]";
        json="[{\"action\": \"TRADE_OPEN\", \"orders\": [{\"magicNumber\": 0, \"ticketNumber\": 138590705, \"openTime\": \"2020.01.17 15:43:11\", \"openPrice\": 1.11018000}]}]";

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try {
            obj = jsonParser.parse(json);
            JSONArray responseObject = (JSONArray) obj;
            System.out.println(responseObject);
            JSONObject order = (JSONObject) responseObject.get(0);

            System.out.println(order.get("orders"));
            JSONArray orderList = (JSONArray) order.get("orders");
            if (orderList!=null) {
                for (Object o : orderList) {
                    if (o instanceof JSONObject) {
                        JSONObject jso= (JSONObject)o;
                        System.out.println(jso.get("ticketNumber"));
                        Long lTicket=(Long)jso.get("ticketNumber");
                        int mt4TicketNumber = ((Long) jso.get("ticketNumber")).intValue();
                    }
                }
            }


//            System.out.println(order.get("openTime"));
//            System.out.println(order);
//

            //Iterate over employee array
//            employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );

        } catch (ParseException e) {
            e.printStackTrace();
        }

//        JSONObject employeeDetails = new JSONObject();
//        employeeDetails.put("firstName", "Lokesh");
//        employeeDetails.put("lastName", "Gupta");
//        employeeDetails.put("website", 1.23454);
//
//
//        System.out.println(employeeDetails.toJSONString());

//        JSONObject employeeObject = new JSONObject();
//        employeeObject.put("employee", employeeDetails);
//
//        //Second Employee
//        JSONObject employeeDetails2 = new JSONObject();
//        employeeDetails2.put("firstName", "Brian");
//        employeeDetails2.put("lastName", "Schultz");
//        employeeDetails2.put("website", "example.com");
//
//        JSONObject employeeObject2 = new JSONObject();
//        employeeObject2.put("employee", employeeDetails2);
//
//        //Add employees to list
//        JSONArray employeeList = new JSONArray();
//        employeeList.add(employeeObject);
//        employeeList.add(employeeObject2);
//
//        //Write JSON file
//        try (FileWriter file = new FileWriter("employees.json")) {
//
//            file.write(employeeList.toJSONString());
//            file.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



//        final String json =  "[{ \"ticketNumber\": 138446650,\"openTime\": \"2020.01.15 10:49:20\", \"openPrice\": 1.11252000,\"comment\":\"xtx comment\"}]";
//
//        //JSON parser object to parse read file
//        JSONParser jsonParser = new JSONParser();
//        Object obj = null;
//        try {
//            obj = jsonParser.parse(json);
//            JSONArray responseObject = (JSONArray) obj;
//            System.out.println(responseObject);
//            JSONObject order = (JSONObject) responseObject.get(0);
//            System.out.println(order.get("ticketNumber"));
//            System.out.println(order.get("openTime"));
////            System.out.println(order);
////
//
//            //Iterate over employee array
////            employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

//        try (FileReader reader = new FileReader("employees.json"))
//        {
//            //Read JSON file
//
//
//            JSONArray employeeList = (JSONArray) obj;
//            System.out.println(employeeList);
//
//            //Iterate over employee array
//            employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

    }

    private static void parseEmployeeObject(JSONObject employee)
    {
        //Get employee object within list
        JSONObject employeeObject = (JSONObject) employee.get("employee");

        //Get employee first name
        String firstName = (String) employeeObject.get("firstName");
        System.out.println(firstName);

        //Get employee last name
        String lastName = (String) employeeObject.get("lastName");
        System.out.println(lastName);

        //Get employee website name
        String website = (String) employeeObject.get("website");
        System.out.println(website);
    }
}



