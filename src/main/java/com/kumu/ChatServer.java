package com.kumu;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务端的代码
 */
public class ChatServer {
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        /*
        * try-with-resources 结构，可以自动关闭实现了 AutoCloseable 或 Closeable 接口的资源
        * 创建ServerSocket对象时，可能会抛出IOException异常，因为端口号可能已经被占用或者无效。
         */
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("Server is running on port 8888...");

            //无限循环，用于持续接收新客户端的连接
            while (true) {
                /**
                 * serverSocket.accept() 是一个阻塞方法，它用于等待客户端的连接。只有新客户端接入，才会执行下面的代码
                 */
                //为每一个客户端创建一个新的socket对象
                Socket clientSocket = serverSocket.accept();
                //新建客户处理器，处理这个客户的请求，在新线程中工作
                ClientHandler clientHandler = new ClientHandler(clientSocket,this);
                clientHandlerList.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.sendMessage(message);
            }
    }
    public void sendMessagePrivate(String message,String receiverUsername) {
        for (ClientHandler client : clientHandlerList) {
            if (client.getUserName().equals(receiverUsername)) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clientHandlerList.remove(client);
    }

    public void upLoadToServer(String fileName,String userName){
        if (Utils.isPortAvailable(SystemConst.THREAD_UPLOAD_PORT)) {
            transThread_upLoadToServer threadFile = new transThread_upLoadToServer(fileName, this, userName);
            new Thread(threadFile).start();
        }
    }

    public void downloadFromServer(String fileName) {
        transThread_downloadToClient threadFile = new transThread_downloadToClient(fileName);
        new Thread(threadFile).start();
    }
}
