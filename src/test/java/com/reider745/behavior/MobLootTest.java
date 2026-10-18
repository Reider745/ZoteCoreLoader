package com.reider745.behavior;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.item.Item;
import com.reider745.behavior.loots.MobLoot;
import com.reider745.behavior.entities.runtime.MobHost;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import team.unnamed.mocha.runtime.value.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MobLootTest {
    @TempDir Path root;
    private final MobHost host = new MobHost() {
        public Object query(String name, List<Value> args) { return 0; }
        public Object filter(String test, String subject, String domain, Object value) {
            return test.equals("is_on_fire") ? false : true;
        }
        public void componentsChanged(JSONObject components, boolean initial) {}
        public void action(String action, Object value) {}
        public double random() { return .5; }
    };
    private void init() throws Exception {
        if (Block.fullList == null) {
            Block.fullList = new Block[Block.MAX_BLOCK_ID << Block.DATA_BITS];
            Block.fullList[0] = new BlockAir();
        }
        Item.NAMESPACED_ID_ITEM.put("test:loot", () -> new Item(6200, 0, 1, "Loot"));
        Files.writeString(root.resolve("manifest.json"), "{}");
    }

    @Test void resolvesNestedPackLocalTablesAndFunctions() throws Exception {
        init();
        MobLoot.register(root.resolve("loot_tables/base.json").toString(), new JSONObject("""
                {"pools":[{"rolls":1,"entries":[{"type":"loot_table","name":"loot_tables/sub.json","weight":1}]}]}
                """));
        MobLoot.register(root.resolve("loot_tables/sub.json").toString(), new JSONObject("""
                {"pools":[{"rolls":1,"entries":[{"type":"item","name":"test:loot","functions":[
                  {"function":"set_count","count":{"min":2,"max":4}},{"function":"set_data","data":3},
                  {"function":"set_name","name":"Prize"}]}]}]}
                """));
        List<Item> items = MobLoot.generate(root.resolve("entities/mob.json").toString(), "loot_tables/base.json", host);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getCount());
        assertEquals(3, items.get(0).getDamage());
        assertEquals("Prize", items.get(0).getCustomName());
    }

    @Test void rejectsRecursiveAndEscapingTablesAndChecksEntityConditions() throws Exception {
        init();
        MobLoot.register(root.resolve("loot_tables/cycle.json").toString(), new JSONObject("""
                {"pools":[{"entries":[{"type":"loot_table","name":"loot_tables/cycle.json"}]}]}
                """));
        String source = root.resolve("entities/mob.json").toString();
        assertThrows(IllegalArgumentException.class, () -> MobLoot.generate(source, "loot_tables/cycle.json", host));
        assertThrows(IllegalArgumentException.class, () -> MobLoot.generate(source, "../outside.json", host));
        MobLoot.register(root.resolve("loot_tables/fire.json").toString(), new JSONObject("""
                {"pools":[{"conditions":[{"condition":"entity_properties","properties":{"on_fire":true}}],
                 "entries":[{"type":"item","name":"test:loot"}]}]}
                """));
        assertTrue(MobLoot.generate(source, "loot_tables/fire.json", host).isEmpty());
    }
}
