package ru.otus.java.basic.oop.remoteassistantlocal.common;

import java.awt.event.InputEvent;
import java.io.Serializable;

/**
 * Класс для передачи команд между клиентом и сервером
 */
public class Command implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        // Команды мыши
        MOUSE_MOVE,
        MOUSE_PRESS,
        MOUSE_RELEASE,
        MOUSE_CLICK,
        MOUSE_DRAG,
        MOUSE_WHEEL,

        // Команды клавиатуры
        KEY_PRESS,
        KEY_RELEASE,
        KEY_TYPED,

        // Команды системы
        SCREENSHOT_REQUEST,
        SCREENSHOT_RESPONSE,
        SYSTEM_INFO,
        FILE_TRANSFER,
        DISCONNECT,

        // Команды управления
        SET_QUALITY,
        SET_FPS,
        START_STREAM,
        STOP_STREAM,

        // Команды чата
        CHAT_MESSAGE,

        // Команды авторизации
        AUTH_REQUEST,
        AUTH_RESPONSE
    }

    private Type type;
    private Object data;
    private long timestamp;

    public Command(Type type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Command(Type type) {
        this(type, null);
    }

    // Геттеры и сеттеры
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Вспомогательные методы для создания команд
    public static Command mouseMove(int x, int y) {
        return new Command(Type.MOUSE_MOVE, new Point(x, y));
    }

    public static Command mouseClick(int x, int y, int button) {
        Object[] data = {x, y, button};
        return new Command(Type.MOUSE_CLICK, data);
    }

    public static Command keyPress(int keyCode) {
        return new Command(Type.KEY_PRESS, keyCode);
    }

    public static Command setQuality(int quality) {
        return new Command(Type.SET_QUALITY, quality);
    }

    public static Command disconnect() {
        return new Command(Type.DISCONNECT);
    }

    public static Command screenshotRequest() {
        return new Command(Type.SCREENSHOT_REQUEST);
    }

    // Вложенный класс для передачи координат
    public static class Point implements Serializable {
        private static final long serialVersionUID = 1L;
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Вложенный класс для передачи информации о файле
    public static class FileInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public long size;
        public boolean isDirectory;
        public String path;

        public FileInfo(String name, long size, boolean isDirectory, String path) {
            this.name = name;
            this.size = size;
            this.isDirectory = isDirectory;
            this.path = path;
        }
    }

    // Вложенный класс для передачи сообщения чата
    public static class ChatMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        public String sender;
        public String message;
        public long timestamp;

        public ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
