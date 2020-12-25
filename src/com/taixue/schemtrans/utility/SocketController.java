package com.taixue.schemtrans.utility;

import com.taixue.schemtrans.SchematicTransmission;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class SocketController {
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Scanner scanner;
    private PrintWriter printWriter;
    
    public SocketController(String serverName, int port) {
        try {
            Socket socket = new Socket(serverName, port);
            setSocket(socket);
        }
        catch (ConnectException connectException) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("请求被拒绝，无法连接至服务器。");
            alert.setContentText("请检查本地是否开启了多个程序，及服务器是否开启。");

            alert.showAndWait();
            System.exit(0);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean isOpened() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void setSocket(Socket socket) throws IOException {
        socket.setSoTimeout(SchematicTransmission.timeOut);
        this.socket = socket;
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);

        objectOutputStream = new ObjectOutputStream(outputStream);
        objectInputStream = new ObjectInputStream(inputStream);

        scanner = new Scanner(inputStream);
        printWriter = new PrintWriter(outputStream);
    }

    public void sendCommand(Command token) {
        sendObject(token);
    }

    public void sendCommand(String type) {
        sendCommand(type, null);
    }

    public void sendCommand(String type, String detail) {
        sendCommand(new Command(type, detail));
    }

    public synchronized void sendObject(Object object) {
        try {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Command receiveCommand()
            throws SocketTimeoutException, ClassNotFoundException, ClassCastException, IOException {
        if (!isOpened()) {
            return null;
        }
        return (Command) receiveObject();
    }

    public Pair<Boolean, String> sendFile(File file) {
        boolean success = false;
        String reason = null;

        if (!isOpened()) {
            reason = "无法连接至服务器";
            return new Pair<>(success, reason);
        }
        if (!file.exists()) {
            reason = "文件 " + file.getAbsoluteFile() + " 不存在";
            return new Pair<>(success, reason);
        }
        if (!file.canRead()) {
            reason = "没有对文件 " + file.getAbsoluteFile() + " 的读权限";
            return new Pair<>(success, reason);
        }

        try {

            sendCommand("send_file");
            sendObject(file.getName());

            long totalSize = file.length();
            long sentSize = 0;
            sendObject(totalSize);

            Command res = receiveCommand();
            if (res.isType("accept")) {
                byte[] buffer = new byte[SchematicTransmission.SEND_PACKAGE_SIZE];
                int len = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);) {
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                        outputStream.flush();
                        sentSize += len;
//                        updateProgress(sentSize, totalSize);
                    }
                    try {
                        res = receiveCommand();
                    } catch (SocketTimeoutException e) {
                    }
                    if (res.isType("success")) {
                        success = true;
                    } else {
                        if (res.isType("fail")) {
                            reason = res.getDetail();
                        } else {
                            reason = "程序出现异常（res = " + res + "），请联系开发者椽子";
                        }
                    }
                }
            } else {
                reason = res.getDetail();
            }
        } catch (SocketException socketException) {
            success = false;
            reason = "网络连接出现问题";
        } catch (SocketTimeoutException timeoutException) {
            success = false;
            reason = "网络超时";
        } catch (Exception exception) {
            success = false;
            reason = "发送文件时出现异常 " + exception.toString();
        } finally {
            return new Pair<>(success, reason);
        }
    }

    public boolean receiveBoolean()
            throws IOException {
        return objectInputStream.readBoolean();
    }

    public long receiveLong()
            throws IOException {
        return objectInputStream.readLong();
    }

    public Task receiveFile(File saveTo, long size) throws IOException {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                long receivedSize = 0;

                try (FileOutputStream fileOutputStream = new FileOutputStream(saveTo);) {
                    byte[] buffer = new byte[SchematicTransmission.SEND_PACKAGE_SIZE];

                    int len = 0;
                    // 因为文件内容较大，不能一次发送完毕，因此需要通过循环来分次发送

                    try {
                        while ((len = inputStream.read(buffer)) != -1 && (receivedSize += len) <= size) {
                            fileOutputStream.write(buffer, 0, len);
                            updateProgress(receivedSize, size);
                        }
                    } catch (SocketTimeoutException e) {
                    }
                    if (receivedSize != size) {
                        sendCommand("fail", "文件未完整传送（传送 " + receivedSize + " B，文件总大小 " + size + " B）");
                        failed();
                    } else {
                        sendCommand("success");
                        succeeded();
                    }
                }
                return null;
            }
        };
    }

    public Object receiveObject() throws SocketTimeoutException, ClassNotFoundException, IOException{
        return objectInputStream.readObject();
    }
}
