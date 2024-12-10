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
    private static final ArrayList<Pair<Integer, Long>> assignedRuntimeIds = new ArrayList<>();

    private static long computeStateHash(int id, int[][] allStatesSorted) {
        long hash = (long) id;
        // hash = hash * 314159L + (long) data;

        for (int[] state : allStatesSorted) {
            hash = hash * 314159L + (long) (state[1] | ((state[0] + 1) << 5));
        }
        return hash;
    }

    private static long computeStateHash(int id, JSONObject statesJson, Map<String, Integer> stateIdByName) {
        ArrayList<int[]> states = new ArrayList<>();
        for (String key : statesJson.keySet()) {
            if (!stateIdByName.containsKey(key))
                Server.getInstance().getLogger().error("state not found: " + key);
            states.add(new int[] {
                    stateIdByName.get(key).intValue(),
                    statesJson.getInt(key)
            });
        }
        states.sort(Comparator.comparingInt(a -> a[0]));
        return computeStateHash(id, states.toArray(new int[0][0]));
    }

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

    public static ArrayList<Pair<Integer, Long>> getAssignedRuntimeIds() {
        return assignedRuntimeIds;
    }

    private static int getFlagBitLength(int flagBit) {
        int count = 0;
        while(flagBit != 1) {
            if (((flagBit & 1) == 1)) {
                break;
            }
            count++;
            flagBit >>>= 1;
        }
        return count;
    }

    private static int setDamage(int damage, int flagBit, int data) {
        return damage & ~flagBit | data << getFlagBitLength(flagBit) & flagBit;
    }

    public static void init() {
        final long start = System.currentTimeMillis();

        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        final HashMap<String, Integer> stateIdByName = getStateNameToId();
        final HashMap<Long, Map.Entry<Integer, JSONObject>> stateHashToLegacyIdData = new HashMap<>();
        final AtomicInteger runtimeId = new AtomicInteger(1);

        final ArrayList<Integer> doors = new ArrayList<>();

        BlockStateRegisters.init(stateIdByName);


        try {
            // vanilla runtime block
            for (Object elem : BlockNukkitIdConverter.json) {
                final JSONObject e = (JSONObject) elem;

                final int newId = e.getInt("newId");
                final int legacyData = e.getInt("data");
                final JSONObject states = e.getJSONObject("states");

                final long hash = computeStateHash(newId, states, stateIdByName);
                if (stateHashToLegacyIdData.containsKey(hash))
                    throw new RuntimeException("hash collision: " + hash + " " + e);

                if(e.getString("nameId").contains("_door") && !doors.contains(newId))
                    doors.add(newId);

                BlockStateRegisters.addState(hash, states, runtimeId.getAndIncrement(), e.getInt("oldId"), legacyData, newId);
                stateHashToLegacyIdData.put(hash, new AbstractMap.SimpleEntry<>(newId << 16 | legacyData, states));
            }


            // custom runtime block
            final JSONObject[] states = new JSONObject[16];
            for(int i = 0;i < states.length;i++)
                states[i] = new JSONObject().put("color", i);

            CustomBlock.blocks.forEach((id, manager) -> {
                final ArrayList<String> variants = CustomBlock.getVariants(manager);

                for (int i = 0; i < variants.size(); i++) {
                    final JSONObject state = states[i];
                    final long hash = computeStateHash(id, state, stateIdByName);

                    BlockStateRegisters.addState(hash, state, runtimeId.getAndIncrement(), id, i, id);
                    stateHashToLegacyIdData.put(hash, new AbstractMap.SimpleEntry<>(id << 16 | i, state));
                }
            });

            final ArrayList<Long> sortedStateHash = new ArrayList<>(stateHashToLegacyIdData.keySet());
            sortedStateHash.sort((a, b) -> {
                // uint64_t comparator
                if (a < 0 && b >= 0)
                    return 1;
                if (a >= 0 && b < 0)
                    return -1;
                return Long.compare(a, b);
            });


            for (int i = 0; i < sortedStateHash.size(); i++) {
                final long hash = sortedStateHash.get(i);
                final Map.Entry<Integer, JSONObject> entry = stateHashToLegacyIdData.get(hash);

                final int idAndData = entry.getKey();
                final JSONObject state = entry.getValue();

                final int blockId = idAndData >> 16;
                final int blockMeta = idAndData & 0xffff;

                int legacyId = (blockId << 6) | blockMeta;

                if(doors.contains(blockId)) {
                    int doorMeta = state.getInt("direction");

                    if(state.getInt("open_bit") == 1)
                        doorMeta = doorMeta ^ BlockDoor.DOOR_OPEN_BIT;

                    if(state.getInt("upper_block_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_TOP_BIT;

                    if(state.getInt("door_hinge_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_HINGE_BIT;

                    legacyId = (blockId << 6) | doorMeta;
                }

                if (blockMeta < 64) {
                    legacyToRuntimeId.put(legacyId, i);
                    legacyToRuntimeId.putIfAbsent(legacyId, i);
                    runtimeIdToLegacy.put(i, legacyId);
                }

                assignedRuntimeIds.add(new Pair<>(i, hash));
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
