package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by fsh on 5/25/17.
 */
public class MessengerLoginController implements Initializable {
    @FXML
    private TextField userNameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button createButton;
    @FXML
    private Label promptLabel;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem closeMenuItem;

    private boolean flagAboutWindow = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Login button
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get userName and password from input
                String userName = userNameTextField.getText();
                String password = passwordField.getText();
                // check input
                if(userName.equals("")){
                    promptLabel.setText("please choose a username");
                    return;
                }
                if(password.equals("")){
                    promptLabel.setText("please choose a password");
                    return;
                }
                // create client and data input stream (DIS)
                // todo: this id should come from server and get updated
                // todo: should create a client for every click???
                Client user = null;
                try {
                     user = new Client("localhost", 1234, "0", userName);
                }catch (IOException e){
                    promptLabel.setText("problem connecting to server");
                    return;
                }
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
                    user.send(loginRequestMessage.toString(), Type.loginRequest);
                    Message message = new Message(dis.readUTF());

                    if (message.getMessageType() == Type.loginRequest) {
                        JSONObject answer = null;
                        try {
                            answer = (JSONObject) new JSONParser().parse(message.getContent());
                        } catch (ParseException e) {
                            System.err.println("couldn't parse json");
                            e.printStackTrace();
                        }
                        // if accepted
                        if (((String) answer.get("status")).equals("ACCEPTED")) {
                            System.out.println("login accepted");
                            // set user id
                            user.setId((String) answer.get("id"));
                            loadMessengerView(user);
                        } else {
                            if(((String)answer.get("status")).equals("WRONG PASSWORD")) {
                                promptLabel.setText("wrong password");
                            }else if(((String)answer.get("status")).equals("FAILED")){
                                promptLabel.setText("wrong username");
                            }
                            System.out.println(message.getContent());
                        }
                    }
                } catch (IOException e) {
                    promptLabel.setText("problem connecting to server");
                    e.printStackTrace();
                }
            }
        });
        // create button
        createButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get userName and password from input
                String userName = userNameTextField.getText();
                String password = passwordField.getText();
                // check input
                if(userName.equals("")){
                    promptLabel.setText("please choose a username");
                    return;
                }
                if(password.equals("")){
                    promptLabel.setText("please choose a password");
                    return;
                }
                // create client and data input stream (DIS)
                // todo: this id should come from server and get updated
                // todo: should create a client for every click???
                Client user = null;
                try {
                    user = new Client("localhost", 1234, "0", userName);
                }catch (IOException e){
                    promptLabel.setText("problem connecting to server");
                    return;
                }
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
                    user.send(loginRequestMessage.toString(), Type.createRequest);
                    Message message = new Message(dis.readUTF());
                    if (message.getMessageType() == Type.createRequest) {
                        JSONObject answer = null;
                        try {
                            answer = (JSONObject) new JSONParser().parse(message.getContent());
                        } catch (ParseException e) {
                            System.err.println("couldn't parse json");
                            e.printStackTrace();
                        }

                        if (((String)answer.get("status")).equals("ACCEPTED")) {
                            user.setId((String) answer.get("id"));
                            loadMessengerView(user);
                        } else {
                            if(((String)answer.get("status")).equals("FAILED ?USED-USERNAME")){
                                promptLabel.setText("Username is used.");
                            }
                            System.out.println(message.getContent());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }); // end of create button

        // close menuItem
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });// end of close menuItem

        aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(flagAboutWindow) {
                    flagAboutWindow = false;
                    Stage aboutStage = new Stage();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/About.fxml"));
                    Parent root = null;
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    aboutStage.setScene(new Scene(root));
                    aboutStage.setTitle("About");
                    aboutStage.setResizable(false);
                    aboutStage.show();
                    aboutStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            aboutWindowBeforeCloes();
                        }
                    });
                }
            }
        });

    }

    private void aboutWindowBeforeCloes(){
        flagAboutWindow = true;
    }

    private void loadMessengerView(Client user){
        try{
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
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
