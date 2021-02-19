package com.taixue.schemtrans;

import com.taixue.schemtrans.utility.Icon;
import com.taixue.schemtrans.node.MenuBar;
import com.taixue.schemtrans.stage.OperatorApplication;
import com.taixue.schemtrans.utility.Fold;
import com.taixue.schemtrans.utility.Setting;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.taixue.schemtrans.stage.LoginInApplication;
import com.taixue.schemtrans.node.Status;
import com.taixue.schemtrans.utility.SocketController;

import java.io.*;

public class SchematicTransmission {
    public static Setting setting = new Setting(Fold.SETTING_FILE_NAME);

    public static String serverName = setting.getServerName();
    public static int port = setting.getPort();

    public static final String VERSION = "1.0";
//    public static final String VERSION = "2.0";
    public static final String SOFTWARE_NAME = "太学服务器 Schematic 操作助手";

    public static final int STAGE_HEIGHT = 618;
    public static final int STAGE_WIDTH = 1000;

    public static final int ERROR_HEIGHT = ((int) (618 * 0.9));
    public static final int ERROR_WIDTH = ((int) (1000 * 0.9));
    public static final int ERROR_PADDING = 15;

    public static final int PANE_GAP = 10;
    public static final int PANE_SPACING = 10;
    public static final int STAGE_PADDING = 35;

    public static final int HEADER_PICTURE_HEIGHT = 124;
    public static final int HEADER_PICTURE_WIDTH = 544;


    public static final int timeOut = ((int) 8e3);

    public static SocketController socket = new SocketController(serverName, port);

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 32;
    public static final int MIN_USER_NAME_LENGTH = 3;
    public static final int MAX_USER_NAME_LENGTH = 32;

    public static final int SEND_PACKAGE_SIZE = 256;

    public static final Status status = new Status();

    public static LoginInApplication loginInApplication;
    public static OperatorApplication operatorApplication;

    public static MenuBar menuBar;

    public static boolean isOpened() {
        return socket.isOpened();
    }

    public static void autoUpdateClient() throws Exception {
        if (!checkVersion()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setHeaderText("当前版本（" + SchematicTransmission.VERSION + "）已不可使用");
            alert.setContentText("是否立刻获取更新？");

            alert.showAndWait();
            if (alert.getResult() == ButtonType.OK) {
                File newClient = downNewestVersion();
                if (newClient != null) {
                    runNewVersion(newClient);
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        }
    }

    public static boolean checkVersion() {
        try {
            if (isOpened()) {
                socket.sendCommand("check_version");
                socket.sendObject(VERSION);
                return socket.receiveBoolean();
            } else {
                return true;
            }
        }
        catch (Exception exception) {
            return true;
        }
    }

    public static boolean setInternetStatus(Status status) {
        if (isOpened()) {
            status.setSuccess("网络畅通");
            return true;
        }
        else {
            status.setError("网络存在问题");
            return false;
        }
    }

    public static void showNoSuchFunctionDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("当前版本尚无此功能 (；′⌒`)");
        alert.setContentText(!checkVersion() ? "有最新版本 Σ(っ °Д °;)っ！请重启程序进行自动更新" :
                "暂无最新版本，请等待版本更新。版本检查将在每次启动程序时执行。");

        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(Icon.ERROR_ICON);

        alert.showAndWait();
    }

    public static void showExceptionDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("程序错误");

        alert.setHeaderText("小明抓到了一个未经处理的异常 (´；ω；`)");

        BorderPane borderPane = new BorderPane();
        alert.getDialogPane().setContent(borderPane);
        try {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(Icon.ERROR_ICON);
        }
        catch (Exception exception) {
        }

        alert.getDialogPane().setPadding(new Insets(ERROR_PADDING));

        TextArea textArea = new TextArea();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);
        textArea.appendText(stringWriter.toString());
        textArea.setEditable(false);

        borderPane.setTop(new Label("下面是该错误的详细信息，你可以提交相关 Bug，或者将此信息反馈给程序制作者。\n" +
                "程序制作者：椽子，邮箱：chuanwise@qq.com。"));
        borderPane.setCenter(textArea);

        alert.getButtonTypes().clear();
        ButtonType ok = new ButtonType("(；′⌒`) 好吧", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().add(ok);
        ((Button) alert.getDialogPane().lookupButton(ok)).setDefaultButton(true);

        alert.getDialogPane().setPrefHeight(ERROR_HEIGHT);
        alert.getDialogPane().setPrefWidth(ERROR_WIDTH);

        alert.showAndWait();
    }

    public static void showExceptionDialogAndExit(Throwable throwable) {
        showExceptionDialog(throwable);
        System.exit(-1);
    }

    public static File downNewestVersion() {
        socket.sendCommand("get_client");

        try {
            String fileName = ((String) socket.receiveObject());
            long size = socket.receiveLong();

            File file = new File(Fold.newVersionDirName, fileName);
            Alert alert = new Alert(Alert.AlertType.ERROR);

            if (!Fold.newVersionDir.exists() && !Fold.newVersionDir.mkdirs()) {
                alert.setHeaderText("无法存放更新文件");
                alert.setContentText("更新文件应放在 " + Fold.newVersionDir.getAbsolutePath() + "，但找不到该文件，且无法创建。");

                alert.showAndWait();
                return null;
            }
            else if (file.exists() && !file.delete()) {
                alert.setHeaderText("无法删除原有的文件");
                alert.setContentText("更新文件应放在 " + file.getAbsolutePath() + "，检测到此处已有新文件，覆盖时出现错误");

                alert.showAndWait();
                return null;
            }
            else if (file.getParentFile().getFreeSpace() < size) {
                alert.setHeaderText("没有足够的空间存放即将发送的文件");
                alert.setContentText("新版客户端大小：" + size + " B，存储区域仅剩 " + file.getParentFile().getFreeSpace() + " B\n");

                alert.showAndWait();
                return null;
            }
            else {
                socket.sendCommand("accept");

                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.setHeaderText("正在下载最新版本（" + fileName + "）");

                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(400);
                ButtonType cancel = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().clear();
                alert.getButtonTypes().addAll(cancel);
                alert.getDialogPane().setContent(progressBar);

                socket.receiveFile(file, size);
                progressBar.progressProperty().unbind();
            }
        }
        catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setHeaderText("更新软件时出现异常！");
            alert.setContentText("异常：" + exception);
            exception.printStackTrace();
            alert.show();
        }
        return null;
    }

    public static void runNewVersion(File newVersionFile) {
        if (SchematicTransmission.operatorApplication != null) {
            SchematicTransmission.operatorApplication.shutdown();
        }
        try {
            Runtime.getRuntime().exec(new String[]{"java", "-jar", newVersionFile.getAbsolutePath()});
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static boolean checkUpdateHeaderPicture() throws IOException{
        socket.sendCommand("check_header_picture");
        socket.sendObject(Icon.HEADER_ICON.hashCode());
        return socket.receiveBoolean();
    }

    public static void autoUpdateHeaderPicture() {
        try {
            socket.sendCommand("get_header_picture");
            socket.receiveFile(Fold.newVersionDir);
        }
        catch (Throwable throwable) {
            showExceptionDialog(throwable);
        }
    }
}
