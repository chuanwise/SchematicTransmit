package com.taixue.schemtrans.Progress;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.taixue.schemtrans.stage.Event;
import com.taixue.schemtrans.utility.Icon;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import com.taixue.schemtrans.node.Status;
import com.taixue.schemtrans.SchematicTransmission;
import com.taixue.schemtrans.stage.EventTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SendFiles implements Runnable{
    private Text totalDetailText, currentDetailText;
    private ProgressBar totalProgress, currentProgress;
    private Status status;
    private EventTable eventTable;
    private Stage stage;

    public SendFiles(Text totalDetailText, ProgressBar totalProgress, Text currentDetailText, ProgressBar currentProgress, EventTable exentTable, Status status) {
        this.currentDetailText = currentDetailText;
        this.currentProgress = currentProgress;
        this.totalDetailText = totalDetailText;
        this.totalProgress = totalProgress;
        this.status = status;
        this.eventTable = exentTable;
    }

    @Override
    public void run() {
        uploadFile(new Stage());
    }

    public void uploadFile(Stage stage) {
        List<File> files = showUploadDialog(stage);
        if (files == null) {
            return;
        }
        showProgress();
        ArrayList<Event> res = new ArrayList<>();
        List<Pair<File, String>> result = sendFiles(files, res);
        eventTable.getItems().addAll(res);

        if (result.size() > 0) {
            if (result.size() == files.size()) {
                status.setError("全部文件传送失败（共 " + files.size() + " 个文件）");
            }
            else {
                status.setError("部分文件传送失败 " + result.size() + " 个文件（共 " + files.size() + " 个文件）");
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误报告");
            alert.setHeaderText(status.getText() + "\n成功率：" + (100 * (files.size() - result.size()) / ((double) files.size())) + "%");
            VBox vBox = new VBox(SchematicTransmission.STAGE_PADDING);
            alert.getDialogPane().setPadding(new Insets(SchematicTransmission.ERROR_PADDING));
            vBox.setPrefHeight(SchematicTransmission.ERROR_HEIGHT - 100);
            vBox.setPrefWidth(SchematicTransmission.ERROR_WIDTH);

            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(Icon.ERROR_ICON);

            TableView<Pair<File, String>> failReasonTable = new TableView<>();
            TableColumn<Pair<File, String>, String> filePathColumn = new TableColumn<>("路径");
            TableColumn<Pair<File, String>, String> fileNameColumn = new TableColumn<>("文件名");
            TableColumn<Pair<File, String>, String> reasonColumn = new TableColumn<>("失败原因");

            failReasonTable.getColumns().addAll(reasonColumn, fileNameColumn, filePathColumn);
            fileNameColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getKey().getName()));
            filePathColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getKey().getAbsolutePath()));
            reasonColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getValue()));

            vBox.getChildren().addAll(failReasonTable);

            failReasonTable.getItems().addAll(result);

            alert.getDialogPane().setContent(vBox);
            alert.showAndWait();
        }
        else {
            status.setSuccess("传送成功 " + files.size() + " 个文件");
        }
        hideProgress();
    }

    public void showProgress() {
        SchematicTransmission.operatorApplication.showProgress();
    }

    public void hideProgress() {
        SchematicTransmission.operatorApplication.hideProgress();
    }

    public static List<File> showUploadDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("1.14 版本之前的 SCHEMATIC 文件", "*.schematic"),
                new FileChooser.ExtensionFilter("1.14 版本及之后的 SCHEM 文件", "*.schem")
        );

        return fileChooser.showOpenMultipleDialog(new Stage());
    }

    public ArrayList<Pair<File, String>> sendFiles(List<File> files,
                                                   ArrayList<Event> events) {
        if (files == null) {
            return null;
        }
        ArrayList<Pair<File, String>> result = new ArrayList<>();
        Pair<Boolean, String> current;

        int sentFile = 0;
        int totalFile = files.size();
        totalDetailText.setText("正在发送 " + files.size() + " 个文件");
        Event currentEvent;

        for (File file : files) {
//            updateProgress(sentFile, files.size());
            sentFile++;

            current = SchematicTransmission.socket.sendFile(file);
            currentDetailText.setText("正在发送 " + file.getAbsolutePath());

            currentEvent = new Event();
            currentEvent.setType("上传文件");
            currentEvent.setResult(current.getKey());
            currentEvent.setDetail((current.getValue() == null ? "" : current.getValue()) + "（" + file.getAbsolutePath() +"）");
            events.add(currentEvent);

            if (!current.getKey()) {
                result.add(new Pair<>(file, current.getValue()));
            }
            totalProgress.setProgress(((double) sentFile) / files.size());
        }
        return result;
    }
}
