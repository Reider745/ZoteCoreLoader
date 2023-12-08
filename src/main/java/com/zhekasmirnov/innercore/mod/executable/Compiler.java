package com.zhekasmirnov.innercore.mod.executable;

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.API;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.executable.library.Library;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

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

    private static int tempDexCounter = 0;

    public static Script loadScriptFromDex(File dexFile) throws IOException {
        File dexOut = new File("innercore/temp/ic-dex-cache" + (tempDexCounter++));
        dexOut.mkdirs();

        final String temp = dexOut.getAbsolutePath()+"/";
        InnerCoreServer.unzip(new ZipFile(new File(dexFile.getAbsolutePath())), temp);

        final String PATH_TO_JAR = temp+"classes.jar";
        Dex2jarCmd.main("-f", temp+"classes.dex", "--output", PATH_TO_JAR);

        final URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:///"+PATH_TO_JAR)}, Compiler.class.getClassLoader());

        final JarFile jarFile = new JarFile(PATH_TO_JAR);
        final Enumeration<JarEntry> entries = jarFile.entries();
        String className = null;

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory())
                className = entry.getName().replace('/', '.').replace(".class", "");
        }
        jarFile.close();

        if (className == null) {
            throw new IOException("invalid compiled js dex file: no class entries found");
        }

        try {
            Class<?> clazz = loader.loadClass(className);
            return (Script) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Executable loadDex(File dex, final CompilerConfig compilerConfig) throws IOException {
        Context ctx = Compiler.enter(compilerConfig.getOptimizationLevel());

        Script script = loadScriptFromDex(dex);

        if (script == null) {
            return null;
        }

        Executable exec = wrapScript(ctx, script, compilerConfig);
        exec.isLoadedFromDex = true;
        return exec;
    }

    public static Executable loadDexList(File[] dexes, final CompilerConfig compilerConfig) {
        Context ctx = Compiler.enter(compilerConfig.getOptimizationLevel());

        MultidexScript multidex = new MultidexScript();
        LoadingUI.setTip("Wrapping " + compilerConfig.getFullName());

        for (File dex : dexes) {
            try {
                Script script = loadScriptFromDex(dex);
                if (script != null) {
                    multidex.addScript(script);
                }
            } catch (IOException e) {
                ICLog.e("COMPILER", "failed to load dex file into multi-dex executable: file=" + dex, e);
            }
        }

        if (multidex.getScriptCount() == 0) {
            return null;
        }

        Executable exec = wrapScript(ctx, multidex, compilerConfig);
        exec.isLoadedFromDex = true;

        LoadingUI.setTip("");
        return exec;
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

        Context ctx = enter(9);
        ctx.compileReader(input, name + "_" + genUniqueId(), 0, null);

      //  AndroidClassLoader.exitCompilationMode();
    }
}
