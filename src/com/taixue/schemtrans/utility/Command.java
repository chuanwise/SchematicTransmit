package com.taixue.schemtrans.utility;

import java.io.Serializable;

public class Command implements Serializable {
    public static final long serialVersionUID = 4915752022418340922L;
    private String type;
    private String detail;

    public Command(String type, String detail) {
        this.type = type;
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public boolean isType(String string) {
        return type.equals(string);
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return type + (detail == null ? "" : "：" + detail);
    }
}