package wp.anthony;

import java.io.Console;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Connection connection : connectionMap.values()) {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не смогли отправить сообщение " + connection.getRemoteSocketAddress());
            }
        }
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера: ");
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен!");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Ошибка при запуске или работе сервера!");
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение " + socket.getRemoteSocketAddress());
            String userName = null;
            try(Connection connection = new Connection(socket)){
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данных с " + socket.getRemoteSocketAddress());
            }

            if (userName != null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто");
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            Message message;
            while (true) {
                message = connection.receive();
                if (MessageType.TEXT.equals(message.getType())) {
                    Server.sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + message.getData()));
                }else {
                    ConsoleHelper.writeMessage("Ошибка при отправке сообщения");
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST, "Введите имя: "));
                message = connection.receive();
                String username = message.getData();
                if (message.getType() == MessageType.USER_NAME && !message.getData().isEmpty() && !username.equals("") && !connectionMap.containsKey(username)) {
                    connectionMap.put(username, connection);
                    break;
                }
            }
            connection.send(new Message(MessageType.NAME_ACCEPTED, "Вы добавленны в чат. Добро пожаловать!"));
            return message.getData();
        }

        private void notifyUsers(Connection connection, String username) throws IOException{
            for (String name : connectionMap.keySet()){
                if (name.equals(username)) continue;
                connection.send(new Message(MessageType.USER_ADDED, name));
            }
        }
    }
}
