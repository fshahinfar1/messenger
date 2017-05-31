package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class Main extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/clientMessengerLogin.fxml"));
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Messenger");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
