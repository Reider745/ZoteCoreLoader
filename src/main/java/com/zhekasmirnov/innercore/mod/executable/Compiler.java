package com.zhekasmirnov.innercore.mod.executable;

import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.library.Library;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Compiler {
    public static Context assureContextForCurrentThread() {
        Context ctx = Context.getCurrentContext();
        if (ctx == null) {
            ctx = enter(9);
        }
        return ctx;
    }

    public static Context enter(int level) {
        Context ctx = Context.enter();
        ctx.setOptimizationLevel(level);
        ctx.setLanguageVersion(200);

        return ctx;
    }

    private static Executable wrapScript(Context ctx, Script script, CompilerConfig compilerConfig) {
        API apiInstance = compilerConfig.getApiInstance();

        ScriptableObject scope = apiInstance != null ? ctx.initStandardObjects(apiInstance.newInstance(), false) : ctx.initStandardObjects();

        if (compilerConfig.isLibrary) {
            return new Library(ctx, script, scope, compilerConfig, compilerConfig.getApiInstance());
        }
        else {
            if (apiInstance != null) {
                apiInstance.injectIntoScope(scope);
            }

            return new Executable(ctx, script, scope, compilerConfig, compilerConfig.getApiInstance());
        }
    }

    public static Executable compileReader(Reader input, CompilerConfig compilerConfig) throws IOException {
        Context ctx = Compiler.enter(compilerConfig.getOptimizationLevel());

        LoadingUI.setTip("Compiling " + compilerConfig.getFullName());
        Script script = ctx.compileReader(input, compilerConfig.getFullName(), 0, null);

        return wrapScript(ctx, script, compilerConfig);
    }

    public static boolean compileMod(Mod mod){
        return false;
    }

    private static String genUniqueId() {
        return  Integer.toHexString((int) (Math.random() * 16777216)) + "_" + Integer.toHexString((int) (Math.random() * 16777216));
    }

    public static void compileScriptToFile(Reader input, String name, String targetFile) throws IOException {
        //AndroidClassLoader.enterCompilationMode(targetFile);

        //Context ctx = enter(9);
        //ctx.compileReader(input, name + "_" + genUniqueId(), 0, null);

        //AndroidClassLoader.exitCompilationMode();
    }
}
