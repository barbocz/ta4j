package org.test;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import org.json.simple.JSONObject;

public class ZMQClientController {
    ZContext context;
    ZMQ.Socket socket;
    int portNumber;
    MT4TimeSeries series;
    HashMap<String, Integer> tradingCores = new HashMap<>();
    int barIndex = 0;
    DateTimeFormatter zdtFormatter;

    @FXML
    private TextField symbol;

    @FXML
    private TextField lot;

    @FXML
    private TextField message;

    @FXML
    private RadioButton minuteRadioButton;

    @FXML
    private RadioButton tickRadioButton;

    ThreadTest instance;


    public ZMQClientController() {
        context = new ZContext();
        socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://*:6000");
        socket.setReceiveTimeOut(1000);
        zdtFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.of("Etc/GMT-2"));

//        Runnable myRunnable =
//                new Runnable(){
//                    public void run(){
//                        try {
//                            Class<?> clazz = Class.forName("org.test.ThreadTest");
//                            Constructor<?> constructor = clazz.getConstructor(String.class);
//                            instance = (ThreadTest)constructor.newInstance("stringparam");
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//
//
//        Thread thread = new Thread(myRunnable);
//        thread.start();
    }


    void sendCommandToMetaTrader() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://*:6000");
        socket.setReceiveTimeOut(1000);
//        TRADE|OPEN|1|EURUSD|0.1|0|0|R-to-MetaTrader4|12345678
        JSONObject orderDetails = new JSONObject();
        orderDetails.put("time", zdtFormatter.format(Instant.now().atZone(ZoneId.of("Etc/GMT-2"))));
//        orderDetails.put("action", "TRADE_OPEN");
//        orderDetails.put("action", "TRADE_CLOSE");
//        orderDetails.put("action", "TRADE_CLOSE_ALL");
//        orderDetails.put("action", "GET_OPEN_ORDERS");
        orderDetails.put("action", "TRADE_CLOSE_PARTIAL");



//        orderDetails.put("type", 0);
//        orderDetails.put("symbol", "EURUSD");
        orderDetails.put("lot", 0.14);
////        orderDetails.put("stopLoss", 0.0);
////        orderDetails.put("takeProfit", 1.117);
        orderDetails.put("ticketNumber", 138805259);
//        orderDetails.put("magicNumber", 12345);
//        orderDetails.put("comment", "txt 1234");


        System.out.println(orderDetails.toJSONString());


        socket.send(orderDetails.toJSONString().getBytes(ZMQ.CHARSET), 0);
//            try {
//                Thread.sleep(1000); //  Do some 'work'
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        byte[] reply = socket.recv(0);
        if (reply == null) System.out.println("EMPTY");
        else {
            String response = new String(reply, ZMQ.CHARSET);
            System.out.println("response " + response);
        }


//        System.out.println("Received " + portNumber);


    }

    public void test(ActionEvent e) {
        instance.run();
    }

    public void open(ActionEvent e) {
        sendCommandToMetaTrader();
    }

    public void close(ActionEvent e) {

    }

    public void send(ActionEvent e) {
//        ZContext context = new ZContext();
//        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
//        socket.connect("tcp://localhost:"+tradingCores.get(symbol.getText()+";"+period.getText()));
//
//        System.out.println(tradingCores.get(symbol.getText()+";"+period.getText()));
//
//        // Barchange , az MT4 most küldené az új bar adatait
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
//        // T - első tag = TICK
//        // M - első tag = perces adat ohlcv formátumban
//        String request="";
//        if (minuteRadioButton.isSelected()) request = "M;"+dateFormatter.format(Date.from(series.getBar(barIndex).getEndTime().toInstant()))+";"+series.getBar(barIndex).getOpenPrice()+";"+series.getBar(barIndex).getHighPrice()+";"+series.getBar(barIndex).getLowPrice()+";"+series.getBar(barIndex).getClosePrice()+";"+series.getBar(barIndex).getVolume();
//        else request = "T;"+series.getBar(barIndex).getHighPrice()+";"+series.getBar(barIndex).getLowPrice();
//
//        barIndex++;
//
//        socket.send(request.getBytes(ZMQ.CHARSET), 0);
//        byte[] reply = socket.recv(0);
//
//
//        socket.close();
//        context.close();

//        for (int i = 0; i <9 ; i++) {
//            System.out.println(series.getBar(i).getEndTime()+" - "+series.getBar(i).getClosePrice());
//
//            String request = dateFormatter.format(Date.from(series.getBar(i).getEndTime().toInstant()))+";"+series.getBar(i).getOpenPrice()+";"+series.getBar(i).getHighPrice()+";"+series.getBar(i).getLowPrice()+";"+series.getBar(i).getClosePrice()+";"+series.getBar(i).getVolume();
//
//            socket.addSymbol(request.getBytes(ZMQ.CHARSET), 0);
//            byte[] reply = socket.recv(0);
//
//            try {
//                Thread.sleep(1000); //  Do some 'work'
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//
//        }
    }
}
