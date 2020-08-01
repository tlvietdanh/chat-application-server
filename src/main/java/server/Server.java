package server;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListModel;

import controller.RecieveMessage;
import controller.RequestHandler;
import swings.ServerDashBoard;

public class Server extends Thread {

    private ServerSocket serverSocket;
    public boolean isRunning = false;
    public static HashMap<String, Socket> listUser = new HashMap<>();
    public static HashMap<String, HashMap<String ,Socket>> groupChat = new HashMap<>();

    private static final String GET_LIST_ONLINE_FRIEND = "GET_LIST_ONLINE_FRIEND";
    private static final String KEY="5d5ac8f28d8b61ecc42c739310b1b1bb";

    public static ArrayList<String> message = new ArrayList<>();

    static javax.swing.JList<String> msgList;
    static javax.swing.JScrollPane jScrollPane1;
    public Server(javax.swing.JList<String> msgList, javax.swing.JScrollPane jScrollPane1) {
        this.msgList = msgList;
        this.jScrollPane1 = jScrollPane1;
    }
    
    public static void setMessage(String msg) {
        message.add(msg);
        DefaultListModel<String> listFriend = new DefaultListModel<String>();
        for (int i = 0; i < message.size(); i++) {
            listFriend.addElement(message.get(i));
        }
        msgList.setModel(listFriend);
        jScrollPane1.getVerticalScrollBar().setValue(jScrollPane1.getHorizontalScrollBar().getMaximum());
    }
    
    public void startServer(int port) throws BindException {
        try {
            serverSocket = new ServerSocket(port);
            ServerDashBoard.isRunning = true;
            System.out.println("Starting server at port " + port);
            setMessage("Starting server at port " + port);
            if (this.getState() == Thread.State.NEW)
            {
                 this.start();
            }
            return;
        } catch(BindException b) {
            isRunning = false;
            ServerDashBoard.isRunning = false;
            throw b;
        }
        catch (Exception e) {
            e.printStackTrace();
            isRunning = false;
            ServerDashBoard.isRunning = false;
            System.out.println("Start server failed!");
            setMessage("Start server failed!");

            return;
        }
    }

    public void stopServer() {

        isRunning = false;
        ServerDashBoard.isRunning = false;
        if(serverSocket == null) {
            return;
        }
        setMessage("Interrupt server!");
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
//            Thread.currentThread().interrupt();
        }
        serverSocket = null;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            while (true) {
                if(isRunning) {
                    System.out.println("Waiting for clients...");
                    setMessage("Waiting for clients...");
                    try {
                        Socket s = serverSocket.accept();
                        RecieveMessage recieveMessage = new RecieveMessage(s);
                        recieveMessage.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.stopServer();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.stopServer();
        }
    }

    public static void SendListOnlineToClient() {
        String listOnline = "";

        Iterator iterator = Server.listUser.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry user = (Map.Entry) iterator.next();
            listOnline += user.getKey() + "-";
        }
        if(listOnline.length() > 0) {
            listOnline = listOnline.substring(0, listOnline.length() - 1);
        }

        System.out.println(listOnline);
        String finalMessage = createCommand(GET_LIST_ONLINE_FRIEND, listOnline);
        Iterator iterator1 = Server.listUser.entrySet().iterator();

        while (iterator1.hasNext()) {
            Map.Entry user = (Map.Entry) iterator1.next();
            Socket socket = (Socket) user.getValue();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                bw.write(finalMessage);
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String createCommand(String type, String message) {
        return type + "," + KEY + "," + message;
    }
}


