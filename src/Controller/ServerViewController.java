package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import Model.Server;

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
    }

    public void beforeClose() {
        server.stop();
    }
}
