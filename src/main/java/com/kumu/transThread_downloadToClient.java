package com.kumu;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * 服务器向用户传输文件
 */
public class transThread_downloadToClient implements Runnable{
    private String _fileName;
    transThread_downloadToClient(){

    }

    public transThread_downloadToClient(String fileName) {
        _fileName = fileName;
    }

    public void run() {
        /**
         * try-with-resources 结构，可以自动关闭实现了 AutoCloseable 或 Closeable 接口的资源
         * 创建ServerSocket对象时，可能会抛出IOException异常，因为端口号可能已经被占用或者无效。
         */
        try (ServerSocket server = new ServerSocket(SystemConst.THREAD_PORT_DOWNLOAD)) {
            System.out.println("Thread_File is running on port 8801...");

            while (true) {
                Socket socketTrans = server.accept();
                // 创建本地文件输入流
                FileInputStream fIS = new FileInputStream("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\"+SystemConst.FILE_FOLDER_SERVER+"\\"+_fileName);

                // 获取网络字节输出流
                OutputStream opStream = socketTrans.getOutputStream();
                // 读取要上传的文件数据
                byte[] bytes = new byte[1024];
                int i = 0;
                while ((i = fIS.read(bytes)) != -1) {
                    // 使用输出流将文件数据发送到客户端的Socket
                    opStream.write(bytes, 0, i);
                }
                // 禁用此套接字的输出流，此时会写入一个终止标记，这样客户端就可以读取到此标记，就不会出现阻塞的问题了
                socketTrans.shutdownOutput();
                // 8.释放资源
                fIS.close();
                socketTrans.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}