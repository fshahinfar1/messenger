package Model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Created by fsh on 5/25/17.
 */
public class Client {
    private Socket connection;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String clientName;

    public Client(String host, int port, String clientName){
        try {
            connection = new Socket(host, port);
            ois = new ObjectInputStream(connection.getInputStream());
            oos = new ObjectOutputStream(connection.getOutputStream());
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

    public <T extends Serializable> void send(Message<T> message) throws IOException{
        oos.writeObject(message);
        oos.flush();
    }

    public <T extends Serializable> void send(T content, Type type) throws IOException{
        send(new Message<T>(type,content));
    }

    public ObjectInputStream getInputStream() throws IOException{
        return new ObjectInputStream(ois);
    }

    public void close() throws IOException{
        ois.close();
        oos.close();
        connection.close();
    }

}
