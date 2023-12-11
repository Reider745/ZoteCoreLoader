package com.zhekasmirnov.innercore.mod.resource.types;

import android.support.annotation.NonNull;
import com.zhekasmirnov.innercore.mod.resource.pack.IResourcePack;
import com.zhekasmirnov.innercore.mod.resource.types.enums.AnimationType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.FileType;
import com.zhekasmirnov.innercore.mod.resource.types.enums.ParseError;
import com.zhekasmirnov.innercore.mod.resource.types.enums.TextureType;

import java.io.File;

/**
 * Created by zheka on 27.07.2017.
 */

public class ResourceFile extends File {
    private FileType type;
    private TextureType textureType;
    private AnimationType animationType;

    protected ParseError parseError = ParseError.NONE;

    public ResourceFile(@NonNull String path) {
        super(path);
        String name = getName();

        if (name.contains(".anim.")) {
            type = FileType.ANIMATION;
            if (path.endsWith(".png"))
                animationType = AnimationType.TEXTURE;
            else if (path.endsWith(".json"))
                animationType = AnimationType.DESCRIPTOR;
            else {
                type = FileType.INVALID;
                parseError = ParseError.ANIMATION_INVALID_NAME;
            }
        } else if (name.endsWith(".png") || name.endsWith(".tga")) {
            type = FileType.TEXTURE;
            if (path.contains("items-opaque/"))
                textureType = TextureType.ITEM;
            else if (path.contains("terrain-atlas/"))
                textureType = TextureType.BLOCK;
            else if (path.contains("particle-atlas/"))
                textureType = TextureType.PARTICLE;
            else
                textureType = TextureType.DEFAULT;
        } else if (name.endsWith(".json")) {
            if (name.equals("pack_manifest.json"))
                type = FileType.MANIFEST;
            else
                type = FileType.JSON;
        } else if (name.endsWith(".js")) {
            type = FileType.EXECUTABLE;
        } else {
            type = FileType.RAW;
        }
    }

    public ResourceFile(File f) {
        this(f.getAbsolutePath());
    }

    public ResourceFile(IResourcePack pack, File f) {
        this(f);
        setResourcePack(pack);
    }

    private IResourcePack resourcePack;

    public IResourcePack getResourcePack() {
        return resourcePack;
    }

    /* resource pack path is absolute path to its dir, that ends with '/' */
    public void setResourcePack(IResourcePack pack) {
        resourcePack = pack;
    }

    public String getLocalPath() {
        String path = getAbsolutePath();
        if (resourcePack == null) {
            return path;
        } else {
            return path.substring(resourcePack.getAbsolutePath().length());
        }
    }

    public String getLocalDir() {
        String locPath = getLocalPath();
        return locPath.substring(0, locPath.lastIndexOf('/') + 1);
    }

    public FileType getType() {
        return type;
    }

    public TextureType getTextureType() {
        return textureType;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public ParseError getParseError() {
        return parseError;
    }
}
