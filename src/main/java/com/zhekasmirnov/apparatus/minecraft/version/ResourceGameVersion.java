package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ResourceGameVersion {
    private final int minVersion;
    private final int maxVersion;
    private final int targetVersion;

    public ResourceGameVersion(int minVersion, int maxVersion, int targetVersion) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.targetVersion = targetVersion;
    }

    public ResourceGameVersion() {
        this(-1, -1, MinecraftVersions.getCurrent().getCode());
    }

    public ResourceGameVersion(JSONObject json) {
        if (json == null) {
            this.minVersion = -1;
            this.maxVersion = -1;
            this.targetVersion = MinecraftVersions.getCurrent().getCode();
            return;
        }
        int minVersion = json.optInt("minGameVersion", -1);
        int maxVersion = json.optInt("maxGameVersion", -1);
        int targetVersion = json.optInt("targetGameVersion", -1);
        if (minVersion == -1 && maxVersion == -1 && targetVersion == -1) {
            this.minVersion = -1;
            this.maxVersion = -1;
            this.targetVersion = MinecraftVersions.getCurrent().getCode();
        } else if (minVersion == -1 && maxVersion == -1) {
            this.minVersion = this.maxVersion = this.targetVersion = targetVersion;
        } else {
            // validate target version
            if (targetVersion != -1) {
                if (maxVersion != -1) {
                    targetVersion = Math.min(maxVersion, targetVersion);
                }
                if (minVersion != -1) {
                    targetVersion = Math.max(minVersion, targetVersion);
                }
            }

            // apply
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
            this.targetVersion = targetVersion != -1 ? targetVersion : Math.max(minVersion, maxVersion);
        }
    }

    private static JSONObject readJsonFile(File file) {
        try {
            return FileTools.readJSON(file);
        } catch (IOException | JSONException exception) {
            return new JSONObject();
        }
    }

    public ResourceGameVersion(File file) {
        this(readJsonFile(file));
    }

    public int getMinVersion() {
        return minVersion;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public boolean isCompatibleWithAnyVersion() {
        return minVersion == -1 && maxVersion == -1;
    }

    public boolean isCompatible(MinecraftVersion version) {
        int code = version.getCode();
        return (minVersion == -1 || code >= minVersion) && (maxVersion == -1 || code <= maxVersion);
    }

    public boolean isCompatible() {
        return isCompatible(MinecraftVersions.getCurrent());
    }


    @Override
    public String toString() {
        return "ResourceVersion{" +
                "minVersion=" + minVersion +
                ", maxVersion=" + maxVersion +
                ", targetVersion=" + targetVersion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceGameVersion that = (ResourceGameVersion) o;
        return minVersion == that.minVersion &&
                maxVersion == that.maxVersion &&
                targetVersion == that.targetVersion;
    }
}

