package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.IOException;

/**
 * Created by zheka on 31.08.2017.
 */

@Deprecated(since = "Zote")
public class BitmapCache {
    public static final String CACHE_DIR = FileTools.DIR_WORK + "cache/bmp/";

    public static void init() {
    }

    static File getCacheFile(String name) {
        return new File(CACHE_DIR, name);
    }

    private static int useId = 0;

    static int getUseId() {
        return useId++;
    }

    static int getStackPos(int id) {
        return useId - id;
    }

    static void registerWrap(BitmapWrap wrap) {
    }

    static void unregisterWrap(BitmapWrap wrap) {
    }

    static void writeToFile(File file, Bitmap bitmap) throws IOException {
    }

    static void readFromFile(File file, Bitmap bitmap) throws IOException {
    }

    public static Bitmap testCaching(Bitmap src) {
        return Bitmap.getSingletonInternalProxy();
    }

    static synchronized void storeOldWraps(int maxStackPos) {
    }

    public static void immediateGC() {
    }

    public static void asyncGC() {
    }
}
