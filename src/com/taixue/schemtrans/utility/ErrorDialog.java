package com.taixue.schemtrans.utility;

import com.taixue.schemtrans.SchematicTransmission;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;

public class ErrorDialog extends Alert {
    private TableView<Pair<File, String>> failReasonTable = new TableView<>();
    public ErrorDialog() {
        super(AlertType.ERROR);
        initialize();
    }

    private void initialize() {
        setTitle("错误报告");
        setHeaderText("文件传输错误报告");
        VBox vBox = new VBox(SchematicTransmission.STAGE_PADDING);
        getDialogPane().setPadding(new Insets(SchematicTransmission.ERROR_PADDING));
        vBox.setPrefHeight(SchematicTransmission.ERROR_HEIGHT - 100);
        vBox.setPrefWidth(SchematicTransmission.ERROR_WIDTH);

        ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(Icon.ERROR_ICON);

        TableColumn<Pair<File, String>, String> filePathColumn = new TableColumn<>("路径");
        TableColumn<Pair<File, String>, String> fileNameColumn = new TableColumn<>("文件名");
        TableColumn<Pair<File, String>, String> reasonColumn = new TableColumn<>("失败原因");

        failReasonTable.getColumns().addAll(reasonColumn, fileNameColumn, filePathColumn);
        fileNameColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getKey().getName()));
        filePathColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getKey().getAbsolutePath()));
        reasonColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getValue()));

        vBox.getChildren().addAll(failReasonTable);
        getDialogPane().setContent(vBox);
    }

    public ObservableList<Pair<File, String>> getItems() {
        return failReasonTable.getItems();
    }
}
