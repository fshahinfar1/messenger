package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.WindowEvent;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Login button
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get userName and password from input
                String userName = userNameTextField.getText();
                String password = passwordField.getText();
                // create client and data input stream (DIS)
                Client user = new Client("localhost", 1234, userName);
                DataInputStream dis = null;
                try {
                    dis = user.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("connected to server");
                // send login request to the server
                try {
                    // pack userName and password in a json
                    JSONObject loginRequestMessage = new JSONObject();
                    loginRequestMessage.put("userName", userName);
                    loginRequestMessage.put("password", password);
                    // send the data to the server and get the result
                    user.send(new Message(loginRequestMessage.toString(), Type.loginRequest));
                    Message message = new Message(dis.readUTF());
                    // if accepted
                    if (message.getMessageType() == Type.loginRequest) {
                        if (message.getContent().equals("ACCEPTED")) {
                            System.out.println("login accepted");
                            // load fxml
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                                    .getResource("/view/clientMessengerView.fxml"));
                            // create controller
                            MessengerClientController controller = new MessengerClientController(user);
                            // connect fxml and controller
                            fxmlLoader.setController(controller);
                            GridPane root = fxmlLoader.load();
                            // create the scene
                            Scene scene = new Scene(root, 600, 400);
                            Main.stage.setScene(scene);
                            Main.stage.show();
                            // set the behavior for exit
                            Main.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                @Override
                                public void handle(WindowEvent event) {
                                    controller.beforeClose();
                                }
                            });
                        } else {
                            System.out.println(message.getContent());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
