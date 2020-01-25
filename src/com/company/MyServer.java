package com.company;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

public class MyServer {
    int port = 7172;
    Vector<Integer> waitingList = new Vector<Integer>();
    public MyServer() {


        try (ServerSocket serverSocket = new ServerSocket(this.port)) {

            System.out.println("Server is listening on port " + this.port);

            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("New client connected");

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);


                System.out.println(socket);
                this.waitingList.add(socket.getPort());
                System.out.println(this.waitingList.size());
                if(this.waitingList.size() < 2)
                {
                    writer.println(0);
                }
                else
                {
                    writer.println(1);
                }

                writer.println(new Date().toString());
                writer.println(socket.getPort());
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
