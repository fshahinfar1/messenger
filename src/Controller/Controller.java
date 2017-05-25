package Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private ListView usersListView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
