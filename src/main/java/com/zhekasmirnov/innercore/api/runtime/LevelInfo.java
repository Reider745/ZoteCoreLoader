package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;

/**
 * Created by zheka on 20.08.2017.
 */

public class LevelInfo {
    public static final int STATE_OFFLINE = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_IN_WORLD = 2;

    public static String worldsPathOverride = null;

    public static int state = STATE_OFFLINE;
    public static String levelName, levelDir;

    public static int getState() {
        return state;
    }

    public static boolean isOnline() {
        return getState() != STATE_OFFLINE;
    }

    public static boolean isLoaded() {
        return getState() == STATE_IN_WORLD;
    }

    public static String getLevelName() {
        return isOnline() ? levelName : null;
    }

    public static String getLevelDir() {
        return isOnline() ? levelDir : null;
    }

    public static String getAbsoluteDir() {
        if (levelDir == null) {
            return null;
        }
        if (isOnline()) {
            if (worldsPathOverride != null) {
                String path = new File(worldsPathOverride, levelDir).getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                return path;
            } else {
                return FileTools.DIR_ROOT + "games/horizon/minecraftWorlds/" + levelDir + "/";
            }
        }
        return null;
    }

    public static void onEnter(String name, String dir) {
        state = STATE_LOADING;
        levelName = name;
        levelDir = dir;
    }

    public static void onLoaded() {
        state = STATE_IN_WORLD;
    }

    public static void onLeft() {
        state = STATE_OFFLINE;
    }
}
