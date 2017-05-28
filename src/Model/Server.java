package Model;

import org.json.simple.JSONArray;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fsh on 5/25/17.
 */
public class Server {

    private ServerSocket server;
    private ExecutorService executor;
    private HashMap<Socket,String> connectedUsers;
    private String lastUserName;

    public Server(int port) throws IOException{
        executor = Executors.newCachedThreadPool();
        connectedUsers = new HashMap<Socket, String>();
        server = new ServerSocket(port);
        System.out.println("Start Server on port: "+port);
        listenForClient();
    }

    private void listenForClient(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                Socket client = null;
                while(true){
                    try {
                        System.out.println("waiting for client...");
                        client = server.accept();
                        System.out.println("client connected");
                        // getting user name
                        dis = new DataInputStream(client.getInputStream());
//                        System.out.println("input stream obtained");
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    String name = "";
                    Message message = null;
                    try {
//                        System.out.println("waiting for username");
                        String m = dis.readUTF();
                        message = new Message(m);
                    }catch (IOException e){
                        e.printStackTrace();
                        System.out.println("connecting problem, didn't get username");
                        continue;
                    }
                    if (message.getMessageType() == Type.textMessage){
                        name = message.getContent();
                        System.out.println(name);
                    }
                    else{
                        throw new RuntimeException("clientName Not recieved");
                    }
                    connectedUsers.put(client,name);
                    handleClient(client, name);
                    System.out.println("Client connected and handle thread started");
                }
            }
        });
    }

    private void handleClient(Socket client, String name){
        // start in another thread
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DataInputStream dis = null;
                Message message = null;
                try {
                   dis = new DataInputStream(client.getInputStream());
                }catch (IOException e){
                    e.printStackTrace();
                }
                while(true){

                    // get a message from client
                    try {
                        message = new Message(dis.readUTF());
                        if(message == null){
                            System.out.println("A client handler thread went down");
                            break;
                        }
                    }catch (SocketException e) {
                        System.out.println("A client handler thread went down");
                        break;
                    }catch (IOException e){
//                        e.printStackTrace();
                        System.out.println("A client handler thread went down");
                        break;
                    }

                    if(lastUserName != name) {
                        sendToAll("-" + name + ":\n");
                        lastUserName = name;
                    }

                    //send message to all clients
                    if(message.getMessageType() == Type.textMessage){
                        sendToAll(message);
                    }else if(message.getMessageType() == Type.fileMessage){
                        // todo: send the file
                        sendToAll("A file is sent");
                    }else if(message.getMessageType() == Type.clientRequestUserList){
                        JSONArray users = new JSONArray();
                        for(String clientName : connectedUsers.values())
                            users.add(clientName);
                        System.out.println(users.toString());
                        sendToAll(new Message(users.toString(),Type.clientRequestUserList));
                    }
                }
            }
        });
    }

    private void sendToAll(Message message){
        for (Socket c: connectedUsers.keySet()){
            try {
                DataOutputStream dos = new DataOutputStream(c.getOutputStream());
                dos.writeUTF(message.toString());
            }catch (SocketException e){
                connectedUsers.remove(c);
                System.out.println("Remove a user from connectedUsers collection");
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToAll(String message){
        sendToAll(new Message(message, Type.textMessage));
    }

    public static void main(String[] args) {
        try {
            Server s = new Server(1234);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
