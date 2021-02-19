package com.taixue.schemtrans.stage;

import com.taixue.schemtrans.SchematicTransmission;
import com.taixue.schemtrans.utility.ErrorDialog;
import com.taixue.schemtrans.utility.SocketController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DownloadApplication extends Application {
    private Stage stage;

    private VBox fileList = new VBox(SchematicTransmission.PANE_SPACING);
    private ScrollPane fileListPane = new ScrollPane(fileList);
    private VBox vBox = new VBox(SchematicTransmission.PANE_SPACING);

    private Button fresh = new Button("刷新列表");
    private Button download = new Button("下载选中的文件");

    private int chooseCounter = 0;
    private Text counter = new Text();
    private Text counterHeader = new Text();

    private void initialize(Stage stage) {
        this.stage = stage;
        stage.setWidth(SchematicTransmission.STAGE_WIDTH * 0.75);
        stage.setHeight(SchematicTransmission.STAGE_HEIGHT * 0.75);
        stage.setResizable(false);

        stage.setScene(new Scene(vBox));
        vBox.setPadding(new Insets(SchematicTransmission.STAGE_PADDING));

        Text header = new Text("下载文件");
        header.setFont(Font.font(null, 30));
        Text comment = new Text("这是你有权限下载的文件列表。请勾选本次需要下载的文件名，然后点击下载。");
        vBox.getChildren().addAll(header, comment);

        HBox counterHBox = new HBox(SchematicTransmission.PANE_SPACING);
        counterHBox.getChildren().addAll(counterHeader, counter);
        vBox.getChildren().add(counterHBox);

        vBox.getChildren().add(fileListPane);

        HBox buttonsLine = new HBox(SchematicTransmission.PANE_SPACING);
        buttonsLine.getChildren().addAll(fresh, download);
        buttonsLine.setAlignment(Pos.CENTER_RIGHT);
        vBox.getChildren().add(buttonsLine);

        freshFileList();
        setHandler();
    }

    private void setHandler() {
        fresh.setOnAction(event -> freshFileList());

        download.setOnAction(event -> downloadChooseFile());
    }

    private void downloadChooseFile() {
        ArrayList<String> chooseFile = new ArrayList<>();
        for (Node node: fileList.getChildren()) {
            if (node instanceof CheckBox) {
                if (((CheckBox) node).isSelected()) {
                    chooseFile.add(((CheckBox) node).getText());
                }
            }
            else {
                SchematicTransmission.showExceptionDialogAndExit(new ClassCastException("在下载选择菜单的 Children 列表中意外地发现非 CheckBox 的对象"));
            }
        }
        if (chooseFile.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("至少选择一个文件！");
            alert.setContentText("若无下载文件需求，请关闭下载窗口");
            alert.showAndWait();
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("设置存储路径");
        File dir = directoryChooser.showDialog(new Stage());

        ErrorDialog errorDialog = new ErrorDialog();
        File currentFile;
        for (String fileName: chooseFile) {
            currentFile = new File(fileName);
            try {
                SchematicTransmission.socket.sendCommand("download");
                SchematicTransmission.socket.sendObject(fileName);

                if (SchematicTransmission.socket.receiveBoolean()) {
                    Pair<Boolean, String> result = SchematicTransmission.socket.receiveFile(dir);
                    if (!result.getKey()) {
                        errorDialog.getItems().add(new Pair<>(currentFile, result.getValue()));
                    }
                }
                else {
                    errorDialog.getItems().add(new Pair<>(currentFile, "权限不足"));
                }
            }
            catch (IOException ioException) {
                errorDialog.getItems().add(new Pair<>(currentFile, "出现异常：" + ioException));
            }
        }
    }

    private void freshFileList() {
        fileList.getChildren().clear();
        try {
            SchematicTransmission.socket.sendCommand("get_current_schem_list");
            ArrayList<String> fileNames = ((ArrayList<String>) SchematicTransmission.socket.receiveObject());
            for (String fileName: fileNames) {
                CheckBox checkBox = new CheckBox(fileName);
                fileList.getChildren().add(checkBox);

                checkBox.setOnAction(event -> {
                    if (checkBox.isSelected()) {
                        chooseCounter++;
                    }
                    else {
                        chooseCounter--;
                    }
                    freshCounter();
                });
            }
            counterHeader.setText("总共 " + fileNames.size() + " 个文件，");
            chooseCounter = 0;
            freshCounter();
        }
        catch (Exception exception) {
            SchematicTransmission.showExceptionDialog(exception);
        }
    }

    private void freshCounter() {
        counter.setText("已选中 " + chooseCounter + " 个文件");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initialize(primaryStage);
    }
}
