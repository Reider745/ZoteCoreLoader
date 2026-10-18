package com.reider745.behavior;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.item.Item;
import cn.nukkit.recipe.RecipeRegistry;
import com.reider745.item.CustomItem;
import com.reider745.hooks.item.ItemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPackIntegrationTest {
    @TempDir Path root;

    private void write(String path, String text) throws Exception {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
    }

    @Test
    void resolvesItemsFromAnotherPackAndDeduplicatesPackSources() throws Exception {
        if (Block.fullList == null) {
            Block.fullList = new Block[Block.MAX_BLOCK_ID << Block.DATA_BITS];
            Block.fullList[0] = new BlockAir();
        }
        if (Item.list == null) Item.list = new Class[65535];
        write("a/manifest.json", """
                {"format_version":2,"header":{"name":"Recipes","uuid":"pack-a"}}
                """);
        write("b/manifest.json", """
                {"format_version":2,"header":{"name":"Items","uuid":"pack-b"}}
                """);
        write("a-copy/manifest.json", """
                {"format_version":2,"header":{"name":"Recipes copy","uuid":"pack-a"}}
                """);
        write("a/recipes/nested/smelt.json", """
                {"format_version":"1.12","minecraft:recipe_furnace":{
                  "tags":["furnace"],"input":"integration:raw","output":"integration:cooked"}}
                """);
        for (String name : List.of("raw", "cooked")) {
            write("b/items/" + name + ".json", """
                    {"format_version":"1.16.100","minecraft:item":{
                      "description":{"identifier":"integration:%s","category":"none"},
                      "components":{"minecraft:max_stack_size":16,"minecraft:display_name":{"value":"Test"}}}}
                    """.formatted(name));
        }
        List<BehaviorPack> packs = BehaviorPack.loadDefinitionsFromDirectories(List.of(
                root.resolve("a").toFile(), root.resolve("b").toFile(), root.resolve("b/../a").toFile(),
                root.resolve("a-copy").toFile()));
        assertEquals(2, packs.size());
        CustomItem.init();
        packs.forEach(BehaviorPack::loadRecipes);
        int rawId = CustomItem.customItems.get("integration:raw");
        int cookedId = CustomItem.customItems.get("integration:cooked");
        assertTrue(rawId >= 2048);
        // Call the creation hook directly; plain unit tests do not install bytecode hooks into Nukkit.
        assertEquals(16, ItemUtils.get(rawId, Integer.valueOf(0), 1, new byte[0]).getMaxStackSize());
        assertEquals(cookedId, RecipeRegistry.matchFurnaceRecipe(Item.get(rawId)).getResult().getId());
    }
}
