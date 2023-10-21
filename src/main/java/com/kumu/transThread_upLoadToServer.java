package com.kumu;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Random;

/**
 * 用户向服务器传输文件
 */
public class transThread_upLoadToServer implements Runnable{
    private ChatServer _chatServer;
    private String _fileName;

    private String _userName;

    /**
     *  构造函数，传fileName
     */
    transThread_upLoadToServer(String fileName,ChatServer chatServer,String userName){
        _fileName = fileName;
        _chatServer = chatServer;
        _userName = userName;
    }
    transThread_upLoadToServer(){
        _fileName = null;
    }
    public void run() {

        try (ServerSocket server = new ServerSocket(SystemConst.THREAD_UPLOAD_PORT)) {
            System.out.println(SystemConst.THREAD_UPLOAD +" is running on port 8800...");

            while (true) {
                Socket socket = server.accept();
                // 4.判断服务端的目标目录路径是否存在，若不存在要创建此目录
                File file = new File("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\"+SystemConst.FILE_FOLDER_SERVER);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String fileName = System.currentTimeMillis() + (new Random().nextInt(9) + 1) + _fileName;
                // 5.创建一个本地字节输出流对象
                FileOutputStream fos = new FileOutputStream(file + File.separator + fileName);
                // 获得输入流
                InputStream inStream = socket.getInputStream();
                // 6.使用输入流的方法 read 读取客户端发送过来的文件数据
                byte[] bytes = new byte[1024];
                int i = 0;
                while ((i = inStream.read(bytes)) != -1) {
                    // 7.使用 FileOutputStream 对象的方法 write 将读取到文件数据写入到服务器本地文件中
                    fos.write(bytes, 0, i);
                }
                System.out.println("接收文件："+fileName+" 到 "+file.getPath());

                // 9.给客户端发送一段文字：文件上传成功！
                _chatServer.sendMessagePrivate(SystemConst.FILE_UPLOAD_SUCCESS,_userName);
                fos.close();
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}