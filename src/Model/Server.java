package Model;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fsh on 5/25/17.
 */
public class Server {

    private ServerSocket server;
    private ExecutorService executor;
    private HashMap<Socket, String> connectedUsers;

    private boolean flagRun;

    private String id;
    private Date date;
    private DateFormat dFormat;

    public Server(int port) throws IOException {
        executor = Executors.newCachedThreadPool();
        dFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        connectedUsers = new HashMap<Socket, String>();
        server = new ServerSocket(port);
        System.out.println("Start Server on port: " + port);
        flagRun = true;
        listenForClient();
        id = "SERVER-0";

    }

    private void listenForClient() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                Socket client = null;
                while (flagRun) {
                    try {
                        System.out.println("waiting for client...");
                        client = server.accept();
                        System.out.println("client connected");
                        // getting user name
                        dis = new DataInputStream(client.getInputStream());
//                        System.out.println("input stream obtained");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String name = "";
                    Message message = null;
                    try {
//                        System.out.println("waiting for username");
                        String m = dis.readUTF();
                        message = new Message(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("connecting problem, didn't get username");
                        continue;
                    }
                    if (message.getMessageType() == Type.textMessage) {
                        name = message.getContent();
                        System.out.println(name);
                    } else {
                        throw new RuntimeException("clientName Not received");
                    }
                    connectedUsers.put(client, name);
                    handleClient(client, name);
                    System.out.println("Client connected and handle thread started");
                }
            }
        });
    }

    private void handleClient(Socket client, String name) {
        // start in another thread
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                Message message = null;
                try {
                    dis = new DataInputStream(client.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("couldn't establish DataInputStream connection");
                    return;
                }
                while (true) {

                    // get a message from client
                    try {
                        message = new Message(dis.readUTF());
                    } catch (SocketException e) {
                        System.out.println("A client handler thread went down");
                        break;
                    } catch (IOException e) {
//                        e.printStackTrace();
                        System.out.println("A client handler thread went down");
                        break;
                    }
                    //send message to all clients
                    if (message.getMessageType() == Type.textMessage) {
//                        checkLastUser(name);
                        sendToAll(message);
                    } else if (message.getMessageType() == Type.fileMessage) {
                        // todo: send the file
//                        checkLastUser(name);
                        sendToAll("A file is sent");
                    } else if (message.getMessageType() == Type.clientRequestUserList) {
                        JSONArray users = new JSONArray();
                        users.addAll(connectedUsers.values());
                        System.out.println(users.toString());
                        sendToAll(users.toString(), Type.clientRequestUserList);
                    } else if (message.getMessageType() == Type.loginRequest) {
                        // todo: working here
                        try {
                            JSONObject loginRequestMessage = (JSONObject) new JSONParser().parse(message.getContent());
                            String userName = (String) loginRequestMessage.get("userName");
                            String password = (String) loginRequestMessage.get("password");
                            System.out.println("user: " + userName + " pass: " + password);
                            sendTo("ACCEPTED", Type.loginRequest, client);
                        } catch (ParseException e) {
                            System.err.println("couldn't get username and password due to parse issue");
                            e.printStackTrace();
                            sendTo("FAILED", Type.loginRequest, client);
                        }
                    }
                }
            }
        });
    }

    private void sendToAll(Message message) {
        for (Socket c : connectedUsers.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(c.getOutputStream());
                dos.writeUTF(message.toString());
            } catch (SocketException e) {
                connectedUsers.remove(c);
                System.out.println("Remove a user from connectedUsers collection");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToAll(String message) {
        sendToAll(new Message(message, Type.textMessage, id, "SERVER", dFormat.format(date.getTime())));
    }

    private void sendToAll(String message, Type type) {
        sendToAll(new Message(message, type, id, "SERVER", dFormat.format(date.getTime())));
    }

    private void sendTo(Message message, Socket c) {
        try {
            DataOutputStream dos = new DataOutputStream(c.getOutputStream());
            dos.writeUTF(message.toString());
        } catch (SocketException e) {
            connectedUsers.remove(c);
            System.out.println("Remove a user from connectedUsers collection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTo(String message, Type type, Socket c) {
        sendTo(new Message(message, type, id, "SERVER", dFormat.format(date.getTime())), c);
    }

    private void sendTo(String message, Socket c) {
        sendTo(new Message(message, Type.textMessage, id, "SERVER", dFormat.format(date.getTime())), c);
    }

    public static void main(String[] args) {
        // todo: this should change and UI should be added
        try {
            Server s = new Server(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
