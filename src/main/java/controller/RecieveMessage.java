package controller;

import server.Server;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// recieve from client
public class RecieveMessage extends Thread {
    Socket socket;
    String username;
    BufferedReader br;
    BufferedWriter bw;

    private final String GET_LIST_ONLINE_FRIEND = "GET_LIST_ONLINE_FRIEND";
    private final String QUIT = "QUIT";
    private final String KEY="5d5ac8f28d8b61ecc42c739310b1b1bb";
    private final String USERNAME="USERNAME";
    public final String SEND_MESSAGE="SEND_MESSAGE";
    public final String USERNAME_FILE="USERNAME_FILE";
    public final String SEND_MESSAGE_FILE="SEND_MESSAGE_FILE";
    public final String CREATE_GROUP="CREATE_GROUP";
    public final String ADD_USER_TO_GROUP="ADD_USER_TO_GROUP";
    public final String GROUP_MESSAGE="GROUP_MESSAGE";
    public final String DOWNLOAD_FILE="DOWNLOAD_FILE";



    public RecieveMessage(Socket socket) throws IOException {
        this.socket = socket;
        this.username = "";
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    }

    String createCommand(String type, String message) {
        return type + "," + KEY + "," + message;
    }

    public void storeFile(String filename) {
        System.out.println("store files");
        InputStream in = null;
        OutputStream out = null;


        try {
            File file = new File("Store_data");
            file.mkdir();

            in = socket.getInputStream();

            File file1 = new File("Store_data\\" + filename);
            out = new FileOutputStream(file1);

            byte[] bytes = new byte[8*1024];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void sendFile(String filename) {
        System.out.println("store files");
        InputStream in = null;
        OutputStream out = null;


        try {
            File file1 = new File("Store_data\\" + filename);
            byte[] bytes = new byte[8 * 1024];
            in = new FileInputStream(file1);
            out = socket.getOutputStream();

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.close();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(String message, String target, boolean isFile) throws Exception {
        try {
            Iterator iterator = Server.listUser.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry user = (Map.Entry) iterator.next();
                String this_user = (String) user.getKey();
                if(this_user.equals(target)) {
                    Socket socket = (Socket) user.getValue();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    String usernameCommand = createCommand(SEND_MESSAGE+ "-" +username, message);
                    if(isFile) {
                        usernameCommand = createCommand(SEND_MESSAGE_FILE+ "-" +username, message);
                    }
                    System.out.println("Send message from " + username);

                    bw.write(usernameCommand);
                    bw.newLine();
                    bw.flush();
                }
            }
        }catch (Exception e) {
            throw e;
        }
        return true;
    }

    public void Interrupting() {
        if(Server.listUser.containsKey(username)) {
            Server.listUser.remove(username);
            Server.SendListOnlineToClient();
            Server.setMessage("number: " + Server.listUser.size());
            System.out.println("number: " + Server.listUser.size());
        }
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            do {
                String recieveMessage = "";
                try {
                    recieveMessage = br.readLine();
                } catch (Exception e) {
                    this.Interrupting();
                    Server.setMessage("Not Listening to "+ username);
                    break;
                }

                String[] specificMessage = recieveMessage.split(",");
                if(specificMessage != null && specificMessage.length == 3) {
                    if(specificMessage[1].equals(KEY)) {
                        switch (specificMessage[0]){
                            case USERNAME:
                                if(!Server.listUser.containsKey(specificMessage[2])) {
                                    Server.listUser.put(specificMessage[2], socket);
                                    this.username = specificMessage[2];
                                }
                                break;
                            case QUIT: {
                                Server.setMessage(username + "interrupt!");
                                Interrupting();
                                break;
                            }
                            case USERNAME_FILE: {
                                storeFile(specificMessage[2]);
                                break;
                            }
                            case CREATE_GROUP: {
                                String groupname = specificMessage[2];
                                if(Server.groupChat.containsKey(groupname)) {
                                    break;
                                }
                                Server.setMessage("create group " + specificMessage[2]);
                                HashMap<String, Socket> a = new HashMap<>();
                                a.put(username, socket);
                                Server.groupChat.put(groupname, a);
                                break;
                            }
                            case DOWNLOAD_FILE: {
                                sendFile(specificMessage[2]);
                                break;
                            }


                            default:
                                String[] cmd = specificMessage[0].split("-");
                                if(cmd.length == 2 && cmd[0].equals(SEND_MESSAGE)) {
                                    System.out.println(cmd[1] +  " send: " + specificMessage[2]);
                                    sendMessage(specificMessage[2], cmd[1], false);
                                }
                                else if(cmd.length == 2 && cmd[0].equals(SEND_MESSAGE_FILE)) {
                                    System.out.println(cmd[1] +  " send: " + specificMessage[2]);
                                    sendMessage(specificMessage[2], cmd[1], true);
                                } else if(cmd.length == 2 && cmd[0].equals(ADD_USER_TO_GROUP)) {
                                    String groupname = specificMessage[2];
                                    if(Server.groupChat.containsKey(groupname)) {
                                        HashMap<String, Socket> hm = Server.groupChat.get(groupname);
                                        if(hm.containsKey(cmd[1])) {
                                            break;
                                        }

                                        Iterator i = Server.listUser.entrySet().iterator();
                                        while (i.hasNext()) {
                                            Map.Entry el = (Map.Entry) i.next();

                                            String this_user = (String) el.getKey();
                                            if(this_user.equals(cmd[1])) {
                                                Socket s = (Socket) el.getValue();
                                                hm.put(cmd[1], s);

                                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                                String usernameCommand = createCommand(ADD_USER_TO_GROUP, groupname);

                                                bw.write(usernameCommand);

                                                bw.newLine();
                                                bw.flush();
                                            }

                                        }
                                    }
                                } else if(cmd.length == 2 && cmd[0].equals(GROUP_MESSAGE)) {
                                    String groupname = cmd[1];
                                    if(Server.groupChat.containsKey(groupname)) {
                                        HashMap<String, Socket> hm = Server.groupChat.get(groupname);


                                        Iterator i = hm.entrySet().iterator();
                                        while (i.hasNext()) {
                                            Map.Entry el = (Map.Entry) i.next();

                                            Socket s = (Socket) el.getValue();

                                            try {

                                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                                                String usernameCommand = createCommand(SEND_MESSAGE+ "-" +username, specificMessage[2]);

                                                bw.write(usernameCommand);

                                                bw.newLine();
                                                bw.flush();

                                            } catch (Exception e) {
                                                e.printStackTrace();

                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }
                }
                else {
                    switch (recieveMessage) {
                        case GET_LIST_ONLINE_FRIEND:
                            Server.setMessage(username + " get list online user!");
                            Server.SendListOnlineToClient();
                            break;
                        case QUIT:
                            System.out.println("Client Stoped!");
                            this.interrupt();
                            break;
                        default:

                            System.out.println( username + " said: " + recieveMessage);
                            break;
                    }
                }
            }while (true);
        } catch (Exception e) {
            e.printStackTrace();
            Interrupting();
        }
    }

    private static String hashMD5Password(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());

        byte[] digest = md.digest();

        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

}
