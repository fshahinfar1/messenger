package Model;

import java.io.*;
import java.net.Socket;

/**
 * Created by fsh on 5/25/17.
 */
public class Client {
    private Socket connection;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String clientName;

    public Client(String host, int port, String clientName){
        try {
            connection = new Socket(host, port);
            System.out.println("Client connected");
            dos = new DataOutputStream(connection.getOutputStream());
            dos.flush();
            dis = new DataInputStream(connection.getInputStream());
            System.out.println("streams");
        }catch (IOException e){
            e.printStackTrace();
        }

        this.clientName = clientName;
        // send client name to server
        try {
            send(clientName, Type.textMessage);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Client(String host, int port){
        this(host,port,"GhostUser");
    }

    public void send(Message message) throws IOException{
        dos.writeUTF(message.toString());
        dos.flush();
        System.out.println("Sent");
    }

    public void send(String content, Type type) throws IOException{
        send(new Message(content, type));
    }

    public ObjectInputStream getInputStream() throws IOException{
        return new ObjectInputStream(dis);
    }

    public void close() throws IOException{
        dis.close();
        dos.close();
        connection.close();
    }

    public String getClientName() {
        return clientName;
    }

    public static void main(String[] args) throws Exception{
        // todo: remove this
        // this is for test
        Client user = new Client("localhost",1234, "Farbod");
        System.out.println("Client connected");
    }

}
