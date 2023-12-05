package com.reider745.hooks;

import cn.nukkit.utils.MainLogger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

@Hooks
public class Other implements HookClass {
    private static final Logger logger = LogManager.getLogger(MainLogger.class);

    @Inject(class_name = "org.mozilla.javascript.ScriptRuntime")
    public static String[] getTopPackageNames() {
        return new String[] { "java", "javax", "org", "com", "edu", "net", "android" };
    }

    @Inject(class_name = "cn.nukkit.utils.MainLogger", type_hook = TypeHook.BEFORE_REPLACE)
    public static void warning(MainLogger self, String message) {
        if (message == null || !message.startsWith("Ignoring InnerCorePacket from ")) {
            logger.warn(message);
        }
    }
}
