package ru.otus.java.basic.oop.remoteassistantlocal.agent;

import ru.otus.java.basic.oop.remoteassistantlocal.common.Protocol;

import java.awt.*;
import java.io.IOException;

// AgentApp.java - Запуск агента
public class AgentApp {
    public static void main(String[] args) {
        try {
            System.out.println("=== Удаленный помощник - Агент ===");
            System.out.println("Для выхода нажмите Ctrl+C");

            DesktopAgent agent = new DesktopAgent();
            agent.start(Protocol.PORT);

        } catch (AWTException e) {
            System.err.println("Ошибка: Не удалось создать Robot (нет прав?)");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера:");
            e.printStackTrace();
        }
    }



}
