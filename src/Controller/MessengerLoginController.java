package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fsh on 5/25/17.
 */
public class MessengerLoginController implements Initializable {
    @FXML
    TextField userNameTextField;
    @FXML
    PasswordField passwordField;
    @FXML
    Button loginButton;
    @FXML
    Button createButton;
    @FXML
    MenuItem aboutMenuItem;
    @FXML
    MenuItem closeMenutItem;

    private Client user;
    private DataInputStream dis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // todo: it should not initialize here
        user = new Client("localhost",1234, "Farbod");
        System.out.println("connected to server");
        try{
            dis = user.getInputStream();
        }catch(IOException e){
            e.printStackTrace();
        }

        // loginButton
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String userName = userNameTextField.getText();
                String password = passwordField.getText();
                try {
                    JSONObject loginRequestMessage = new JSONObject();
                    loginRequestMessage.put("userName", userName);
                    loginRequestMessage.put("password", password);
                    user.send(new Message(loginRequestMessage.toString(), Type.loginRequest));
                    Message message = new Message(dis.readUTF());
                    if(message.getMessageType()==Type.loginRequest) {
                        if (message.getContent().equals("ACCEPTED")) {
                            System.out.println("login accepted");
                        } else {
                            System.out.println(message.getContent());
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });

    }
}
