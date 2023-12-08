package com.zhekasmirnov.mcpe161;

import android.app.Activity;
import com.zhekasmirnov.apparatus.adapter.env.EnvironmentSetupProxy;
import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.library.LibraryDirectory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.ui.TextureSource;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.ColorsPatch;
import com.zhekasmirnov.innercore.utils.ReflectionPatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InnerCore {
    public static final String LOGGER_TAG = "INNERCORE-LAUNHER";
    public static final boolean isLicenceVersion = true;

    private static InnerCore instance = new InnerCore(null, null);

   // private final Pack pack;

   // private WeakReference<Activity> currentActivity;

    public static InnerCore getInstance() {
        return instance;
    }

   /* public Pack getPack() {
        return pack;
    }*/

    public static List<File> getJavaDirectoriesFromProxy() {
        return javaDirectoriesFromProxy;
    }

    public InnerCore(Activity context, Object pack) {

        instance = this;
        //this.pack = pack;
        //currentActivity = new WeakReference<>(context);
        
        //Logger.info("initializing innercore");
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

   /* public void setMinecraftActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public Activity getCurrentActivity(){
        return currentActivity.get();
    }*/

    private static boolean isMCPEInstalled(Activity activity) {
        return true;
        /*try {
            activity.getPackageManager().getPackageInfo("com.mojang.minecraftpe", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (NullPointerException e) {
            // check fails are rare, so just let in
            return true;
        }*/
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
        /*File defaultResourcePacksDir = new File(pack.getWorkingDirectory(), "resourcepacks");
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
        }*/
    }

    private void addAllModResources() {
        /*File defaultTextures = new File(pack.getWorkingDirectory(), "assets/textures/");
        allResourceDirectories.add(defaultTextures);

        allResourceDirectories.addAll(resourceDirectoriesFromProxy);*/
    }

    private void preloadInnerCore() {
        // select default modpack if none selected
        ModPackContext.getInstance().assurePackSelected();


        LoadingUI.setTextAndProgressBar("Initializing Resources...", 0f);
        try {
            Thread.sleep(500); // letting loading ui to draw
        } catch (InterruptedException e) {
        }
        LoadingStage.setStage(LoadingStage.STAGE_RESOURCES_LOADING);


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


    /*public void addNativeDirectories(ArrayList<LibraryDirectory> list) {
        for (File directory : nativeDirectoriesFromProxy) {
            list.add(new LibraryDirectory(directory));
        }
    }*/

    public void addJavaDirectories(ArrayList<JavaDirectory> list){
        for (File directory : javaDirectoriesFromProxy) {
            list.add(new JavaDirectory(null, directory));
        }
    }
    
    public void onFinalLoadComplete(){
        LoadingStage.setStage(LoadingStage.STAGE_COMPLETE);
        LoadingUI.close();
        LoadingStage.outputTimeMap();
    }

    /*public String getWorkingDirectory(){
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
    }*/


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