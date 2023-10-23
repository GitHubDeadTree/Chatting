package com.kumu.gui.client;

import com.kumu.SystemConst;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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
            JButton downloadButton = new JButton("Download");
            downloadButton.addActionListener(e -> downloadFile());

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
            //TODO 让用户输入接受者的用户名
            writer.println(SystemConst.UPLOAD_FILE+" "+filePath);
        }
    }


    private void downloadFile() {
        // Implement the logic for file download here
        // You can request a file from the server and save it to your local system
    }

}

