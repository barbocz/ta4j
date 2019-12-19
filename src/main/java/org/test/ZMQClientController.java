package org.test;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ZMQClientController {
    ZContext context;
    ZMQ.Socket socket;
    int portNumber;
    MT4TimeSeries series;
    HashMap<String,Integer> tradingCores=new HashMap<>();
    int barIndex=0;

    @FXML
    private TextField symbol;

    @FXML
    private TextField period;

    @FXML
    private RadioButton minuteRadioButton;

    @FXML
    private RadioButton tickRadioButton;

    ThreadTest instance;

    public ZMQClientController(){




        series =new MT4TimeSeries.SeriesBuilder().
                withName("*").
                withPeriod(1).
                withOhlcvFileName("EURUSD_1_TEST.csv").
                withDateFormatPattern("yyyy.MM.dd HH:mm").
                withNumTypeOf(DoubleNum.class).
                build();






        Runnable myRunnable =
                new Runnable(){
                    public void run(){
                        try {
                            Class<?> clazz = Class.forName("org.test.ThreadTest");
                            Constructor<?> constructor = clazz.getConstructor(String.class);
                            instance = (ThreadTest)constructor.newInstance("stringparam");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };


        Thread thread = new Thread(myRunnable);
        thread.start();
    }

    public void portInit(ActionEvent e) {
        System.out.printf(symbol.getText()+";"+period.getText());
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:5000");


        String request=symbol.getText()+";"+period.getText();
        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv(0);
        try {
            portNumber=Integer.parseInt(new String(reply, ZMQ.CHARSET));
            tradingCores.put(request,portNumber);

        } catch (NumberFormatException ex) {
            portNumber=0;
        }
        System.out.println("Received " + portNumber);

        socket.close();
        context.close();

    }

    public void test(ActionEvent e) {
        instance.run();
    }

    public void send(ActionEvent e) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:"+tradingCores.get(symbol.getText()+";"+period.getText()));

        System.out.println(tradingCores.get(symbol.getText()+";"+period.getText()));

        // Barchange , az MT4 most küldené az új bar adatait
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        // T - első tag = TICK
        // M - első tag = perces adat ohlcv formátumban
        String request="";
        if (minuteRadioButton.isSelected()) request = "M;"+dateFormatter.format(Date.from(series.getBar(barIndex).getEndTime().toInstant()))+";"+series.getBar(barIndex).getOpenPrice()+";"+series.getBar(barIndex).getHighPrice()+";"+series.getBar(barIndex).getLowPrice()+";"+series.getBar(barIndex).getClosePrice()+";"+series.getBar(barIndex).getVolume();
        else request = "T;"+series.getBar(barIndex).getHighPrice()+";"+series.getBar(barIndex).getLowPrice();

        barIndex++;

        socket.send(request.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv(0);


        socket.close();
        context.close();

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
