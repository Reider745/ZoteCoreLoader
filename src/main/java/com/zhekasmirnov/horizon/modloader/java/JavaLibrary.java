package com.zhekasmirnov.horizon.modloader.java;

import com.zhekasmirnov.horizon.launcher.env.ClassLoaderPatch;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public void initialize() {
        for (File dexFile : this.dexFiles) {
            ClassLoaderPatch.addDexPath(JavaLibrary.class.getClassLoader(), dexFile, true);
        }
        HashMap<String, Object> data = new HashMap<>();
        for (String name : this.directory.getBootClassNames()) {
            try {
                Class<?> clazz = Class.forName(name);
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
