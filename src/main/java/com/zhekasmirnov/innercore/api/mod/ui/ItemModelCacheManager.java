package com.zhekasmirnov.innercore.api.mod.ui;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.IOException;

public class ItemModelCacheManager {
    private static final ItemModelCacheManager singleton = new ItemModelCacheManager();

    public static ItemModelCacheManager getSingleton() {
        return singleton;
    }

    private final File cacheRoot = new File(FileTools.DIR_WORK, "cache/item-models-new");

    private String currentCacheGroup = null;

    private void assureCacheDirectory(File directory, String lock) {
        if (directory.isFile()) {
            directory.delete();
        }
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (lock != null) {
            File lockFile = new File(directory, ".lock");
            String storedLock = null;
            try {
                storedLock = FileUtils.readFileText(lockFile).trim();
            } catch (Exception ignore) {
            }

            if (!lock.equals(storedLock)) {
                ICLog.d("ItemModelCache", "cleaning up item model cache directory " + directory.getName()
                        + ", new lock = " + lock + ", stored lock = " + storedLock);
                FileUtils.clearFileTree(directory, false);
                try {
                    FileUtils.writeFileText(lockFile, lock);
                } catch (Exception ignore) {
                }
            }
        }
        File noMediaFlag = new File(directory, ".nomedia");
        if (!noMediaFlag.exists()) {
            try {
                noMediaFlag.createNewFile();
            } catch (IOException ignore) {
            }
        }
    }

    private ItemModelCacheManager() {
    }

    public File getCacheGroupDirectory(String group) {
        return new File(cacheRoot, group);
    }

    public File getCachePath(String group, String name) {
        if (name != null) {
            name = name.replace(':', '#');
        }
        if (group != null && group.length() > 0) {
            File directory = getCacheGroupDirectory(group);
            if (!directory.exists()) {
                assureCacheDirectory(directory, null);
            }
            return new File(new File(cacheRoot, group), name);
        } else {
            return new File(cacheRoot, name);
        }
    }

    public String getCurrentCacheGroup() {
        return currentCacheGroup;
    }

    public void setCurrentCacheGroup(String currentCacheGroup, String lock) {
        this.currentCacheGroup = currentCacheGroup;
    }
}
