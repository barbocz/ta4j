package org.test;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import javafx.concurrent.Task;

public class ZMQServerThread {



    public void run() {
        try (
                ZContext context = new ZContext()) {
            // Socket to talk to clients
            System.out.println("Server started");
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                final String message = new String(reply, ZMQ.CHARSET);


                String response = "responseMessage";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
                System.out.println("message got: " + message);

            }
        }
    }
}
