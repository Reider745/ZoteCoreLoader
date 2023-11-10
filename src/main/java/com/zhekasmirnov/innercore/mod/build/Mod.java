package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.apparatus.minecraft.enums.EnumsJsInjector;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.mod.executable.library.Library;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 30.07.2017.
 */

public class Mod {
    public String dir;
    public BuildConfig buildConfig;

    private ModDebugInfo debugInfo = new ModDebugInfo();

    public ArrayList<Executable> compiledLibs = new ArrayList<>();
    public ArrayList<Executable> compiledModSources = new ArrayList<>();
    public ArrayList<Executable> compiledLauncherScripts = new ArrayList<>();
    public ArrayList<Executable> compiledPreloaderScripts = new ArrayList<>();

    public HashMap<String, Executable> compiledCustomSources = new HashMap<>();

    private boolean isConfiguredForMultiplayer = false;
    private boolean isClientOnly = false;
    private String multiplayerName = null;
    private String multiplayerVersion = null;


    private ModPack modPack;
    private String modPackLocationName;

    public void setModPackAndLocation(ModPack modPack, String modPackLocationName) {
        this.modPack = modPack;
        this.modPackLocationName = modPackLocationName;
    }

    public ModPack getModPack() {
        return modPack;
    }

    public String getModPackLocationName() {
        return modPackLocationName;
    }


    private void importConfigIfNeeded() {
        if (config == null) {
            config = new Config(modPack.getRequestHandler(ModPackDirectory.DirectoryType.CONFIG).get(modPackLocationName, "config.json"));
            try {
                config.checkAndRestore("{\"enabled\":true}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isEnabled = (boolean) config.get("enabled");
    }

    public Config getConfig() {
        importConfigIfNeeded();
        return config;
    }

    /* dir must end with / */
    public Mod(String dir) {
        this.dir = dir;
    }

    public CompiledSources createCompiledSources() {
        return new CompiledSources(new File(dir, ".dex"));
    }

    public ModDebugInfo getDebugInfo() {
        return debugInfo;
    }

    public void onImportExecutable(Executable exec) {
        importConfigIfNeeded();

        ScriptableObject additionalScope = ScriptableObjectHelper.createEmpty();
        additionalScope.put("__version__", additionalScope, MinecraftVersions.getCurrent().getCode());
        additionalScope.put("__mod__", additionalScope, this);
        additionalScope.put("__name__", additionalScope, getName());
        additionalScope.put("__dir__", additionalScope, dir);
        additionalScope.put("__config__", additionalScope, Context.javaToJS(config, exec.getScope()));
        additionalScope.put("__debug_typecheck__", additionalScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                ICLog.d("DEBUG", "checked object type: obj=" + objects[0] + " class=" + objects[0].getClass());
                return "" + objects[0].getClass();
            }
        });

        new EnumsJsInjector(additionalScope, true).injectAllEnumScopes("E");

        additionalScope.put("runCustomSource", additionalScope, new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable parent, Scriptable current, Object[] params) {
                String path = (String) params[0];
                ScriptableObject additionalScope = null;
                if (params.length > 1 && params[1] instanceof ScriptableObject) {
                    additionalScope = (ScriptableObject) params[1];
                }
                runCustomSource(path, additionalScope);
                return null;
            }
        });

        exec.addToScope(additionalScope);
        exec.setParentMod(this);
        if (exec instanceof Library) {
            LibraryRegistry.addLibrary((Library) exec);
        }
    }

    public Config config;
    public boolean isEnabled = false;

    /* must be called only if build config is parsed successfully */
    public void onImport() {
        isEnabled = true;
        importConfigIfNeeded();

        if (isEnabled) {
            API apiInstance = buildConfig.getDefaultAPI();
            if (apiInstance != null) {
                apiInstance.onModLoaded(this);
            }
        }

        loadModInfo();
    }

    public void loadModInfo(){
        if (FileTools.exists(this.dir + "mod.info")) {
            try {
                modInfoJson = FileTools.readJSON(this.dir + "mod.info");
            } catch (IOException | JSONException e) {
                Logger.error(e.getMessage());
            }
        }else
            Logger.error("Not mod.info");
    }

    public BuildType getBuildType() {
        return buildConfig.getBuildType();
    }

    public void setBuildType(BuildType buildType) {
        buildConfig.defaultConfig.setBuildType(buildType);
        buildConfig.save();
    }

    public void setBuildType(String strType) {
        setBuildType(BuildType.fromString(strType));
    }



    // ui helper methods

    private static int guiIconCounter = 0;
    private String guiIconName = "missing_mod_icon";

    private JSONObject modInfoJson = null;

    public String getGuiIcon() {
        return guiIconName;
    }

    public String getName() {
        Object infoName = getInfoProperty("name");
        return infoName != null ? infoName.toString() : buildConfig.getName();
    }

    public String getVersion() {
        Object version = getInfoProperty("version");
        return version != null ? version.toString() : buildConfig.getName();
    }

    public boolean isClientOnly() {
        return isClientOnly;
    }

    public boolean isConfiguredForMultiplayer() {
        return isConfiguredForMultiplayer;
    }

    public String getMultiplayerName() {
        return multiplayerName != null ? multiplayerName : getName();
    }

    public String getMultiplayerVersion() {
        return multiplayerVersion != null ? multiplayerVersion : getVersion();
    }

    public String getFormattedAPIName() {
        API apiInstance = buildConfig.defaultConfig.apiInstance;
        if (apiInstance != null) {
            return apiInstance.getCurrentAPIName();
        }
        return "???";
    }

    public Object getInfoProperty(String name) {
        if (modInfoJson == null)
            loadModInfo();

        return modInfoJson.opt(name);
    }

    public ArrayList<Executable> getAllExecutables() {
        ArrayList<Executable> all = new ArrayList<>();
        all.addAll(compiledModSources);
        all.addAll(compiledLibs);
        all.addAll(compiledLauncherScripts);
        all.addAll(compiledPreloaderScripts);
        return all;
    }



    // runtime

    private boolean isPreloaderRunning = false;
    public void RunPreloaderScripts() {
        if (!isEnabled) {
            return;
        }
        if (isPreloaderRunning) {
            throw new RuntimeException("mod " + this + " is already running preloader scripts.");
        }
        isPreloaderRunning = true;
        for (int i = 0; i < compiledPreloaderScripts.size(); i++) {
            Executable preloaderScript = compiledPreloaderScripts.get(i);
            preloaderScript.run();
        }
    }

    private boolean isLauncherRunning = false;
    public void RunLauncherScripts() {
        if (!isEnabled) {
            return;
        }
        if (isLauncherRunning) {
            throw new RuntimeException("mod " + this + " is already running launcher scripts.");
        }
        isLauncherRunning = true;
        for (int i = 0; i < compiledLauncherScripts.size(); i++) {
            Executable launcherScript = compiledLauncherScripts.get(i);
            launcherScript.run();
        }
    }

    public boolean isModRunning = false;
    public void RunMod(ScriptableObject additionalScope) {
        if (!isEnabled) {
            return;
        }
        if (isModRunning) {
            throw new RuntimeException("mod " + this + " is already running.");
        }

        // reset current cache group before launching mod
        //ItemModelCacheManager.getSingleton().setCurrentCacheGroup(null, null);

        isModRunning = true;
        for (int i = 0; i < compiledModSources.size(); i++) {
            Executable modSource = compiledModSources.get(i);
            modSource.addToScope(additionalScope);
            modSource.run();
        }

        // ... and after
       // ItemModelCacheManager.getSingleton().setCurrentCacheGroup(null, null);
    }

    public void configureMultiplayer(String name, String version, boolean isClientOnly) {
        multiplayerName = name;
        if (multiplayerName == null || multiplayerName.equals("auto")) {
            multiplayerName = getName();
        }
        multiplayerVersion = version;
        if (multiplayerVersion == null || multiplayerVersion.equals("auto")) {
            multiplayerVersion = getVersion();
        }
        isConfiguredForMultiplayer = true;
        this.isClientOnly = isClientOnly;
    }

    public void runCustomSource(String name, ScriptableObject additionalScope) {
        if (compiledCustomSources.containsKey(name)) {
            Executable exec = compiledCustomSources.get(name);
            if (additionalScope != null) {
                exec.addToScope(additionalScope);
            }
            exec.reset();
            exec.run();
        }
    }
}
