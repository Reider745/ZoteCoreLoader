package com.zhekasmirnov.apparatus.adapter.innercore;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.Apparatus;

public class PackInfo {
    public static String getPackName() {
        return EngineConfig.getString("name",  InnerCoreServer.getName());
    }

    public static String getPackVersionName() {
        return EngineConfig.getString("version_name",  InnerCoreServer.getVersion());
    }

    public static int getPackVersionCode() {
        return EngineConfig.getInt("version", Apparatus.getVersionCode());
    }

    public static String getNetworkPackIdentifier() {
        return getPackName() + "#" + getPackVersionName();
    }

    public static String toInfo(){
        return "===="+getPackName()+"====\n"+
                "version: "+getPackVersionName()+"   "+getPackVersionCode();
    }
}
