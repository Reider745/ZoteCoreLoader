package com.reider745.hooks.block;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockID;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.MainLogger;
import com.google.common.io.ByteStreams;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.block.BlockNukkitIdConverter;
import com.reider745.block.BlockStateRegisters;
import com.reider745.block.CustomBlock;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Hooks(className = "cn.nukkit.level.GlobalBlockPalette")
public class BlockPalette {
    private static final MainLogger logger = Server.getInstance().getLogger();
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();

    private static HashMap<String, Integer> getStateNameToId() {
        try {
            final HashMap<String, Integer> stateIdByName = new HashMap<>();
            JSONObject json = new JSONObject(new String(ByteStreams
                    .toByteArray(Server.class.getClassLoader().getResourceAsStream("state-name-to-id.json"))));
            for (String key : json.keySet()) {
                stateIdByName.put(key, json.getInt(key));
            }
            return stateIdByName;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HashMap<String, Integer> getStateCapacity() {
        try {
            final HashMap<String, Integer> capacity = new HashMap<>();
            JSONObject json = new JSONObject(new String(ByteStreams
                    .toByteArray(Server.class.getClassLoader().getResourceAsStream("state-capacity.json"))));
            for (String key : json.keySet()) {
                capacity.put(key, json.getInt(key));
            }
            return capacity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class BlockDescription {
        private final int id, idAndData, newId;
        private final JSONObject states;

        public BlockDescription(int id, int idAndData, int newId, JSONObject states) {
            this.id = id;
            this.newId = newId;
            this.idAndData = idAndData;
            this.states = states;
        }
    }

    public static void init() {
        final long start = System.currentTimeMillis();

        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        final HashMap<String, Integer> stateIdByName = getStateNameToId();
        final HashMap<String, Integer> capacity = getStateCapacity();
        final ArrayList<BlockDescription> sortStates = new ArrayList<>();
        final ArrayList<Integer> doors = new ArrayList<>();

        BlockStateRegisters.init(stateIdByName);

        try {
            // vanilla runtime block
            for (Object elem : BlockNukkitIdConverter.json) {
                final JSONObject e = (JSONObject) elem;

                final int newId = e.getInt("newId");
                final int legacyData = e.getInt("data");
                final JSONObject states = e.getJSONObject("states");

                if(e.getString("nameId").contains("_door") && !doors.contains(newId))
                    doors.add(newId);

                sortStates.add(new BlockDescription(e.getInt("oldId"), newId << 16 | legacyData, newId, states));
            }

            // custom runtime block
            final JSONObject[] states = new JSONObject[16];
            for(int i = 0;i < states.length;i++)
                states[i] = new JSONObject().put("color", i);

            CustomBlock.blocks.forEach((id, manager) -> {
                final ArrayList<String> variants = CustomBlock.getVariants(manager);

                for (int i = 0; i < variants.size(); i++) {
                    final JSONObject state = states[i];
                    sortStates.add(new BlockDescription(id, id << 16 | i, id, state));
                }
            });

            final List<String> sortedStates = new ArrayList<>(stateIdByName.keySet());
            Collections.sort(sortedStates);

            sortStates.sort((a, b) -> {
                if (a.id != b.id) {
                    return Integer.compare(a.id, b.id);
                }

                for (String name : sortedStates) {
                    boolean hasStateA = a.states.has(name);
                    boolean hasStateB = b.states.has(name);

                    if (hasStateA != hasStateB) {
                        return Boolean.compare(hasStateA, hasStateB);
                    } else if (hasStateA) {
                        int valueA = a.states.getInt(name);
                        int valueB = b.states.getInt(name);
                        if (valueA != valueB) {
                            return Integer.compare(valueA, valueB);
                        }
                    }
                }

                return 0;
            });

            for (int i = 0; i < sortStates.size(); i++) {
                final BlockDescription description = sortStates.get(i);

                final int blockId = description.idAndData >> 16;
                final int blockMeta = description.idAndData & 0xffff;

                int legacyId = (blockId << 6) | blockMeta;

                if(doors.contains(blockId)) {
                    int doorMeta = description.states.getInt("direction");

                    if(description.states.getInt("open_bit") == 1)
                        doorMeta = doorMeta ^ BlockDoor.DOOR_OPEN_BIT;

                    if(description.states.getInt("upper_block_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_TOP_BIT;

                    if(description.states.getInt("door_hinge_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_HINGE_BIT;

                    legacyId = (blockId << 6) | doorMeta;
                }

                if (blockMeta < 64) {
                    legacyToRuntimeId.put(legacyId, i);
                    legacyToRuntimeId.putIfAbsent(legacyId, i);
                    runtimeIdToLegacy.put(i, legacyId);

                    BlockStateRegisters.addState(description.states, i, description.id, blockMeta, description.newId);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Loading runtime blocks, time: " + (System.currentTimeMillis() - start) + "...");
    }

    public static int get(int id, int meta) {
        final int legacyId = id << 6 | meta;

        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << 6);

            if (runtimeId == -1 && id != BlockID.INFO_UPDATE) {
                logger.error("failed to locate state for id: " + id + ":" + meta);

                runtimeId = legacyToRuntimeId.get(BlockID.INFO_UPDATE << 6);
                if (runtimeId == -1)
                    throw new RuntimeException("failed to fallback to BlockID.INFO_UPDATE state " + id + " " + meta);
                return runtimeId;
            } else if (id == BlockID.INFO_UPDATE) {
                throw new IllegalStateException("BlockID.INFO_UPDATE state is missing! " + id + " " + meta);
            }
        }

        return runtimeId;
    }

    @Inject
    public static int getLegacyFullId(int protocol, int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject
    public static int getLegacyFullId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject
    public static int getLegacyFullId(int protocol, CompoundTag tag) {
        throw new RuntimeException("use getLegacyFullId(int, CompoundTag)");
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        return get(id, meta);
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int legacyId) {
        return get(legacyId >> 4, legacyId & 0xf);
    }

    @Inject
    public static int getOrCreateRuntimeId(int legacyId) {
        return getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, legacyId >> 4, legacyId & 0xf);
    }
}
