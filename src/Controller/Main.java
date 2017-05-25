package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/clientMessengerView.fxml"));
        AnchorPane root = (AnchorPane)fxmlLoader.load();
        stage.setScene(new Scene(root, 400, 500));
        stage.setTitle("Messenger");
        stage.setScene(new Scene(root, 300, 275));
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
