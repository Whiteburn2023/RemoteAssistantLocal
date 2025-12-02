package ru.otus.java.basic.oop.remoteassistantlocal.viewer;

import com.remoteassistant.common.Command;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Менеджер соединения - отвечает за обмен данными с агентом
 */
public class ConnectionManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private Thread receiverThread;
    private Thread senderThread;
    private BlockingQueue<Command> sendQueue = new LinkedBlockingQueue<>();

    private ConnectionListener listener;

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onCommandReceived(Command command);
        void onError(String message);
    }

    public ConnectionManager(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Подключение к агенту
     */
    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                socket.setTcpNoDelay(true); // Отключаем задержку Nagle

                // Создаем потоки в правильном порядке!
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush(); // Важно для ObjectInputStream
                in = new ObjectInputStream(socket.getInputStream());

                connected.set(true);

                // Запускаем потоки приема и отправки
                startReceiver();
                startSender();

                Platform.runLater(() -> listener.onConnected());

            } catch (Exception e) {
                connected.set(false);
                Platform.runLater(() ->
                        listener.onError("Ошибка подключения: " + e.getMessage())
                );
            }
        }).start();
    }

    /**
     * Запуск потока приема команд
     */
    private void startReceiver() {
        receiverThread = new Thread(() -> {
            try {
                while (connected.get() && !socket.isClosed()) {
                    Command command = (Command) in.readObject();

                    if (command.getType() == Command.Type.DISCONNECT) {
                        disconnect();
                        break;
                    }

                    Platform.runLater(() ->
                            listener.onCommandReceived(command)
                    );
                }
            } catch (EOFException e) {
                // Нормальное завершение соединения
            } catch (Exception e) {
                if (connected.get()) {
                    Platform.runLater(() ->
                            listener.onError("Ошибка приема: " + e.getMessage())
                    );
                }
            } finally {
                disconnect();
            }
        }, "Receiver-Thread");
        receiverThread.start();
    }

    /**
     * Запуск потока отправки команд
     */
    private void startSender() {
        senderThread = new Thread(() -> {
            try {
                while (connected.get() && !socket.isClosed()) {
                    Command command = sendQueue.take(); // Блокируется пока нет команд
                    out.writeObject(command);
                    out.flush();
                }
            } catch (Exception e) {
                if (connected.get()) {
                    Platform.runLater(() ->
                            listener.onError("Ошибка отправки: " + e.getMessage())
                    );
                }
            }
        }, "Sender-Thread");
        senderThread.setDaemon(true);
        senderThread.start();
    }

    /**
     * Отправка команды агенту
     */
    public void sendCommand(Command command) {
        if (connected.get()) {
            try {
                sendQueue.put(command);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Отправка команды немедленно (без очереди)
     */
    public void sendCommandImmediately(Command command) {
        if (connected.get()) {
            try {
                out.writeObject(command);
                out.flush();
            } catch (IOException e) {
                Platform.runLater(() ->
                        listener.onError("Ошибка отправки: " + e.getMessage())
                );
                disconnect();
            }
        }
    }

    /**
     * Проверка соединения
     */
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    /**
     * Получение задержки соединения (ping)
     */
    public long getPing() {
        if (!isConnected()) return -1;

        try {
            long start = System.currentTimeMillis();
            sendCommandImmediately(new Command(Command.Type.SCREENSHOT_REQUEST));
            // Здесь нужно было бы ждать ответа, упрощенная версия
            return System.currentTimeMillis() - start;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Отключение от агента
     */
    public void disconnect() {
        if (connected.compareAndSet(true, false)) {
            try {
                // Отправляем команду отключения
                if (out != null) {
                    sendCommandImmediately(Command.disconnect());
                }

                // Закрываем потоки
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();

                // Прерываем потоки
                if (receiverThread != null) receiverThread.interrupt();
                if (senderThread != null) senderThread.interrupt();

                // Очищаем очередь
                sendQueue.clear();

                Platform.runLater(() -> listener.onDisconnected());

            } catch (Exception e) {
                Platform.runLater(() ->
                        listener.onError("Ошибка отключения: " + e.getMessage())
                );
            }
        }
    }

    /**
     * Получение статистики соединения
     */
    public ConnectionStats getStats() {
        return new ConnectionStats(
                isConnected(),
                sendQueue.size(),
                getPing()
        );
    }

    /**
     * Статистика соединения
     */
    public static class ConnectionStats {
        public final boolean connected;
        public final int queueSize;
        public final long ping;

        public ConnectionStats(boolean connected, int queueSize, long ping) {
            this.connected = connected;
            this.queueSize = queueSize;
            this.ping = ping;
        }
    }
}
