package com.taixue.schemtrans.stage;

import com.taixue.schemtrans.SchematicTransmission;
import com.taixue.schemtrans.utility.Icon;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.taixue.schemtrans.utility.Command;

public class RegisterApplication extends Application {
    private VBox vBox = new VBox(SchematicTransmission.PANE_SPACING);
    private Stage stage;
    private Scene scene = new Scene(vBox);

    private TextField userNameTextField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private PasswordField certainPasswordField = new PasswordField();
    private TextArea addition = new TextArea();

    private Button rewriteAll = new Button("重写");
    private Button register = new Button("注册");

    private void init(Stage stage) {
        this.stage = stage;
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setTitle("账户注册");
        stage.getIcons().add(Icon.LOGIN_IN_ICON);
        vBox.setPadding(new Insets(SchematicTransmission.STAGE_PADDING));
        GridPane gridPane = new GridPane();
        gridPane.setHgap(SchematicTransmission.PANE_GAP);
        gridPane.setVgap(SchematicTransmission.PANE_GAP);

        gridPane.add(new Label("用户名："), 0, 0);
        gridPane.add(userNameTextField, 1, 0);
        userNameTextField.setPromptText("长度应在 " + SchematicTransmission.MIN_USER_NAME_LENGTH + " 到 " + SchematicTransmission.MAX_USER_NAME_LENGTH + " 之间，" +
                "不能为纯数字或夹杂空格");

        gridPane.add(new Label("密码："), 0, 1);
        gridPane.add(passwordField, 1, 1);
        passwordField.setPromptText("长度应在 " + SchematicTransmission.MIN_PASSWORD_LENGTH + " 到 " + SchematicTransmission.MAX_PASSWORD_LENGTH + " 之间");

        gridPane.add(new Label("确认密码："), 0, 2);
        gridPane.add(certainPasswordField, 1, 2);

        gridPane.add(new Label("备注："), 0, 3);
        gridPane.add(addition, 1, 3);
        addition.setPromptText("备注是可选项，可以为管理员在后台审批注册申请时提供有效的信息（例如你在服务器中的用户名）");

        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(80),
                new ColumnConstraints(500)
        );

        vBox.getChildren().add(gridPane);

        HBox buttons = new HBox(SchematicTransmission.PANE_SPACING);
        buttons.getChildren().addAll(rewriteAll, register);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox.getChildren().add(buttons);

        setHandler();
    }

    private void setAllNodeToDefault() {
        userNameTextField.setStyle(" -fx-text-fill: BLACK");
        passwordField.setStyle(" -fx-text-fill: BLACK");
        certainPasswordField.setStyle(" -fx-text-fill: BLACK");
    }

    private void setHandler() {
        rewriteAll.setOnAction(event -> {
            setAllNodeToDefault();
            userNameTextField.clear();
            passwordField.clear();
            certainPasswordField.clear();
            addition.clear();
        });

        register.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            setAllNodeToDefault();
            if (userNameTextField.getText().length() < SchematicTransmission.MIN_USER_NAME_LENGTH) {
                alert.setHeaderText("用户名过短！");
                alert.setContentText("用户名长度应介于 " + SchematicTransmission.MIN_USER_NAME_LENGTH + " 和 " + SchematicTransmission.MAX_USER_NAME_LENGTH + " 之间");

                userNameTextField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return;
            }
            if (userNameTextField.getText().length() > SchematicTransmission.MAX_USER_NAME_LENGTH) {
                alert.setHeaderText("用户名过长！");
                alert.setContentText("用户名长度应介于 " + SchematicTransmission.MIN_USER_NAME_LENGTH + " 和 " + SchematicTransmission.MAX_USER_NAME_LENGTH + " 之间");

                userNameTextField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return;
            }
            if (!passwordField.getText().equals(certainPasswordField.getText())) {
                alert.setHeaderText("两次输入的密码不一致！");
                alert.setContentText("请检查密码和确认密码栏");

                passwordField.setStyle(" -fx-text-fill: RED");
                certainPasswordField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return;
            }
            if (passwordField.getText().length() < SchematicTransmission.MIN_PASSWORD_LENGTH) {
                alert.setHeaderText("密码过短！");
                alert.setContentText("密码长度应介于 " + SchematicTransmission.MIN_PASSWORD_LENGTH + " 和 " + SchematicTransmission.MAX_PASSWORD_LENGTH + " 之间");

                passwordField.setStyle(" -fx-text-fill: RED");
                certainPasswordField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return;
            }
            if (passwordField.getText().length() > SchematicTransmission.MAX_PASSWORD_LENGTH) {
                alert.setHeaderText("密码过长！");
                alert.setContentText("密码长度应介于 " + SchematicTransmission.MIN_PASSWORD_LENGTH + " 和 " + SchematicTransmission.MAX_PASSWORD_LENGTH + " 之间");

                passwordField.setStyle(" -fx-text-fill: RED");
                certainPasswordField.setStyle(" -fx-text-fill: RED");
                alert.showAndWait();
                return;
            }

            if (register(userNameTextField.getText(), passwordField.getText(), addition.getText())) {
                stage.close();
                SchematicTransmission.loginInApplication.setUserNameTextField(userNameTextField.getText());
            }
        });


    }

    public boolean register(String userName, String password, String addition) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        try {
            SchematicTransmission.socket.sendCommand("register");
            SchematicTransmission.socket.sendObject(userName);
            SchematicTransmission.socket.sendObject(password);
            SchematicTransmission.socket.sendObject(addition);
            Command result = SchematicTransmission.socket.receiveCommand();

            if (result.getType().equals("success")) {
                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.setHeaderText("请求已发送，请等待管理员审批");

                alert.setContentText("请牢记你的账号密码：\n" +
                        "帐号：" + userName + "\n" +
                        "密码：" + password);

                alert.show();
                return true;
            }
            else {
                alert.setHeaderText("注册失败！");
                alert.setContentText("原因：" + result.getDetail());
                alert.showAndWait();
                return false;
            }
        }
        catch (Exception exception) {
            alert.setHeaderText("注册遇到问题！");
            alert.setContentText("详情：" + exception.toString());

            alert.showAndWait();
            return false;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        stage.show();
    }
}
