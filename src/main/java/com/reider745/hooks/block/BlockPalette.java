package com.reider745.hooks.block;

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
import com.reider745.block.BlockStateStorage;
import com.reider745.block.CustomBlock;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;

@Hooks(className = "cn.nukkit.level.GlobalBlockPalette")
public class BlockPalette {
    private static final MainLogger logger = Server.getInstance().getLogger();
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private static final Int2IntMap stateCapacity = new Int2IntOpenHashMap();
    private static final Map<String, Integer> stateIdByName = new HashMap<>();
    private static final Map<Integer, String> nameByStateId = new HashMap<>();

    static {
        try {
            JSONObject json = new JSONObject(new String(ByteStreams
                    .toByteArray(Server.class.getClassLoader().getResourceAsStream("state-name-to-id.json"))));
            for (String key : json.keySet()) {
                final int id = json.getInt(key);
                stateIdByName.put(key, id);
                nameByStateId.put(id, key);
            }

            json = new JSONObject(new String(ByteStreams
                    .toByteArray(Server.class.getClassLoader().getResourceAsStream("state-capacity.json"))));
            for (String key : json.keySet()) {
                stateCapacity.put(stateIdByName.get(key).intValue(), json.getInt(key));
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerBlockState(String name, int id, int capacity) {
        stateIdByName.put(name, id);
        nameByStateId.put(id, name);
        setCapacityForId(id, capacity);
    }

    public static void setCapacityForId(int id, int capacity) {
        stateCapacity.put(id, capacity);
    }

    public static int getCapacityForId(int id) {
        return stateCapacity.get(id);
    }

    @Nullable
    public static String getNameForId(int id) {
        return nameByStateId.get(id);
    }

    public static int getIdByName(String name) {
        return stateIdByName.get(name);
    }

    private record BlockDescription(int id, int newId, JSONObject states, int data) {
    }

    private static JSONObject cloneJSON(JSONObject json) {
        final JSONObject cloneJson = new JSONObject();
        for(String key : json.keySet())
            cloneJson.put(key, json.get(key));
        return cloneJson;
    }

    private static boolean containsJSON(List<JSONObject> list, JSONObject states) {
        for(JSONObject check : list) {
            boolean result = true;

            for(String key : states.keySet())
                if(!check.get(key).equals(states.get(key))) {
                    result = false;
                    break;
                }

            if(result) {
                return true;
            }
        }
        return false;
    }

    private static void variantGenerations(List<Integer> states, List<JSONObject> variants, JSONObject temp, int offset) {
        if (states.size() - offset >= 0) {
            final Integer currentState = states.get(states.size() - offset);

            for (int value = 0; value < getCapacityForId(currentState); value++) {
                temp.put(getNameForId(currentState), value);
                if(!containsJSON(variants, temp))
                    variants.add(cloneJSON(temp));
                variantGenerations(states, variants, temp, offset + 1);
            }

            temp.put(getNameForId(currentState), 0);
        }
    }

    private static List<JSONObject> variantGenerations(List<Integer> states) {
        final JSONObject temp = new JSONObject();
        for(int state : states) {
            temp.put(getNameForId(state), 0);
        }

        final List<JSONObject> variants = new ArrayList<>();
        if(states.isEmpty()) {
            variants.add(temp);
        }

        variantGenerations(states, variants, temp, 1);
        return variants;
    }


    public static void init() {
        final long start = System.currentTimeMillis();

        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        final ArrayList<BlockDescription> sortStates = new ArrayList<>();
        final ArrayList<Integer> doors = new ArrayList<>();

        try {
            // vanilla runtime block
            for (Object elem : BlockNukkitIdConverter.json) {
                final JSONObject e = (JSONObject) elem;

                final int newId = e.getInt("newId");
                final int legacyData = e.getInt("data");
                final JSONObject states = e.getJSONObject("states");

                if(e.getString("nameId").contains("_door") && !doors.contains(newId))
                    doors.add(newId);

                sortStates.add(new BlockDescription(e.getInt("oldId"), newId, states, legacyData));
            }

            // custom runtime block
            CustomBlock.blocks.forEach((id, manager) -> {
                final ArrayList<String> variants = CustomBlock.getVariants(manager);
                final List<JSONObject> states = variantGenerations(CustomBlock.getStates(manager));

                if(id == 8275)
                    System.out.println(states);

                for (int data = 0; data < variants.size(); data++) {
                    sortStates.add(new BlockDescription(id, id, states.get(data), data));
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

            for (int runtimeId = 0; runtimeId < sortStates.size(); runtimeId++) {
                final BlockDescription description = sortStates.get(runtimeId);

                final int blockId = description.newId;
                final int blockMeta = description.data;

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
                    legacyToRuntimeId.put(legacyId, runtimeId);
                    legacyToRuntimeId.putIfAbsent(legacyId, runtimeId);
                    runtimeIdToLegacy.put(runtimeId, legacyId);

                    BlockStateStorage.addState(description.states, runtimeId, description.id, blockMeta, description.newId);
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
