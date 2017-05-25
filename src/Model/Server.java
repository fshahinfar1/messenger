package Model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    public Server(int port) throws IOException{
        executor = Executors.newCachedThreadPool();
        connectedUsers = new HashMap<Socket, String>();
        server = new ServerSocket(port);
        System.out.println("Start Server on port: "+port);
    }

    private void listenForClien(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ObjectInputStream ois = null;
                Socket client = null;
                while(true){
                    try {
                        client = server.accept();
                        ois = new ObjectInputStream(client.getInputStream());
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    String name = "";
                    Message m = null;
                    try {
                        m = (Message) ois.readObject();
                    }catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if (m.getMessageType() == Type.fileMessage){
                        name = (String) m.getContent();
                    }
                    else{
                        throw new RuntimeException("clientName Not recieved");
                    }
                    connectedUsers.put(client,name);
                    handleClient(client, name);
                }
            }
        });
    }

    private void handleClient(Socket client, String name){
        // start in another thread
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ObjectInputStream ois = null;
                Message message = null;
                try {
                   ois = new ObjectInputStream(client.getInputStream());

                }catch (IOException e){
                    e.printStackTrace();
                }
                while(true){
                    try {
                        message = (Message) ois.readObject();
                    }catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }catch (SocketException e) {
                        System.out.println("A client handler thread went down");
                        break;
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if(message.getMessageType() == Type.textMessage){
                        for (Socket c: connectedUsers.keySet()){
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(c.getOutputStream());
                                oos.writeObject(message);
                            }catch (SocketException e){
                                connectedUsers.remove(c);
                                System.out.println("Remove a user from connectedUsers collection");
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }else if(message.getMessageType() == Type.fileMessage){
                        // todo: send the file
                        for (Socket c: connectedUsers.keySet()) {
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(c.getOutputStream());
                                oos.writeObject(new Message<String>(Type.textMessage, "A file is sent"));
                            } catch (SocketException e) {
                                connectedUsers.remove(c);
                                System.out.println("Remove a user from connectedUsers collection");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }
}
