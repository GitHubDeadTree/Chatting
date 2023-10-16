package com.kumu;

import java.io.*;
import java.net.Socket;
/*
 * 客户端处理器，就是服务器提供的服务线程
 * 封装了从服务器到客户端的Socket套接字
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatServer chatServer;
    private String username;

    /*
     * 构造方法，接收一个socket对象
     */
    public ClientHandler(Socket socket, ChatServer chatServer) { //接收从服务器到客户端的套接字
        this.clientSocket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            this.chatServer = chatServer;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            username = reader.readLine();
            System.out.println(username + " connected.");

            chatServer.broadcastMessage(username + " joined the chat.", this);

            String clientMessage;
            /*
             * 如果客户端不输入，就会一直等待
             */
            while ((clientMessage = reader.readLine()) != null) {
                /*
                 * 如果用户要传文件的话，就一定是私发(不可能广播文件)
                 */
                if (clientMessage.startsWith(SystemConst.Send_File)) {
                    String[] parts = clientMessage.split(" ", 3);
                    String receiverUsername = parts[1];
                    chatServer.sendFile(parts[2].getBytes(), this, receiverUsername);
                } else if(clientMessage.startsWith(SystemConst.Send_Message_Private)){

                    clientMessage = clientMessage.substring(SystemConst.Send_Message_Private.length());

                    String receiverUserName = clientMessage.substring(0,clientMessage.indexOf(' '));
                    clientMessage = clientMessage.substring(receiverUserName.length()+1);
                    chatServer.sendMessagePrivate("(悄悄话)" + username + ": " + clientMessage, this,receiverUserName);
                } else{
                    chatServer.broadcastMessage(username + ": " + clientMessage, this);
                }

                if (clientMessage.equals("bye")) {
                    break;
                }
            }

            chatServer.removeClient(this);
            clientSocket.close();
            System.out.println(username + " disconnected.");
            chatServer.broadcastMessage(username + " left the chat.", this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }

    public void sendFile(byte[] fileBytes, String senderUsername) {
        // Implement file sending logic to the client
    }
}

