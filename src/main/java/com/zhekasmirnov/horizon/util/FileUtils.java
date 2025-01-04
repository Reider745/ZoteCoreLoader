package com.zhekasmirnov.horizon.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static void clearFileTree(File var0, boolean var1) {
        if (var0.exists()) {
            if (var0.isDirectory()) {
                File[] var4 = var0.listFiles();
                int var3 = var4.length;

                for (int var2 = 0; var2 < var3; ++var2) {
                    clearFileTree(var4[var2], true);
                }
            }

            if (var1) {
                var0.delete();
            }
        }
    }

    public static String comment(String input) {
        return input.replaceAll("//.*", "")
                .replaceAll("/\\*(.|\\R)*?\\*/", "");
    }

    public static JSONObject readJSON(File file) throws JSONException, IOException {
        return new JSONObject(comment(readFileText(file)));
    }

    public static String readFileText(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString();
        }
    }

    public static void writeFileText(File file, String text) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)))) {
            writer.write(text);
        }
    }

    public static void writeJSON(File file, JSONObject jsonObject) throws IOException {
        String result = jsonObject.toString();
        FileUtils.writeFileText(file, result);
    }

    public static void writeJSON(File file, JSONArray jsonObject) throws IOException {
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

    private static void getAllRelativePaths(File file, File directoryBase, List<String> result,
            boolean addDirectories) {
        if (addDirectories || file.isFile()) {
            result.add(
                    FileUtils.cleanupPath(file.getAbsolutePath().substring(directoryBase.getAbsolutePath().length())));
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                FileUtils.getAllRelativePaths(child, directoryBase, result, addDirectories);
            }
        }
    }

    public static List<String> getAllRelativePaths(File directory, boolean addDirectories) {
        ArrayList<String> result = new ArrayList<>();
        FileUtils.getAllRelativePaths(directory, directory, result, addDirectories);
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
                } catch (IOException e) {
                    throw new RuntimeException("failed to set flag: " + (Object) flag, (Throwable) e);
                }
            }
        } else {
            flag.delete();
        }
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inputStream = new FileInputStream(src);
        FileOutputStream outputStream = new FileOutputStream(dst);

        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        WritableByteChannel outputChannel = Channels.newChannel(outputStream);

        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (inputChannel.read(buffer) != -1) {
            buffer.flip();
            outputChannel.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            outputChannel.write(buffer);
        }
    }


    public static void copyFileTree(File srcDir, File dstDir, Object dialog, String prefix) throws IOException {
        if (!dstDir.isDirectory() && !dstDir.mkdirs()) {
            throw new IOException("mkdirs failed: " + dstDir);
        }
        for (String name : srcDir.list()) {
            if (dialog != null) {
                return;
            }
            File srcFile = new File(srcDir, name);
            File dstFile = new File(dstDir, name);
            if (srcFile.isDirectory()) {
                copyFileTree(srcFile, dstFile, dialog, prefix + name + "/");
            } else {
                copy(srcFile, dstFile);
            }
        }
    }
}
