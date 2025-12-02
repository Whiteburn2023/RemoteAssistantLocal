module ru.otus.java.basic.oop.remoteassistantlocal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens ru.otus.java.basic.oop.remoteassistantlocal to javafx.fxml;
    exports ru.otus.java.basic.oop.remoteassistantlocal;
}