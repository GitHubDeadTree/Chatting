package com.kumu.gui.client;

import com.kumu.SystemConst;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatClient {
    private String _username;
    private PrintWriter writer;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient().startClient());
    }

    public void startClient() {
        try {
            Socket socket = new Socket("localhost", 8888);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            // Create the GUI
            frame = new JFrame("Chat Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);

            chatArea = new JTextArea();
            chatArea.setEditable(false);
            JScrollPane chatScrollPane = new JScrollPane(chatArea);
            chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            inputField = new JTextField();
            inputField.addActionListener(e -> sendMessage());

            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(e -> sendMessage());

            // Create the "Upload" button
            JButton uploadButton = new JButton("Upload");
            uploadButton.addActionListener(e -> uploadFile());

            // Create the "Download" button
            JButton downloadButton = new JButton("Setting");
            downloadButton.addActionListener(e -> settingFilePath());

            // Create a panel for the buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(sendButton);
            buttonPanel.add(uploadButton);
            buttonPanel.add(downloadButton);

            // Add the button panel to the frame
            frame.add(buttonPanel, BorderLayout.NORTH);


            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BorderLayout());
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            frame.add(chatScrollPane, BorderLayout.CENTER);
            frame.add(inputPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

            // Start a new thread for receiving messages
            _username = getUsernameFromInput();
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = reader.readLine()) != null) {
                        updateChatArea(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUsernameFromInput() {
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            return getUsernameFromInput();
        }
        writer.println(username);
        return username;
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            inputField.setText("");
        }
    }

    private void updateChatArea(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            // 现在，filePath 包含用户选择的文件的路径
            // 将 filePath 发送到服务器或进行其他逻辑处理
            String receiverName = JOptionPane.showInputDialog("选择接收者: ");
            String order = SystemConst.PRE_UPLOAD_FILE +"-"+filePath+"-"+receiverName;
            writer.println(order);
        }
    }


    private void settingFilePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // 设置为选择文件夹模式

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile(); // 获取用户选择的文件夹
            String folderPath = selectedDirectory.getAbsolutePath(); // 获取文件夹的路径
            // 现在，folderPath 包含用户选择的文件夹的路径
            // 将 folderPath 发送到服务器或进行其他逻辑处理
            writer.println(SystemConst.PRE_SETTING_PATH + " " + folderPath);
        }
    }

}

