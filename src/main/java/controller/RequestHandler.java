package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

// send something to client
public class RequestHandler extends Thread {
    private Socket socket;
    private HashMap listUser;
    private String username;
    public RequestHandler(Socket socket) {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() {
        try {

            System.out.println("Start talking to "+ username);

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String sendMessage;
            do {
                Scanner sc = new Scanner(System.in);
                sendMessage = sc.nextLine().trim();

                bw.write(sendMessage);
                bw.newLine();
                bw.flush();

                if(sendMessage.equalsIgnoreCase("quit")) {
                    System.out.println("Closing...");
                    this.interrupt();
                    break;
                }


            } while (true);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
