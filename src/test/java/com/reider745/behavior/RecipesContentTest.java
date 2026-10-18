package com.reider745.behavior;

import cn.nukkit.item.Item;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.recipe.RecipeRegistry;
import cn.nukkit.recipe.impl.ShapedRecipe;
import cn.nukkit.recipe.impl.ShapelessRecipe;
import com.reider745.behavior.recipes.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipesContentTest {
    @BeforeAll
    static void items() {
        // Exercise the real Nukkit recipe registry without starting a network server.
        Block.fullList = new Block[Block.MAX_BLOCK_ID << Block.DATA_BITS];
        Block.fullList[0] = new BlockAir();
        Item.NAMESPACED_ID_ITEM.put("test:raw", () -> new Item(6000, 0, 1, "Raw"));
        Item.NAMESPACED_ID_ITEM.put("test:cooked", () -> new Item(6001, 0, 1, "Cooked"));
        Item.NAMESPACED_ID_ITEM.put("test:variant", () -> new Item(6002, 5, 1, "Variant"));
    }

    @Test
    void acceptsStringObjectArrayAndWildcardInput() {
        assertEquals(6000, RecipeItems.parse("test:raw").getId());
        assertEquals(5, RecipeItems.parse("test:variant").getDamage());
        assertEquals(5, RecipeItems.parse(new JSONObject("{\"item\":\"test:variant\"}")).getDamage());
        Item counted = RecipeItems.parse(new JSONObject("{\"item\":\"test:raw\",\"count\":3,\"data\":2}"));
        assertEquals(3, counted.getCount());
        assertEquals(2, counted.getDamage());
        assertFalse(RecipeItems.parse(new JSONObject("{\"item\":\"test:raw\",\"data\":-1}")).hasMeta());
        assertThrows(IllegalArgumentException.class, () -> RecipeItems.result(
                new JSONObject("{\"item\":\"test:raw\",\"data\":-1}")));
        assertThrows(IllegalArgumentException.class, () -> RecipeItems.parse(
                new JSONObject("{\"item\":\"test:raw\",\"count\":0}")));
    }

    @Test
    void compactShapedPatternKeepsWidthAndExtraResults() {
        JSONObject body = new JSONObject("""
                {"description":{"identifier":"test:compact"},"pattern":["AA"," A"],
                 "key":{"A":{"item":"test:raw"}},
                 "result":[{"item":"test:cooked","count":2},{"item":"test:variant"}]}
                """);
        ShapedRecipe recipe = new RecipesShapedContent().parseRecipe(body);
        assertEquals(2, recipe.getWidth());
        assertEquals(2, recipe.getHeight());
        assertEquals(2, recipe.getResult().getCount());
        assertEquals(1, recipe.getExtraResults().size());
        assertEquals("test:compact", recipe.getRecipeId());
        assertArrayEquals(new String[]{"AA", " A"}, recipe.getShape());
        body.put("pattern", List.of("AB"));
        assertThrows(IllegalArgumentException.class, () -> new RecipesShapedContent().parseRecipe(body));
    }

    @Test
    void registersShapelessCountsAsIndividualSlots() {
        JSONObject body = new JSONObject("""
                {"description":{"identifier":"test:shapeless_count"},"tags":["crafting_table"],
                 "ingredients":[{"item":"test:raw","count":2}],"result":{"item":"test:cooked"}}
                """);
        new RecipesShapelessContent().load(new JSONObject().put("minecraft:recipe_shapeless", body), "recipe.json");
        ShapelessRecipe recipe = RecipeRegistry.getRecipes().stream().filter(ShapelessRecipe.class::isInstance)
                .map(ShapelessRecipe.class::cast).filter(r -> r.getRecipeId().equals("test:shapeless_count"))
                .findFirst().orElseThrow();
        assertEquals(2, recipe.getIngredientCount());
        assertTrue(recipe.matchItems(List.of(new Item(6000, 0, 2)), List.of()));
    }

    @Test
    void registersFurnaceBlastFurnaceAndCampfireRecipes() {
        JSONObject body = new JSONObject("""
                {"tags":["furnace","blast_furnace","campfire","soul_campfire"],
                 "input":"test:raw","output":{"item":"test:cooked","count":2}}
                """);
        new RecipesFurnaceContent().load(new JSONObject().put("minecraft:recipe_furnace", body), "furnace.json");
        assertEquals(6001, RecipeRegistry.matchFurnaceRecipe(RecipeItems.parse("test:raw")).getResult().getId());
        assertEquals(2, RecipeRegistry.matchBlastFurnaceRecipe(RecipeItems.parse("test:raw")).getResult().getCount());
        assertEquals(6001, RecipeRegistry.matchCampfireRecipe(RecipeItems.parse("test:raw")).getResult().getId());
    }
}
