package com.taixue.schemtrans.stage;

import com.taixue.schemtrans.SchematicTransmission;
import com.taixue.schemtrans.utility.Icon;
import com.taixue.schemtrans.node.Status;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.taixue.schemtrans.utility.Command;
import com.taixue.schemtrans.utility.Setting;
import com.taixue.schemtrans.utility.SocketController;
import com.taixue.schemtrans.utility.User;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public class LoginInApplication extends Application {

    private TextField userNameTextField = new TextField();
    private PasswordField passwordField = new PasswordField();

    private Button registerButton = new Button("注册");
    private Button loginInButton = new Button("登录");
    private Button rewriteButton = new Button("清空");
    private Button setting = new Button("设置");

    private CheckBox autoLogin = new CheckBox("自动登录");
    private CheckBox rememberPassword = new CheckBox("记住密码");

    private Status status = new Status();

    private Stage stage;
    private BorderPane borderPane = new BorderPane();
    private Scene scene = new Scene(borderPane);

    private void init(Stage stage) {
        SchematicTransmission.loginInApplication = this;
        this.stage = stage;
        stage.setScene(scene);

        stage.getIcons().add(Icon.LOGO_ICON);
        stage.setTitle(SchematicTransmission.SOFTWARE_NAME + " - 身份验证");

        ImageView headerPicture = new ImageView(Icon.HEADER_ICON);
        headerPicture.setFitHeight(SchematicTransmission.HEADER_PICTURE_HEIGHT);
        headerPicture.setFitWidth(SchematicTransmission.HEADER_PICTURE_WIDTH);

        borderPane.setTop(headerPicture);
        VBox content = new VBox(20);
        borderPane.setCenter(content);
        content.setPadding(new Insets(SchematicTransmission.PANE_SPACING, SchematicTransmission.STAGE_PADDING, SchematicTransmission.STAGE_PADDING, SchematicTransmission.STAGE_PADDING));

        GridPane gridPane = new GridPane();

        status.setNormal("请进行身份验证。若无帐号，注册后管理员审批通过后即可使用。");

        /** use the remember setting */
        userNameTextField.setText(SchematicTransmission.setting.getUserName());
        passwordField.setText(SchematicTransmission.setting.getPassword() == null ? "" : SchematicTransmission.setting.getPassword());
        autoLogin.setSelected(SchematicTransmission.setting.isAutoLoginIn());
        rememberPassword.setSelected(SchematicTransmission.setting.getPassword() != null);

        gridPane.add(new Label("用户名："), 0, 0);
        gridPane.add(userNameTextField, 1, 0);
        userNameTextField.setPromptText("User Name");
        gridPane.add(new Label("密码："), 0, 1);
        gridPane.add(passwordField, 1, 1);

        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(70),
                new ColumnConstraints(390)
        );
        gridPane.setHgap(SchematicTransmission.PANE_GAP);
        gridPane.setVgap(SchematicTransmission.PANE_GAP);

        BorderPane bottom = new BorderPane();
        HBox buttons = new HBox(SchematicTransmission.PANE_SPACING);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(rewriteButton, registerButton, setting, loginInButton);
        loginInButton.setDefaultButton(true);
        bottom.setRight(buttons);

        HBox checkBoxes = new HBox(SchematicTransmission.PANE_GAP);
        checkBoxes.getChildren().addAll(autoLogin, rememberPassword);
        bottom.setLeft(checkBoxes);
        content.getChildren().addAll(status, gridPane, bottom);

        setHandler();
    }

    private void setHandler() {
        rewriteButton.setOnAction(e -> {
            userNameTextField.clear();
            passwordField.clear();
            setAllTextFieldToDefault();
        });

        loginInButton.setOnAction(event -> {
            if (rememberPassword.isSelected()) {
                SchematicTransmission.setting.setPassword(passwordField.getText());
            }
            else {
                SchematicTransmission.setting.setPassword(null);
            }
            if (loginIn(userNameTextField.getText(), passwordField.getText())) {
                loginInSuccessfully(userNameTextField.getText());
            }
        });

        registerButton.setOnAction(event -> {
            Stage stage = new Stage();
            try {
                new RegisterApplication().start(stage);
            }
            catch (Exception exception) {
                stage.close();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("注册程序遭遇未知问题");
                alert.setContentText("请稍后重试");

                alert.showAndWait();
            }
        });

        setting.setOnAction(e -> showSettingDialog());

        autoLogin.setOnAction(e -> {
            SchematicTransmission.setting.setAutoLoginIn(autoLogin.isSelected());

            if (autoLogin.isSelected() && !rememberPassword.isSelected()) {
                rememberPassword.setSelected(true);
            }
        });

        rememberPassword.setOnAction(e -> {
            if (!rememberPassword.isSelected() && autoLogin.isSelected()) {
                autoLogin.setSelected(false);
                SchematicTransmission.setting.setAutoLoginIn(false);
            }
        });

        stage.setOnCloseRequest(e -> {
            SchematicTransmission.socket.sendCommand("close");
        });
    }

    public void showSettingDialog() {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        GridPane gridPane = new GridPane();
        dialog.getDialogPane().setContent(gridPane);
        dialog.setTitle("设置");
        dialog.setHeaderText("设置服务器信息\n" + SchematicTransmission.VERSION + " 版仅支持 TCP/IP 协议，暂不支持 UDP 协议");
        dialog.getDialogPane().setPadding(new Insets(SchematicTransmission.ERROR_PADDING));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(Icon.SETTING_ICON);

        String originalSeverName = SchematicTransmission.serverName;
        int originalPort = SchematicTransmission.port;

        TextField severNameTextField = new TextField(SchematicTransmission.serverName);
        TextField portTextField = new TextField(Integer.toString(SchematicTransmission.port));

        gridPane.setHgap(SchematicTransmission.PANE_GAP);
        gridPane.setVgap(SchematicTransmission.PANE_GAP);

        gridPane.add(new Label("服务器名："), 0, 0);
        gridPane.add(severNameTextField, 1, 0);

        gridPane.add(new Label("端口："), 0, 1);
        gridPane.add(portTextField, 1, 1);

        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(100),
                new ColumnConstraints(300)
        );

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType setToDefaultButtonType = new ButtonType("恢复默认", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().addAll(saveButtonType, setToDefaultButtonType);

        Button saveButton = ((Button) dialog.getDialogPane().lookupButton(saveButtonType));
        Button setToDefaultButton = ((Button) dialog.getDialogPane().lookupButton(setToDefaultButtonType));

        saveButton.setDefaultButton(true);

        saveButton.setOnAction(e -> {
            if (severNameTextField.getText().isEmpty()) {
                status.setError("修改设置失败：服务器名格式错误");
                return;
            }
            if (portTextField.getText().isEmpty()) {
                status.setError("修改设置失败：端口格式错误");
                return;
            }
            try {
                SchematicTransmission.setting.setPort(Integer.parseInt(portTextField.getText()));
                SchematicTransmission.port = SchematicTransmission.setting.getPort();

                SchematicTransmission.setting.setServerName(severNameTextField.getText());
                SchematicTransmission.serverName = SchematicTransmission.setting.getServerName();
            }
            catch (NumberFormatException numberFormatException) {
                status.setError("修改设置失败：端口格式错误");
                return;
            }
            catch (Throwable throwable) {
                SchematicTransmission.showExceptionDialogAndExit(throwable);
            }
            status.setSuccess("成功修改设置");
        });

        setToDefaultButton.setOnAction(e -> {
            severNameTextField.setText(Setting.DEFAULT_SEVER_NAME);
            portTextField.setText(Integer.toString(Setting.DEFAULT_PORT));

            SchematicTransmission.setting.setPort(Integer.parseInt(portTextField.getText()));
            SchematicTransmission.port = SchematicTransmission.setting.getPort();

            SchematicTransmission.setting.setServerName(severNameTextField.getText());
            SchematicTransmission.serverName = SchematicTransmission.setting.getServerName();

            status.setSuccess("成功恢复默认设置");
        });

        dialog.showAndWait();

        if (!SchematicTransmission.serverName.equals(originalSeverName) || originalPort != SchematicTransmission.port) {
            SchematicTransmission.socket = new SocketController(SchematicTransmission.serverName, SchematicTransmission.port);
        }
    }

    private void loginInSuccessfully(String userName) {
        stage.close();
        try {
            new OperatorApplication(new User(userName)).start(new Stage());
        }
        catch (Throwable throwable) {
            SchematicTransmission.showExceptionDialogAndExit(throwable);
        }
    }

    public boolean loginIn(String userName, String password) {
        SchematicTransmission.setting.setUserName(userName);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        try {
            if (userName.length() < SchematicTransmission.MIN_USER_NAME_LENGTH) {
                alert.setHeaderText("用户名过短！");
                status.setWarning("用户名过短！");
                alert.setContentText("用户名长度应介于 " + SchematicTransmission.MIN_USER_NAME_LENGTH + " 和 " + SchematicTransmission.MAX_USER_NAME_LENGTH + " 之间");

                userNameTextField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return false;
            }
            if (userName.length() > SchematicTransmission.MAX_USER_NAME_LENGTH) {
                alert.setHeaderText("用户名过长！");
                status.setWarning("用户名过长！");
                alert.setContentText("用户名长度应介于 " + SchematicTransmission.MIN_USER_NAME_LENGTH + " 和 " + SchematicTransmission.MAX_USER_NAME_LENGTH + " 之间");

                userNameTextField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return false;
            }

            if (password.length() < SchematicTransmission.MIN_PASSWORD_LENGTH) {
                alert.setHeaderText("密码过短！");
                status.setWarning("密码过短！");
                alert.setContentText("密码长度应介于 " + SchematicTransmission.MIN_PASSWORD_LENGTH + " 和 " + SchematicTransmission.MAX_PASSWORD_LENGTH + " 之间");

                passwordField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return false;
            }
            if (password.length() > SchematicTransmission.MAX_PASSWORD_LENGTH) {
                alert.setHeaderText("密码过长！");
                status.setWarning("密码过长！");
                alert.setContentText("密码长度应介于 " + SchematicTransmission.MIN_PASSWORD_LENGTH + " 和 " + SchematicTransmission.MAX_PASSWORD_LENGTH + " 之间");

                passwordField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return false;
            }

            status.setNormal("正在验证...");

            SchematicTransmission.socket.sendCommand("sign_in");
            SchematicTransmission.socket.sendObject(userName);
            SchematicTransmission.socket.sendObject(password);

            Command command = SchematicTransmission.socket.receiveCommand();
            if (command.isType("success")) {
                status.setSuccess("通过验证，正在载入数据...");
                return true;
            }
            else {
                status.setWarning("无法通过验证");
                alert.setAlertType(Alert.AlertType.WARNING);
                alert.setHeaderText("登陆失败！");
                alert.setContentText("原因：" + command.getDetail());

                alert.showAndWait();
                return false;
            }
        }
        catch (SocketTimeoutException timeoutException) {
            status.setError("网络超时");
        }
        catch (SocketException exception) {
            status.setError("请重启本程序再试");
        }
        catch (Exception exception) {
            SchematicTransmission.showExceptionDialog(exception);
            exception.printStackTrace();
        }
        return false;
    }

    private void setAllTextFieldToDefault() {
        userNameTextField.setStyle(" -fx-text-fill: BLACK");
        passwordField.setStyle(" -fx-text-fill: BLACK");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            try {
                if (SchematicTransmission.checkUpdateHeaderPicture()) {
                    SchematicTransmission.autoUpdateHeaderPicture();
                }
            }
            catch (Exception exception) {
            }
            SchematicTransmission.autoUpdateClient();
        }
        catch (Throwable throwable) {
            SchematicTransmission.showExceptionDialogAndExit(throwable);
        }
        try {
            init(primaryStage);
            stage.show();
            if (SchematicTransmission.setting.isAutoLoginIn() && SchematicTransmission.setting.getPassword() != null) {
                if (loginIn(SchematicTransmission.setting.getUserName(), SchematicTransmission.setting.getPassword())) {
                    loginInSuccessfully(SchematicTransmission.setting.getUserName());
                } else {
                    status.setError("自动登录失败：" + status.getText());
                }
            }
        }
        catch (Throwable throwable) {
            SchematicTransmission.showExceptionDialogAndExit(throwable);
        }
    }

    public void setUserNameTextField(String userName) {
        this.userNameTextField.setText(userName);
    }
}
