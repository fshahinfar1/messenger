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
import javafx.stage.Modality;
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
    @FXML
    private MenuItem settingMenuItem;

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
                // load setting to create a connection
                String ip="";
                int port = 0;
                try {
                    ip = SettingController.getSettingFileIp();
                    port = SettingController.getSettingFilePort();
                }catch (RuntimeException ex){
                    promptLabel.setText("Couldn't load setting file");
                    loadSettingWindow();
                    return;
                }
                // connect to server
                try {
                     user = new Client(ip, port, "0", userName);
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
        });  // end of login button
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
                // load setting to create a connection
                String ip="";
                int port = 0;
                try {
                    ip = SettingController.getSettingFileIp();
                    port = SettingController.getSettingFilePort();
                }catch (RuntimeException ex){
                    promptLabel.setText("Couldn't load setting file");
                    loadSettingWindow();
                    return;
                };
                Client user = null;
                try {
                    user = new Client(ip, port, "0", userName);
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
        });  // end of create button

        // close menuItem
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });  // end of close menuItem

        // about menuItem
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
                            aboutWindowBeforeClose();
                        }
                    });
                }
            }
        });  // end of about menuItem

        // setting menuItem
        settingMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadSettingWindow();
            }
        });  // end of setting menuItem

    }

    private void aboutWindowBeforeClose(){
        flagAboutWindow = true;
    }

    private void loadSettingWindow(){
        try {
            // load setting stage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/clientSetting.fxml"));
            Parent root = loader.load();
            Stage settingStage =  new Stage();
            settingStage.setTitle("Setting");
            settingStage.setScene(new Scene(root));
//                    settingStage.show();
            // disable current stage
            settingStage.initModality(Modality.WINDOW_MODAL);
            settingStage.initOwner(Main.stage);
            settingStage.showAndWait();
        }catch (IOException e){
            e.printStackTrace();
        }
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
