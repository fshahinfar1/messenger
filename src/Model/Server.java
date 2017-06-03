package Model;

import DataBase.DataBaseManager;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

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
    private DataBaseManager db;


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

        try {
            db = new DataBaseManager();
        } catch (SQLException e) {
            System.err.println("couldn't connect to db");
            e.printStackTrace();
        }

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

                    final Socket argScoket = client;
                    final String argName = name;
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handleClient(argScoket, argName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    System.out.println("Client connected and handle thread started");
                }
            }
        });
    }

    private void handleClient(Socket client, String name) throws IOException {
        // start in another thread
        DataInputStream dis = null;
        Message message = null;

        try {
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("couldn't establish DataInputStream connection");
            return;
        }

        while (true) {

            // get a message from client
            try {
                message = new Message(dis.readUTF());
            } catch (IOException e) {
                System.out.println("A client handler thread went down");
                client.close();
                synchronized (this){
                    connectedUsers.remove(client);
                }
                break;
            }
            //send message to all clients
            if (message.getMessageType() == Type.textMessage) {
                sendToAll(message);
            } else if (message.getMessageType() == Type.fileMessage) {
                // todo: send the file
                sendToAll("A file is sent");
            } else if (message.getMessageType() == Type.clientRequestUserList) {
                JSONArray users = new JSONArray();
                //todo: should I send to all users
                users.addAll(connectedUsers.values());
                sendToAll(users.toString(), Type.clientRequestUserList);
            } else if (message.getMessageType() == Type.loginRequest) {
                // get user data from message
                JSONObject accountData = null;
                String userName = "";
                String password = "";
                try {
                    accountData = (JSONObject) new JSONParser().parse(message.getContent());
                    userName = (String) accountData.get("userName");
                    password = (String) accountData.get("password");
                } catch (ParseException e) {
                    System.err.println("couldn't get username and password due to parse issue");
                    e.printStackTrace();
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "FAILED");
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    synchronized (this) {
                        connectedUsers.remove(client);
                    }
                    client.close();
                    break;
                }
                // get user data from database
                HashMap userLoginData = null;
                try {
                    userLoginData = db.getUserData(userName);
                } catch (SQLException e) {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "FAILED");
                    System.err.println("couldn't get username and password due to parse issue");
                    System.err.println("username: " + userName + " password: " + password);
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    synchronized (this) {
                        connectedUsers.remove(client);
                    }
                    client.close();
                    break;
                }
                // check password
                if (password.equals(userLoginData.get("password"))) {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "ACCEPTED");
                    sendStatus.put("id", userLoginData.get("id"));
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                } else {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "WRONG PASSWORD");
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    synchronized (this) {
                        connectedUsers.remove(client);
                    }
                    client.close();
                    break;
                }
                // end of loginRequest
            } else if (message.getMessageType() == Type.createRequest) {
                // get user data from message
                JSONObject accountData = null;
                String userName = "";
                String password;
                try {
                    accountData = (JSONObject) new JSONParser().parse(message.getContent());
                    userName = (String) accountData.get("userName");
                    password = (String) accountData.get("password");
                } catch (ParseException e) {
                    System.err.println("couldn't parse json -- createRequest --");
                    accountData.put("status", "FAILED");
                    sendTo(accountData.toString(), Type.createRequest, client);
                    synchronized (this) {
                        connectedUsers.remove(client);
                    }
                    client.close();
                    break;
                }
                // todo: I should use a better id
//                    String id = UUID.randomUUID().toString();
                    String id = String.valueOf(db.getLastID() + 1);
                // inser data into database
                try {
                    db.insertUserData(userName, password, id);
                    accountData.put("id", id);
                    accountData.put("status", "ACCEPTED");
                    sendTo(accountData.toString(), Type.createRequest, client);
                } catch (SQLException err) {
                    // couldn't insert into db
                    System.err.println("couldn't insert into db");
                    accountData.put("status", "FAILED ?USED-USERNAME");
                    sendTo(accountData.toString(), Type.createRequest, client);
                    synchronized (this) {
                        connectedUsers.remove(client);
                    }
                    client.close();
                    break;
                }
                // end of createRequest
            }
        }
    }

    private synchronized void sendToAll(Message message) {
        for (Socket c : connectedUsers.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(c.getOutputStream());
                dos.writeUTF(message.toString());
            } catch (IOException e) {
                connectedUsers.remove(c);
                System.out.println("Remove a user from connectedUsers collection");
            }
        }
    }

    private synchronized void sendToAll(String message) {
        sendToAll(new Message(message, Type.textMessage, id, "SERVER", dFormat.format(date.getTime())));
    }

    private synchronized void sendToAll(String message, Type type) {
        sendToAll(new Message(message, type, id, "SERVER", dFormat.format(date.getTime())));
    }

    private synchronized void sendTo(Message message, Socket c) {
        try {
            DataOutputStream dos = new DataOutputStream(c.getOutputStream());
            dos.writeUTF(message.toString());
        } catch (IOException e) {
            connectedUsers.remove(c);
            System.out.println("Remove a user from connectedUsers collection");
        }
    }

    private synchronized void sendTo(String message, Type type, Socket c) {
        sendTo(new Message(message, type, id, "SERVER", dFormat.format(date.getTime())), c);
    }

    private synchronized void sendTo(String message, Socket c) {
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
