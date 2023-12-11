package com.zhekasmirnov.innercore.mod.build.enums;

/**
 * Created by zheka on 29.07.2017.
 */

public enum BuildType {
    RELEASE("release"),
    DEVELOP("develop");

    private final String name;

    BuildType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public static BuildType fromString(String str) {
        return switch (str) {
            case "release" -> RELEASE;
            default -> DEVELOP;
        };
    }
}
