package org.mineprogramming.horizon.innercore.model;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import com.zhekasmirnov.innercore.mod.build.BuildConfig;
import com.zhekasmirnov.innercore.mod.build.BuildHelper;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModBuilder;
import com.zhekasmirnov.innercore.mod.build.enums.BuildType;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.modpack.ModPack.TaskReporter;

public class ModCompilationHelper {
    private Mod mod;

    public ModCompilationHelper(File directory){
        mod = new Mod(directory.getAbsolutePath() + "/");
        mod.buildConfig = ModBuilder.loadBuildConfigForDir(mod.dir);
    }


    public void setDevelop(){
        mod.setBuildType(BuildType.DEVELOP);
    }


    public boolean isCompiled(){
        return mod.getBuildType() == BuildType.RELEASE;
    }
}
