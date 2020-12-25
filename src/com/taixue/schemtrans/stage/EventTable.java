package com.taixue.schemtrans.stage;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class EventTable extends TableView<Event> {
    private TableColumn<Event, String> dateColumn = new TableColumn<>("发生时间");
    private TableColumn<Event, String> typeColumn = new TableColumn<>("事件类型");
    private TableColumn<Event, String> resultColumn = new TableColumn<>("结果");
    private TableColumn<Event, String> detailColumn = new TableColumn<>("详情");

    public EventTable() {
        initialize();
    }

    private void initialize() {
        getColumns().addAll(dateColumn, typeColumn, resultColumn, detailColumn);

        dateColumn.setCellValueFactory(e -> e.getValue().dateProperty());
        typeColumn.setCellValueFactory(e -> e.getValue().typeProperty());
        resultColumn.setCellValueFactory(e -> e.getValue().resultProperty());
        detailColumn.setCellValueFactory(e -> e.getValue().detailProperty());
    }
}
