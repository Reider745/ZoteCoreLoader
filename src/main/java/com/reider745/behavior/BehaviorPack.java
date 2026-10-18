package com.reider745.behavior;

import com.reider745.behavior.entities.EntitiesContent;
import com.reider745.behavior.items.ItemsContent;
import com.reider745.behavior.recipes.RecipesFurnaceContent;
import com.reider745.behavior.recipes.RecipesShapedContent;
import com.reider745.behavior.recipes.RecipesShapelessContent;
import com.reider745.behavior.spawnrules.SpawnRulesContent;
import com.reider745.behavior.loots.LootsContent;
import com.reider745.behavior.entities.runtime.MobAssets;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class BehaviorPack {
    private static final List<BehaviorContent> CONTENTS = List.of(
            new ItemsContent(), new LootsContent(), new MobAssets(true), new MobAssets(false),
            new EntitiesContent(), new SpawnRulesContent(),
            new RecipesShapedContent(), new RecipesShapelessContent(), new RecipesFurnaceContent());

    private final Path directory;
    private final List<BehaviorContent> handlers;
    private final Map<Path, JSONObject> documents = new LinkedHashMap<>();
    private final Set<Path> loaded = new HashSet<>();
    private boolean prepared;
    private String uuid;
    public String name = "Not-load";

    public BehaviorPack(String path) {
        this(path, CONTENTS);
    }

    protected BehaviorPack(String path, List<BehaviorContent> handlers) {
        directory = Path.of(path).toAbsolutePath().normalize();
        this.handlers = List.copyOf(handlers);
    }

    protected void loadManifest2(JSONObject json) {
        loadManifest1(json);
    }

    protected void loadManifest1(JSONObject json) {
        name = json.getJSONObject("header").getString("name");
        uuid = json.getJSONObject("header").optString("uuid", null);
    }

    protected void loadManifest() {
        Path manifest = directory.resolve("manifest.json");
        if (!Files.isRegularFile(manifest)) {
            throw new IllegalArgumentException("Missing behavior pack manifest: " + manifest);
        }
        JSONObject json = readJson(manifest);
        switch (json.getInt("format_version")) {
            case 1 -> loadManifest1(json);
            case 2 -> loadManifest2(json);
            default -> throw new IllegalArgumentException("Unsupported manifest version: " + json.get("format_version"));
        }
    }

    /** All packs must register their items before any pack registers recipes. */
    public static List<BehaviorPack> loadDefinitionsFromDirectory(File directory) {
        File[] folders = directory.listFiles(File::isDirectory);
        if (folders == null) return List.of();
        Arrays.sort(folders, Comparator.comparing(File::getName));
        return loadDefinitionsFromDirectories(Arrays.asList(folders));
    }

    public static List<BehaviorPack> loadDefinitionsFromDirectories(Collection<File> directories) {
        List<BehaviorPack> packs = new ArrayList<>();
        Set<Path> roots = new HashSet<>();
        Set<String> uuids = new HashSet<>();
        for (File folder : directories) {
            try {
                if (!folder.isDirectory() || !roots.add(folder.toPath().toRealPath())) continue;
                BehaviorPack pack = new BehaviorPack(folder.getAbsolutePath());
                pack.loadManifest();
                if (pack.uuid != null && !uuids.add(pack.uuid.toLowerCase(Locale.ROOT))) continue;
                pack.loadDefinitions();
                packs.add(pack);
            } catch (Exception e) {
                Logger.error("Error loading behavior pack " + folder, e);
            }
        }
        return packs;
    }

    public void load() {
        loadDefinitions();
        loadRecipes();
    }

    public void loadDefinitions() {
        loadContents(false);
    }

    public void loadRecipes() {
        loadContents(true);
    }

    private void prepare() {
        if (prepared) return;
        loadManifest();
        Set<Path> files = new LinkedHashSet<>();
        Path contentsFile = directory.resolve("contents.json");
        if (Files.isRegularFile(contentsFile)) {
            try {
                JSONArray content = readJson(contentsFile).optJSONArray("content");
                if (content != null) {
                    for (int i = 0; i < content.length(); i++) {
                        String relative = content.getJSONObject(i).getString("path");
                        Path file = directory.resolve(relative.replace('\\', '/')).normalize();
                        if (file.startsWith(directory) && !file.equals(directory.resolve("manifest.json"))) {
                            files.add(file);
                        } else {
                            warning("Ignoring invalid content path: " + relative);
                        }
                    }
                }
            } catch (Exception e) {
                warning("Cannot read " + contentsFile + ": " + e.getMessage());
            }
        }
        Set<String> folders = new LinkedHashSet<>();
        for (BehaviorContent handler : handlers) folders.add(handler.getDefaultDirectory());
        // Report unsupported server content instead of silently claiming full compatibility.
        folders.addAll(List.of("blocks", "loot_tables", "functions", "animation_controllers", "animations"));
        for (String folder : folders) {
            Path path = directory.resolve(folder);
            if (!Files.isDirectory(path)) continue;
            try (Stream<Path> children = Files.walk(path)) {
                children.filter(Files::isRegularFile).sorted().forEach(files::add);
            } catch (IOException e) {
                warning("Cannot scan " + path + ": " + e.getMessage());
            }
        }
        Set<Path> canonicalFiles = new HashSet<>();
        for (Path file : files) {
            if (!Files.isRegularFile(file)) continue;
            try {
                Path realPath = file.toRealPath();
                if (!realPath.startsWith(directory.toRealPath()) || !canonicalFiles.add(realPath)) continue;
                if (file.getFileName().toString().endsWith(".mcfunction")) {
                    warning("Unsupported behavior content: " + file);
                } else if (file.getFileName().toString().endsWith(".json")) {
                    JSONObject json = readJson(file);
                    if (handlers.stream().noneMatch(handler -> json.has(handler.getTagName()))) {
                        warning("Unsupported behavior content: " + file);
                    } else {
                        documents.put(file, json);
                    }
                }
            } catch (Exception e) {
                warning("Cannot read " + file + ": " + e.getMessage());
            }
        }
        prepared = true;
    }

    private void loadContents(boolean recipes) {
        prepare();
        for (BehaviorContent handler : handlers) {
            if (handler.isRecipe() != recipes) continue;
            documents.forEach((file, json) -> {
                if (json.has(handler.getTagName()) && loaded.add(file)) {
                    try {
                        handler.load(json, file.toString());
                    } catch (Exception e) {
                        warning("Cannot load " + file + ": " + e.getMessage());
                    }
                }
            });
        }
    }

    protected void warning(String message) {
        Logger.warning("[BehaviorPack " + name + "] " + message);
    }

    // Preserve comment markers inside strings (e.g. URLs in a manifest description).
    private static JSONObject readJson(Path path) {
        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            if (text.startsWith("\uFEFF")) text = text.substring(1);
            return new JSONObject(stripComments(text));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read " + path, e);
        }
    }

    private static String stripComments(String text) {
        StringBuilder result = new StringBuilder(text.length());
        boolean string = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (string && ch == '\\' && i + 1 < text.length()) {
                result.append(ch).append(text.charAt(++i));
                continue;
            }
            if (ch == '"') string = !string;
            if (!string && ch == '/' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == '/') {
                    while (i < text.length() && text.charAt(i) != '\n') i++;
                    result.append('\n');
                    continue;
                }
                if (next == '*') {
                    int end = text.indexOf("*/", i + 2);
                    if (end < 0) throw new IllegalArgumentException("Unterminated JSON comment");
                    result.append(' ');
                    i = end + 1;
                    continue;
                }
            }
            result.append(ch);
        }
        return result.toString();
    }

    public boolean isJsonObject(String path) {
        try {
            readJson(Path.of(path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void loadFileContext(String path) {
        JSONObject json = readJson(Path.of(path));
        for (BehaviorContent handler : handlers) {
            if (json.has(handler.getTagName())) {
                handler.load(json, path);
                return;
            }
        }
        warning("Unsupported behavior content: " + path);
    }
}
