package com.rokid.ai.sdkdemo.pickup;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Func:
 *
 * @author: liuweiming
 * @version: 1.0
 * Create Time: 2018/9/7
 */
public class SaveFile {

    private static String totalfilepath = Environment.getExternalStorageDirectory() + "/picktest/totalFile.pcm";
    private static String activationFilePath = Environment.getExternalStorageDirectory() + "/picktest/activationFile.pcm";
    private static String pickupFilePath = Environment.getExternalStorageDirectory() + "/picktest/pickupFile.pcm";

    public static void saveTotalFile(byte[] data) {
        try {

            File file = new File(totalfilepath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveActivationFile(byte[] data) {
        try {

            File file = new File(activationFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePickupFile(byte[] data) {
        try {

            File file = new File(pickupFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(data);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
