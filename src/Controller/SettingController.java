package Controller;

import Model.Client;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;


/**
 * Created by fsh on 6/14/17.
 */
public class SettingController implements Initializable {
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Button applyButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button testButton;
    @FXML
    private Label promptLabel;

    private String settingFileAddress = "data/setting.txt";
    private File settingFile = new File(settingFileAddress);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // read last setting
        String configStr = "";
        try (Scanner reader = new Scanner(settingFile)) {
            while (reader.hasNext()) {
                configStr += reader.nextLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // show last setting
        try {
            JSONObject config = (JSONObject) new JSONParser().parse(configStr);
            String ip = (String) config.get("ip");
            String port = (String) config.get("port");
            ipTextField.setText(ip);
            portTextField.setText(port);
        } catch (ParseException e) {
            System.err.println("couldn't parse config string");
        }

        // apply button
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get ip and port form input
                String ip = ipTextField.getText();
                String port = portTextField.getText();
                // check ip and port
                if (ip.equals("")) {
                    promptLabel.setText("ip field can't left empty");
                    return;
                }
                if (ip.equals("")) {
                    promptLabel.setText("port field can't left empty");
                    return;
                }
                // write ip and port to the file in JSON format
                JSONObject config = new JSONObject();
                config.put("ip", ip);
                config.put("port", port);
                try (PrintWriter writer = new PrintWriter(settingFile)) {
                    writer.write(config.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                promptLabel.setText("Setting is set");
            }
        });  // end of apply button

        // cancel button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get a handle to the stage
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
            }
        });  // end of cancel button

        // test button
        testButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get ip and port form input
                String ip = ipTextField.getText();
                String port = portTextField.getText();
                // check ip and port
                if (ip.equals("")) {
                    promptLabel.setText("ip field can't left empty");
                    return;
                }
                if (ip.equals("")) {
                    promptLabel.setText("port field can't left empty");
                    return;
                }
                // test connection
                Client testClient = null;
                try {
                    testClient = new Client(ip, Integer.parseInt(port));
                    promptLabel.setText("connected to server");
                } catch (IOException e) {
                    try {
                        testClient.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                    promptLabel.setText("couldn't connect to server");
                }
            }
        });  // end of test button
    }
}
