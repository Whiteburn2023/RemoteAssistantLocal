package ru.otus.java.basic.oop.remoteassistantlocal;

import ru.otus.java.basic.oop.remoteassistantlocal.agent.AgentApp;
import ru.otus.java.basic.oop.remoteassistantlocal.viewer.ViewerApp;
import javafx.application.Application;

/**
 * Главный класс приложения - точка входа
 */
public class Main {

    /**
     * Основной метод запуска приложения
     */
    public static void main(String[] args) {
        System.out.println("=== Remote Assistant ===");
        System.out.println("Доступные режимы:");
        System.out.println("1. viewer   - запуск интерфейса помощника (JavaFX)");
        System.out.println("2. agent    - запуск агента на компьютере пользователя");
        System.out.println("3. server   - запуск сервера-посредника (для интернета)");
        System.out.println();
        System.out.println("Примеры использования:");
        System.out.println("  java -jar RemoteAssistant.jar viewer");
        System.out.println("  java -jar RemoteAssistant.jar agent");
        System.out.println();

        if (args.length == 0) {
            // Запуск по умолчанию - режим помощника
            System.out.println("Запуск в режиме помощника (viewer)...");
            launchViewer();
        } else {
            switch (args[0].toLowerCase()) {
                case "viewer":
                case "v":
                    launchViewer();
                    break;

                case "agent":
                case "a":
                    launchAgent();
                    break;

                case "server":
                case "s":
                    launchServer();
                    break;

                case "help":
                case "h":
                case "-help":
                case "--help":
                    printHelp();
                    break;

                default:
                    System.err.println("Неизвестный режим: " + args[0]);
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Запуск приложения помощника (JavaFX)
     */
    private static void launchViewer() {
        System.out.println("Запуск JavaFX приложения...");

        try {
            // Проверяем доступность JavaFX
            Class.forName("javafx.application.Application");

            // Запускаем JavaFX приложение
            Application.launch(ViewerApp.class);

        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка: JavaFX не найден!");
            System.err.println("Убедитесь, что используете JDK с JavaFX или");
            System.err.println("добавьте модули JavaFX в classpath.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Ошибка запуска JavaFX приложения:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Запуск агента
     */
    private static void launchAgent() {
        System.out.println("Запуск агента...");
        AgentApp.main(new String[0]);
    }

    /**
     * Запуск сервера (опционально)
     */
    private static void launchServer() {
        System.out.println("Запуск сервера-посредника...");
        // ServerApp.main(new String[0]);
        System.out.println("Режим сервера пока не реализован");
    }

    /**
     * Вывод справки
     */
    private static void printHelp() {
        System.out.println();
        System.out.println("Remote Assistant - система удаленного доступа");
        System.out.println();
        System.out.println("Использование:");
        System.out.println("  java -jar RemoteAssistant.jar [режим]");
        System.out.println();
        System.out.println("Режимы:");
        System.out.println("  viewer    - интерфейс помощника (по умолчанию)");
        System.out.println("  agent     - агент на компьютере пользователя");
        System.out.println("  server    - сервер-посредник для работы через интернет");
        System.out.println("  help      - показать эту справку");
        System.out.println();
        System.out.println("Пример для локальной сети:");
        System.out.println("  1. На компьютере пользователя:");
        System.out.println("     java -jar RemoteAssistant.jar agent");
        System.out.println("  2. На компьютере помощника:");
        System.out.println("     java -jar RemoteAssistant.jar viewer");
        System.out.println("     Введите IP адрес агента и нажмите 'Подключиться'");
        System.out.println();
        System.out.println("Автор: Курсовой проект по Java");
        System.out.println("Версия: 1.0");
    }

    /**
     * Получение версии приложения
     */
    public static String getVersion() {
        return "1.0.0";
    }

    /**
     * Получение информации о системе
     */
    public static void printSystemInfo() {
        System.out.println("Системная информация:");
        System.out.println("  Java версия: " + System.getProperty("java.version"));
        System.out.println("  ОС: " + System.getProperty("os.name") + " " +
                System.getProperty("os.version"));
        System.out.println("  Архитектура: " + System.getProperty("os.arch"));
        System.out.println("  Пользователь: " + System.getProperty("user.name"));
        System.out.println("  Директория: " + System.getProperty("user.dir"));
    }
}
