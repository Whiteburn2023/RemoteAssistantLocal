package ru.otus.java.basic.oop.remoteassistantlocal.viewer;

// ViewerApp.java

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewerApp extends Application {

    public ViewerApp() {
        System.out.println("Конструктор ViewerApp вызван");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ИСПРАВЛЕННЫЙ ПУТЬ:
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/otus/java/basic/oop/remoteassistantlocal/viewer.fxml")
        );
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
        System.out.println("Запуск приложения Viewer...");
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("Фатальная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

