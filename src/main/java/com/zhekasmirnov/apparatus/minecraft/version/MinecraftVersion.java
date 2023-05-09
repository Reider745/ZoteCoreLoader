package com.zhekasmirnov.apparatus.minecraft.version;

import com.zhekasmirnov.apparatus.util.Java8BackComp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class MinecraftVersion {
    // list of version dependent features
    public static final String FEATURE_VANILLA_ID_MAPPING = "vanilla_id_mapping";
    public static final String FEATURE_VANILLA_WORLD_GENERATION_LEVELS = "vanilla_world_generation_levels";
    public static final String FEATURE_ACTOR_RENDER_OVERRIDE = "actor_render_override";
    public static final String FEATURE_ATTACHABLE_RENDER = "attachable_render";
    public static final String FEATURE_GLOBAL_SHADER_UNIFORM_SET = "global_shader_uniform_set";

    private final String name;
    private final int code;
    private final boolean isBeta;

    private final Set<String> supportedFeatures = new HashSet<>();

    protected MinecraftVersion(String name, int code, boolean isBeta) {
        this.name = name;
        this.code = code;
        this.isBeta = isBeta;

        addSupportedFeatures(supportedFeatures);
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public boolean isBeta() {
        return isBeta;
    }

    public String getMainVanillaResourcePack() {
        return getVanillaResourcePacksDirs()[0];
    }

    public String getMainVanillaBehaviorPack() {
        return getVanillaBehaviorPacksDirs()[0];
    }

    public abstract File getMinecraftExternalStoragePath();
    public abstract String[] getVanillaResourcePacksDirs();
    public abstract String[] getVanillaBehaviorPacksDirs();
    public abstract JSONObject createRuntimePackManifest(String name, String headerUuid, String moduleType, JSONArray version);

    public abstract void addSupportedFeatures(Set<String> features);

    public Set<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    public boolean isFeatureSupported(String feature) {
        return supportedFeatures.contains(feature);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion that = (MinecraftVersion) o;
        return code == that.code && Java8BackComp.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Java8BackComp.hash(code, name);
    }
}
