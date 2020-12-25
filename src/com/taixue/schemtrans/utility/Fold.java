package com.taixue.schemtrans.utility;

import java.io.File;

public class Fold {
    public static String SETTING_FILE_NAME = "setting.dat";
    public static File SETTING_FILE = new File(SETTING_FILE_NAME);

    public static String newVersionDirName = "update";
    public static File newVersionDir = new File(newVersionDirName);
    
    private Fold() {}
}
