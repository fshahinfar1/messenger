package Controller;

import Model.Client;
import Model.Message;
import Model.Type;
import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessengerClientController implements Initializable {

    @FXML
    private Button sendButton;
    @FXML
    private Button sendFileButton;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private ListView usersListView;
    @FXML
    private VBox messageVBox;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private MenuItem signoutMenuItem;
    @FXML
    private MenuItem exportMenuItem;

    private Client user;
    private DataInputStream dis;
    private ExecutorService executor;

    private String lastAuthor;
    private boolean flagAboutWindow = true;

    public MessengerClientController(Client u) throws RuntimeException {
        // constructor

        if (u.isClosed() || u == null) {
            throw new RuntimeException("null client");
        }

        this.user = u;

        try {
            this.dis = new DataInputStream(u.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // start listen for server
        listenForServer();
        System.out.println("listening on servers port");
        // ask for online users from server
        try {
            user.send("GET", Type.clientRequestUserList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // send button
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = chatTextArea.getText() + "\n";
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (this) {
                                user.send(text);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                chatTextArea.clear();
            }
        });  // end of send button

        //fileSend button
        sendFileButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                List<File> files  = fileChooser.showOpenMultipleDialog(Main.stage);
                String filePath = null;
                for(File file : files){
                    filePath = file.getAbsolutePath();
                    System.out.println(filePath);
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("before start");
                            sendFile(file);
                            System.out.println("start");
                        }
                    });
                }

            }
        });  // end of fileSend button

        // drag over chat TextArea
        chatTextArea.setOnDragOver(new EventHandler<DragEvent>() {
            // accecpt draging over
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if(event.getGestureSource()!= chatTextArea &&db.hasFiles()){
                    event.acceptTransferModes(TransferMode.COPY);
                }
                event.acceptTransferModes(TransferMode.COPY);
            }
        }); // end of on drag over

        chatTextArea.setOnDragDropped(new EventHandler<DragEvent>() {
            // handle drop event
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    String filePath = null;
                    for (File file:db.getFiles()) {
                        filePath = file.getAbsolutePath();
                        System.out.println(filePath);
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("before start");
                                sendFile(file);
                                System.out.println("start");
                            }
                        });
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });  // end of on drag droped

        // close menuItem
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                beforeClose();
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
                            aboutWindowBeforeCloes();
                        }
                    });
                }
            }
        });  // end of about menuItem

        // signout menuItem
        signoutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                beforeClose();
                loadLogin();
            }
        });  // end of signout menuItem

        // export menuItem
        exportMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // open export file
                FileChooser fileChooser = new FileChooser();
                File exportFile = fileChooser.showSaveDialog(Main.stage);
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(exportFile);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }
                // read history
                System.out.println(messageVBox.getChildren().size());
                for(int i=0;i<messageVBox.getChildren().size();i++){
                    Node n = ((HBox) messageVBox.getChildren().get(i)).getChildren().get(0);
                    if(n instanceof Label){
                        Label textLabel = (Label) n;
                        String text = textLabel.getText();
                        // write history to export file
                        writer.append(text);
                    }
                }
                writer.close();
            }
        });  // end of export menuItem
    }

    private void listenForServer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Message message = null;
                while (true) {
                    // if it is interrupted close the thread
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    // get message from server
                    try {
                        message = new Message(dis.readUTF());
                    } catch (IOException e) {
                        System.err.println("cant read from server");
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                connectionLost();
                            }
                        });
                        return;
                    }
                    // handle messages
                    if (message.getMessageType() == Type.textMessage) {
                        // handles textMessage coming from server
                        // show author if needed
                        // todo: it should be by id not by user name
                        String messageAuthor = message.getMessageAuthor();
                        if (!messageAuthor.equals(lastAuthor)) {
                            lastAuthor = messageAuthor;
                            int baseLine = 0;
                            if (messageAuthor.equals(user.getClientName())) {
                                baseLine = 2;
                            }
                            showMessage(messageAuthor+"\n", baseLine);
                        }
                        // show message
                        int baseLine = 0;
                        if (messageAuthor.equals(user.getClientName())) {
                            baseLine = 2;
                        }
                        showMessage(message.getContent(), baseLine);

                    } else if (message.getMessageType() == Type.clientRequestUserList) {
                        // handles UserList request answer comming form server
                        String arrayString = message.getContent();
                        try {
                            JSONArray arr = (JSONArray) new JSONParser().parse(arrayString);
                            usersListView.setItems(new ObservableListWrapper(arr));
                        } catch (ParseException e) {
                            System.out.println("couldn't parse");
                            System.out.println(arrayString);
                        }
                    }
                }
            }
        });
    }

    private void showMessage(String message, int baseLine){
        // create label
        Label messageLabel = new Label(message);
        // pack in HBox
        HBox hBox = new HBox();
        hBox.setPrefWidth(350);
        hBox.getChildren().add(messageLabel);
        if (baseLine == 0) {
            hBox.setAlignment(Pos.BASELINE_LEFT);
        } else if (baseLine == 1) {
            hBox.setAlignment(Pos.BASELINE_CENTER);
        } else {
            hBox.setAlignment(Pos.BASELINE_RIGHT);
        }
        hBox.setSpacing(10);
        // add it to messageVBox
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    messageVBox.getChildren().add(hBox);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(File file){
        System.out.println("start sending a file");
        InputStream reader = null;
        try {
            reader = new FileInputStream(file);
        }catch (FileNotFoundException e){
            System.err.println("file not found");
            return;
        }

        byte[] bytes = new byte[1024];
        int count = 0;
        String prefix = UUID.randomUUID().toString();
        System.out.println(prefix);
        try {
            while (reader.available() > 0) {
                Arrays.fill(bytes, (byte) 0);
                count = reader.read(bytes,0,1024);
                JSONObject fileData = new JSONObject();
                fileData.put("name", prefix+file.getName());
                String tmp = Base64.encode(bytes,count);
                fileData.put("content",tmp );
                fileData.put("status", "SENDING");
                fileData.put("length", String.valueOf(count));
                synchronized (this) {
                    user.send(fileData.toString(), Type.fileMessage);
                }
            }
            JSONObject fileData = new JSONObject();
            fileData.put(prefix+"name", file.getName());
            fileData.put("content","" );
            fileData.put("status", "DONE");
            fileData.put("length", 0);
            synchronized (this) {
                user.send(fileData.toString(), Type.fileMessage);
            }
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("couldn't send the file");
        }
        System.out.println("File has been sent");
    }

    public void beforeClose() {
        executor.shutdownNow();
        try {
            dis.close();
            user.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void aboutWindowBeforeCloes(){
        flagAboutWindow = true;
    }

    private void loadLogin(){
        try{
            // load fxml
            FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                    .getResource("/view/clientMessengerLogin.fxml"));
            Parent root = fxmlLoader.load();
            // create the scene
            Scene scene = new Scene(root, 600, 400);
            Main.stage.setScene(scene);
            Main.stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void connectionLost(){
        System.out.println("*** connection lost ***");
        // stop every thing client is doing
        beforeClose();
        // try to start again
        while(true) {
            // while your not connected try to connect to server
            try {
                user = new Client("localhost", 1234, user.getId(), user.getClientName());
                break;
            } catch (IOException e) {
                System.err.println("couldn't connect");
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                System.err.println("re-try connecting ...");
            }
        }
        System.out.println("Connected to Server");
        try {
            dis = user.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // start the thread pool again
        executor = Executors.newCachedThreadPool();
        // listen for server
        listenForServer();
        // ask for online users from server
        try {
            user.send("GET", Type.clientRequestUserList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
