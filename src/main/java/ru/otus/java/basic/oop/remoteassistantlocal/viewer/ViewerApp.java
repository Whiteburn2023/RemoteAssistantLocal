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
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ru/otus/java/basic/oop/remoteassistantlocal/viewer.fxml")
        );
        Parent root = loader.load();

        // Устанавливаем размер окна под 1920x1080 + панели управления
        Scene scene = new Scene(root, 2000, 1200); // Чуть больше для полос прокрутки/панелей
        primaryStage.setTitle("Remote Assistant - Viewer (1920x1080)");
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

