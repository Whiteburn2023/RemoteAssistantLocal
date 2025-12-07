package ru.otus.java.basic.oop.remoteassistantlocal.agent;
// DesktopAgent.java
import ru.otus.java.basic.oop.remoteassistantlocal.common.ImageUtils;
import ru.otus.java.basic.oop.remoteassistantlocal.common.Protocol;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DesktopAgent {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Robot robot;
    private boolean running = false;
    private int quality = 70; // качество JPEG в процентах

    public DesktopAgent() throws AWTException {
        this.robot = new Robot();
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Агент запущен на порту " + port);
        System.out.println("IP адрес: " + getLocalIP());
        System.out.println("Ожидание подключения помощника...");

        running = true;

        // Основной цикл ожидания подключений
        while (running) {
            clientSocket = serverSocket.accept();
            System.out.println("Подключился помощник: " +
                    clientSocket.getInetAddress().getHostAddress());

            handleClient(clientSocket);

            clientSocket.close();
            System.out.println("Соединение закрыто, ожидание нового подключения...");
        }
    }

    private void handleClient(Socket socket) {
        try (
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            // ФИКСИРОВАННЫЙ размер 1920x1080
            final int FIXED_WIDTH = 1920;
            final int FIXED_HEIGHT = 1080;

            System.out.println("Работа в фиксированном разрешении: " + FIXED_WIDTH + "x" + FIXED_HEIGHT);

            Thread screenshotThread = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        // Создаем скриншот
                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage screenshot = robot.createScreenCapture(screenRect);

                        BufferedImage scaled = new BufferedImage(FIXED_WIDTH, FIXED_HEIGHT, BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = scaled.createGraphics();
                        g2d.drawImage(screenshot, 0, 0, FIXED_WIDTH, FIXED_HEIGHT, null);
                        g2d.dispose();

                        // Конвертируем в байты
                        byte[] imageData = ImageUtils.imageToBytes(scaled, quality / 100f);

                        // Отправляем размер и данные
                        out.writeInt(imageData.length);
                        out.write(imageData);
                        out.flush();

                        Thread.sleep(100); // 10 FPS
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка отправки скриншота: " + e.getMessage());
                }
            });
            screenshotThread.start();

            // Поток обработки команд
            while (!socket.isClosed()) {
                String command = in.readUTF();
                processCommand(command);
            }

            screenshotThread.interrupt();

        } catch (IOException e) {
            System.out.println("Клиент отключился: " + e.getMessage());
        }
    }

    private void processCommand(String command) {
        try {
            String[] parts = command.split(":");
            String cmd = parts[0];
            String[] params = parts.length > 1 ? parts[1].split(",") : new String[0];

            switch (cmd) {
                case Protocol.CMD_MOUSE_MOVE:
                    int x = Integer.parseInt(params[0]);
                    int y = Integer.parseInt(params[1]);
                    robot.mouseMove(x, y);
                    break;

                case Protocol.CMD_MOUSE_CLICK:
                    int btn = Integer.parseInt(params[0]);
                    robot.mousePress(btn);
                    robot.mouseRelease(btn);
                    break;

                case Protocol.CMD_KEY_PRESS:
                    int keyCode = Integer.parseInt(params[0]);
                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);
                    break;

                case "SET_QUALITY":
                    quality = Integer.parseInt(params[0]);
                    System.out.println("Качество установлено: " + quality + "%");
                    break;

                case Protocol.CMD_DISCONNECT:
                    System.out.println("Получена команда отключения");
                    running = false;
                    break;
            }
        } catch (Exception e) {
            System.out.println("Ошибка обработки команды: " + command);
            e.printStackTrace();
        }
    }

    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "неизвестно";
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
