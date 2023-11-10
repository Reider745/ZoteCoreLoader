package com.zhekasmirnov.innercore.mod.build;

import android.util.Log;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;
import com.zhekasmirnov.innercore.utils.FileTools;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;

import org.mineprogramming.horizon.innercore.model.ModTracker;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by zheka on 22.12.2017.
 */

public class ExtractionHelper {
    public static final String TEMP_DIR;

    static {
        TEMP_DIR = FileTools.DIR_WORK + "temp/extract/";
        FileTools.assureDir(TEMP_DIR);
    }

    static String searchForSubPath(ZipFile modArchiveFile, String searchFor) {
        Enumeration<? extends ZipEntry> entries = modArchiveFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry == null) {
                break;
            }
            String name = entry.getName();
            // Logger.debug("DEBUG", "searching: " + name);

            if (name.endsWith(searchFor)) {
                return name.substring(0, name.length() - searchFor.length());
            }
        }

        return null;
    }

    private static ArrayList<String> extractionLocationList = new ArrayList<>();


    private static String getFreeLocation(String defaultLocation){
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler handler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);

        File directory = handler.get(defaultLocation);
        int suffix = 0;
        while(directory.exists()){
            directory = handler.get(defaultLocation + "-" + (++suffix));
        }
        return suffix == 0? defaultLocation: defaultLocation + "-" + suffix;
    }


    private static File getInstallationPath(String locationName){
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        return modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS).get(locationName);
    }

    
    static String extractAs(final ZipFile modArchiveFile, String subPath, String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0 || dirName.indexOf('\\') != -1 || dirName.indexOf('/') != -1) {
            throw new IllegalArgumentException("invalid directory name passed to the method extractAs: '" + dirName + "', it must be not empty and must not contain '\\' or '/' symbols");
        }

        String path = getInstallationPath(dirName).getAbsolutePath();

        byte[] buffer = new byte[1024];

        Enumeration<? extends ZipEntry> entries = modArchiveFile.entries();
        while (true){
            ZipEntry entry;
            try {
                entry = entries.nextElement();
                if (entry == null) {
                    break;
                }
            } catch (NoSuchElementException e) {
                break;
            }

            String name = entry.getName();
            if (name.startsWith(subPath) && !name.contains(".setup/")) {
                name = name.substring(subPath.length());
                if (!entry.isDirectory()) {
                    File out = new File(path, name);
                    FileTools.assureFileDir(out);

                    int count;
                    InputStream inStream = modArchiveFile.getInputStream(entry);
                    FileOutputStream outStream = new FileOutputStream(out);

                    while ((count = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, count);
                    }

                    outStream.close();
                    inStream.close();
                }
            }
        }

        extractionLocationList.add(dirName);
        return path;
    }

    static void extractEntry(ZipFile modArchiveFile, final String subPath, String entryName, String target) throws IOException {
        ZipEntry entry = modArchiveFile.getEntry(subPath + entryName);
        if (entry == null) {
            throw new IllegalArgumentException("entry " + subPath + entryName + " does not exist for file " + modArchiveFile);
        }

        FileTools.assureFileDir(new File(target));

       Logger.debug("DEBUG", "started entry extraction " + subPath + entryName);
        int count;
        byte[] buffer = new byte[1024];
        InputStream inStream = modArchiveFile.getInputStream(entry);
        FileOutputStream outStream = new FileOutputStream(target);

        while ((count = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, count);
        }

        outStream.close();
        inStream.close();
    }

    static void runSetupScript(final ZipFile modArchiveFile, final String subPath, final File setupScriptFile, final String defaultDir) throws Exception {
        FileReader reader = new FileReader(setupScriptFile);
        CompilerConfig config = new CompilerConfig(null);
        Executable setupScript = Compiler.compileReader(reader, config);

        ScriptableObject scope = setupScript.getScope();
        scope.put("extractAs", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                String name = args.length > 0 ? (String) args[0] : null;
                try {
                    String dir = name != null ? name : defaultDir;
                    Logger.debug("extracting mod to <mods>/" + dir);
                    return extractAs(modArchiveFile, subPath, dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        scope.put("unpack", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] args) {
                try {
                    extractEntry(modArchiveFile, subPath, String.valueOf(args[0]), String.valueOf(args[1]));
                    Logger.debug("extracting mod entry " + args[0] + " to <mods>/" + args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        scope.put("log", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                StringBuilder output = new StringBuilder();
                for (Object obj : objects) {
                    output.append(obj).append(" ");
                }
                Logger.debug(output.toString());
                return null;
            }
        });

        scope.put("print", scope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                return null;
            }
        });

        scope.put("__modsdir__", scope, FileTools.DIR_WORK + "mods/");
        scope.put("__subpath__", scope, subPath);
        scope.put("__defdirname__", scope, defaultDir);

        setupScript.run();

        Throwable throwable = setupScript.getLastRunException();
        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }


    private static String lastLocation;


    public static String getLastLocation(){
        return lastLocation;
    }
}
