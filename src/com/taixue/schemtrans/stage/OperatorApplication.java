package com.taixue.schemtrans.stage;

import com.taixue.schemtrans.Progress.SendFiles;
import com.taixue.schemtrans.utility.Icon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.taixue.schemtrans.node.MenuBar;
import com.taixue.schemtrans.node.Status;
import com.taixue.schemtrans.SchematicTransmission;
import com.taixue.schemtrans.utility.User;

public class OperatorApplication extends Application {
    private User user;
    private Stage stage;

    private BorderPane borderPane = new BorderPane();
    private Scene scene = new Scene(borderPane);

    private EventTable eventTable = new EventTable();

    private BorderPane content = new BorderPane();

    private VBox progressVBox = new VBox(SchematicTransmission.PANE_SPACING);

    private Text totalProgressText = new Text("总进度");
    private Text totalDetailText = new Text("细节");
    private ProgressBar totalProgress = new ProgressBar();

    private Text currentProgressText = new Text("当前项");
    private Text currentDetailText = new Text("当前细节");
    private ProgressBar currentProgress = new ProgressBar();

    private Button upload = new Button("上传");
    private Button download = new Button("下载");
    private Button exit = new Button("退出");
    private Button clear = new Button("清屏");

    private MenuBar menuBar = new MenuBar();
    private Status status;

    public OperatorApplication() {
        user = new User("TextUser", "password");
    }

    public OperatorApplication(User user) {
        this.user = user;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initialize(primaryStage);
        stage.show();
    }

    private void initialize(Stage stage) {
        this.stage = stage;
        SchematicTransmission.operatorApplication = this;

        stage.getIcons().add(Icon.LOGO_ICON);
        stage.setHeight(SchematicTransmission.STAGE_HEIGHT);
        stage.setWidth(SchematicTransmission.STAGE_WIDTH);
        stage.setScene(scene);
        stage.setTitle(SchematicTransmission.SOFTWARE_NAME +" - " + user.getName());

        borderPane.setTop(menuBar);
        borderPane.setCenter(content);
        borderPane.setPadding(new Insets(0, 5, 5, 5));
        content.setPadding(new Insets(SchematicTransmission.STAGE_PADDING));

        HBox totalProgressHeader = new HBox(SchematicTransmission.PANE_SPACING);
        totalProgressHeader.getChildren().addAll(totalProgressText, totalDetailText);
        HBox currentProgressHeader = new HBox(SchematicTransmission.PANE_SPACING);
        currentProgressHeader.getChildren().addAll(currentProgressText, currentDetailText);
        progressVBox.getChildren().addAll(totalProgressHeader, totalProgress, currentProgressHeader, currentProgress);

        totalProgress.setPrefWidth(SchematicTransmission.STAGE_WIDTH - 2 * SchematicTransmission.STAGE_PADDING);
        currentProgress.setPrefWidth(totalProgress.getPrefWidth());

//        content.setTop(progressVBox);

        VBox eventTableVBox = new VBox(SchematicTransmission.PANE_SPACING);
        eventTableVBox.setPadding(new Insets(0, 0, 10, 0));
        eventTableVBox.getChildren().addAll(new Text("执行结束的事件"), eventTable);
        content.setCenter(eventTableVBox);

        HBox buttons = new HBox(SchematicTransmission.PANE_SPACING);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        upload.setDefaultButton(true);
        download.setDefaultButton(true);
        buttons.getChildren().addAll(upload, download, clear, exit);
        content.setBottom(buttons);

        status = SchematicTransmission.status;
        borderPane.setBottom(status);

        if (!SchematicTransmission.setInternetStatus(status)) {
            status.setWarning("可能无法连接至服务器，请检查网络");
        }

        setHandler();
    }

    public void setHandler() {
        upload.setOnAction(event -> {
            uploadFiles();
        });

        clear.setOnAction(event -> {
            eventTable.getItems().clear();
        });

        download.setOnAction(event -> {
            SchematicTransmission.showNoSuchFunctionDialog();
        });

        exit.setOnAction(event -> {
            shutdown();
        });

        stage.setOnCloseRequest(e -> {
            shutdown();
        });
    }

    public void uploadFiles() {
        Platform.runLater(new SendFiles(totalDetailText, totalProgress, currentDetailText, currentProgress, eventTable, status));
    }

    public void showProgress() {
        content.setTop(progressVBox);
    }

    public void hideProgress() {
        content.setTop(null);
    }

    public void shutdown() {
        close();
        System.exit(0);
    }

    public void close() {
        stage.close();
        SchematicTransmission.socket.sendCommand("close");
    }
}
