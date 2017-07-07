package Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import Model.Server;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by fsh on 5/30/17.
 */
public class ServerViewController implements Initializable {
    @FXML
    private Label ipLabel;
    @FXML
    private Label portLabel;
    @FXML
    private ListView onlineUserListView;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    private Server server;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // todo find a better way to make the server
        try {
            server = new Server(1234);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.connectListView(onlineUserListView);

        String ip = "";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            ip = "UnKnown";
        }
        ipLabel.setText(ip);
        // todo: find a way to find the port address
        portLabel.setText("1234");

        // Events
        // close menu item
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                beforeClose();
                Platform.exit();
                System.exit(0);
            }
        });  // end of close menu item

        // about menu item
        aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // load setting stage
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/About.fxml"));
                    Parent root = loader.load();
                    Stage aboutStage = new Stage();
                    aboutStage.setTitle("About");
                    aboutStage.setScene(new Scene(root));
                    // disable current stage
                    aboutStage.initModality(Modality.WINDOW_MODAL);
                    aboutStage.initOwner(ServerMain.stage);
                    aboutStage.showAndWait();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });  // end of about menu item
    }

    public void beforeClose() {
        server.stop();
    }
}
