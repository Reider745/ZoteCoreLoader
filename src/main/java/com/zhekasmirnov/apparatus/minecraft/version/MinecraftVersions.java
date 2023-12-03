package com.zhekasmirnov.apparatus.minecraft.version;

//import android.os.Environment;
//import com.zhekasmirnov.apparatus.minecraft.addon.AddonContext;
//import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser11;
//import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser16;
import com.zhekasmirnov.apparatus.minecraft.addon.AddonContext;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser11;
import com.zhekasmirnov.apparatus.minecraft.addon.recipe.AddonRecipeParser16;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MinecraftVersions {
    public static MinecraftVersion getCurrent() {
        return MINECRAFT_1_16_201;
    }

    public static final MinecraftVersion MINECRAFT_1_11_4 = new MinecraftVersion("1.11.4", 11, false) {
        @Override
        public File getMinecraftExternalStoragePath() {
            return null;
            //return new File(Environment.getExternalStorageDirectory(), "games/horizon");
        }

        @Override
        public String[] getVanillaResourcePacksDirs() {
            return new String[] {
                    "resource_packs/vanilla/"
            };
        }

        @Override
        public String[] getVanillaBehaviorPacksDirs() {
            return new String[] {
                    "behavior_packs/vanilla/"
            };
        }

        @Override
        public JSONObject createRuntimePackManifest(String name, String headerUuid, String moduleType, JSONArray version) {
            String moduleUuid = UUID.randomUUID().toString();
            String description = "This pack is generated by Inner Core mod, pack name ID is " + name;
            try {
                JSONObject manifest = new JSONObject();
                manifest
                        .put("format_version", 1)
                        .put("header", new JSONObject()
                                .put("uuid", headerUuid)
                                .put("name", "runtime pack: " + name)
                                .put("version", version)
                                .put("description", description)
                        )
                        .put("modules", new JSONArray().put(new JSONObject()
                                .put("uuid", moduleUuid)
                                .put("type", moduleType)
                                .put("version", version)
                                .put("description", "module " + name)
                        ));
                return manifest;
            } catch (JSONException ignore) {
                return null;
            }
        }

        @Override
        public AddonContext createAddonContext() {
            return new AddonContext(
                    this,
                    new AddonRecipeParser11()
            );
        }

        @Override
        public void addSupportedFeatures(Set<String> features) {
            features.add(MinecraftVersion.FEATURE_ACTOR_RENDER_OVERRIDE);
        }
    };

    public static final MinecraftVersion MINECRAFT_1_16_201 = new MinecraftVersion("1.16.201", 16, false) {
        @Override
        public File getMinecraftExternalStoragePath() {
            return null;
        }

        @Override
        public String[] getVanillaResourcePacksDirs() {
            return new String[] {
                    "resource_packs/vanilla/",
                    "resource_packs/vanilla_1.14/",
                    "resource_packs/vanilla_1.15/",
                    "resource_packs/vanilla_1.16/",
                    "resource_packs/vanilla_1.16.100/", // TODO: 1.16 check, if 1.16.100 is really used
                    "resource_packs/vanilla_1.16.200/",
            };
        }

        @Override
        public String[] getVanillaBehaviorPacksDirs() {
            return new String[] {
                    "behavior_packs/vanilla/",
                    "behavior_packs/vanilla_1.14/",
                    "behavior_packs/vanilla_1.15/",
                    "behavior_packs/vanilla_1.16/",
                    "behavior_packs/vanilla_1.16.100/", // TODO: 1.16 check, if 1.16.100 is really used
                    "behavior_packs/vanilla_1.16.200/",
            };
        }

        @Override
        public JSONObject createRuntimePackManifest(String name, String headerUuid, String moduleType, JSONArray version) {
            String moduleUuid = UUID.randomUUID().toString();
            String description = "This pack is generated by Inner Core mod, pack name ID is " + name;
            JSONArray minEngineVersion = new JSONArray().put(1).put(16).put(0);
            try {
                JSONObject manifest = new JSONObject();
                manifest
                        .put("format_version", 2)
                        .put("header", new JSONObject()
                                .put("uuid", headerUuid)
                                .put("name", "runtime pack: " + name)
                                .put("version", version)
                                .put("min_engine_version", minEngineVersion)
                                .put("description", description)
                        )
                        .put("modules", new JSONArray().put(new JSONObject()
                                .put("uuid", moduleUuid)
                                .put("type", moduleType)
                                .put("version", version)
                                .put("description", "module " + name)
                        ));
                return manifest;
            } catch (JSONException ignore) {
                return null;
            }
        }

        @Override
        public AddonContext createAddonContext() {
            return new AddonContext(
                    this,
                    new AddonRecipeParser16()
            );
        }

        @Override
        public void addSupportedFeatures(Set<String> features) {
            features.add(MinecraftVersion.FEATURE_VANILLA_ID_MAPPING);
            features.add(MinecraftVersion.FEATURE_VANILLA_WORLD_GENERATION_LEVELS);
            features.add(MinecraftVersion.FEATURE_ATTACHABLE_RENDER);
            features.add(MinecraftVersion.FEATURE_GLOBAL_SHADER_UNIFORM_SET);
        }
    };


    private static final List<MinecraftVersion> allVersions = new ArrayList<>();

    static {
        allVersions.add(MINECRAFT_1_11_4);
        allVersions.add(MINECRAFT_1_16_201);
    }

    public static List<MinecraftVersion> getAllVersions() {
        return allVersions;
    }

    public static MinecraftVersion getVersionByCode(int code) {
        for (MinecraftVersion version : allVersions) {
            if (version.getCode() == code) {
                return version;
            }
        }
        return null;
    }
}
