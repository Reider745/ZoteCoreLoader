package com.zhekasmirnov.innercore.api.mod.ui.memory;

import android.graphics.Bitmap;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by zheka on 31.08.2017.
 */

public class BitmapCache {
    public static final String CACHE_DIR = FileTools.DIR_WORK + "cache/bmp/";

    static {
        FileTools.mkdirs(CACHE_DIR);

        try {
            File[] files = (new File(CACHE_DIR)).listFiles();
            for (File file : files) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private static ArrayList<BitmapWrap> registeredWraps = new ArrayList<>();
    static void registerWrap(BitmapWrap wrap) {
        registeredWraps.add(wrap);
    }
//
//    private static HashMap<String, BitmapWrap> wrapMap = new HashMap<>();
//    static BitmapWrap getForNameOrRegister(String name, BitmapWrap wrap) {
//        BitmapWrap mapped = wrapMap.get(name);
//        if (mapped != null) {
//            return mapped;
//        }
//        wrapMap.put(name, wrap);
//        return wrap;
//    }

    //
    static void unregisterWrap(BitmapWrap wrap) {
        registeredWraps.remove(wrap);
    }



    static void writeToFile(File file, Bitmap bitmap) throws IOException {
        try {
            int size = bitmap.getRowBytes() * bitmap.getHeight();
            ByteBuffer buffer = ByteBuffer.allocateDirect(size);
            bitmap.copyPixelsToBuffer(buffer);

            FileChannel channel = new FileOutputStream(file, false).getChannel();
            buffer.rewind();
            channel.write(buffer);
            channel.close();
        } catch (NullPointerException e) {
            throw new IOException("failed to write bitmap: " + e);
        }
    }

    static void readFromFile(File file, Bitmap bitmap) throws IOException {
        try {
            RandomAccessFile rFile = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[(int) rFile.length()];
            rFile.readFully(bytes);

            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes).rewind();
            bitmap.copyPixelsFromBuffer(buffer);
        } catch (NullPointerException e) {
            throw new IOException("failed to read bitmap: " + e);
        }
    }

    public static Bitmap testCaching(Bitmap src) {
        File f = getCacheFile(src.toString());
        try {
            writeToFile(f, src);
        } catch (IOException e) {
            e.printStackTrace();
            return src;
        }
        Bitmap dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        try {
            readFromFile(f, dst);
        } catch (IOException e) {
            e.printStackTrace();
            return src;
        }
        return dst;
    }



    static synchronized void storeOldWraps(int maxStackPos) {
        try {
            for (BitmapWrap wrap : new ArrayList<BitmapWrap>(registeredWraps)) {
                if (wrap != null && wrap.getStackPos() > maxStackPos) {
                    wrap.storeIfNeeded();
                }
            }
        } catch (ConcurrentModificationException e) {
            ICLog.i("UI", "GC failed and will be restarted: " + e);
            storeOldWraps(maxStackPos);
        }
    }

    private static final int DEFAULT_MAX_STACK_POS = 64;

    public static void immediateGC() {
        // long time = System.currentTimeMillis();
        storeOldWraps(DEFAULT_MAX_STACK_POS);
        // Logger.info("UI", "immediate GC took " + (System.currentTimeMillis() - time) + " ms");
    }

    private static boolean isGCRunning = false;
    public static void asyncGC() {
        if (!isGCRunning) {
            isGCRunning = true;

            (new Thread(new Runnable() {
                @Override
                public void run() {
                    long time = System.currentTimeMillis();
                    storeOldWraps(DEFAULT_MAX_STACK_POS);
                    Logger.info("UI", "async GC took " + (System.currentTimeMillis() - time) + " ms");
                    isGCRunning = false;
                }
            })).start();
        }
    }
}
