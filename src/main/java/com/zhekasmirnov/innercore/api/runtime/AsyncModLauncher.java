package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.apparatus.mod.ContentIdSource;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.Version;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.log.ModLoaderEventHandler;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.coreengine.CoreEngineAPI;
import com.zhekasmirnov.innercore.api.mod.recipes.RecipeLoader;
import com.zhekasmirnov.innercore.api.mod.recipes.furnace.FurnaceRecipeRegistry;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.library.LibraryRegistry;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.mcpe161.InnerCore;

import java.io.InputStreamReader;
import java.io.Reader;

public class AsyncModLauncher {

    public void launchModsInThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                long start = System.currentTimeMillis();
                launchModsInCurrentThread();
                ICLog.i("LOADING", "mods launched in " + (System.currentTimeMillis() - start) + "ms");
            }
        }).start();
    }

    public void launchModsInCurrentThread() {
        // load apparatus classes and register listeners
        Apparatus.loadClasses();

        // select default modpack if none selected
        ModPackContext.getInstance().assurePackSelected();

        // prepare basic modules for menu scripts and further loading
        LoadingUI.setTextAndProgressBar("Preparing...", 0.65f);
        NameTranslation.refresh(false);

        // load menu scripts (workbench for example)
        loadAllMenuScripts();

        // switch to final loading stage
        ICLog.setupEventHandlerForCurrentThread(new ModLoaderEventHandler());
        LoadingStage.setStage(LoadingStage.STAGE_FINAL_LOADING);
        NativeAPI.setInnerCoreVersion(Version.INNER_CORE_VERSION.toString());

        // prepare registries
        // VanillaIdConversionMap.getSingleton().reloadFromAssets();
        BlockRegistry.onInit();
        LibraryRegistry.loadAllBuiltInLibraries();
        LibraryRegistry.prepareAllLibraries();
        FurnaceRecipeRegistry.loadNativeRecipesIfNeeded();
        RecipeLoader loader = new RecipeLoader();
        loader.load();

        // run core engine
        LoadingUI.setTextAndProgressBar("Running Core Engine...", 0.4f);
        CoreEngineAPI.getOrLoadCoreEngine();

        // run mods
        LoadingUI.setTextAndProgressBar("Running Mods...", 0.5f);
        ModLoader.runModsViaNewModLoader(); // ModLoader.instance.startMods();

        // invoke post-initialization
        LoadingUI.setTextAndProgressBar("Defining Blocks...", 1);
        BlockRegistry.onModsLoaded();
        LoadingUI.setTextAndProgressBar("Post Initialization...", 1);
        invokePostLoadedCallbacks();

        // finalize
        ContentIdSource.getGlobal().save();
        InnerCore.getInstance().onFinalLoadComplete();
        ICLog.flush();
    }

    private static void invokePostLoadedCallbacks() {
        Callback.invokeAPICallback("CoreConfigured", InnerCoreConfig.config);
        Callback.invokeAPICallback("PreLoaded");
        Callback.invokeAPICallback("APILoaded");
        Callback.invokeAPICallback("ModsLoaded");
        Callback.invokeAPICallback("PostLoaded");
    }

    private static void loadAllMenuScripts() {
        loadMenuScript("innercore/scripts/workbench", "screen_workbench");
    }

    private static void loadMenuScript(String asset, String name) {
        CompilerConfig cfg = new CompilerConfig(API.getInstanceByName("PrefsWinAPI"));
        cfg.setName(name);
        cfg.setOptimizationLevel(-1);

        try {
            Reader input = new InputStreamReader(FileTools.getAssetInputStream(asset + ".js"));
            Compiler.compileReader(input, cfg).run();
        } catch (Exception e) {
            ICLog.e("ERROR", "failed to load script " + name, e);
        }
    }
}
