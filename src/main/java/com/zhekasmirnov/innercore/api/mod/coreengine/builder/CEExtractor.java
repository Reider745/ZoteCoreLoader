package com.zhekasmirnov.innercore.api.mod.coreengine.builder;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zheka on 24.08.2017.
 */

public class CEExtractor {
    // if set to true, core-engine.dev.js is used as core en
    private final static boolean CE_DEBUG = true;
    private final static boolean COMPILE = false;

    private static boolean isExtracted = false;

    private static final String PATH_IN_ASSETS = "innercore/coreengine/";
    private static final String DIR_CORE_ENGINE = FileTools.DIR_WORK + "coreengine/";

    private static boolean unpackAsset(String name, String dir) {
        dir = DIR_CORE_ENGINE + dir;
        FileTools.assureFileDir(new File(dir));
        try {
            FileTools.unpackAsset(PATH_IN_ASSETS + name, dir);
            return true;
        } catch (IOException e) {
            ICLog.e("COREENGINE", "unpacking core engine file failed name=" + name, e);
            return false;
        }
    }

    private static boolean unpackAsset(String name) {
        return unpackAsset(name, name);
    }

    private static void prepareExtraction() {
        FileTools.assureDir(DIR_CORE_ENGINE);
    }

    private static boolean tryToCompile() {
        long start = System.currentTimeMillis();
        ICLog.i("CORE-ENGINE", "starting compilation of Core Engine");
        try {
            Compiler.compileScriptToFile(new FileReader(new File(DIR_CORE_ENGINE + "core-engine.dev.js")), "core-engine", DIR_CORE_ENGINE + "core-engine.script");
        } catch (IOException e) {
            ICLog.e("CORE-ENGINE", "compilation failed", e);
            return false;
        }
        long end = System.currentTimeMillis();
        ICLog.i("CORE-ENGINE", "successfully compiled in " + (end - start) + " ms");
        return true;
    }

    private static boolean tryReleaseBuild() {
        return unpackAsset("core-engine.script");
    }

    private static boolean isExtractionSucceeded = false;

    public static void extractIfNeeded() {
        if (!isExtracted) {
            isExtracted = true;

            prepareExtraction();

            boolean success = false;
            if (CE_DEBUG) {
                if(COMPILE){
                    success = tryToCompile();
                } else {
                    success = true;
                }
            }
            else {
                success = tryReleaseBuild();
            }

            isExtractionSucceeded = success;
        }
    }

    public static boolean isCompiledExecutable() {
        return !CE_DEBUG || COMPILE;
    }

    public static File getExecutableFile() {
        if (isExtractionSucceeded) {
            if (CE_DEBUG && !COMPILE) {
                return new File(DIR_CORE_ENGINE, "core-engine.dev.js");
            }
            else {
                return new File(DIR_CORE_ENGINE, "core-engine.script");
            }
        }
        return null;
    }

    public static boolean isExtracted() {
        return isExtracted;
    }
}
