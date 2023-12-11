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
        return switch (this) {
            case NONE -> "No Error";
            case ANIMATION_INVALID_FILE -> "Animation file has invalid extension (.png or .json needed)";
            case ANIMATION_INVALID_NAME ->
                "Animation file has invalid name (use <tile>.anim.png or <tile>.anim.<delay>.png)";
            case ANIMATION_INVALID_JSON -> "Animation json file could not be parsed";
            case ANIMATION_NAME_MISSING -> "Animation json missing animation texture name";
            case ANIMATION_TILE_MISSING -> "Animation json missing tile texture name";
            case ANIMATION_INVALID_DELAY -> "Animation delay is not a number or less than 1";
            default -> "Unknown Error";
        };
    }
}
