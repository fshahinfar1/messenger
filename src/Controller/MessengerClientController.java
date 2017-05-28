package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessengerClientController implements Initializable {

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private ListView usersListView;

    // todo: it should not initialize here
    // it should be when user log in
    private Client user;
    private DataInputStream dis;
    private ExecutorService executor;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = new Client("localhost",1234, "Farnad");
        System.out.println("connected to server");
        // todo: it should not initialize here
        try{
            dis = user.getInputStream();
        }catch(IOException e){
            e.printStackTrace();
        }
        executor = Executors.newFixedThreadPool(1);
        listenForServer();  // start listen for server
        System.out.println("listening on servers port");

        // get users
        try {
            user.send("GET", Type.clientRequestUserList);
        }catch (IOException e){
            e.printStackTrace();
        }

        // send button
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = chatTextArea.getText()+"\n";
                try {
                    user.send(text, Type.textMessage);
                }catch (IOException e){
                    e.printStackTrace();
                }
                chatTextArea.setText("");
            }
        });
    }

    private void listenForServer(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Message message = null;
                while(true){
                    try{
                        message = new Message(dis.readUTF());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if(message.getMessageType() == Type.textMessage){
                        messageTextArea.setText(messageTextArea.getText()+message.getContent());
                    }else if(message.getMessageType() == Type.clientRequestUserList){
                        String arrayString = message.getContent();
                        try {
                            JSONArray arr = (JSONArray) new JSONParser().parse(arrayString);
                            usersListView.setItems(new ObservableListWrapper(arr));
                        }catch (ParseException e){
                            System.out.println("couldn't parse");
                            System.out.println(arrayString);
                        }
                    }
                }
            }
        });

    }
}
