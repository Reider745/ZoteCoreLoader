package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.mod.executable.CompilerConfig;
import com.zhekasmirnov.innercore.mod.executable.Executable;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class LibraryReplaced {
    private static ArrayList<String> disables = new ArrayList<>();
    private static HashMap<String, String> repalced = new HashMap<>();

    public static boolean canLibraryCompile(String library){
        return !repalced.containsKey(library.split("\\.")[0]);
    }

    public static void loadJsonSetting(){
        try{
            final JSONArray json = FileTools.readJSONArray(FileTools.DIR_WORK+"assets/innercore/scripts/libraries/config.json");

            for(int i = 0;i < json.length();i++){
                final JSONObject obj = json.getJSONObject(i);

                final String name = obj.getString("library");
                final String path = FileTools.DIR_WORK+"assets/innercore/scripts/libraries/"+name;

                final JSONArray disables = obj.getJSONArray("disables");
                for(int a = 0;a < disables.length();a++){
                    final String replace_name = disables.getString(a);
                    repalced.put(replace_name, path);
                    LibraryReplaced.disables.add(path);
                }

            }
        }catch (Exception e){
            ICLog.e("LibraryReplaced", "error loaded libraries", e);
        }
    }

    public static Executable build(String name, CompilerConfig compilerConfig) throws IOException {
        Reader sourceReader = new FileReader(new File(repalced.get(name.split("\\.")[0])));
        compilerConfig.setOptimizationLevel(-1);
        return Compiler.compileReader(sourceReader, compilerConfig);
    }
}
