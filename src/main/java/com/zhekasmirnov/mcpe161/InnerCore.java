package com.zhekasmirnov.mcpe161;

import android.app.Activity;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.horizon.modloader.JavaEnvironment;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InnerCore {
    public static final String LOGGER_TAG = "INNERCORE-LAUNHER";
    public static final boolean isLicenceVersion = false;

    private static InnerCore instance;
    private static JavaEnvironment javaEnvironment;

    public static InnerCore getInstance() {
        return instance;
    }

    public /* Pack */ Object getPack() {
        InnerCoreServer.useNotSupport("InnerCore.getPack()");
        return null;
    }

    public static List<File> getJavaDirectoriesFromProxy() {
        return javaDirectoriesFromProxy;
    }

    @Deprecated
    public InnerCore(Activity context, /* Pack */ Object pack) {
        throw new UnsupportedOperationException("InnerCore(context, pack)");
    }

    public InnerCore(File packDirectory) {
        FileTools.initializeDirectories(packDirectory);
        instance = this;
        Logger.info("initializing innercore");
        if (javaEnvironment == null) {
            javaEnvironment = new JavaEnvironment();
        }
    }

    public static boolean checkLicence(Activity activity) {
        return !isLicenceVersion;
    }

    public void load() {
        ReflectionPatch.init();
        ColorsPatch.init();

        API.loadAllAPIs();
        initiateLoading();
    }

    public void setMinecraftActivity(Activity activity) {
        InnerCoreServer.useNotSupport("InnerCore.setMinecraftActivity(activity)");
    }

    public Activity getCurrentActivity() {
        return Activity.getSingletonInternalProxy();
    }

    private void initiateLoading() {
        Logger.debug("INNERCORE", String.format("Inner Core %s Started", PackInfo.getPackVersionName()));
        LoadingStage.setStage(LoadingStage.STAGE_START);
        preloadInnerCore();
    }

    public void build() {
        ArrayList<JavaDirectory> javaDirectories = new ArrayList<>();
        addJavaDirectories(javaDirectories);
        for (JavaDirectory javaDirectory : javaDirectories) {
            javaEnvironment.addDirectory(javaDirectory);
        }
        javaEnvironment.build();
    }

    public List<File> allResourceDirectories = new ArrayList<>();

    private void preloadInnerCore() {
        // select default modpack if none selected
        ModPackContext.getInstance().assurePackSelected();

        ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());

        LoadingUI.setTextAndProgressBar("Initializing Resources...", 0f);
        LoadingStage.setStage(LoadingStage.STAGE_RESOURCES_LOADING);

        // load
        ResourcePackManager.instance.initializeResources();
        LoadingUI.setTextAndProgressBar("Loading Mods...", 0.15f);
        ModLoader.initialize();
        ModLoader.loadModsAndSetupEnvViaNewModLoader(); // ModLoader.instance.loadMods();
        ModLoader.prepareResourcesViaNewModLoader(); // ModLoader.instance.runPreloaderScripts();
        ModLoader.addGlobalMinecraftPacks();
        ModLoader.instance.loadResourceAndBehaviorPacks();
        LoadingUI.setTextAndProgressBar("Generating Cache...", 0.4f);
        LoadingUI.setTextAndProgressBar("Starting Minecraft...", 0.5f);
    }

    public void addResourceDirectories(ArrayList<?> list) {
    }

    public void addNativeDirectories(ArrayList<LibraryDirectory> list) {
        for (File directory : nativeDirectoriesFromProxy) {
            list.add(new LibraryDirectory(directory));
        }
    }

    public void addJavaDirectories(ArrayList<JavaDirectory> list) {
        for (File directory : javaDirectoriesFromProxy) {
            list.add(new JavaDirectory(null, directory));
        }
    }

    public void onFinalLoadComplete() {
        LoadingStage.setStage(LoadingStage.STAGE_COMPLETE);
        LoadingUI.close();
        ICLog.showIfErrorsAreFound();
        LoadingStage.outputTimeMap();
    }

    public String getWorkingDirectory() {
        return FileTools.DIR_PACK;
    }

    public /* ResourceManager */ Object getResourceManager() {
        return null;
    }

    public /* TextureAtlas */ Object getBlockTextureAtlas() {
        return null;
    }

    public /* TextureAtlas */ Object getItemTextureAtlas() {
        return null;
    }

    // environment proxy for new code
    private static final List<File> javaDirectoriesFromProxy = new ArrayList<>();
    private static final List<File> nativeDirectoriesFromProxy = new ArrayList<>();
    private static final List<File> resourceDirectoriesFromProxy = new ArrayList<>();

    private static final EnvironmentSetupProxy environmentSetupProxy = new EnvironmentSetupProxy() {

        @Override
        public void addResourceDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                resourceDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addGuiAssetsDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                TextureSource.instance.loadDirectory(directory);
            }
        }

        @Override
        public void addNativeDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                nativeDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addJavaDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                javaDirectoriesFromProxy.add(directory);
            }
        }

        @Override
        public void addResourcePackDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                ModLoader.addMinecraftResourcePack(directory);
            }
        }

        @Override
        public void addBehaviorPackDirectory(ApparatusMod mod, File directory) {
            if (directory.isDirectory()) {
                ModLoader.addMinecraftBehaviorPack(directory);
            }
        }
    };

    public static EnvironmentSetupProxy getEnvironmentSetupProxy() {
        return environmentSetupProxy;
    }
}
