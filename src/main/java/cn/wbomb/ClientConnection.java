package cn.wbomb;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author George
 */
public class ClientConnection extends Thread {
    private final Socket socket;
    private final int clientId;
    private String clientName;
    private final Server server;

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public ClientConnection(Socket socket, Server server, int clientId) {
        this.clientId = clientId;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (isNotOnlineYet()) {
                    clientName = line;
                    server.registerClient(this);
                } else {
                    Message message = JSON.parseObject(line, Message.class);
                    server.sendMessage(this, message);
                }
            }
            System.out.println(line+"123");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.clientOffline(this);
        }
    }

    private boolean isNotOnlineYet() {
        return clientName == null;
    }

    public void sendMessage(String message) throws IOException {
        Util.writeMessage(socket, message);
    }
}
