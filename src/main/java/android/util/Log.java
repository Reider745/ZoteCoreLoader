package android.util;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class Log {
    public static void e(String tag, String name){
        Logger.error(tag, name);
    }
}
