package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.Config;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Created by zheka on 17.08.2017.
 */

public class InnerCoreConfig {
    public static Config config;

    // TODO: FileTools.DIR_PACK + "assets/innercore/innercore_default_config.json"
    static {
        config = new Config(getConfigFile());
        try {
            String contents = FileTools
                    .readFileText(FileTools.DIR_PACK + "innercore_default_config.json");
            config.checkAndRestore(contents);
        } catch (JSONException | IOException e) {
            ICLog.e("CONFIG", "cannot load and validate default config.", e);
        }
    }

    public static void reload() {
        ICLog.d("CONFIG", "reloading inner core config");
        config.reload();
    }

    public static File getConfigFile() {
        return new File(FileTools.DIR_WORK, "config.json");
    }

    public static Object get(String name) {
        return config.get(name);
    }

    public static boolean getBool(String name) {
        if (name.equals("developer_mode"))
            return isDevelop();
        Object b = get(name);
        return b instanceof Boolean && (boolean) b;
    }

    public static int getInt(String name) {
        return ((Number) config.get(name)).intValue();
    }

    public static int getInt(String name, int fallback) {
        try {
            return getInt(name);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int convertThreadPriority(int val) {
        return 20 - Math.min(40, Math.max(1, val));
    }

    public static void set(String name, Object val) {
        config.set(name, val);
    }

    public static boolean isDevelop() {
        return Apparatus.isDevelop();
    }
}
