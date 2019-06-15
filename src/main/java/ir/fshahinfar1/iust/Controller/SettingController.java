package ir.fshahinfar1.iust.Controller;

import ir.fshahinfar1.iust.Model.Client;
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

    private static String settingFileAddress = "data/setting.txt";
    private File settingFile = new File(settingFileAddress);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // read last setting
        String configStr = "";
        if (settingFile.exists()) {
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
                promptLabel.setText("setting configuration is wrong, please set it again.");
            }
        } else {
            promptLabel.setText("setting is not configured yet, please set it.");
        }

        // apply button
        applyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get ip and port form input
                String ip = ipTextField.getText();
                String port = portTextField.getText();
                // check ip and port to be valid
                if (!checkInput()) {
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
                // check ip and port to be valid
                if (!checkInput()) {
                    return;
                }
                // test connection
                Client testClient = null;
                try {
                    testClient = new Client(ip, Integer.parseInt(port));
                    promptLabel.setText("connected to server");
                    testClient.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                    promptLabel.setText("couldn't connect to server");
                }
            }
        });  // end of test button
    }

    private boolean checkInput() {
        // get ip and port form input
        String ip = ipTextField.getText();
        String port = portTextField.getText();
        // check ip and port not to be empty
        if (ip.equals("")) {
            promptLabel.setText("ip field can't left empty");
            return false;
        }
        if (port.equals("")) {
            promptLabel.setText("port field can't left empty");
            return false;
        }
        // port should be integer
        try {
            Integer.parseInt(port);
        } catch (Exception e) {
            promptLabel.setText("port field should be integer");
            return false;
        }
        // it is okay then
        return true;
    }

    // get port from setting file
    public static int getSettingFilePort() throws RuntimeException {
        String configStr = "";
        try (Scanner reader = new Scanner(new File(settingFileAddress))) {
            while (reader.hasNext()) {
                configStr += reader.nextLine();
            }
        } catch (IOException e) {
            System.out.println("couldn't work with file");
        }
        // show last setting
        try {
            JSONObject config = (JSONObject) new JSONParser().parse(configStr);
            String ip = (String) config.get("ip");
            String portStr = (String) config.get("port");
            int port = Integer.parseInt(portStr);
            return port;
        } catch (ParseException e) {
            System.err.println("couldn't parse config string");
            throw new RuntimeException("couldn't get setting");
        }
    }

    // get ip from setting file
    public static String getSettingFileIp() throws RuntimeException {
        String configStr = "";
        try (Scanner reader = new Scanner(new File(settingFileAddress))) {
            while (reader.hasNext()) {
                configStr += reader.nextLine();
            }
        } catch (IOException e) {
            System.out.println("couldn't work with file");
        }
        // show last setting
        try {
            JSONObject config = (JSONObject) new JSONParser().parse(configStr);
            String ip = (String) config.get("ip");
            String portStr = (String) config.get("port");
            return ip;
        } catch (ParseException e) {
            System.err.println("couldn't parse config string");
            throw new RuntimeException("couldn't get setting");
        }
    }
}
