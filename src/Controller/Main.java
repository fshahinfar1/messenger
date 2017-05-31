package Controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

public class Main extends Application {
    public static Stage stage;
    @Override
    public void start(Stage stage) throws Exception{
        this.stage = stage;
//        System.out.println("main");
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/clientMessengerView.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/clientMessengerLogin.fxml"));
//        System.out.println("init");
        Parent root = fxmlLoader.load();
//        System.out.println("load");
        stage.setScene(new Scene(root, 600, 400));
//        System.out.println("scene");
        stage.setTitle("Messenger");
        stage.show();
//        System.out.println("show");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
