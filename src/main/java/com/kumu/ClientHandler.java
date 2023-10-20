package com.kumu;

import java.io.*;
import java.net.Socket;
/**
 * 客户端处理器，就是服务器提供的服务线程
 * 封装了从服务器到客户端的Socket套接字
 * 客户端通过这个Handler与服务器通信，上传文件等
 */
public class ClientHandler implements Runnable {
    /**
     * 服务器连到客户端的Socket
     */
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatServer chatServer;
    private String username;


    /*
     * 构造方法，接收一个socket对象
     */
    public ClientHandler(Socket socket, ChatServer chatServer) { //接收从服务器到客户端的Socket
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
                if (clientMessage.startsWith(SystemConst.SEND_FILE_START)) {
                    uploadFileToServer();
                } else if(clientMessage.startsWith(SystemConst.SEND_MESSAGE_PRIVATE)){

                    clientMessage = clientMessage.substring(SystemConst.SEND_MESSAGE_PRIVATE.length());

                    String receiverUserName = clientMessage.substring(0,clientMessage.indexOf(' '));
                    clientMessage = clientMessage.substring(receiverUserName.length()+1);
                    chatServer.sendMessagePrivate(SystemConst.PRIVATE_PREFIX + username + ": " + clientMessage, this,receiverUserName);
                } else{
                    chatServer.broadcastMessage(username + ": " + clientMessage, this);
                }

                if (clientMessage.equals(SystemConst.END_SIGH)) {
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

    /**
     * 传文件到服务器，用一个新的Socket去连接服务器
     * */
    public void uploadFileToServer() throws IOException {
        Socket socketToFile = new Socket("localhost", 8800);
        // 1.创建本地文件输入流
        FileInputStream fIS = new FileInputStream("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\test.txt");

        // 3.获取网络字节输出流
        OutputStream opStream = socketToFile.getOutputStream();
        // 4.读取要上传的文件数据
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = fIS.read(bytes)) != -1) {
            // 5.使用网络字节输出流将读取到的文件数据发送到服务端的Socket
            opStream.write(bytes, 0, i);
        }
        // 禁用此套接字的输出流，此时会写入一个终止标记，这样服务端就可以读取到此标记，就不会出现阻塞的问题了
        // 终止标记表示输出流写出的数据已经没有了，服务端解析到这个标记后就，有关的线程就不会一直处于等待接收
        // 数据的状态
        socketToFile.shutdownOutput();
        // 6.使用 Socket 对象的方法 getInputStream 获取网络字节输入流对象
        InputStream is = socketToFile.getInputStream();
        // 7.读取服务端回写的数据
        i = is.read(bytes);
        // 打印到控制台
        System.out.println(new String(bytes, 0, i));
        // 8.释放资源（FileInputStream、Socket）
        fIS.close();
        socketToFile.close();
    }

    /**
     * 从服务器下载文件，用新的Socket访问服务器的下载线程
     * */
    public void downloadFileFromServer() throws IOException {
        Socket socketToFile = new Socket("localhost", 8800);
        // 1.创建本地文件输入流
        FileInputStream fIS = new FileInputStream("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\test.txt");

        // 3.获取网络字节输出流
        OutputStream opStream = socketToFile.getOutputStream();
        // 4.读取要上传的文件数据
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = fIS.read(bytes)) != -1) {
            // 5.使用网络字节输出流将读取到的文件数据发送到服务端的Socket
            opStream.write(bytes, 0, i);
        }
        // 禁用此套接字的输出流，此时会写入一个终止标记，这样服务端就可以读取到此标记，就不会出现阻塞的问题了
        // 终止标记表示输出流写出的数据已经没有了，服务端解析到这个标记后就，有关的线程就不会一直处于等待接收
        // 数据的状态
        socketToFile.shutdownOutput();
        // 6.使用 Socket 对象的方法 getInputStream 获取网络字节输入流对象
        InputStream is = socketToFile.getInputStream();
        // 7.读取服务端回写的数据
        i = is.read(bytes);
        // 打印到控制台
        System.out.println(new String(bytes, 0, i));
        // 8.释放资源（FileInputStream、Socket）
        fIS.close();
        socketToFile.close();
    }

    /**
     *
     * @param senderUsername
     * 接受文件的方法
     */
    private void receiveFile(String senderUsername) {
        try {
            InputStream inputStream = clientSocket.getInputStream();

            // 创建一个临时文件来保存接收的文件数据
            File receivedFile = new File("received_file.txt");
            FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();
            System.out.println("Received file saved as received_file.txt");

            writer.println(SystemConst.SEND_FILE_END);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

