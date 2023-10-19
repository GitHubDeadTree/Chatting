package com.kumu;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
/**
 * 客户端的代码
 */
public class ChatClient {
    public static void main(String[] args) {
        new ChatClient().startClient();
    }

    public void startClient() {
        try (Socket socket = new Socket("localhost", 8888);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            writer.println(username);
            /**
                * 启动一个新线程，用于持续接收服务器发来的消息。
                * 这个线程中通过 reader.readLine() 从输入流中读取服务器的消息，并将其打印到控制台。
             */
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String message = scanner.nextLine();
                writer.println(message);

                if (message.equals("bye")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
