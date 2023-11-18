package com.zhekasmirnov.horizon.util;

import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static JSONObject readJSON(File file){
        try{
            return new JSONObject(readFileText(file));
        }catch (Exception e){
            throw new RuntimeException(e.getCause().toString());
        }
    }

    public static String readFileText(File file) throws Exception {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder text = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            text.append(line).append("\n");
        }
        return text.toString();
    }

    public static void writeFileText(File file, String text) throws Exception {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
        writer.write(text);
        writer.close();
    }

    public static void writeJSON(File file, JSONObject jsonObject) throws Exception{
        String result = jsonObject.toString();
        FileUtils.writeFileText(file, result);
    }

    public static String cleanupPath(String path) {
        path = path.replaceAll("\\\\", "/");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private static void getAllRelativePaths(File file, File directoryBase, List<String> result, boolean addDirectories) {
        if (addDirectories || file.isFile()) {
            result.add(FileUtils.cleanupPath(file.getAbsolutePath().substring(directoryBase.getAbsolutePath().length())));
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                FileUtils.getAllRelativePaths(child, directoryBase, result, addDirectories);
            }
        }
    }

    public static List<String> getAllRelativePaths(File directory, boolean addDirectories) {
        ArrayList result = new ArrayList();
        FileUtils.getAllRelativePaths(directory, directory, (List<String>)result, addDirectories);
        return result;
    }


    public static boolean getFileFlag(File directory, String name) {
        return new File(directory, "." + name).exists();
    }

    public static void setFileFlag(File directory, String name, boolean exists) {
        File flag = new File(directory, "." + name);
        if (exists) {
            if (!flag.exists()) {
                try {
                    flag.createNewFile();
                }
                catch (IOException e) {
                    throw new RuntimeException("failed to set flag: " + (Object)flag, (Throwable)e);
                }
            }
        } else {
            flag.delete();
        }
    }
}
