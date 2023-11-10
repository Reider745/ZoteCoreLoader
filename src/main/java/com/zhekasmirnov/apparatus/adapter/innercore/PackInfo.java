package com.zhekasmirnov.apparatus.adapter.innercore;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.Apparatus;

public class PackInfo {
    public static String getPackName() {
        return EngineConfig.getString("pack.name",  InnerCoreServer.server.getName());
    }

    public static String getPackVersionName() {
        return EngineConfig.getString("pack.version_name",  InnerCoreServer.server.getVersion());
    }

    public static int getPackVersionCode() {
        return EngineConfig.getInt("pack.version", Apparatus.getVersionCode());
    }

    public static String getNetworkPackIdentifier() {
        return getPackName() + "#" + getPackVersionName();
    }

}
