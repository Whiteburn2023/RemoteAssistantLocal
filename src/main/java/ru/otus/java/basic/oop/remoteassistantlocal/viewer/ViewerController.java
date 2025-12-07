package ru.otus.java.basic.oop.remoteassistantlocal.viewer;

// ViewerController.java
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;

import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import javafx.application.Platform;
import ru.otus.java.basic.oop.remoteassistantlocal.common.Protocol;

import javax.imageio.ImageIO;

public class ViewerController implements Initializable {

    @FXML private TextField ipField;
    @FXML private Button connectButton;
    @FXML private ImageView desktopView;
    @FXML private Label statusLabel;
    @FXML private Label fpsLabel;
    @FXML private Slider qualitySlider;
    @FXML private Pane overlayPane;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private boolean connected = false;
    private long frameCount = 0;
    private long lastFpsUpdate = 0;
    private Thread receiveThread;

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    private final int FIXED_WIDTH = 1920;
    private final int FIXED_HEIGHT = 1080;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Устанавливаем обработчики событий мыши на ImageView
        setupMouseHandlers();

        // Заполняем IP поле локальным IP (для тестирования)
        try {
            String localIP = InetAddress.getLocalHost().getHostAddress();
            ipField.setText(localIP);
        } catch (Exception e) {
            ipField.setText("127.0.0.1");
        }

        // Обновление FPS каждую секунду
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    long currentTime = System.currentTimeMillis();
                    if (lastFpsUpdate > 0) {
                        long fps = frameCount * 1000 / (currentTime - lastFpsUpdate);
                        Platform.runLater(() -> fpsLabel.setText("FPS: " + fps));
                    }
                    lastFpsUpdate = currentTime;
                    frameCount = 0;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    @FXML
    private void handleConnect() {
        if (connected) {
            disconnect();
            connectButton.setText("Подключиться");
        } else {
            connect();
            connectButton.setText("Отключиться");
        }
    }

    private void connect() {
        String ip = ipField.getText();
        if (ip.isEmpty()) {
            showError("Введите IP адрес");
            return;
        }

        Task<Void> connectTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> statusLabel.setText("Подключение..."));

                socket = new Socket(ip, Protocol.PORT);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                connected = true;

                Platform.runLater(() -> {
                    statusLabel.setText("Подключено к " + ip);
                    overlayPane.setVisible(false);
                });

                startReceiving();
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка подключения");
                    showError("Не удалось подключиться к " + ip);
                });
            }
        };

        new Thread(connectTask).start();
    }

    private void startReceiving() {
        receiveThread = new Thread(() -> {
            try {
                while (connected && !socket.isClosed()) {
                    int size = in.readInt();
                    if (size <= 0) continue;

                    byte[] imageData = new byte[size];
                    in.readFully(imageData);

                    Image image = new Image(new ByteArrayInputStream(imageData));

                    Platform.runLater(() -> {
                        desktopView.setImage(image);
                        frameCount++;

                        // УСТАНАВЛИВАЕМ ФИКСИРОВАННЫЙ РАЗМЕР
                        desktopView.setFitWidth(FIXED_WIDTH);
                        desktopView.setFitHeight(FIXED_HEIGHT);
                        desktopView.setPreserveRatio(false);
                    });
                }
            } catch (Exception e) {
                if (connected) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Соединение разорвано");
                        showError("Потеряно соединение с агентом");
                        disconnect();
                    });
                }
            }
        });
        receiveThread.start();
    }

    private void setupMouseHandlers() {
        desktopView.setOnMouseMoved(event -> {
            if (!connected) return;

            // Просто ограничиваем координаты 1920x1080
            int x = (int) Math.max(0, Math.min(event.getX(), FIXED_WIDTH - 1));
            int y = (int) Math.max(0, Math.min(event.getY(), FIXED_HEIGHT - 1));

            sendCommand(Protocol.CMD_MOUSE_MOVE + ":" + x + "," + y);
        });

        // Клики мыши - БЕЗ масштабирования
        desktopView.setOnMouseClicked(event -> {
            if (!connected) return;

            int x = (int) Math.max(0, Math.min(event.getX(), FIXED_WIDTH - 1));
            int y = (int) Math.max(0, Math.min(event.getY(), FIXED_HEIGHT - 1));
            int button = getMouseButtonCode(event.getButton());

            sendCommand(Protocol.CMD_MOUSE_MOVE + ":" + x + "," + y);
            sendCommand(Protocol.CMD_MOUSE_CLICK + ":" + button);
        });

        // Перетаскивание
        desktopView.setOnMouseDragged(event -> {
            if (!connected) return;

            int x = (int) Math.max(0, Math.min(event.getX(), FIXED_WIDTH - 1));
            int y = (int) Math.max(0, Math.min(event.getY(), FIXED_HEIGHT - 1));

            sendCommand(Protocol.CMD_MOUSE_MOVE + ":" + x + "," + y);
        });
    }


    private int getMouseButtonCode(MouseButton button) {
        switch (button) {
            case PRIMARY: return InputEvent.BUTTON1_DOWN_MASK;
            case SECONDARY: return InputEvent.BUTTON3_DOWN_MASK;
            case MIDDLE: return InputEvent.BUTTON2_DOWN_MASK;
            default: return InputEvent.BUTTON1_DOWN_MASK;
        }
    }

    private void sendCommand(String command) {
        if (!connected || out == null) return;

        try {
            out.writeUTF(command);
            out.flush();
        } catch (IOException e) {
            System.err.println("Ошибка отправки команды: " + e.getMessage());
        }
    }

    @FXML
    private void handleQualityChange() {
        if (!connected) return;

        int quality = (int) qualitySlider.getValue();
        sendCommand("SET_QUALITY:" + quality);
    }

    @FXML
    private void takeScreenshot() {
        if (desktopView.getImage() != null) {
            try {
                // Сохраняем текущее изображение в файл
                Image image = desktopView.getImage();
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                File file = new File("screenshot_" + System.currentTimeMillis() + ".png");
                ImageIO.write(bufferedImage, "png", file);

                showInfo("Скриншот сохранен: " + file.getName());
            } catch (Exception e) {
                showError("Ошибка сохранения скриншота");
            }
        }
    }

    public void disconnect() {
        connected = false;

        if (receiveThread != null) {
            receiveThread.interrupt();
        }

        try {
            if (out != null) {
                sendCommand(Protocol.CMD_DISCONNECT);
                out.close();
            }
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            statusLabel.setText("Не подключено");
            connectButton.setText("Подключиться");
            overlayPane.setVisible(true);
            desktopView.setImage(null);
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
