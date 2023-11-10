package com.zhekasmirnov.innercore.mod.build.enums;

/**
 * Created by zheka on 29.07.2017.
 */

public enum ResourceDirType {
    RESOURCE ("resource"),
    GUI ("gui");

    private final String name;
    ResourceDirType (String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public static ResourceDirType fromString(String str) {
        switch (str) {
            case "gui":
                return GUI;
            default:
                return RESOURCE;
        }
    }
}
