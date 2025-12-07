package ru.otus.java.basic.oop.remoteassistantlocal.viewer;

import ru.otus.java.basic.oop.remoteassistantlocal.common.Command;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;  // Добавляем импорт для ScrollEvent
import java.awt.event.InputEvent;

/**
 * Обработчик событий мыши для преобразования координат и отправки команд
 */
public class MouseHandler {
    private ImageView desktopView;
    private ConnectionManager connectionManager;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    private boolean mousePressed = false;
    private int lastX = 0;
    private int lastY = 0;

    public MouseHandler(ImageView desktopView, ConnectionManager connectionManager) {
        this.desktopView = desktopView;
        this.connectionManager = connectionManager;
        setupEventHandlers();
    }

    /**
     * Настройка обработчиков событий мыши
     */
    private void setupEventHandlers() {
        desktopView.setOnMousePressed(this::handleMousePressed);
        desktopView.setOnMouseReleased(this::handleMouseReleased);
        desktopView.setOnMouseMoved(this::handleMouseMoved);
        desktopView.setOnMouseDragged(this::handleMouseDragged);
        desktopView.setOnMouseClicked(this::handleMouseClicked);
        desktopView.setOnMouseExited(this::handleMouseExited);
        desktopView.setOnScroll(this::handleMouseScroll);  // Теперь должно работать

        desktopView.setFocusTraversable(true);
    }

    /**
     * Обновление параметров масштабирования
     */
    public void updateScaling(double imageWidth, double imageHeight,
                              double viewWidth, double viewHeight) {
        if (imageWidth > 0 && imageHeight > 0 && viewWidth > 0 && viewHeight > 0) {
            // Рассчитываем масштаб для сохранения пропорций
            double scaleFit = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

            scaleX = imageWidth / (imageWidth * scaleFit);
            scaleY = imageHeight / (imageHeight * scaleFit);

            // Рассчитываем смещение для центрированного изображения
            offsetX = (viewWidth - imageWidth * scaleFit) / 2;
            offsetY = (viewHeight - imageHeight * scaleFit) / 2;
        }
    }

    /**
     * Преобразование координат мыши в координаты экрана
     */
    private Command.Point convertCoordinates(double viewX, double viewY) {
        // Учитываем смещение для центрированного изображения
        double adjustedX = viewX - offsetX;
        double adjustedY = viewY - offsetY;

        // Масштабируем к оригинальному размеру изображения
        int screenX = (int)(adjustedX * scaleX);
        int screenY = (int)(adjustedY * scaleY);

        // Ограничиваем координаты
        screenX = Math.max(0, screenX);
        screenY = Math.max(0, screenY);

        lastX = screenX;
        lastY = screenY;

        return new Command.Point(screenX, screenY);
    }

    /**
     * Преобразование координат из MouseEvent
     */
    private Command.Point convertCoordinates(MouseEvent event) {
        return convertCoordinates(event.getX(), event.getY());
    }

    /**
     * Преобразование координат из ScrollEvent
     */
    private Command.Point convertCoordinates(ScrollEvent event) {
        return convertCoordinates(event.getX(), event.getY());
    }

    /**
     * Обработка нажатия кнопки мыши
     */
    private void handleMousePressed(MouseEvent event) {
        if (!connectionManager.isConnected()) return;

        mousePressed = true;
        Command.Point point = convertCoordinates(event);

        int button = getMouseButtonCode(event.getButton());
        Command command = new Command(Command.Type.MOUSE_PRESS,
                new Object[]{point.x, point.y, button});

        connectionManager.sendCommand(command);
        event.consume();
    }

    /**
     * Обработка отпускания кнопки мыши
     */
    private void handleMouseReleased(MouseEvent event) {
        if (!connectionManager.isConnected()) return;

        mousePressed = false;
        Command.Point point = convertCoordinates(event);

        int button = getMouseButtonCode(event.getButton());
        Command command = new Command(Command.Type.MOUSE_RELEASE,
                new Object[]{point.x, point.y, button});

        connectionManager.sendCommand(command);
        event.consume();
    }

    /**
     * Обработка движения мыши
     */
    private void handleMouseMoved(MouseEvent event) {
        if (!connectionManager.isConnected()) return;

        Command.Point point = convertCoordinates(event);
        Command command = Command.mouseMove(point.x, point.y);
        connectionManager.sendCommand(command);
        event.consume();
    }

    /**
     * Обработка перетаскивания мыши
     */
    private void handleMouseDragged(MouseEvent event) {
        if (!connectionManager.isConnected()) return;

        Command.Point point = convertCoordinates(event);

        if (mousePressed) {
            Command command = new Command(Command.Type.MOUSE_DRAG,
                    new Object[]{point.x, point.y, lastX, lastY});
            connectionManager.sendCommand(command);
        } else {
            Command command = Command.mouseMove(point.x, point.y);
            connectionManager.sendCommand(command);
        }

        event.consume();
    }

    /**
     * Обработка клика мыши
     */
    private void handleMouseClicked(MouseEvent event) {
        if (!connectionManager.isConnected()) return;

        // Для одинарных кликов - просто отправляем координаты
        Command.Point point = convertCoordinates(event);

        if (event.getClickCount() == 1) {
            // Одиночный клик уже обработан в pressed/released
        } else if (event.getClickCount() == 2) {
            // Двойной клик
            int button = getMouseButtonCode(event.getButton());
            Command command = new Command(Command.Type.MOUSE_CLICK,
                    new Object[]{point.x, point.y, button, 2}); // 2 = двойной клик
            connectionManager.sendCommand(command);
        }

        event.consume();
    }

    /**
     * Обработка выхода мыши за пределы изображения
     */
    private void handleMouseExited(MouseEvent event) {
        // Можно отправить команду для скрытия курсора на удаленной стороне
        mousePressed = false;
    }

    /**
     * Обработка прокрутки колесика мыши
     */
    private void handleMouseScroll(ScrollEvent event) {  // Изменяем параметр на ScrollEvent
        if (!connectionManager.isConnected()) return;

        Command.Point point = convertCoordinates(event);
        double deltaY = event.getDeltaY();  // Используем getDeltaY() вместо getY()
        int scrollAmount = (int) deltaY;

        Command command = new Command(Command.Type.MOUSE_WHEEL,
                new Object[]{point.x, point.y, scrollAmount});

        connectionManager.sendCommand(command);
        event.consume();
    }

    /**
     * Преобразование кнопки мыши JavaFX в код AWT
     */
    private int getMouseButtonCode(MouseButton button) {
        switch (button) {
            case PRIMARY:
                return InputEvent.BUTTON1_DOWN_MASK;
            case SECONDARY:
                return InputEvent.BUTTON3_DOWN_MASK;
            case MIDDLE:
                return InputEvent.BUTTON2_DOWN_MASK;
            default:
                return InputEvent.BUTTON1_DOWN_MASK;
        }
    }

    /**
     * Эмуляция клика по заданным координатам
     */
    public void emulateClick(int x, int y, int button) {
        if (!connectionManager.isConnected()) return;

        Command command = Command.mouseClick(x, y, button);
        connectionManager.sendCommand(command);
    }

    /**
     * Эмуляция движения мыши
     */
    public void emulateMove(int x, int y) {
        if (!connectionManager.isConnected()) return;

        Command command = Command.mouseMove(x, y);
        connectionManager.sendCommand(command);
    }

    /**
     * Сброс состояния мыши
     */
    public void reset() {
        mousePressed = false;
        lastX = 0;
        lastY = 0;
    }
}
