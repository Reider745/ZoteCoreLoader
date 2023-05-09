package com.zhekasmirnov.mcpe161;

/*import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.apparatus.util.HorizonPackUtils;
import com.zhekasmirnov.horizon.launcher.pack.Pack;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.modloader.resource.ResourceManager;
import com.zhekasmirnov.horizon.modloader.resource.directory.ResourceDirectory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.horizon.util.JsonIterator;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.modpack.DirectorySetRequestHandler;
import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;
import com.zhekasmirnov.innercore.utils.UIUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;*/

public class InnerCore {
    /*public static final String LOGGER_TAG = "INNERCORE-LAUNHER";
    public static final boolean isLicenceVersion = true;

    private static InnerCore instance;

    private final Pack pack;

    private WeakReference<Activity> currentActivity;

    public static InnerCore getInstance() {
        return instance;
    }

    public Pack getPack() {
        return pack;
    }

    public static List<File> getJavaDirectoriesFromProxy() {
        return javaDirectoriesFromProxy;
    }

    public InnerCore(Activity context, Pack pack) {
        FileTools.initializeDirectories(pack.directory);

        instance = this;
        this.pack = pack;
        currentActivity = new WeakReference<>(context);
        
        Logger.info("initializing innercore");
    }

    public static boolean checkLicence(Activity activity){
        return !isLicenceVersion || isMCPEInstalled(activity);
    }

    public void load() {
        ReflectionPatch.init();
        ColorsPatch.init();
        
        API.loadAllAPIs();
        initiateLoading();
    }

    public void setMinecraftActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public Activity getCurrentActivity(){
        return currentActivity.get();
    }

    private static boolean isMCPEInstalled(Activity activity) {
        try {
            activity.getPackageManager().getPackageInfo("com.mojang.minecraftpe", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (NullPointerException e) {
            // check fails are rare, so just let in
            return true;
        }
    }

    private void initiateLoading() {
        Logger.debug("INNERCORE", String.format("Inner Core %s Started", PackInfo.getPackVersionName()));
        LoadingStage.setStage(LoadingStage.STAGE_START);

        // init
        // LoadingUI.initializeFor(current.get());
        // LoadingUI.open();

        preloadInnerCore();
    }


    public List<File> allResourceDirectories = new ArrayList<>();

    private void addAllResourcePacks() {
        File defaultResourcePacksDir = new File(pack.getWorkingDirectory(), "resourcepacks");
        if (!defaultResourcePacksDir.isDirectory()) {
            defaultResourcePacksDir.delete();
        }
        if (!defaultResourcePacksDir.isDirectory()) {
            defaultResourcePacksDir.mkdirs();
        }

        ModPack modPack = ModPackContext.getInstance().getCurrentModPack();
        DirectorySetRequestHandler texturePacks = modPack.getRequestHandler(ModPackDirectory.DirectoryType.TEXTURE_PACKS);

        List<String> allNames = new ArrayList<String>();
        List<String> names = new ArrayList<String>();

        JSONObject json = new JSONObject();
        try {
            json = FileUtils.readJSON(texturePacks.get("", "resourcepacks.json"));
        } catch(IOException ignore) {
        } catch(JSONException ignore) {
        }

        JSONArray packsJson = json.optJSONArray("packs");
        if (packsJson != null) {
            for (JSONObject resourcePackJson : new JsonIterator<JSONObject>(packsJson)) {
                if (resourcePackJson != null) {
                    String name = resourcePackJson.optString("name", null);
                    if (name != null && name.length() > 0 && !allNames.contains(name)) {
                        if (resourcePackJson.optBoolean("enabled", true)) {
                            names.add(name);
                        }
                        allNames.add(name);
                    }
                }
            }
        }

        for (String name : texturePacks.getAllLocations()) {
            if (!allNames.contains(name)) {
                names.add(name);
                allNames.add(name);
            }
        }

        for (String name : names) {
            File resourceDir = texturePacks.get(name);
            if (resourceDir.isDirectory()) {
                allResourceDirectories.add(resourceDir);
            }
        }
    }

    private void addAllModResources() {
        File defaultTextures = new File(pack.getWorkingDirectory(), "assets/textures/");
        allResourceDirectories.add(defaultTextures);

        allResourceDirectories.addAll(resourceDirectoriesFromProxy);
        *//*
        for(Mod mod: ModLoader.instance.modsList){
            for(ResourceDir resourceDir: mod.buildConfig.resourceDirs){
                File file = new File(mod.dir + resourceDir.path);
                if(file.exists() && file.isDirectory()){
                    allResourceDirectories.add(file);
                }
            }
        } *//*
    }

    private void preloadInnerCore() {
        // select default modpack if none selected
        ModPackContext.getInstance().assurePackSelected();

        ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());

        LoadingUI.setTextAndProgressBar("Initializing Resources...", 0f);
        try {
            Thread.sleep(500); // letting loading ui to draw
        } catch (InterruptedException e) {
        }
        LoadingStage.setStage(LoadingStage.STAGE_RESOURCES_LOADING);

        // init
        UIUtils.initialize(currentActivity.get());

        // load
        ResourcePackManager.instance.initializeResources();
        LoadingUI.setTextAndProgressBar("Loading Mods...", 0.15f);
        ModLoader.initialize();
        ModLoader.loadModsAndSetupEnvViaNewModLoader(); // ModLoader.instance.loadMods();
        addAllResourcePacks();
        addAllModResources();
        ModLoader.prepareResourcesViaNewModLoader(); // ModLoader.instance.runPreloaderScripts();
        ModLoader.addGlobalMinecraftPacks();
        ModLoader.instance.loadResourceAndBehaviorPacks();
        LoadingUI.setTextAndProgressBar("Generating Cache...", 0.4f);
        LoadingUI.setTextAndProgressBar("Starting Minecraft...", 0.5f);
    }


	public void addResourceDirectories(ArrayList<ResourceDirectory> list) {
        ResourceManager manager = getResourceManager();
        Logger.debug("addResourceDirectories", list.size() + " " + list.toString());
        for (File dir : allResourceDirectories) {
            list.add(new ResourceDirectory(manager, dir));
        }
        File additionalResDir = new File(FileTools.DIR_PACK, "assets/innercore/default_additional_resources");
        if (additionalResDir.isDirectory()) {
            list.add(new ResourceDirectory(manager, additionalResDir));
        }
    }
    
    public void addNativeDirectories(ArrayList<LibraryDirectory> list) {
        for (File directory : nativeDirectoriesFromProxy) {
            list.add(new LibraryDirectory(directory));
        }
        *//* for(Mod mod: ModLoader.instance.modsList){
            for(String nativeDir: mod.buildConfig.nativeDirectories){
                File file = new File(mod.dir + nativeDir);
                list.add(new LibraryDirectory(file));
            }
        } *//*
    }

    public void addJavaDirectories(ArrayList<JavaDirectory> list){
        for (File directory : javaDirectoriesFromProxy) {
            list.add(new JavaDirectory(null, directory));
        }
        *//* for(Mod mod: ModLoader.instance.modsList){
            for(String javaDir: mod.buildConfig.javaDirectories){
                File file = new File(mod.dir + javaDir);
                list.add(new JavaDirectory(null, file));
            }
        } *//*
    }
    
    public void onFinalLoadComplete(){
        LoadingStage.setStage(LoadingStage.STAGE_COMPLETE);
        LoadingUI.close();
        ICLog.showIfErrorsAreFound();
        LoadingStage.outputTimeMap();
    }

    public String getWorkingDirectory(){
        return pack.getWorkingDirectory().getAbsolutePath();
    }

    public ResourceManager getResourceManager(){
        return pack.getModContext().getResourceManager();
    }

    public TextureAtlas getBlockTextureAtlas(){
        return EnvironmentSetup.getBlockTextureAtlas();
    }

    public TextureAtlas getItemTextureAtlas(){
        return EnvironmentSetup.getItemTextureAtlas();
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
    }*/
}