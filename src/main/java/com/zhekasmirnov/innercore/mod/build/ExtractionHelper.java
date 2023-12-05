package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.IMessageReceiver;

import com.zhekasmirnov.innercore.modpack.ModPackDirectory;

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
            Logger.debug("DEBUG", "searching: " + name);

            if (name.endsWith(searchFor)) {
                return name.substring(0, name.length() - searchFor.length());
            }
        }

        return null;
    }

    private static ArrayList<String> extractionLocationList = new ArrayList<>();

    private static String getFreeLocation(String defaultLocation) {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler handler = modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS);

        File directory = handler.get(defaultLocation);
        int suffix = 0;
        while (directory.exists()) {
            directory = handler.get(defaultLocation + "-" + (++suffix));
        }
        return suffix == 0 ? defaultLocation : defaultLocation + "-" + suffix;
    }

    private static File getInstallationPath(String locationName) {
        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        return modPack.getRequestHandler(ModPackDirectory.DirectoryType.MODS).get(locationName);
    }

    static String extractAs(final ZipFile modArchiveFile, String subPath, String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0 || dirName.indexOf('\\') != -1 || dirName.indexOf('/') != -1) {
            throw new IllegalArgumentException("invalid directory name passed to the method extractAs: '" + dirName
                    + "', it must be not empty and must not contain '\\' or '/' symbols");
        }

        String path = getInstallationPath(dirName).getAbsolutePath();

        byte[] buffer = new byte[1024];

        Enumeration<? extends ZipEntry> entries = modArchiveFile.entries();
        while (true) {
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

    static void extractEntry(ZipFile modArchiveFile, final String subPath, String entryName, String target)
            throws IOException {
        ZipEntry entry = modArchiveFile.getEntry(subPath + entryName);
        if (entry == null) {
            throw new IllegalArgumentException(
                    "entry " + subPath + entryName + " does not exist for file " + modArchiveFile);
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

    static void runSetupScript(final ZipFile modArchiveFile, final String subPath, final File setupScriptFile,
            final String defaultDir, final IMessageReceiver logger) throws Exception {
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
                    logger.message("extracting mod to <mods>/" + dir);
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
                    logger.message("extracting mod entry " + args[0] + " to <mods>/" + args[1]);
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
                logger.message(output.toString());
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

    private static ArrayList<String> extractionPathList = new ArrayList<>();
    private static String lastLocation;

    public static synchronized ArrayList<String> extractICModFile(File file, IMessageReceiver logger,
            Runnable readyToInstallCallback) {
        synchronized (ExtractionHelper.class) {
            logger.message("preparing to install " + file.getName());
            extractionPathList.clear();
            try {
                try {
                    ZipFile modArchiveFile = new ZipFile(file, Charset.forName("UTF-8"));
                    String subPath = searchForSubPath(modArchiveFile, "build.config");
                    if (subPath == null) {
                        logger.message("mod archive has incorrect structure: build.config file was not found anywhere");
                        return null;
                    }
                    logger.message("mod installation dir was found at path '/" + subPath + "'");
                    String[][] filesToExtract = { new String[] { "cfg", "build.config" },
                            new String[] { "icon", "mod_icon.png" }, new String[] { "info", "mod.info" } };
                    logger.message("extracting installation files");
                    int length = filesToExtract.length;
                    int offset = 0;
                    while (offset < length) {
                        String[] fileRelocation = filesToExtract[offset];
                        File tmp = new File(TEMP_DIR, fileRelocation[0]);
                        if (tmp.exists()) {
                            tmp.delete();
                        }
                        try {
                            extractEntry(modArchiveFile, subPath, fileRelocation[1], TEMP_DIR + fileRelocation[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        offset++;
                    }
                    BuildConfig buildConfig = new BuildConfig(new File(TEMP_DIR, "cfg"));
                    if (!buildConfig.read()) {
                        logger.message(
                                "build config cannot be loaded correctly, it failed to extract or was corrupted");
                        return null;
                    }
                    logger.message("we are ready to install");
                    if (readyToInstallCallback != null) {
                        readyToInstallCallback.run();
                    }
                    String setupScriptDir = buildConfig.defaultConfig.setupScriptDir;
                    String expectedDefaultDirectory;
                    if (subPath.length() > 0) {
                        int slashIndex = Math.max(subPath.indexOf(47), subPath.indexOf(92));
                        expectedDefaultDirectory = subPath.substring(0,
                                slashIndex != -1 ? slashIndex : subPath.length());
                    } else {
                        String archiveName = file.getName();
                        if (archiveName.endsWith(".icmod")) {
                            expectedDefaultDirectory = archiveName.substring(0, archiveName.length() - 6);
                        } else {
                            expectedDefaultDirectory = archiveName;
                        }
                    }
                    String defaultDirectory = getFreeLocation(expectedDefaultDirectory);
                    logger.message("installing mod (default directory name is '" + defaultDirectory
                            + "', but it probably will change).");
                    if (setupScriptDir != null) {
                        try {
                            extractEntry(modArchiveFile, subPath, setupScriptDir, TEMP_DIR + "setup");
                            logger.message("running setup script");
                            runSetupScript(modArchiveFile, subPath, new File(TEMP_DIR, "setup"), defaultDirectory,
                                    logger);
                            lastLocation = defaultDirectory;
                            return extractionPathList;
                        } catch (Exception e2) {
                            logger.message("failed to extract setup script: " + e2);
                            return null;
                        }
                    }
                    try {
                        logger.message("extracting mod to ...mods/" + defaultDirectory);
                        extractAs(modArchiveFile, subPath, defaultDirectory);
                        lastLocation = defaultDirectory;
                        return extractionPathList;
                    } catch (IOException extractExc) {
                        logger.message("failed to extract mod archive: " + extractExc);
                        return null;
                    }
                } catch (ZipException corruptExc) {
                    logger.message("mod archive is corrupt: " + corruptExc);
                    corruptExc.printStackTrace();
                    return null;
                }
            } catch (IOException accessExc) {
                logger.message("io exception occurred: " + accessExc);
                accessExc.printStackTrace();
                return null;
            }
        }
    }

    public static String getLastLocation() {
        return lastLocation;
    }
}
