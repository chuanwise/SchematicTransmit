package com.taixue.schemtrans.node;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Status extends Text {
    public Status() {
    }

    public Status(String str) {
        setNormal(str);
    }

    public void set(String str) {
        setText(str);
    }

    public void setError(String str) {
        setFill(Color.RED);
        set(str);
    }

    public void setNormal(String str) {
        setFill(Color.BLACK);
        set(str);
    }

    public void setSuccess(String str) {
        setFill(Color.GREEN);
        set(str);
    }

    public void setWarning(String str) {
        setFill(Color.DARKORANGE);
        set(str);
    }
}
