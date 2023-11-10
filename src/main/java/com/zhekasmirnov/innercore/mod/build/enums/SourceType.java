package com.zhekasmirnov.innercore.mod.build.enums;

/**
 * Created by zheka on 29.07.2017.
 */

public enum SourceType {
    PRELOADER ("preloader"),
    LAUNCHER ("launcher"),
    MOD ("mod"),
    CUSTOM ("custom"),
    LIBRARY ("library");

    private final String name;
    SourceType (String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public static SourceType fromString(String str) {
        switch (str) {
            case "preloader":
                return PRELOADER;
            case "launcher":
                return LAUNCHER;
            case "library":
                return LIBRARY;
            case "custom":
                return CUSTOM;
            default:
                return MOD;
        }
    }
}
