package com.taixue.schemtrans.stage;

import javafx.beans.property.SimpleStringProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
    private Date date = new Date();
    private SimpleStringProperty type = new SimpleStringProperty();
    private SimpleStringProperty detail = new SimpleStringProperty();
    private boolean result = true;

    public Event() {
    }

    public Event(String type, String detail, boolean result) {
        setType(type);
        setDetail(detail);
        this.result = result;
    }

    public void setDetail(String detail) {
        this.detail.set(detail);
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getDetail() {
        return detail.get();
    }

    public SimpleStringProperty detailProperty() {
        return detail;
    }

    public SimpleStringProperty dateProperty() {
        return new SimpleStringProperty(dateFormat.format(date));
    }

    public SimpleStringProperty resultProperty() {
        return new SimpleStringProperty(result ? "成功" : "失败");
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
