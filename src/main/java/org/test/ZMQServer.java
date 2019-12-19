package org.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ZMQServer extends Application {



    @Override
    public void start(Stage primaryStage) {

        FXMLLoader loader = null;
        Parent root = null;
        try {
            loader = new FXMLLoader(getClass().getResource("/ZMQServer.fxml"));
            root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("ZMQ - Server");
            primaryStage.show();
            primaryStage.setX(2960);
            primaryStage.setY(10);

        } catch (IOException e) {
            e.printStackTrace();
        }


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                System.out.println("OUT EXIT");
                Platform.exit();
                System.exit(0);
            }
        });

//        Task<String> task = new Task<String>() {
//
//            @Override
//            protected String call() throws Exception {

//                    ZMQServerThread zmqServerThread=new ZMQServerThread();
//                    zmqServerThread.run();

//                try (ZContext context = new ZContext()) {
//                    // Socket to talk to clients
////                    System.out.println("Request for starting a server core ");
//                    ZMQ.Socket socket = context.createSocket(SocketType.REP);
//                    socket.bind("tcp://*:5000");
//
//                    while (!Thread.currentThread().isInterrupted()) {
//                        byte[] reply = socket.recv(0);
//                        final String message = new String(reply, ZMQ.CHARSET);
//
//                        String response = "Trading core is already exists";
//                        System.out.println("Init core for: " + message);
//
//                        String items[] = message.split(";");  // Tcp channel kiosztás üzenetformátuma symbol;timeFrame -> EURUSD;3
//                        String messageSymbol = items[0];
//                        int messagePeriod = Integer.parseInt(items[1]);
//                        boolean exists = false;
//                        for (TradingCore trading : tradingCores) {
//                            if (trading.symbol.equals(messageSymbol) && trading.timeFrame == messagePeriod) {
//                                exists=true;
//                                break;
//                            }
//                        }
//                        if (!exists) {
//                            portNumber++;
//                            response = Integer.toString(portNumber);
//                            tradingCores.add(new TradingCore(messageSymbol, messagePeriod,portNumber));
////                            Task<String> priceUpdateTask = new Task<String>() {
////
////                                @Override
////                                protected String call() throws Exception {
////                                    System.out.println("TradingCore started: " + messageSymbol + "-" + messagePeriod);
////                                    return "";
////                                }
////                            };
////                            new Thread(priceUpdateTask).start();
//
//                        }
//                        socket.addSymbol(response.getBytes(ZMQ.CHARSET), 0);
//
////                            Platform.runLater(new Runnable() {
////                                @Override
////                                public void run() {
////                                    btn.setText(message);
////                                }
////                            });
//
////                            series.setSymbol(message);
//
////                            series.tick(message);
//
////                            set(message);
////                            primaryStage.setTitle(message);
//
//
////                            try {
////                                Thread.sleep(1000); //  Do some 'work'
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                    }
//                }
//
//                return ("");
//            }
//        };
//
//
////        task.setOnSucceeded(ev -> {
////            System.out.println("IN");
////            btn.setText(task.getValue());
////
//////            this.setItems(task.getValue());
//////            this.setPlaceholder(oldPlaceHolder);
////        });
//        new Thread(task).start();


    }

    public static void main(String[] args) {
        launch(args);
    }


//    public  class MyRunnable implements Runnable {
//        @Override
//        public void run() {
//            try (ZContext context = new ZContext()) {
//                // Socket to talk to clients
//                System.out.println("Server started");
//                ZMQ.Socket socket = context.createSocket(SocketType.REP);
//                socket.bind("tcp://*:5555");
//
//                while (!Thread.currentThread().isInterrupted()) {
//                    byte[] reply = socket.recv(0);
//                    String message = new String(reply, ZMQ.CHARSET);
//                    btn.setText(message);
//
//
//                    String response = "world";
//                    socket.addSymbol(response.getBytes(ZMQ.CHARSET), 0);
//
//                    try {
//                        Thread.sleep(1000); //  Do some 'work'
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }


}
