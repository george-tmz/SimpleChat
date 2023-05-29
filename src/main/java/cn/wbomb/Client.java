package cn.wbomb;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("输入你的呢称");
        Scanner userInput = new Scanner(System.in);
        String name = userInput.nextLine();

        Socket socket = new Socket("127.0.0.1", 8080);
        Util.writeMessage(socket, name);

        System.out.println("链接成功！");

        new Thread(() -> readFromServer(socket)).start();

        while (true) {
            System.out.println("输入你要发送的聊天消息");
            System.out.println("id:message，例如，1:hello");
            System.out.println("id=0代表向所有人发送消息");

            String line = userInput.nextLine();

            if (!line.contains(":")) {
                System.err.println("输入的格式不对！");
            } else {
                int colonIndex = line.indexOf(':');
                int id = Integer.parseInt(line.substring(0, colonIndex));
                String message = line.substring(colonIndex+1);

                Message messageObj = new Message();
                messageObj.setId(id);
                messageObj.setMessage(message);
                String json = JSON.toJSONString(messageObj);
                Util.writeMessage(socket, json);

            }
        }
    }

    private static void readFromServer(Socket socket) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
