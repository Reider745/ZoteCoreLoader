package com.zhekasmirnov.apparatus.adapter.innercore;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.Apparatus;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;

public class PackInfo {
    public static String getPackName() {
        return InnerCoreServer.getName();
        //return "Inner Core Test";
        //return (String) InnerCoreConfig.get("name");
    }

    public static String getPackVersionName() {
        return InnerCoreServer.getVersionName();
        //return "2.3.1b115 test";
        //return (String) InnerCoreConfig.get("version_name");
    }

    public static int getPackVersionCode() {
        return InnerCoreServer.getVersionCode();
        //return EngineConfig.getInt("version", Apparatus.getVersionCode());
    }

    public static String getNetworkPackIdentifier() {
        return getPackName() + "#" + getPackVersionName();
    }

    public static String toInfo(){
        return "===="+getPackName()+"===="+"version: "+getPackVersionName()+"   "+getPackVersionCode();
    }
}
