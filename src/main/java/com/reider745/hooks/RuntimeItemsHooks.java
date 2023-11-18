package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;

import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;


import java.util.*;

@Hooks(class_name = "cn.nukkit.item.RuntimeItemMapping")
public class RuntimeItemsHooks implements HookClass {
    @Inject(type_hook = TypeHook.BEFORE_REPLACE)
    public static void generatePalette(RuntimeItemMapping self){
        int protocolId = ReflectHelper.getField(self, "protocolId");
        List<RuntimeItemMapping.RuntimeEntry> itemPaletteEntries = ReflectHelper.getField(self, "itemPaletteEntries");

        BinaryStream paletteBuffer = new BinaryStream();
        int size = 0;
        for (RuntimeItemMapping.RuntimeEntry entry : itemPaletteEntries) {
            if (entry.isCustomItem() && (!Server.getInstance().enableExperimentMode || protocolId < ProtocolInfo.v1_16_100)) {
                break;
            }
            size++;
        }
        paletteBuffer.putUnsignedVarInt(size + CustomItem.customItems.size() + CustomBlock.customBlocks.size() * 16);
        for (RuntimeItemMapping.RuntimeEntry entry : itemPaletteEntries) {
            if (entry.isCustomItem()) {
                if (Server.getInstance().enableExperimentMode && protocolId >= ProtocolInfo.v1_16_100) {
                    paletteBuffer.putString(entry.getIdentifier());
                    paletteBuffer.putLShort(entry.getRuntimeId());
                    paletteBuffer.putBoolean(true); // Component item
                }
            } else {
                paletteBuffer.putString(entry.getIdentifier());
                paletteBuffer.putLShort(entry.getRuntimeId());
                if (protocolId >= ProtocolInfo.v1_16_100) {
                    paletteBuffer.putBoolean(false); // Component item
                }
            }
        }

        final Int2ObjectMap<String> runtimeId2Name = ReflectHelper.getField(self, "runtimeId2Name");
        final Map<String, RuntimeItemMapping.LegacyEntry> identifier2Legacy = ReflectHelper.getField(self, "identifier2Legacy");
        final Object2IntMap<String> name2RuntimeId = ReflectHelper.getField(self, "name2RuntimeId");
        final Int2ObjectMap<RuntimeItemMapping.LegacyEntry> runtime2Legacy = ReflectHelper.getField(self, "runtime2Legacy");
        final Int2ObjectMap<RuntimeItemMapping.RuntimeEntry> legacy2Runtime = ReflectHelper.getField(self, "legacy2Runtime");

        CustomItem.customItems.forEach((name, id) -> {
            paletteBuffer.putString(name);
            paletteBuffer.putLShort(id);
            paletteBuffer.putBoolean(false); // Component item

            int fullId = RuntimeItems.getFullId(id, 0);
            int legacyId = (id << 1) | 0;
            int runtimeId = id;

            runtimeId2Name.put(runtimeId, name);
            name2RuntimeId.put(name, runtimeId);

            RuntimeItemMapping.LegacyEntry legacyEntry = new RuntimeItemMapping.LegacyEntry(legacyId, false, 0);

            runtime2Legacy.put(runtimeId, legacyEntry);
            identifier2Legacy.put(name, legacyEntry);

            legacy2Runtime.put(fullId, new RuntimeItemMapping.RuntimeEntry(name, runtimeId, false));
        });

        CustomBlock.customBlocks.forEach((name, id) -> {
            for (int data = 0; data < 16; data++) {
                paletteBuffer.putString(name);
                paletteBuffer.putLShort(id);
                paletteBuffer.putBoolean(false); // Component item

                int fullId = RuntimeItems.getFullId(id, data);
                int legacyId = (id << 1) | 0;
                int runtimeId = id;

                runtimeId2Name.put(runtimeId, name);
                name2RuntimeId.put(name, runtimeId);

                RuntimeItemMapping.LegacyEntry legacyEntry = new RuntimeItemMapping.LegacyEntry(legacyId, false, 0);

                runtime2Legacy.put(runtimeId, legacyEntry);
                identifier2Legacy.put(name, legacyEntry);

                legacy2Runtime.put(fullId, new RuntimeItemMapping.RuntimeEntry(name, runtimeId, false));
            }
        });
        ReflectHelper.setField(self, "itemPalette", paletteBuffer.getBuffer());
    }
}
