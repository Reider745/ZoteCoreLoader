package com.reider745.hooks;

import cn.nukkit.utils.MainLogger;
import com.reider745.api.ReflectHelper;
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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptRuntime;

import java.util.ArrayList;

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

    public static class ClassLoaderPool extends ClassLoader {
        private static final ArrayList<ClassLoader> loaders = new ArrayList<>();

        public ClassLoaderPool(ClassLoader parent) {
            super(parent);
        }

        public static void addClassLoader(ClassLoader loader){
            loaders.add(loader);
        }

        public static Class<?> forName(String name) throws ClassNotFoundException {
            try{
                return Context.getCurrentContext().getApplicationClassLoader().loadClass(name);
            }catch (Exception ignore){}
            return Class.forName(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            for (ClassLoader loader : loaders) {
                try {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException ignored) {}
            }
            return super.findClass(name);
        }
    }

    public static Class<?> classOrNull(ClassLoader loader, String className) {
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException ex) {
        } catch (IllegalArgumentException e) {
            // Can be thrown if name has characters that a class name
            // can not contain
        }
        return null;
    }

    static boolean testIfCanLoadRhinoClasses(ClassLoader loader) {
        Class<?> testClass = ScriptRuntime.ContextFactoryClass;
        Class<?> x = classOrNull(loader, testClass.getName());
        if (x != testClass) {
            // The check covers the case when x == null =>
            // loader does not know about testClass or the case
            // when x != null && x != testClass =>
            // loader loads a class unrelated to testClass
            return false;
        }
        return true;
    }

    @Inject(className = "org.mozilla.javascript.Context")
    public static ClassLoader getApplicationClassLoader(Context self) throws ClassNotFoundException {
        ClassLoader applicationClassLoader = ReflectHelper.getField(self, "applicationClassLoader");
        if (applicationClassLoader == null) {
            ContextFactory f = self.getFactory();
            ClassLoader loader = f.getApplicationClassLoader();
            if (loader == null) {
                ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
                if (threadLoader != null && testIfCanLoadRhinoClasses(threadLoader)) {
                    // Thread.getContextClassLoader is not cached since
                    // its caching prevents it from GC which may lead to
                    // a memory leak and hides updates to
                    // Thread.getContextClassLoader

                    return new ClassLoaderPool(threadLoader);
                }

                // Thread.getContextClassLoader can not load Rhino classes,
                // try to use the loader of ContextFactory or Context
                // subclasses.
                Class<?> fClass = f.getClass();
                if (fClass != ScriptRuntime.ContextFactoryClass) {
                    loader = fClass.getClassLoader();
                } else {
                    loader = self.getClass().getClassLoader();
                }
            }
            applicationClassLoader = new ClassLoaderPool(loader);
            ReflectHelper.setField(self, "applicationClassLoader", applicationClassLoader);
        }
        return applicationClassLoader;
    }
}
