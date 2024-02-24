package com.zhekasmirnov.apparatus.adapter.innercore;

import com.reider745.InnerCoreServer;

public class PackInfo {

    public static String getPackName() {
        return InnerCoreServer.getName();
    }

    public static String getPackVersionName() {
        return InnerCoreServer.getVersionName();
    }

    public static int getPackVersionCode() {
        return InnerCoreServer.getVersionCode();
    }

    public static String getNetworkPackIdentifier() {
        return getPackName() + "#" + getPackVersionName();
    }

    public static String toInfo() {
        return "====" + getPackName() + "====" + "version: " + getPackVersionName() + "   " + getPackVersionCode();
    }
}
