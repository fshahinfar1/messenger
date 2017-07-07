package Model;

import DataBase.DataBaseManager;
import DataBase.UsersData;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fsh on 5/25/17.
 */
public class Server {

    private ServerSocket server;
    private ExecutorService executor;
    // todo : Should I use a database to store connected users? Yes I think
    private HashMap<String, Socket> connectedUsers;

    private boolean flagRun;
    private String id;

    private Date date;
    private DateFormat dFormat;
    private UsersData db;
    private File chatHistory;

    private ListView onlineUsers;

    public Server(int port) throws IOException {
        executor = Executors.newCachedThreadPool();
        dFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        connectedUsers = new HashMap<String, Socket>();
        server = new ServerSocket(port);
        System.out.println("Start Server on port: " + port);
        flagRun = true;
        id = "SERVER-0";
        chatHistory = new File("data/history/" + "fileTest" + ".txt");
        try {
            db = new UsersData("jdbc:sqlite:data/database/users.db", "users_data");
        } catch (SQLException e) {
            System.err.println("couldn't connect to db");
            e.printStackTrace();
        }
        listenForClient();
    }

    private void listenForClient() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                Socket client = null;
                while (flagRun) {
                    if (Thread.currentThread().isInterrupted()) {
                        flagRun = false;
                        return;
                    }

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
                    String id = "";
                    Message message = null;
                    try {
                        String m = dis.readUTF();
                        message = new Message(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("connecting problem, didn't get username");
                        continue;
                    }
                    if (message.getMessageType() == Type.textMessage) {
                        name = message.getContent();
                        id = message.getAuthorId();
                        System.out.println(name);
                    } else {
                        throw new RuntimeException("clientName Not received");
                    }
                    addConnectedClient(id, client);

                    final Socket argScoket = client;
                    final String argId = id;
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handleClient(argScoket, argId);
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

    private void handleClient(Socket client, String id) throws IOException {
        // start in another thread
        DataInputStream dis = null;
        Message message = null;

        try {
            dis = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            beforeClosingHandleThread(id, client, "--");
            return;
        }

        while (flagRun) {
            if (Thread.currentThread().isInterrupted()) {
                flagRun = false;
                beforeClosingHandleThread(id, client, "--");
                return;
            }
            // get a message from client
            try {
                message = new Message(dis.readUTF());  // wait here until a message is sent
            } catch (IOException e) {
                // couldn't read
                beforeClosingHandleThread(id, client, "--");
                return;
            }
            //send message to all clients
            if (message.getMessageType() == Type.textMessage) {
                sendToAll(message);
                // todo: first fix the file manager
//                FileManager fileManager = new FileManager(this.chatHistory);
//                fileManager.InsertMessage(message);
            } else if (message.getMessageType() == Type.fileMessage) {
                // todo: send the file
                JSONObject fileData = null;
                try {
                    fileData = (JSONObject) new JSONParser().parse(message.getContent());
                } catch (ParseException e) {
                    System.err.println("couldn't parse");
                }
                // if file recieved completely
                if (((String) fileData.get("status")).equals("DONE")) {
                    sendToAll("A file is Sent");
                    continue;  // continue the while - listening -
                }
                File file = new File("data/files/" + ((String) fileData.get("name")));
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        System.err.println("*** couldn't make a file ***");
                    }
                }
                RandomAccessFile writer = new RandomAccessFile(file, "rw");
                int lenght = Integer.valueOf((String) fileData.get("length"));
                try {
                    writer.seek(writer.length());  // go to end of the file
                    writer.write(Base64.decode((String) fileData.get("content")), 0, lenght);  // decode and write
                } catch (Base64DecodingException e) {
                    e.printStackTrace();
                }
                writer.close();
            } else if (message.getMessageType() == Type.clientRequestUserList) {
                JSONArray users = new JSONArray();
                //todo: should I send to all users
                users.addAll(connectedUsers.keySet());
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
//                    e.printStackTrace();
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "FAILED");
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;  // login failed so the client is not needed any more
                }
                // get user data from database
                HashMap userLoginData = null;
                try {
                    userLoginData = db.getUserData(userName);
                } catch (SQLException e) {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "FAILED ? USERNAME");
                    System.err.println("couldn't get data from database username don't exist or ...");
                    System.err.println("username: " + userName + " password: " + password);
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;
                }
                // check password
                if (password.equals(userLoginData.get("password"))) {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "ACCEPTED");
                    sendStatus.put("id", userLoginData.get("id"));
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;
                } else {
                    JSONObject sendStatus = new JSONObject();
                    sendStatus.put("status", "WRONG PASSWORD");
                    sendTo(sendStatus.toString(), Type.loginRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;
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
                    beforeClosingHandleThread(id, client, userName);
                    return;
                }
                // todo: I should use a better id
//                    String id = UUID.randomUUID().toString();
                String userId = String.valueOf(db.getLastID("id") + 1);
                // inser data into database
                try {
                    db.insertUserData(userName, password, userId);
                    accountData.put("id", userId);
                    accountData.put("status", "ACCEPTED");
                    sendTo(accountData.toString(), Type.createRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;
                } catch (SQLException err) {
                    // couldn't insert into db
                    System.err.println("couldn't insert into db");
                    accountData.put("status", "FAILED ?USED-USERNAME");
                    sendTo(accountData.toString(), Type.createRequest, client);
                    beforeClosingHandleThread(id, client, userName);
                    return;
                }// end of createRequest
            }
        }
    }

    private synchronized void sendToAll(Message message) {
        for (Socket c : connectedUsers.values()) {
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

    private synchronized void removeDisconnectedClient(String id) {
        // remove disconnected users from the HashMap
        this.connectedUsers.remove(id);
        if (this.onlineUsers != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ObservableList tmp = onlineUsers.getItems();
                    tmp.remove(id);
                    onlineUsers.setItems(tmp);
                }
            });
        }
    }

    private synchronized void addConnectedClient(String id, Socket client) {
        // add client to connectedUsers
        this.connectedUsers.put(id, client);
        if (this.onlineUsers != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ObservableList tmp = onlineUsers.getItems();
                    tmp.add(id);
                    onlineUsers.setItems(tmp);
                }
            });
        }
    }

    private void beforeClosingHandleThread(String id, Socket client, String name) throws IOException {
        client.close();
        removeDisconnectedClient(id);
        System.out.println("client handler thread went down\n\tclient_id: " + id + "\tname: " + name);
    }

    public void connectListView(ListView l) {
        this.onlineUsers = l;
    }

    public void stop() {
        System.out.println("Server is shutting down...");
        this.executor.shutdownNow();
    }

    public static void main(String[] args) {
        // todo: this should change and UI should be added
        Server s = null;
        try {
            s = new Server(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scanner input = new Scanner(System.in);
        while (true) {
            String command = input.next();
            if (command.toLowerCase().equals("quit")) {
                System.out.println("Server is shutting down...");
                s.executor.shutdownNow();
                System.exit(0);
                break;
            }
        }
    }
}
