package com.zhekasmirnov.horizon.modloader.java;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ZoteClassLoaderPool;
import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JavaLibrary {
    private final JavaDirectory directory;
    private final List<File> dexFiles;
    private boolean initialized;

    public JavaLibrary(JavaDirectory directory, File dexFile) {
        this.initialized = false;
        this.directory = directory;
        this.dexFiles = new ArrayList<>(1);
        this.dexFiles.add(dexFile);
    }

    public JavaLibrary(JavaDirectory directory, List<File> dexFiles) {
        this.initialized = false;
        this.directory = directory;
        this.dexFiles = dexFiles;
    }

    public JavaDirectory getDirectory() {
        return this.directory;
    }

    public List<File> getDexFiles() {
        return this.dexFiles;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    private static final List<URL> urls = new ArrayList<>();
    private static URLClassLoader classLoader;

    public void initialize() {
        for (File dexFile : this.dexFiles) {
            try {
                final String filePath = dexFile.getAbsolutePath();
                urls.add(new File(
                        (filePath.endsWith(".dex") ? filePath.substring(0, filePath.length() - 4) : filePath) + ".jar").toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        
        for (File dexFile : this.dexFiles)
            ClassLoaderPatch.addDexPath(null, dexFile, true);
    }

    public void postInitialize() {
        if (classLoader == null) {
            classLoader = new URLClassLoader(urls.toArray(new URL[0]), InnerCoreServer.class.getClassLoader());
            ZoteClassLoaderPool.addClassLoader(classLoader);
        }

        HashMap<String, Object> data = new HashMap<>();
        for (String name : this.directory.getBootClassNames()) {
            try {
                Class<?> clazz = classLoader.loadClass(name);
                Method method = clazz.getMethod("boot", HashMap.class);
                data.put("class_name", name);
                method.invoke(null, new HashMap<>(data));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("failed to find boot class " + name + " in " + this.directory, e);
            } catch (IllegalAccessException e2) {
                throw new RuntimeException("failed to access boot method class " + name + " of " + this.directory, e2);
            } catch (NoSuchMethodException e3) {
                throw new RuntimeException(
                        "failed to find boot(HashMap) method in boot class " + name + " of " + this.directory, e3);
            } catch (InvocationTargetException e4) {
                throw new RuntimeException("failed to call boot method in class " + name + " of " + this.directory, e4);
            }
        }
        this.initialized = true;
    }
}
