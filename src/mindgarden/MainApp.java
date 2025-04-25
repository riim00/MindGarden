package mindgarden;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/view/HomeView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("MindGarden 🌱");
        stage.setScene(scene);
        stage.show();
    }

    public static void changeScene(String fxmlFile) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/view/" + fxmlFile));
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}