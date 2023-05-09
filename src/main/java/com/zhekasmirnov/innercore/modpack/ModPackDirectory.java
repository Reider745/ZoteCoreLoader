package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.modpack.strategy.extract.DirectoryExtractStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.request.DirectoryRequestStrategy;
import com.zhekasmirnov.innercore.modpack.strategy.update.DirectoryUpdateStrategy;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModPackDirectory {
    public enum DirectoryType {
        MODS,
        MOD_ASSETS,
        ENGINE,
        CONFIG,
        CACHE,
        RESOURCE_PACKS,
        BEHAVIOR_PACKS,
        TEXTURE_PACKS,
        CUSTOM;
    }

    private final DirectoryType type;
    private final File location;
    private final String pathPattern; // path pattern for update entries, will be wrapped inside [\s\/\\]*<pathPattern with / replaced with \/>[\s\/\\]*.*
    private final Pattern pathPatternRegex;

    private final DirectoryRequestStrategy requestStrategy;
    private final DirectoryUpdateStrategy updateStrategy;
    private final DirectoryExtractStrategy extractStrategy;

    private ModPack modPack;

    public ModPackDirectory(DirectoryType type, File location, String pathPattern, DirectoryRequestStrategy requestStrategy, DirectoryUpdateStrategy updateStrategy, DirectoryExtractStrategy extractStrategy) {
        this.type = type;
        this.location = location;
        this.pathPattern = pathPattern;
        this.pathPatternRegex = Pattern.compile("[\\s/\\\\]*" + pathPattern + "[\\s/\\\\]*(.*)"); // bruh, but true
        this.requestStrategy = requestStrategy;
        requestStrategy.assignToDirectory(this);
        this.updateStrategy = updateStrategy;
        updateStrategy.assignToDirectory(this);
        this.extractStrategy = extractStrategy;
        extractStrategy.assignToDirectory(this);
    }

    public boolean assureDirectoryRoot() {
        if (location.isDirectory()) {
            return true;
        }
        if (location.isFile()) {
            location.delete();
        }
        return location.mkdirs();
    }

    public void assignToModPack(ModPack modPack) {
        if (this.modPack != null) {
            throw new IllegalStateException("directory " + this + " is already assigned to modpack");
        }
        this.modPack = modPack;
    }


    public DirectoryType getType() {
        return type;
    }

    public File getLocation() {
        return location;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public Pattern getPathPatternRegex() {
        return pathPatternRegex;
    }

    public String getLocalPathFromEntry(String entryName) {
        Matcher matcher = pathPatternRegex.matcher(entryName);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group(1);
    }


    public DirectoryRequestStrategy getRequestStrategy() {
        return requestStrategy;
    }

    public DirectoryUpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public DirectoryExtractStrategy getExtractStrategy() {
        return extractStrategy;
    }
}
