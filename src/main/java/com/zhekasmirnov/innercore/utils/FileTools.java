package com.zhekasmirnov.innercore.utils;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by zheka on 27.06.2017.
 */

public class FileTools {
    public static final String LOGGER_TAG = "INNERCORE-FILE";
    public static String DIR_ROOT, DIR_PACK, DIR_WORK, DIR_MINECRAFT, DIR_HORIZON;

    // static {
        // DIR_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        // DIR_HORIZON = DIR_ROOT + "games/horizon/";
        // DIR_MINECRAFT = MinecraftVersions.getCurrent().getMinecraftExternalStoragePath().getAbsolutePath() + "/";
    // }

    public static void init() {
        DIR_ROOT = DIR_HORIZON = DIR_PACK = DIR_MINECRAFT = InnerCoreServer.dataPath + "/";
        DIR_WORK = DIR_ROOT + "innercore/";
        checkdirs();
    }

    // Should be called before anything else
    public static void initializeDirectories(File packPath) {
        DIR_PACK = packPath.getAbsolutePath() + "/";
        DIR_WORK = DIR_PACK + "innercore/";
        checkdirs();
    }

    public static String assureAndGetCrashDir() {
        String path = DIR_WORK + "crash-dump/";
        assureDir(path);
        return path;
    }

    public static File unpackInputStream(InputStream inputStream, String path) throws IOException {
        File outputFile = new File(path);
        outputFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(outputFile);

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        outputStream.close();
        inputStream.close();

        return outputFile;
    }

    public static boolean assetExists(String name) {
        InputStream is = getAssetInputStream(name);
        if (is == null) {
            return false;
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static InputStream getAssetInputStream(String name) {
        return new ByteArrayInputStream(getAssetBytes(name));
    }

    public static byte[] getAssetBytes(String name) {
        try {
            return readFileText(DIR_WORK + "assets/" + name).getBytes();
        } catch (IOException e) {
            Logger.debug("Failed to find resource or it should not exist: " + name);
        }
        return new byte[0];
    }

    public static byte[] getAssetBytes(String name, String[] searchPaths, boolean includeAbsPath) {
        return getAssetBytes(name);
    }

    public static Bitmap bitmapFromBytes(byte[] bytes) {
        return bytes != null ? Bitmap.getSingletonInternalProxy() : null;
    }

    public static Bitmap getAssetAsBitmap(String name) {
        return Bitmap.getSingletonInternalProxy();
    }

    public static String[] listAssets(String dir) {
        return listDirectory(DIR_WORK + "assets/" + dir);
    }

    public static String getAssetAsString(String name) {
        return new String(getAssetBytes(name));
    }

    public static JSONObject getAssetAsJSON(String name) throws JSONException {
        return new JSONObject(getAssetAsString(name));
    }

    public static JSONArray getAssetAsJSONArray(String name) throws JSONException {
        return new JSONArray(getAssetAsString(name));
    }

    public static File unpackResource(int resource, String path) throws IOException {
        throw new IOException("FileTools.unpackResource");
    }

    public static File unpackAsset(String name, String path) throws IOException {
        File inputFile = new File(DIR_WORK + "assets/" + name);
        File outputFile = new File(path);

        if (inputFile.equals(outputFile)) {
            return outputFile;
        }

        outputFile.createNewFile();
        copy(inputFile, outputFile);
        return outputFile;
    }

    public static void unpackAssetDir(String name, String path) {
    }

    public static void checkdirs() {
        File dir = new File(DIR_WORK);

        if (!dir.exists()) {
            boolean succeeded = dir.mkdirs();
            if (succeeded) {
                Logger.debug(LOGGER_TAG, "created work directory: " + DIR_WORK);
            } else {
                Logger.debug(LOGGER_TAG, "failed to create work directory: " + DIR_WORK);
            }
        } else {
            Logger.debug(LOGGER_TAG, "work directory check successful");
        }
    }

    public static boolean exists(String path) {
        return (new File(path)).exists();
    }

    public static boolean mkdirs(String path) {
        return (new File(path)).mkdirs();
    }

    public static boolean assureDir(String path) {
        if (!exists(path))
            return mkdirs(path);
        return true;
    }

    public static boolean assureFileDir(File file) {
        String path = file.getAbsolutePath();
        String dir = path.substring(0, path.lastIndexOf(File.separatorChar));
        return assureDir(dir);
    }

    public static Typeface getMcTypeface() {
        return Typeface.getSingletonInternalProxy();
    }

    public static String[] listDirectory(String path) {
        File directory = new File(path);
        return directory.list();
    }

    public static String readFileText(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

        String text = "";
        String line;
        while ((line = reader.readLine()) != null) {
            text += line + "\n";
        }
        reader.close();
        return text;
    }

    public static void writeFileText(String path, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
        writer.write(text);
        writer.close();
    }

    public static void addFileText(String path, String text) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
        writer.write(text);
        writer.close();
    }

    public static Bitmap readFileAsBitmap(String path) {
        return Bitmap.getSingletonInternalProxy();
    }

    public static void writeBitmap(String path, Bitmap bmp) {
    }

    public static JSONObject readJSON(String path) throws IOException, JSONException {
        return new JSONObject(readFileText(path));
    }

    public static JSONArray readJSONArray(String path) throws IOException, JSONException {
        return new JSONArray(new JSONTokener(readFileText(path)));
    }

    public static void writeJSON(String path, JSONObject json) throws IOException {
        String result = json.toString();
        result = com.cedarsoftware.util.io.JsonWriter.formatJson(result);
        writeFileText(path, result);
    }

    public static void writeJSON(String path, JSONArray json) throws IOException {
        String result = json.toString();
        result = com.cedarsoftware.util.io.JsonWriter.formatJson(result);
        writeFileText(path, result);
    }

    public static String getPrettyPath(File dir, File fileInDir) {
        String dirPath = dir == null ? "" : dir.getAbsolutePath();
        String filePath = fileInDir == null ? "" : fileInDir.getAbsolutePath();
        return filePath.substring(dirPath.length() + 1);
    }

    public static void inStreamToOutStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        WritableByteChannel outputChannel = Channels.newChannel(outputStream);
        ByteBuffer buffer = ByteBuffer.allocateDirect(16384);

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

    public static byte[] convertStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toByteArray();
    }

    public static void delete(String path) {
        deleteRecursive(new File(path));
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursive(child);
            }
        }

        file.delete();
    }

    public static String convertStreamToString(InputStream inputStream) {
        try {
            int length;
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String readFileText(File file) throws IOException {
        String line;
        try (BufferedReader reader = new BufferedReader((Reader) new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            line = text.toString();
        }
        return line;
    }

    public static void writeFileText(File path, String text) throws IOException {
        try (PrintWriter writer = new PrintWriter((Writer) new BufferedWriter((Writer) new FileWriter(path, false)))) {
            writer.write(text);
        }
    }

    public static void addFileText(File path, String text) throws IOException {
        try (PrintWriter writer = new PrintWriter((Writer) new BufferedWriter((Writer) new FileWriter(path, true)))) {
            writer.write(text);
        }
    }

    public static JSONObject readJSON(File path) throws IOException, JSONException {
        return new JSONObject(readFileText(path));
    }

    public static JSONArray readJSONArray(File path) throws IOException, JSONException {
        return new JSONArray(new JSONTokener(readFileText(path)));
    }

    public static void writeJSON(File path, JSONObject json) throws IOException {
        writeFileText(path, json.toString());
    }

    public static void writeJSON(File path, JSONArray json) throws IOException {
        writeFileText(path, json.toString());
    }

    public static String cleanupPath(String path) {
        path = path.replaceAll("\\\\", "/");
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
