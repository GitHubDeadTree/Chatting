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
    private InetAddress from;
    private InetAddress to;
    private String fileName;

    /**
     *  构造函数，传
     */
    transThread_downloadToClient(){

    }
    public void run() {
        /*
         * try-with-resources 结构，可以自动关闭实现了 AutoCloseable 或 Closeable 接口的资源
         * 创建ServerSocket对象时，可能会抛出IOException异常，因为端口号可能已经被占用或者无效。
         */
        try (ServerSocket server = new ServerSocket(8801)) {
            System.out.println("Thread_File is running on port 8801...");

            while (true) {
                Socket socket = server.accept();
                // 3.使用 Socket 对象的方法 getInputStream 获取网络字节输入流
                InputStream is = socket.getInputStream();
                System.out.println("收到一份文件");
                // 4.判断服务端的目标目录路径是否存在，若不存在要创建此目录
                File file = new File("E:\\Game\\college\\大二\\java\\实验课\\聊天室\\dowloadFile");
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 自定义文件名的规则
                String fileName = System.currentTimeMillis() + (new Random().nextInt(9) + 1) + ".jpeg";
                // 5.创建一个本地字节输出流对象（即 FileOutputStream），构造方法中要绑定写入数据的目标文件路径
                FileOutputStream fos = new FileOutputStream(file + File.separator + fileName);

                // 6.使用网络字节输入流的方法 read 读取客户端发送过来的文件数据
                byte[] bytes = new byte[1024];
                int i = 0;
                while ((i = is.read(bytes)) != -1) {
                    // 7.使用 FileOutputStream 对象的方法 write 将读取到文件数据写入到服务器本地文件中
                    fos.write(bytes, 0, i);
                }

                // 8.使用 Socket 对象的方法 getOutputStream 获取网络字节输出流对象
                OutputStream ops = socket.getOutputStream();
                // 9.使用网络字节输出流对象的方法 write 给客户端发送一段文字：文件上传成功！
                ops.write("文件上传成功！".getBytes());
                // 10.释放资源（FileOutputStream、Socket、ServerSocket）
                fos.close();
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}