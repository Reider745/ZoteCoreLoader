package com.reider745.behavior;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPackTest {
    @TempDir Path root;

    private Path write(String path, String text) throws Exception {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
        return file;
    }

    private static class Content extends BehaviorContent {
        private final boolean recipe;
        final List<JSONObject> loaded = new ArrayList<>();
        Content(boolean recipe) { this.recipe = recipe; }
        public String getTagName() { return recipe ? "test:recipe" : "test:item"; }
        public String getDefaultDirectory() { return recipe ? "recipes" : "items"; }
        public boolean isRecipe() { return recipe; }
        public void load(JSONObject json, String file) {
            if (json.optBoolean("fail")) throw new IllegalArgumentException("broken content");
            loaded.add(json);
        }
    }

    private static class Pack extends BehaviorPack {
        final List<String> warnings = new ArrayList<>();
        Pack(Path path, BehaviorContent... contents) { super(path.toString(), List.of(contents)); }
        protected void warning(String message) { warnings.add(message); }
    }

    private void manifest(int format) throws Exception {
        write("manifest.json", """
                {"format_version": %d, "header": {"name": "Test", "description": "https://example.com/*pack*/"}}
                """.formatted(format));
    }

    @Test
    void recursivelyLoadsWithoutContentsAndDefersRecipes() throws Exception {
        manifest(2);
        write("items/nested/item.json", "{\"test:item\":{}}");
        write("recipes/nested/recipe.json", "{\"test:recipe\":{}}");
        Content items = new Content(false);
        Content recipes = new Content(true);
        Pack pack = new Pack(root, items, recipes);
        pack.loadDefinitions();
        assertEquals(1, items.loaded.size());
        assertTrue(recipes.loaded.isEmpty());
        pack.loadRecipes();
        pack.load();
        assertEquals(1, items.loaded.size());
        assertEquals(1, recipes.loaded.size());
        assertEquals("Test", pack.name);
        assertTrue(pack.warnings.isEmpty(), pack.warnings.toString());
    }

    @Test
    void contentsAndDirectoryScanLoadTheSameFileOnce() throws Exception {
        manifest(1);
        write("contents.json", """
                {"content":[{"path":"items/nested/item.json"},{"path":"items/../items/nested/item.json"},
                {"path":"extras/listed.json"},{"path":"manifest.json"},{"path":"../outside.json"}]}
                """);
        write("items/nested/item.json", "{\"test:item\":{}}");
        write("extras/listed.json", "{\"test:item\":{}}");
        Content items = new Content(false);
        Pack pack = new Pack(root, items);
        pack.load();
        assertEquals(2, items.loaded.size());
        assertTrue(pack.warnings.stream().anyMatch(w -> w.contains("../outside.json")));
    }

    @Test
    void commentsBomAndEscapedStringsArePreserved() throws Exception {
        manifest(2);
        write("items/item.json", "\uFEFF" + """
                {
                  // generated file
                  "test:item": {"url":"https://example.com/a/*b*/", "quote":"a\\"//b"},
                  /* multi
                     line */
                  "text":"// keep this"
                }
                """);
        Content items = new Content(false);
        Pack pack = new Pack(root, items);
        pack.load();
        assertTrue(pack.warnings.isEmpty(), pack.warnings.toString());
        assertEquals("https://example.com/a/*b*/", items.loaded.get(0).getJSONObject("test:item").getString("url"));
        assertEquals("a\"//b", items.loaded.get(0).getJSONObject("test:item").getString("quote"));
    }

    @Test
    void badFileDoesNotDiscardOtherContent() throws Exception {
        manifest(2);
        write("items/a.json", "{broken");
        write("items/b.json", "{\"test:item\":{},\"fail\":true}");
        write("items/c.json", "{\"test:item\":{}}");
        write("blocks/unknown.json", "{\"minecraft:block\":{}}");
        write("items/ignored.txt", "not json");
        Content items = new Content(false);
        Pack pack = new Pack(root, items);
        pack.load();
        assertEquals(1, items.loaded.size());
        assertEquals(3, pack.warnings.size(), pack.warnings.toString());
    }

    @Test
    void invalidManifestRejectsPack() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new Pack(root).load());
        manifest(3);
        assertThrows(IllegalArgumentException.class, () -> new Pack(root).load());
    }

    @Test
    void missingDirectoryIsEmpty() {
        assertTrue(BehaviorPack.loadDefinitionsFromDirectory(root.resolve("missing").toFile()).isEmpty());
    }
}
