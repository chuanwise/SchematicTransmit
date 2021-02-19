package com.taixue.schemtrans.Progress;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.taixue.schemtrans.stage.Event;
import com.taixue.schemtrans.utility.ErrorDialog;
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
    private Status status;
    private EventTable eventTable;

    public SendFiles(EventTable exentTable, Status status) {
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
            ErrorDialog errorDialog = new ErrorDialog();
            errorDialog.getItems().addAll(result);
            errorDialog.showAndWait();
        }
        else {
            status.setSuccess("传送成功 " + files.size() + " 个文件");
        }
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
        Event currentEvent;

        for (File file : files) {
//            updateProgress(sentFile, files.size());
            sentFile++;

            current = SchematicTransmission.socket.sendFile(file);

            currentEvent = new Event();
            currentEvent.setType("上传文件");
            currentEvent.setResult(current.getKey());
            currentEvent.setDetail((current.getValue() == null ? "" : current.getValue()) + "（" + file.getAbsolutePath() +"）");
            events.add(currentEvent);

            if (!current.getKey()) {
                result.add(new Pair<>(file, current.getValue()));
            }
        }
        return result;
    }
}
