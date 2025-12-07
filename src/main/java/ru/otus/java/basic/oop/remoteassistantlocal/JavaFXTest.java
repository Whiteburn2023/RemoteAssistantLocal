package ru.otus.java.basic.oop.remoteassistantlocal;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class JavaFXTest extends Application{
    @Override
    public void start(Stage stage) {
        Label label = new Label("JavaFX работает!");
        Scene scene = new Scene(label, 200, 100);
        stage.setScene(scene);
        stage.setTitle("Тест JavaFX");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
