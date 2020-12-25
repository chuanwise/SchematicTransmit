package com.taixue.schemtrans.utility;

import java.io.Serializable;

public class User
        implements Serializable {

    private static final long serialVersionUID = -222467518489253591L;

    private String name;
    private transient String password;
    private String passwordHash;

    public User(String name, String password) {
        this.name = name;
        setPassword(password);
    }

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    private void setPassword(String password) {
        this.password = password;
        try {
            passwordHash = PasswordHash.createHash(password, name.getBytes());
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}