package com.zhekasmirnov.innercore.mod.executable;

import com.zhekasmirnov.innercore.mod.build.Mod;
import org.mozilla.javascript.Context;

import java.io.FileReader;
import java.io.Reader;

public class Compiler {
    public static Context assureContextForCurrentThread() {
        return null;
    }

    public static Executable compileReader(Reader reader, CompilerConfig config){
        return null;
    }

    public static boolean compileMod(Mod mod){
        return false;
    }

    public static void compileScriptToFile(FileReader reader, String name, String path){

    }
}
