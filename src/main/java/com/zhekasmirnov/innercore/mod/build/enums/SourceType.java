package com.zhekasmirnov.innercore.mod.build.enums;

/**
 * Created by zheka on 29.07.2017.
 */

public enum SourceType {
    PRELOADER("preloader"),
    LAUNCHER("launcher"),
    MOD("mod"),
    CUSTOM("custom"),
    LIBRARY("library");

    private final String name;

    SourceType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public static SourceType fromString(String str) {
        return switch (str) {
            case "preloader" -> PRELOADER;
            case "launcher" -> LAUNCHER;
            case "library" -> LIBRARY;
            case "custom" -> CUSTOM;
            default -> MOD;
        };
    }
}
