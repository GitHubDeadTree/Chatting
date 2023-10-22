package com.kumu;

import java.net.ServerSocket;

public class Utils {
    public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true; // 如果端口可用，ServerSocket将成功创建
        } catch (Exception e) {
            return false; // 如果端口被占用，将抛出异常
        }
    }
}
