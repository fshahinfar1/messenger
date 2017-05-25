package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

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
    private ObjectInputStream ois;
    private ExecutorService executor;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Attempting...");
        user = new Client("localhost",1234, "Farbod");
        System.out.println("connected to server");
        // todo: it should not initialize here
        try{
            ois = user.getInputStream();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("Got input Stream");
        executor = Executors.newFixedThreadPool(1);
        listenForServer();  // start listen for server
        System.out.println("listening on servers port");
        // send button
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = chatTextArea.getText();
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
                        message = (Message) ois.readObject();
                    }catch(ClassNotFoundException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if(message.getMessageType() == Type.textMessage){
                        messageTextArea.setText(messageTextArea+"\n"+message.getContent());
                    }
                }
            }
        });

    }
}
