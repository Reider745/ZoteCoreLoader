package com.zhekasmirnov.innercore.mod.build;

/**
 * Created by zheka on 30.07.2017.
 */

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.mod.build.enums.AnalyzedModType;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.build.enums.ResourceDirType;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class ModBuilder {
    public static final String LOGGER_TAG = "INNERCORE-MOD-BUILD";

    public static BuildConfig loadBuildConfigForDir(String dir) {
        BuildConfig config = new BuildConfig(new File(dir + "/build.config"));
        config.read();
        return config;
    }

    public static void addGuiDir(String dir, BuildConfig.ResourceDir resourceDir) {
        String path = dir + resourceDir.path;
        if (!FileTools.exists(path)) {
            ICLog.d(LOGGER_TAG, "failed to import resource or ui dir " + resourceDir.path + ": it does not exist");
            return;
        }

        TextureSource.instance.loadDirectory(new File(path));
    }

    public static String checkRedirect(String dir) {
        File redirectFile = new File(dir + ".redirect");
        if (redirectFile.exists()) {
            try {
                return FileTools.readFileText(redirectFile.getAbsolutePath()).trim();
            } catch (IOException e) {
            }
        }
        return dir;
    }

    private static class LauncherScope extends ScriptableObject {
        Mod mod;

        public LauncherScope(Mod mod) {
            this.mod = mod;
        }

        @JSFunction
        public void Launch(ScriptableObject scope) {
            mod.RunMod(scope);
        }

        @JSFunction
        public void ConfigureMultiplayer(ScriptableObject props) {
            ScriptableObjectWrapper wrapper = new ScriptableObjectWrapper(props);
            mod.configureMultiplayer(wrapper.getString("name"), wrapper.getString("version"),
                    wrapper.getBoolean("isClientOnly") || wrapper.getBoolean("isClientSide"));
        }

        @Override
        public String getClassName() {
            return "LauncherAPI";
        }
    }

    private static void setupLauncherScript(Executable launcherScript, Mod mod) {
        LauncherScope scope = new LauncherScope(mod);
        scope.defineFunctionProperties(new String[] { "Launch", "ConfigureMultiplayer" }, scope.getClass(),
                ScriptableObject.DONTENUM);
        launcherScript.addToScope(scope);
    }

    private static Executable compileOrLoadExecutable(Mod mod, CompiledSources compiledSources,
            BuildConfig.Source source) throws IOException {
        CompilerConfig compilerConfig = source.getCompilerConfig();
        compilerConfig.setModName(mod.getName());

        if (mod.getBuildType() == BuildType.RELEASE) {
            Executable execFromDex = compiledSources.getCompiledExecutableFor(source.path, compilerConfig);
            if (execFromDex != null) {
                return execFromDex;
            } else {
                ICLog.d(LOGGER_TAG, "no multidex executable created for " + source.path);
            }
        }

        Reader sourceReader = new FileReader(new File(mod.dir + source.path));
        compilerConfig.setOptimizationLevel(-1);
        return Compiler.compileReader(sourceReader, compilerConfig);
    }

    /*
     * reads build config from dir and builds mod according it
     * builds all buildDirs
     * adds all resources
     * all sources are compiled, but not run
     * returns built mod or null, if config could not be loaded or parsed or
     * redirect failed
     */
    public static Mod buildModForDir(String dir, ModPack modPack, String locationName) {
        dir = checkRedirect(dir);
        if (!FileTools.exists(dir)) {
            ICLog.d(LOGGER_TAG,
                    "failed to load mod, dir does not exist, maybe redirect file is pointing to the missing dir "
                            + dir);
            return null;
        }

        // load config
        Mod builtMod = new Mod(dir);
        builtMod.setModPackAndLocation(modPack, locationName);
        builtMod.buildConfig = loadBuildConfigForDir(dir);
        if (!builtMod.buildConfig.isValid()) {
            return null;
        } else {
            builtMod.buildConfig.save();
        }

        ModDebugInfo debugInfo = builtMod.getDebugInfo();

        // build dirs
        if (builtMod.buildConfig.getBuildType() == BuildType.DEVELOP) {
            ArrayList<BuildConfig.BuildableDir> buildableDirs = builtMod.buildConfig.buildableDirs;
            for (int i = 0; i < buildableDirs.size(); i++) {
                BuildConfig.BuildableDir buildableDir = buildableDirs.get(i);
                BuildHelper.buildDir(dir, buildableDir);
            }
        }

        // load resources
        ArrayList<BuildConfig.ResourceDir> resourceDirs = builtMod.buildConfig.resourceDirs;
        for (int i = 0; i < resourceDirs.size(); i++) {
            BuildConfig.ResourceDir resourceDir = resourceDirs.get(i);
            if (resourceDir.resourceType == ResourceDirType.GUI) {
                addGuiDir(dir, resourceDir);
            }
        }

        // add minecraft packs only if mod is enabled
        Config modConfig = builtMod.getConfig();
        if (modConfig.getBool("enabled")) {
            // load minecraft resource packs
            String resourcePacksDir = builtMod.buildConfig.defaultConfig.resourcePacksDir;
            if (resourcePacksDir != null) {
                File resourcePacksDirFile = new File(dir, resourcePacksDir);
                if (resourcePacksDirFile.isDirectory()) {
                    for (File directory : resourcePacksDirFile.listFiles()) {
                        if (directory.isDirectory() && new File(directory, "manifest.json").isFile()) {
                            ModLoader.addMinecraftResourcePack(directory);
                        }
                    }
                }
            }

            // load minecraft behavior packs
            String behaviorPacksDir = builtMod.buildConfig.defaultConfig.behaviorPacksDir;
            if (behaviorPacksDir != null) {
                File behaviorPacksDirFile = new File(dir, behaviorPacksDir);
                if (behaviorPacksDirFile.isDirectory()) {
                    for (File directory : behaviorPacksDirFile.listFiles()) {
                        if (directory.isDirectory() && new File(directory, "manifest.json").isFile()) {
                            ModLoader.addMinecraftBehaviorPack(directory);
                        }
                    }
                }
            }
        }

        // compile sources
        CompiledSources compiledSources = builtMod.createCompiledSources();

        ArrayList<BuildConfig.Source> sourcesToCompile = builtMod.buildConfig.getAllSourcesToCompile(true);
        for (int i = 0; i < sourcesToCompile.size(); i++) {
            BuildConfig.Source source = sourcesToCompile.get(i);
            if (!source.gameVersion.isCompatible()) {
                continue;
            }
            if (source.apiInstance == null) {
                String msg = "could not find api for " + source.path
                        + ", maybe it is missing in build.config or name is incorrect, compilation failed.";
                ICLog.d(LOGGER_TAG, msg);
                debugInfo.putStatus(source.path, new IllegalArgumentException(msg));
                continue;
            }
            try {
                Executable compiledSource = compileOrLoadExecutable(builtMod, compiledSources, source);
                switch (source.sourceType) {
                    case PRELOADER:
                        builtMod.compiledPreloaderScripts.add(compiledSource);
                        break;
                    case LAUNCHER:
                        builtMod.compiledLauncherScripts.add(compiledSource);
                        setupLauncherScript(compiledSource, builtMod);
                        break;
                    case LIBRARY:
                        builtMod.compiledLibs.add(compiledSource);
                        break;
                    case MOD:
                        builtMod.compiledModSources.add(compiledSource);
                        break;
                    case CUSTOM:
                        builtMod.compiledCustomSources.put(source.path, compiledSource);
                        break;
                }
                builtMod.onImportExecutable(compiledSource);
                debugInfo.putStatus(source.path, compiledSource);
            } catch (Exception e) {
                ICLog.e(LOGGER_TAG, "failed to compile source " + source.path + ":", e);
                debugInfo.putStatus(source.path, e);
            }
        }

        return builtMod;
    }

    public static AnalyzedModType analyzeModDir(String dir) {
        dir = checkRedirect(dir);

        if (FileTools.exists(dir + "/build.config")) {
            return AnalyzedModType.INNER_CORE_MOD;
        }

        if (FileTools.exists(dir + "/main.js") && FileTools.exists(dir + "/launcher.js")
                && FileTools.exists(dir + "/config.json") && FileTools.exists(dir + "/resources.json")) {
            // return AnalyzedModType.CORE_ENGINE_MOD;
        }

        return AnalyzedModType.UNKNOWN;
    }

    public static boolean analyzeAndSetupModDir(String dir) {
        dir = checkRedirect(dir);

        AnalyzedModType analysisResult = analyzeModDir(dir);

        switch (analysisResult) {
            case UNKNOWN:
            case MODPE_MOD_ARRAY:
                return false;
            default:
                return true;
        }
    }
}
