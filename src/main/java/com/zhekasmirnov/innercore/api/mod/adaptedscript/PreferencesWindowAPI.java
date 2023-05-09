package com.zhekasmirnov.innercore.api.mod.adaptedscript;


import com.zhekasmirnov.apparatus.api.container.ItemContainer;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.ArrayList;

/**
 * Created by zheka on 13.08.2017.
 */

public class PreferencesWindowAPI extends AdaptedScriptAPI {
    @Override
    public String getName() {
        return "PrefsWinAPI";
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("PREFS", str);
    }



    @APIStaticModule
    public static class Prefs {
        @JSStaticFunction
        public static ArrayList<Mod> getModList() {
            return ModLoader.instance.modsList;
        }

        @JSStaticFunction
        public static boolean compileMod(Object mod, Object logger) {
            return Compiler.compileMod((Mod) Context.jsToJava(mod, Mod.class));
        }

        @JSStaticFunction
        public static com.zhekasmirnov.innercore.mod.build.Config getGlobalConfig() {
            return InnerCoreConfig.config;
        }

        @JSStaticFunction
        public static ArrayList<String> installModFile(String path, Object _log) {
            return new ArrayList<>();
            //return ExtractionHelper.extractICModFile(new File(path), log, null);
        }
    }

    public static class WorkbenchRecipeListBuilder extends com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeListBuilder {
        public WorkbenchRecipeListBuilder(long player, com.zhekasmirnov.apparatus.api.container.ItemContainer container) {
            super(player, container);
        }
    }

    public static class WorkbenchRecipeListProcessor extends com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeListProcessor {
        public WorkbenchRecipeListProcessor(ScriptableObject target) {
            super(target);
        }
    }
}
