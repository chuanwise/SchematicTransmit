package com.taixue.schemtrans.utility;

import javafx.scene.image.Image;

import java.io.File;

public class Icon {
    private Icon() {}

    public static final String ICON_FILE_NAME = "icon/";

    public static final String LOGO_ICON_FILE_NAME = ICON_FILE_NAME + "taixue_logo.png";
    public static final String LOGIN_IN_ICON_FILE_NAME = ICON_FILE_NAME + "login_in.png";
    public static final Image LOGIN_IN_ICON = new Image(LOGIN_IN_ICON_FILE_NAME);
    public static final Image LOGO_ICON = new Image(LOGO_ICON_FILE_NAME);

    public static final String ERROR = ICON_FILE_NAME + "error.png";
    public static final Image ERROR_ICON = new Image(ERROR);

    public static final String SETTING_FILE_NAME = ICON_FILE_NAME + "setting.png";
    public static final Image SETTING_ICON = new Image(SETTING_FILE_NAME);

    public static final String HEADER_FILE_NAME = ICON_FILE_NAME + "header_picture.png";
    public static final File HEADER_FILE = new File(HEADER_FILE_NAME);

    public static final Image HEADER_ICON = new Image(HEADER_FILE_NAME);
}
