package cn.wbomb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author George
 */
public class Server {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private final ServerSocket server;
    private final Map<Integer, ClientConnection> clients = new ConcurrentHashMap<>();

    /**
     * 创建socket
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port);
    }

    public void start() throws IOException {
        while (true) {
            //accept()没有链接会阻塞
            Socket socket = server.accept();
            new ClientConnection(socket, this, COUNTER.incrementAndGet()).start();
        }
    }

    public void registerClient(ClientConnection clientConnection) {
        clients.put(clientConnection.getClientId(), clientConnection);
        clientOnline(clientConnection);
    }

    private void clientOnline(ClientConnection clientWhoHasJustLoggedIn) {
        clients.values().forEach(client -> dispatchMessage(clientWhoHasJustLoggedIn, "系统", "所有人", clientWhoHasJustLoggedIn.getClientName() + "上线了。" + getAllClientInfo()));
    }

    public static void main(String[] args) throws IOException {
        new Server(8080).start();
    }

    public void sendMessage(ClientConnection src, Message message) {
        if (message.getId() == 0) {
            clients.values().forEach(client -> dispatchMessage(src, src.getClientName(), "所有人", message.getMessage()));
        } else {
            int targetUser = message.getId();
            ClientConnection target = clients.get(targetUser);
            if (target == null) {
                System.err.println("用户" + targetUser + "不存在");
            } else {
                dispatchMessage(src, src.getClientName(), "你", message.getMessage());
            }
        }
    }

    public void clientOffline(ClientConnection clientConnection) {
        clients.remove(clientConnection.getClientId());
        clients.values().forEach(client -> dispatchMessage(client, "系统", "所有人", client.getClientName() + "下线了。" + getAllClientInfo()));
    }

    private void dispatchMessage(ClientConnection client, String str, String target, String message) {
        try {
            client.sendMessage(str + "对" + target + "说：" + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAllClientInfo() {
        return clients.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue().getClientName())
                .collect(Collectors.joining(","));
    }
}
