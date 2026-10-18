package com.reider745.behavior.entities.runtime;

import com.reider745.behavior.BehaviorContent;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Server animation assets are namespaced by their pack directory. */
public final class MobAssets extends BehaviorContent {
    private static final Map<String, JSONObject> CONTROLLERS = new HashMap<>();
    private static final Map<String, JSONObject> ANIMATIONS = new HashMap<>();
    private final boolean controllers;

    public MobAssets(boolean controllers) { this.controllers = controllers; }
    public String getTagName() { return controllers ? "animation_controllers" : "animations"; }
    public String getDefaultDirectory() { return getTagName(); }
    public void load(JSONObject json, String file) {
        JSONObject definitions = json.getJSONObject(getTagName());
        for (String name : definitions.keySet()) register(file, name, definitions.getJSONObject(name), controllers);
    }
    public static void register(String file, String name, JSONObject json, boolean controller) {
        (controller ? CONTROLLERS : ANIMATIONS).put(key(file, name), new JSONObject(json.toString()));
    }
    public static JSONObject get(String source, String name, boolean controller) {
        return (controller ? CONTROLLERS : ANIMATIONS).get(key(source, name));
    }
    private static String key(String source, String name) { return packRoot(source) + ":" + name; }
    public static Path packRoot(String source) {
        if (source == null) return Path.of("").toAbsolutePath().normalize();
        Path path = Path.of(source).toAbsolutePath().normalize().getParent();
        for (Path candidate = path; candidate != null; candidate = candidate.getParent()) {
            if (Files.isRegularFile(candidate.resolve("manifest.json"))) return candidate;
        }
        return path;
    }
}
