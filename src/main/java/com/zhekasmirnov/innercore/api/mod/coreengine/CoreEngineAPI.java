package com.zhekasmirnov.innercore.api.mod.coreengine;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.api.mod.coreengine.builder.CELoader;
import com.zhekasmirnov.innercore.api.runtime.LoadingStage;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 24.08.2017.
 */

public class CoreEngineAPI extends API {
    private static CEHandler ceHandlerSingleton;

    public static synchronized CEHandler getOrLoadCoreEngine() {
        if (ceHandlerSingleton == null) {
            Logger.info(LOGGER_TAG, "started loading");

            long start = System.currentTimeMillis();
            ceHandlerSingleton = CELoader.loadAndCreateHandler();
            if (ceHandlerSingleton != null) {
                ceHandlerSingleton.load();
            }
            long end = System.currentTimeMillis();

            if (ceHandlerSingleton != null) {
                Logger.info(LOGGER_TAG, "successfully extracted and loaded in " + (end - start) + " ms");
            } else {
                Logger.info(LOGGER_TAG, "failed to create handler, look for details above");
            }
        }
        return ceHandlerSingleton;
    }


    public static final String LOGGER_TAG = "CORE-ENGINE";

    @Override
    public String getName() {
        return "CoreEngine";
    }

    @Override
    public int getLevel() {
        return 8;
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onModLoaded(Mod mod) {

    }

    @Override
    public void onCallback(String name, Object[] args) {

    }

    @Override
    public void setupCallbacks(Executable executable) {

    }

    private void transferValue(Executable executable, String name) {
        executable.injectValueIntoScope(name, getOrLoadCoreEngine().requireGlobal(name));
    }

    @Override
    public void prepareExecutable(Executable executable) {
        super.prepareExecutable(executable);

        if (isLoaded) {
            getOrLoadCoreEngine().injectCoreAPI(executable.getScope());

            transferValue(executable, "Particles");
            transferValue(executable, "ItemExtraData");
            transferValue(executable, "RenderMesh");
        }
        else {
            throw new RuntimeException("cannot prepare executable, Core Engine failed to load");
        }
    }

    @Override
    public void injectIntoScope(ScriptableObject scope) {
        // core engine will inject CoreAPI in prepareExecutable
    }
}
