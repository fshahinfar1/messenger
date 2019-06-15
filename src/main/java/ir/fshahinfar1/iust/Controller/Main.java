package ir.fshahinfar1.iust.Controller;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                        .getResource("/view/clientMessengerLogin.fxml"));
        Parent root = fxmlLoader.load();
        // TODO: FIX ICON PROBLEM
        stage.getIcons().add(new Image("file:/data/resource/img/ICON.png"));
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Messenger");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
