package Model;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fsh on 5/25/17.
 */
public class Client {
    private Socket connection;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String id;
    private String clientName;
    private DateFormat dFormat;
    private Date date;

    public Client(String host, int port, String id, String clientName) {
        dFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        try {
            connection = new Socket(host, port);
//            System.out.println("Client connected");
            dos = new DataOutputStream(connection.getOutputStream());
            dos.flush();
            dis = new DataInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.id = id;
        this.clientName = clientName;
        // send client name to server
        try {
            send(clientName, Type.textMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Client(String host, int port) {
        this(host, port, "1111", "GhostUser");
    }

    public void send(Message message) throws IOException {
        dos.writeUTF(message.toString());
        dos.flush();
//        System.out.println("Sent");
    }

    public void send(String content, Type type) throws IOException {
        send(new Message(content, type, id, clientName, dFormat.format(date.getTime())));
    }

    public void send(String content) throws IOException {
        send(new Message(content, Type.textMessage, id, clientName, dFormat.format(date.getTime())));
    }

    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(dis);
    }

    public void close() throws IOException {
        dis.close();
        dos.close();
        connection.close();
    }

    public String getClientName() {
        return clientName;
    }

    public static void main(String[] args) throws Exception {
        // todo: remove this
        // this is for test
        Client user = new Client("localhost", 1234, "1234", "Test");
        System.out.println("Client connected");
    }

    public boolean isClosed() {
        return connection.isClosed();
    }

}
