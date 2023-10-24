package com.kumu;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;

/**
 * 客户端处理器，就是服务器提供的服务线程
 * 封装了从服务器到客户端的Socket套接字
 * 客户端通过这个Handler与服务器通信，上传文件等
 */
public class ClientService implements Runnable {
    /**
     * 服务器连到客户端的Socket
     */
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ChatServer chatServer;
    private String userName;

    private String _filePath;

    public ClientService(Socket socket, ChatServer chatServer) { //接收从服务器到客户端的Socket
        this.clientSocket = socket;
        _filePath = null;
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
            userName = reader.readLine();
            System.out.println(userName +clientSocket+" connected.");
            chatServer.broadcastMessage(userName + " joined the chat.", userName);

            String clientMessage;
            /**
             * reader.readLine() 方法会一直等待，直到从输入流中读取到一个完整的文本行，或者直到发生异常。如果没有新的消息或文本行可用，它会一直阻塞。
             */
            while (true) {
                clientMessage = reader.readLine();
                System.out.println(userName +" 发送 " + clientMessage);
                if (Objects.isNull(clientMessage)) {
                    continue;
                }

                if (clientMessage.startsWith(SystemConst.PRE_UPLOAD_FILE)) {
                    String[] item = clientMessage.split("-");
                    if (item.length==1) uploadFileToServer("test.txt");
                    else{
                        uploadFileToClient(item[1],item[2]);
                    }
                }else if(clientMessage.startsWith(SystemConst.PRE_DOWNLOAD_FILE)){
                    downloadFileFromServer("test_Server.txt");
                }else if(clientMessage.startsWith(SystemConst.PRE_SETTING_PATH)){
                     _filePath = clientMessage.substring(clientMessage.indexOf(' ')+1);
                }
                else if(clientMessage.startsWith(SystemConst.PRE_SEND_MESSAGE_PRIVATE)){

                    clientMessage = clientMessage.substring(SystemConst.PRE_SEND_MESSAGE_PRIVATE.length());

                    String receiverUserName = clientMessage.substring(0,clientMessage.indexOf(' '));
                    clientMessage = clientMessage.substring(receiverUserName.length()+1);
                    chatServer.sendMessagePrivate(SystemConst.PRIVATE_PREFIX + userName + ": " + clientMessage, receiverUserName);
                    chatServer.sendMessagePrivate("(You) "+SystemConst.PRIVATE_PREFIX + ": "+clientMessage,userName);
                } else{
                    chatServer.broadcastMessage(userName + ": " + clientMessage,userName);
                }

                if (clientMessage.equals(SystemConst.END_SIGH)) {
                    break;
                }
            }

            chatServer.removeClient(this);
            clientSocket.close();
            System.out.println(userName + " disconnected.");
            chatServer.broadcastMessage(userName + " left the chat.", userName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUserName() {
        return userName;
    }

    /**
     * 传文件到服务器，用一个新的Socket去连接服务器
     * */
    public void uploadFileToServer(String fileName) throws IOException {
        chatServer.upLoadToServer(fileName, userName);
        Socket socketTrans = new Socket("localhost", SystemConst.THREAD_UPLOAD_PORT);
        // 创建本地文件输入流
        FileInputStream fIS = new FileInputStream("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\clientFile\\"+fileName);
        // 获取网络字节输出流
        OutputStream opStream = socketTrans.getOutputStream();
        // 读取要上传的文件数据
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = fIS.read(bytes)) != -1) {
            // 使用输出流将文件数据发送到服务端的Socket
            opStream.write(bytes, 0, i);
        }
        // 禁用此套接字的输出流，此时会写入一个终止标记，这样服务端就可以读取到此标记，就不会出现阻塞的问题了
        socketTrans.shutdownOutput();

        fIS.close();
        socketTrans.close();
    }
    /**
     * A用户把文件传给B用户
     * 上传到服务器 -> 服务器分发
     * */
    public void uploadFileToClient(String filePath,String receiverName) throws IOException {
        int lastBackslashIndex = filePath.lastIndexOf('\\'); // 查找最后一个反斜杠的位置
        String _fileName = filePath.substring(lastBackslashIndex + 1); // 提取最后一个反斜杠之后的子字符串
        //随机化文件名
        String fileName = System.currentTimeMillis() + (new Random().nextInt(9) + 1) + _fileName;
        if (lastBackslashIndex >= 0) {
            System.out.println("File Name: " + fileName);
        } else {
            System.out.println("Invalid file path");
        }
        //服务器上传
        chatServer.upLoadToServer(fileName, userName);

        Socket socketTrans = new Socket("localhost", SystemConst.THREAD_UPLOAD_PORT);
        // 创建本地文件输入流
        FileInputStream fIS = new FileInputStream(filePath);
        // 获取网络字节输出流
        OutputStream opStream = socketTrans.getOutputStream();
        // 读取要上传的文件数据
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = fIS.read(bytes)) != -1) {
            // 使用输出流将文件数据发送到服务端的Socket
            opStream.write(bytes, 0, i);
        }
        // 禁用此套接字的输出流，此时会写入一个终止标记，这样服务端就可以读取到此标记，就不会出现阻塞的问题了
        socketTrans.shutdownOutput();
        fIS.close();
        socketTrans.close();
        //服务器分发
        chatServer.postToClient(fileName,receiverName);
    }

    /**
     * 从服务器下载文件，用新的Socket访问服务器的下载线程
     * */
    public void downloadFileFromServer(String _fileName) throws IOException {
        // 判断本地目录路径是否存在，若不存在要创建此目录
        File file = null;
        if (_filePath == null) {
            file = new File("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\" + SystemConst.FILE_FOLDER_CLIENT);
        }else{
            file = new File(_filePath);
        }
        if (!file.exists()) {
            file.mkdirs();
        }

        chatServer.downloadFromServer(_fileName);
        Socket socketTrans = new Socket("localhost", SystemConst.THREAD_PORT_DOWNLOAD);
        String fileName = System.currentTimeMillis() + (new Random().nextInt(9) + 1) + _fileName;
        // 创建一个本地字节输出流对象
        FileOutputStream fos = new FileOutputStream(file + File.separator + fileName);
        // 获得输入流
        InputStream inStream = socketTrans.getInputStream();
        //使用输入流的方法 read 读取客户端发送过来的文件数据
        byte[] bytes = new byte[1024];
        int i = 0;
        while ((i = inStream.read(bytes)) != -1) {
            // 使用本地输出流将读取到文件数据写入到本地文件中
            fos.write(bytes, 0, i);
        }
        chatServer.sendMessagePrivate("接收文件："+fileName+" 到 "+file.getPath(),userName);

        fos.close();
        socketTrans.close();
    }
}

