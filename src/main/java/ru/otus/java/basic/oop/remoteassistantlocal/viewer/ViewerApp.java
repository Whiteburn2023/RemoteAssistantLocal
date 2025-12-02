package ru.otus.java.basic.oop.remoteassistantlocal.viewer;

// ViewerApp.java
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewer.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Remote Assistant - Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Обработка закрытия окна
        primaryStage.setOnCloseRequest(event -> {
            ViewerController controller = loader.getController();
            controller.disconnect();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

