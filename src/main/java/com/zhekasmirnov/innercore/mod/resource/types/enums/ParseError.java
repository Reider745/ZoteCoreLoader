package com.zhekasmirnov.innercore.mod.resource.types.enums;

/**
 * Created by zheka on 28.07.2017.
 */

public enum ParseError {
    NONE,

    ANIMATION_INVALID_FILE,
    ANIMATION_INVALID_NAME,
    ANIMATION_INVALID_JSON,
    ANIMATION_NAME_MISSING,
    ANIMATION_TILE_MISSING,
    ANIMATION_INVALID_DELAY;

    @Override
    public String toString() {
        switch (this) {
            case NONE:
                return "No Error";
            case ANIMATION_INVALID_FILE:
                return "Animation file has invalid extension (.png or .json needed)";
            case ANIMATION_INVALID_NAME:
                return "Animation file has invalid name (use <tile>.anim.png or <tile>.anim.<delay>.png)";
            case ANIMATION_INVALID_JSON:
                return "Animation json file could not be parsed";
            case ANIMATION_NAME_MISSING:
                return "Animation json missing animation texture name";
            case ANIMATION_TILE_MISSING:
                return "Animation json missing tile texture name";
            case ANIMATION_INVALID_DELAY:
                return "Animation delay is not a number or less than 1";
        }
        return "Unknown Error";
    }
}
