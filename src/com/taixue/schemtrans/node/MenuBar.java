package com.taixue.schemtrans.node;

import com.taixue.schemtrans.stage.LoginInApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import com.taixue.schemtrans.SchematicTransmission;
import javafx.stage.Stage;

public class MenuBar extends javafx.scene.control.MenuBar {

    private Menu software = new Menu("程序");
    private MenuItem close = new MenuItem("关闭");
    private MenuItem setting = new MenuItem("设置");

    private Menu file = new Menu("文件");
    private MenuItem upload = new MenuItem("上传");
    private MenuItem download = new MenuItem("下载");

    private Menu account = new Menu("账户");
    private MenuItem changePassword = new MenuItem("修改密码");
    private MenuItem loginOut = new MenuItem("登出帐号");
    private MenuItem totalHistory = new MenuItem("查看账户所有操作记录");

    private Menu help = new Menu("帮助");
    private MenuItem information = new MenuItem("软件信息");
    private MenuItem checkUpdate = new MenuItem("检查更新");

    public MenuBar() {
        initialize();
    }

    private void initialize() {
        SchematicTransmission.menuBar = this;

        getMenus().addAll(software, file, account, help);

        software.getItems().addAll(close, setting);
        file.getItems().addAll(upload, download);
        account.getItems().addAll(changePassword, loginOut, totalHistory);
        help.getItems().addAll(information);

        setHandler();
    }

    private void setHandler() {
        close.setOnAction(event -> {
            SchematicTransmission.operatorApplication.shutdown();
        });

        setting.setOnAction(e -> {
            SchematicTransmission.loginInApplication.showSettingDialog();
        });

        upload.setAccelerator(KeyCombination.keyCombination("Ctrl+u"));
        upload.setOnAction(event -> {
            SchematicTransmission.operatorApplication.uploadFiles();
        });

        download.setAccelerator(KeyCombination.keyCombination("Ctrl+d"));
        download.setOnAction(event -> {
            SchematicTransmission.showNoSuchFunctionDialog();
        });

        changePassword.setOnAction(event -> {
            SchematicTransmission.showNoSuchFunctionDialog();
        });

        loginOut.setOnAction(event -> {
            SchematicTransmission.showNoSuchFunctionDialog();
        });

        totalHistory.setOnAction(event -> {
            SchematicTransmission.showNoSuchFunctionDialog();
        });

        information.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(SchematicTransmission.SOFTWARE_NAME + "（" + SchematicTransmission.VERSION + "）");
            alert.setContentText("作者：椽子 Chuanwise\n" +
                    "邮箱：chuanwise@qq.com\n" +
                    "版本：" + SchematicTransmission.VERSION + "\n" +
                    "发布日期：" + "2020年12月21日\n" +
                    "版权所有 © 明城京联合太学 2020 - 2025\n" +
                    "\n" +
                    "太学是 Minecraft 中式建筑教学-建筑组织。\n" +
                    "有自己的建筑团队（营造司）和教学团队（南书房）。\n" +
                    "长期开办古建筑教学，招收营造司成员，一起做伟大的工程\n" +
                    "也欢迎进入太学群，和超过两千位古建筑和Minecraft古建筑爱好者交流探讨。\n" +
                    "太学 QQ 群号：一群：1043967360、二群：364840986\n" +
                    "\n" +
                    "（软件著作权归明城京联合太学和椽子所有，其他服务器亦可使用该软件，可入群了解）");

            alert.show();
        });
    }
}
