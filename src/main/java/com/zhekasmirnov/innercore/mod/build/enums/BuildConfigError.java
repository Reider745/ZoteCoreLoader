package com.zhekasmirnov.innercore.mod.build.enums;

/**
 * Created by zheka on 29.07.2017.
 */

public enum BuildConfigError {
    NONE,
    FILE_ERROR,
    PARSE_ERROR;

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "No Error";
            case FILE_ERROR -> "File could not be loaded";
            case PARSE_ERROR -> "JSON Parse Error";
            default -> "Unknown Error";
        };
    }
}
