package com.zhekasmirnov.horizon.util;

import org.json.JSONObject;

import java.io.File;

public class FileUtils {
    public static JSONObject readJSON(File file){
        return new JSONObject(file);
    }

    public static String readFileText(File path){
        return "";
    }

    public static void writeJSON(File file, JSONObject jsonObject){

    }

    public static boolean getFileFlag(File directory, String flag){
        return false;
    }

    public static void setFileFlag(File directory, String flag, boolean value){

    }
}
