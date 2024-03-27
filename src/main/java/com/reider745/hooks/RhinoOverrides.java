package com.reider745.hooks;

import cn.nukkit.utils.MainLogger;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

@Hooks(className = "org.mozilla.javascript.JavaMembers")
public class RhinoOverrides implements HookClass {
    private static final Logger logger = LogManager.getLogger(MainLogger.class);

    @Override
    public void init(CtClass ctClass) {
        try {
            CtMethod reflectMethod = ctClass.getDeclaredMethod("reflect");
            reflectMethod.insertAt(reflectMethod.getMethodInfo().getLineNumber(76),
                    "method = org.mozilla.javascript.MembersPatch.override(cl, method);");
        } catch (NotFoundException | CannotCompileException e) {
            System.out.println("Rhino has been updated and overrides for `JavaMembers.reflect` method are no longer available.");
        }
    }

    @Inject(className = "org.mozilla.javascript.ScriptRuntime")
    public static String[] getTopPackageNames() {
        return new String[] { "java", "javax", "org", "com", "edu", "net", "android", "cn"};
    }

    @Inject(className = "org.mozilla.javascript.ScriptRuntime", type = TypeHook.BEFORE_REPLACE)
    public static void warnAboutNonJSObject(Object nonJSObject) {
        if (InnerCoreServer.isDeveloperMode()) {
            logger.debug("Missed Context.javaToJS() conversion: " + nonJSObject);
        }
    }

    @Inject(className = "cn.nukkit.utils.MainLogger", type = TypeHook.BEFORE_REPLACE)
    public static void warning(MainLogger nukkitLogger, String message) {
        if (message == null || !message.startsWith("Ignoring InnerCorePacket from ")) {
            logger.warn(message);
        }
    }
}
