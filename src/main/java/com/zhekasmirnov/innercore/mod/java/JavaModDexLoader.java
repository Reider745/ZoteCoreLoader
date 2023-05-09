package com.zhekasmirnov.innercore.mod.java;

//import com.faendir.rhino_android.AndroidContextFactory;
import com.zhekasmirnov.innercore.api.log.ICLog;
//import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by zheka on 25.01.2018.
 */

public class JavaModDexLoader {
    public ArrayList<Class> loadDexFile(File file) {
//        DexFile dexFile;
//        try {
//            dexFile = new DexFile(file);
//        } catch (IOException e) {
//            ICLog.i("ERROR", "failed to load java class from dex " + e);
//            return null;
//        }
//
//        Enumeration<String> entries = dexFile.entries();
//        ArrayList<Class> classes = new ArrayList<>();
//
//        while(entries.hasMoreElements()) {
//            String name = entries.nextElement();
//            if (name != null) {
//                Class clazz = loadClassFromDex(dexFile, name);
//                if (clazz != null) {
//                    classes.add(clazz);
//                }
//            }
//        }
//
//        return classes;
        return null;
    }

    /*private Class loadClassFromDex(DexFile dex, String name) {
        Class clazz = dex.loadClass(name, AndroidContextFactory.class.getClassLoader());
        return clazz;
    }*/
}
