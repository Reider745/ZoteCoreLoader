package com.zhekasmirnov.horizon.modloader.java;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class JavaLibrary {
    private final List<File> dexFiles;
    private final JavaDirectory directory;
    private boolean initialized = false;

    public JavaLibrary(JavaDirectory var1, File var2) {
        this.directory = var1;
        ArrayList var3 = new ArrayList(1);
        this.dexFiles = var3;
        var3.add(var2);
    }

    public JavaLibrary(JavaDirectory var1, List<File> var2) {
        this.directory = var1;
        this.dexFiles = var2;
    }

    public List<File> getDexFiles() {
        return this.dexFiles;
    }

    public JavaDirectory getDirectory() {
        return this.directory;
    }

    public void initialize() {
        Iterator var1 = this.dexFiles.iterator();

        while(var1.hasNext()) {
            File var2 = (File)var1.next();
            //ClassLoaderPatch.addDexPath(JavaLibrary.class.getClassLoader(), var2);
        }

        HashMap var4 = new HashMap();
        Iterator var11 = this.directory.getBootClassNames().iterator();

        while(var11.hasNext()) {
            String var10 = (String)var11.next();

            StringBuilder var3;
            StringBuilder var12;
            try {
                Method var5 = Class.forName(var10).getMethod("boot", HashMap.class);
                var4.put("class_name", var10);
                HashMap var13 = new HashMap(var4);
                var5.invoke((Object)null, var13);
            } catch (NoSuchMethodException var6) {
                var12 = new StringBuilder();
                var12.append("failed to find boot(HashMap) method in boot class ");
                var12.append(var10);
                var12.append(" of ");
                var12.append(this.directory);
                throw new RuntimeException(var12.toString(), var6);
            } catch (IllegalAccessException var7) {
                var12 = new StringBuilder();
                var12.append("failed to access boot method class ");
                var12.append(var10);
                var12.append(" of ");
                var12.append(this.directory);
                throw new RuntimeException(var12.toString(), var7);
            } catch (InvocationTargetException var8) {
                var3 = new StringBuilder();
                var3.append("failed to call boot method in class ");
                var3.append(var10);
                var3.append(" of ");
                var3.append(this.directory);
                throw new RuntimeException(var3.toString(), var8);
            } catch (ClassNotFoundException var9) {
                var3 = new StringBuilder();
                var3.append("failed to find boot class ");
                var3.append(var10);
                var3.append(" in ");
                var3.append(this.directory);
                throw new RuntimeException(var3.toString(), var9);
            }
        }

        this.initialized = true;
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}
