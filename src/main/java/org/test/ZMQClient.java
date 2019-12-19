package org.test;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.scene.control.TextField;


public class ZMQClient extends Application {


    @Override
    public void start(Stage primaryStage) {

        FXMLLoader loader = null;
        Parent root = null;
        try {

            loader = new FXMLLoader(getClass().getResource("/ZMQClient.fxml"));
            root = loader.load();
//            ZMQClientController controller = (ZMQClientController) loader.getController();
            Scene scene=new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("ZMQ - CLIENT");
            primaryStage.setX(2000);
            primaryStage.setY(100);
            primaryStage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }

//        Timer time = new Timer(); // Instantiate Timer Object
//        timer.ScheduledAction scheduledAction=new timer.ScheduledAction(priceData,controller);
//        time.schedule(scheduledAction, calendar.getTime(), TimeUnit.MINUTES.toMillis(1));


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                System.out.println("OUT EXIT");
                Platform.exit();
                System.exit(0);
            }
        });








    }

    public static void main(String[] args) {
        launch(args);
    }

//    public static class MyRunnable implements Runnable {
//        @Override
//        public void run(){
//            try (ZContext context = new ZContext()) {
//                //  Socket to talk to server
//                System.out.println("Connecting to hello world server");
//
//                ZMQ.Socket socket = context.createSocket(SocketType.REQ);
//                socket.connect("tcp://localhost:5555");
//
//                for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
//                    String request = "Hello";
//                    System.out.println("Sending Hello " + requestNbr);
//                    socket.addSymbol(request.getBytes(ZMQ.CHARSET), 0);
//
//                    byte[] reply = socket.recv(0);
//                    System.out.println(
//                            "Received " + new String(reply, ZMQ.CHARSET) + " " +
//                                    requestNbr
//                    );
//                }
//            }
//        }
//    }




}

