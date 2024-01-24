package com.reider745.hooks;

import cn.nukkit.item.Item;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItemMapping.RuntimeEntry;
import cn.nukkit.item.RuntimeItems;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Hooks(className = "cn.nukkit.item.RuntimeItemMapping")
public class RuntimeItemsHooks implements HookClass {

    public static void register(int protocolId) {
        final Map<String, Integer> legacyString2LegacyInt = ReflectHelper.getField(RuntimeItems.class,
                "legacyString2LegacyInt");
        final RuntimeItemMapping mapping = RuntimeItems.getMapping(protocolId);

        final Int2ObjectMap<String> runtimeId2Name = ReflectHelper.getField(mapping, "runtimeId2Name");
        final Map<String, RuntimeItemMapping.LegacyEntry> identifier2Legacy = ReflectHelper.getField(mapping,
                "identifier2Legacy");
        final Object2IntMap<String> name2RuntimeId = ReflectHelper.getField(mapping, "name2RuntimeId");
        final Int2ObjectMap<RuntimeItemMapping.LegacyEntry> runtime2Legacy = ReflectHelper.getField(mapping,
                "runtime2Legacy");
        final Int2ObjectMap<RuntimeItemMapping.RuntimeEntry> legacy2Runtime = ReflectHelper.getField(mapping,
                "legacy2Runtime");
        final List<RuntimeEntry> itemPaletteEntries = ReflectHelper.getField(mapping, "itemPaletteEntries");

        CustomItem.customItems.forEach((name, id) -> {
            int fullId = RuntimeItems.getFullId(id, 0);
            int legacyId = id;
            int runtimeId = id;
            int damage = ItemMethod.getMaxDamageForId(id, 0);

            runtimeId2Name.put(runtimeId, name);
            name2RuntimeId.put(name, runtimeId);

            RuntimeItemMapping.RuntimeEntry runtimeEntry = new RuntimeItemMapping.RuntimeEntry(name, runtimeId, false);
            legacy2Runtime.put(fullId, runtimeEntry);
            itemPaletteEntries.add(runtimeEntry);

            RuntimeItemMapping.LegacyEntry legacyEntry = new RuntimeItemMapping.LegacyEntry(legacyId, false, damage);
            runtime2Legacy.put(runtimeId, legacyEntry);
            identifier2Legacy.put(name, legacyEntry);

            legacyString2LegacyInt.put(name, id);

            Item item = Item.get(legacyId, 0);
            if (item.getId() != 0) {
                Item.NAMESPACED_ID_ITEM.put(name, () -> item);
            }
        });

        CustomBlock.customBlocks.forEach((name, id) -> {
            for (int data = 0; data < 16; data++) {
                int fullId = RuntimeItems.getFullId(id, data);
                int legacyId = id;
                int runtimeId = id;

                runtimeId2Name.put(runtimeId, name);
                name2RuntimeId.put(name, runtimeId);

                RuntimeItemMapping.RuntimeEntry runtimeEntry = new RuntimeItemMapping.RuntimeEntry(name, runtimeId, false);
                legacy2Runtime.put(fullId, runtimeEntry);
                itemPaletteEntries.add(runtimeEntry);

                RuntimeItemMapping.LegacyEntry legacyEntry = new RuntimeItemMapping.LegacyEntry(legacyId, false, data);
                runtime2Legacy.put(runtimeId, legacyEntry);
                identifier2Legacy.put(name, legacyEntry);
            }

            legacyString2LegacyInt.put(name, id);

            Item item = Item.get(id, 0);
            if (item.getId() != 0) {
                Item.NAMESPACED_ID_ITEM.put(name, () -> item);
            }
        });

        try {
            Method generatePalette = RuntimeItemMapping.class.getDeclaredMethod("generatePalette", new Class[0]);
            generatePalette.setAccessible(true);
            generatePalette.invoke(mapping, new Object[0]);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static void register() {
        register(361);
        register(419);
        register(440);
        register(448);
        register(475);
        register(486);
        register(503);
        register(527);
        register(534);
        register(560);
        register(567);
        register(575);
        register(582);
        register(589);
        register(594);
        register(618);
        register(630);
    }
}
