package com.reider745.behavior;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.level.Level;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.plugin.PluginManager;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntitiesContent;
import com.reider745.behavior.entities.EntityContentFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomEntityLifecycleTest {
    private FullChunk chunk() throws Exception {
        if (Block.fullList == null) {
            Block.fullList = new Block[Block.MAX_BLOCK_ID << Block.DATA_BITS];
            Block.fullList[0] = new BlockAir();
        }
        Server server = mock(Server.class);
        when(server.getPluginManager()).thenReturn(mock(PluginManager.class));
        when(server.getTick()).thenReturn(1);
        Level level = mock(Level.class);
        var updates = Level.class.getField("updateEntities");
        updates.setAccessible(true);
        updates.set(level, new cn.nukkit.utils.collection.nb.Long2ObjectNonBlockingMap<>());
        when(level.getServer()).thenReturn(server);
        when(level.getGameRules()).thenReturn(GameRules.getDefault());
        when(level.getBlock(any(Vector3.class))).thenReturn(new BlockAir());
        when(level.getBlock(anyInt(), anyInt(), anyInt())).thenReturn(new BlockAir());
        FullChunk chunk = mock(FullChunk.class);
        LevelProvider provider = mock(LevelProvider.class);
        when(chunk.getProvider()).thenReturn(provider);
        when(provider.getLevel()).thenReturn(level);
        return chunk;
    }

    private void definition() {
        EntityContentFactory factory = new EntitiesContent().parse(new JSONObject("""
                {"format_version":"1.16.100","minecraft:entity":{
                "description":{"identifier":"test:lifecycle","is_summonable":true},
                "components":{"minecraft:health":{"value":80,"max":100},
                  "minecraft:collision_box":{"width":0.6,"height":1.8},
                  "minecraft:movement":{"value":0.2}},
                "component_groups":{"adult":{"minecraft:health":{"value":120,"max":150}}},
                "events":{"grow":{"add":{"component_groups":["adult"]}}}}}
                """));
        EntityContentFactory.factoryMap.put(factory.description.identifier, factory);
    }

    @Test void createsRealEntityPreservesHealthGroupsAndMolangAcrossNbtReload() throws Exception {
        definition();
        FullChunk chunk = chunk();
        CompoundTag nbt = Entity.getDefaultNBT(new Vector3(0, 64, 0)).putString("___id___", "test:lifecycle");
        CustomEntity first = new CustomEntity(chunk, nbt);
        assertEquals(80, first.getHealth());
        assertEquals(100, first.getMaxHealth());
        assertEquals(.6f, first.getWidth(), .001f);
        assertEquals("test:lifecycle", first.getIdentifier().toString());
        first.setScale(.5f);
        first.recalculateBoundingBox();
        assertEquals(.3, first.getBoundingBox().getMaxX() - first.getBoundingBox().getMinX(), .001);
        first.runtime().event("grow");
        assertEquals(150, first.getMaxHealth());
        first.setHealth(63);
        first.runtime().molang().evaluate("v.test = 17;");
        first.saveNBT();
        CompoundTag saved = first.namedTag.clone();
        assertEquals("test:lifecycle", saved.getString("id"));
        CustomEntity restored = new CustomEntity(chunk, saved);
        assertEquals(63, restored.getHealth());
        assertEquals(150, restored.getMaxHealth());
        assertTrue(restored.runtime().groups().contains("adult"));
        assertEquals(17, restored.runtime().molang().evaluate("v.test"));
        assertEquals(100, EntityContentFactory.factoryMap.get("test:lifecycle").health.max);
        assertSame(first.getEntityDefinition(), restored.getEntityDefinition());
    }

    @Test void spawnEggNumericCreationFindsTheBehaviorDefinition() throws Exception {
        definition();
        FullChunk chunk = chunk();
        Server server = chunk.getProvider().getLevel().getServer();
        server.enableExperimentMode = true;
        var definition = EntityContentFactory.factoryMap.get("test:lifecycle").getEntityDefinition();
        try (var access = mockStatic(Server.class)) {
            access.when(Server::getInstance).thenReturn(server);
            cn.nukkit.entity.custom.EntityManager.get().registerDefinition(definition);
            Entity entity = com.reider745.hooks.entity.EntityOverrides.createEntity(definition.getRuntimeId(), chunk,
                    Entity.getDefaultNBT(new Vector3(0, 64, 0)));
            assertInstanceOf(CustomEntity.class, entity);
            assertEquals("test:lifecycle", ((CustomEntity) entity).getFactory().description.identifier);
        }
    }

    @Test void tamingConsumesFoodAndKeepsTheOwnerAcrossComponentChanges() throws Exception {
        FullChunk chunk = chunk();
        cn.nukkit.item.Item.NAMESPACED_ID_ITEM.put("test:treat", () -> new cn.nukkit.item.Item(6203, 0, 1, "Treat"));
        var factory = new EntitiesContent().parse(new JSONObject("""
                {"format_version":"1.16.100","minecraft:entity":{
                "description":{"identifier":"test:taming"},
                "components":{"minecraft:tameable":{"probability":1,"tame_items":["test:treat"],"tame_event":{"event":"tamed"}}},
                "component_groups":{"tamed":{"minecraft:is_tamed":{}}},
                "events":{"tamed":{"add":{"component_groups":["tamed"]}},"clear":{"remove":{"component_groups":["tamed"]}}}}}
                """));
        EntityContentFactory.factoryMap.put("test:taming", factory);
        CustomEntity entity = new CustomEntity(chunk, Entity.getDefaultNBT(new Vector3(0,64,0)).putString("___id___", "test:taming"));
        cn.nukkit.Player player = mock(cn.nukkit.Player.class);
        when(player.getId()).thenReturn(42L);
        when(player.getUniqueId()).thenReturn(new java.util.UUID(0, 42));
        when(player.getInventory()).thenReturn(mock(cn.nukkit.inventory.PlayerInventory.class));
        var item = new cn.nukkit.item.Item(6203, 0, 2, "Treat");
        assertTrue(entity.onInteract(player, item));
        assertEquals(1, item.getCount());
        assertEquals(42, entity.namedTag.getLong("BehaviorOwner"));
        assertTrue(entity.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_TAMED));
        entity.runtime().event("clear");
        assertTrue(entity.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_TAMED));
    }
}
