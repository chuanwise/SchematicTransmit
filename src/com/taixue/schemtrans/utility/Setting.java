package com.taixue.schemtrans.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import com.taixue.schemtrans.SchematicTransmission;

import java.io.*;

public class Setting implements Serializable {
    private static final long serialVersionUID = 615560069086687056L;

    private String serverName = "r.mcstory.cc";
    private int port = 37738;

    public static transient final String DEFAULT_SEVER_NAME = "r.mcstory.cc";
    public static transient final int DEFAULT_PORT = 37738;

    private boolean autoLoginIn = false;

    private String userName = "";
    private String password = null;

    public transient File file;

    public Setting(String settingFileName) {
        file = new File(settingFileName);
        readSettingFromFile();
    }

    private void readSettingFromObjectInputStream(ObjectInputStream objectInputStream)
            throws ClassNotFoundException, ClassCastException, IOException {
        Setting setting = ((Setting) objectInputStream.readObject());
        serverName = setting.serverName;
        port = setting.port;

        autoLoginIn = setting.autoLoginIn;
        userName = setting.userName;
        password = setting.password;
    }

    private void readSettingFromFile() {
        try {
            if (file.exists()) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
                    readSettingFromObjectInputStream(objectInputStream);
                }
            }
        }
        catch (ClassCastException classCastException) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("初始化错误");
            alert.setHeaderText("错误的配置文件");
            alert.setContentText(file.getAbsolutePath() + " 可能不是 " + SchematicTransmission.SOFTWARE_NAME + " 的配置文件，\n" +
                    "建议将其移动至其他位置后重启本程序。\n" +
                    "你仍可以采用默认的设置运行本程序，但可能会导致该文件被重写为本程序的配置文件。\n" +
                    "若仍出现该问题，请联系管理员。");

            ButtonType ignore = new ButtonType("忽略");
            ButtonType exit = new ButtonType("退出", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().addAll(ignore, exit);
            if (exit.equals(alert.showAndWait().get())) {
                System.exit(0);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!file.exists() && !file.createNewFile()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("无法保存配置文件");
                alert.setContentText("可能软件无在运行位置下的写权限。请移动软件到非系统盘并再次尝试。\n" +
                        "很遗憾本次启动所作所有设置均无法保存。\n" +
                        "若仍存在此问题，请联系管理员。");

                alert.showAndWait();
            }
            else {
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
                    objectOutputStream.writeObject(this);
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean isAutoLoginIn() {
        return autoLoginIn;
    }

    public void setAutoLoginIn(boolean autoLoginIn) {
        this.autoLoginIn = autoLoginIn;
        save();
    }

    public void setPassword(String password) {
        this.password = password;
        save();
    }

    public void setUserName(String userName) {
        this.userName = userName;
        save();
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public File getFile() {
        return file;
    }

    public int getPort() {
        return port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setPort(int port) {
        this.port = port;
        save();
    }

    public void setFile(File file) {
        this.file = file;
        save();
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
        save();
    }
}
