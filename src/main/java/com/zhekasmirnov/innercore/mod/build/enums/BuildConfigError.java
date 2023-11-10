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
        switch (this) {
            case NONE:
                return "No Error";
            case FILE_ERROR:
                return "File could not be loaded";
            case PARSE_ERROR:
                return "JSON Parse Error";
        }

        return "Unknown Error";
    }
}
