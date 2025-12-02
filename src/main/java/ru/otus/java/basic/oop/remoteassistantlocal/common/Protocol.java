package ru.otus.java.basic.oop.remoteassistantlocal.common;

public class Protocol {
    public static final int PORT = 5555;

    public static final String CMD_SCREENSHOT = "SCREENSHOT";
    public static final String CMD_MOUSE_MOVE = "MOUSE_MOVE";
    public static final String CMD_MOUSE_CLICK = "MOUSE_CLICK";
    public static final String CMD_KEY_PRESS = "KEY_PRESS";
    public static final String CMD_DISCONNECT = "DISCONNECT";

    public static String buildCommand(String command, String... params) {
        return command + ":" + String.join(",", params);
    }
}
