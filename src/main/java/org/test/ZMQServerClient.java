package org.test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ta4j.core.mt4.MT4TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;



public class ZMQServerClient extends Application implements OnSeriesTick{
    Button btn;
    MT4TimeSeries series;

    @Override
    public void start(Stage primaryStage) {
        btn = new Button();
        btn.setText("Receiving...");
        btn.setMaxWidth(500);
        btn.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            btn.setText(series.getSymbol());
        }
        });



        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                System.out.println("OUT EXIT");
                Platform.exit();
                System.exit(0);
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("ZMQ Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        series =new MT4TimeSeries.SeriesBuilder().
                withName("*").
                withPeriod(1).
//                withOhlcvFileName("EURUSD_1_TEST.csv").
                withDateFormatPattern("yyyy.MM.dd HH:mm").
                withNumTypeOf(DoubleNum.class).
                build();



//        Thread myThread = new Thread(new MyRunnable(), "ServerThread");
//        myThread.start();

//        series.registerOnTickEventListener(this);



        Task<String> task = new Task<String>() {

                @Override
                protected String call() throws Exception {

//                    ZMQServerThread zmqServerThread=new ZMQServerThread();
//                    zmqServerThread.run();

                    try (ZContext context = new ZContext()) {
                        // Socket to talk to clients
                        System.out.println("Server started");
                        ZMQ.Socket socket = context.createSocket(SocketType.REP);
                        socket.bind("tcp://*:5555");

                        while (!Thread.currentThread().isInterrupted()) {
                            byte[] reply = socket.recv(0);
                            final String message = new String(reply, ZMQ.CHARSET);



                            String response = "world";
                            socket.send(response.getBytes(ZMQ.CHARSET), 0);
                            System.out.println("message got: "+message);

//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    btn.setText(message);
//                                }
//                            });

//                            series.setSymbol(message);

//                            series.tick(message);

//                            set(message);
//                            primaryStage.setTitle(message);


//                            try {
//                                Thread.sleep(1000); //  Do some 'work'
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }

                    return ("");
                }
            };


        task.setOnSucceeded(ev -> {
            System.out.println("IN");
            btn.setText(task.getValue());

//            this.setItems(task.getValue());
//            this.setPlaceholder(oldPlaceHolder);
        });
        new Thread(task).start();


    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onTickEvent(String message){
        System.out.println("onTickEvent------------- "+message);
    }

    public  class MyRunnable implements Runnable {
        @Override
        public void run() {
            try (ZContext context = new ZContext()) {
                // Socket to talk to clients
                System.out.println("Server started");
                ZMQ.Socket socket = context.createSocket(SocketType.REP);
                socket.bind("tcp://*:5555");

                while (!Thread.currentThread().isInterrupted()) {
                    byte[] reply = socket.recv(0);
                    String message = new String(reply, ZMQ.CHARSET);
                    btn.setText(message);


                    String response = "world";
                    socket.send(response.getBytes(ZMQ.CHARSET), 0);

                    try {
                        Thread.sleep(1000); //  Do some 'work'
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
