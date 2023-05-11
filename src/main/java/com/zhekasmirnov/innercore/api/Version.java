package com.zhekasmirnov.innercore.api;

/**
 * Created by zheka on 25.07.2017.
 */

public class Version {
    public boolean isBeta = false;
    public int level = 0;
    public String name = "";

    public int build = -1;

    public Version(String name, int level, boolean beta) {
        this.name = name;
        this.level = level;
        this.isBeta = beta;
    }

    public Version(String name, int level) {
        this(name, level, false);
    }

    public Version(String name) {
        this(name, 0, false);
    }

    public String toString() {
        return "v" + name + (isBeta ? " beta" : "") + (build > 0 ? " build " + build : "");
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public static final Version INNER_CORE_VERSION;

    static {
        INNER_CORE_VERSION = new Version("2.0.0.0", 10, true);
    }
}
