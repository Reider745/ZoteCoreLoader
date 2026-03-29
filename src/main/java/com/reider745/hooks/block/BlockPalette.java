package com.reider745.hooks.block;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockDoor;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.format.leveldb.BlockStateMapping;
import cn.nukkit.level.format.leveldb.LevelDBConstants;
import cn.nukkit.level.format.leveldb.structure.BlockStateSnapshot;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.MainLogger;
import com.google.common.io.ByteStreams;
import com.reider745.InnerCoreServer;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.block.BlockNukkitIdConverter;
import com.reider745.block.BlockStateStorage;
import com.reider745.block.CustomBlock;
import com.reider745.block.wall.BlockWall;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Hooks(className = "cn.nukkit.level.GlobalBlockPalette")
public class BlockPalette {
    private static final int VERSION_STATE = 18168865;
    private static final MainLogger logger = Server.getInstance().getLogger();
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static int[] runtimeIdToLegacy;
    private static int[] runtimeIdToLegacyLevelDb;
    private static final Int2IntMap stateCapacity = new Int2IntOpenHashMap();
    private static final Map<String, Integer> stateIdByName = new HashMap<>();
    private static final Int2ObjectMap<String> nameByStateId = new Int2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<NbtMap, BlockStateSnapshot> paletteMap = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
        @Override
        public int hashCode(NbtMap nbtMap) {
            return nbtMap.hashCode();
        }

        @Override
        public boolean equals(NbtMap nbtMap, NbtMap nbtMap2) {
            return Objects.equals(nbtMap, nbtMap2);
        }
    });
    private static final Object2ObjectMap<NbtMap, BlockStateSnapshot> CACHE_PALETTE = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
        @Override
        public int hashCode(NbtMap nbtMap) {
            return nbtMap.hashCode();
        }

        @Override
        public boolean equals(NbtMap nbtMap, NbtMap nbtMap2) {
            return Objects.equals(nbtMap, nbtMap2);
        }
    });
    private static final Object2ObjectMap<NbtMap, Integer> levelDbRuntimeIdToData = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {
        @Override
        public int hashCode(NbtMap nbtMap) {
            return nbtMap.hashCode();
        }

        @Override
        public boolean equals(NbtMap nbtMap, NbtMap nbtMap2) {
            return Objects.equals(nbtMap, nbtMap2);
        }
    });

    private static final cn.nukkit.level.BlockPalette blockPalette = new cn.nukkit.level.BlockPalette(LevelDBConstants.PALETTE_VERSION);

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

    private record BlockDescription(int id, int newId, JSONObject states, int data, String identifier) {
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

    public static List<JSONObject> variantGenerations(List<Integer> states) {
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


    @Inject(className = "cn.nukkit.level.format.leveldb.NukkitLegacyMapper", type = TypeHook.AFTER_NOT_REPLACE)
    public static void registerStates(BlockStateMapping blockStateMapping) {
        final long start = System.currentTimeMillis();

        CustomBlock.init();

        legacyToRuntimeId.defaultReturnValue(-1);

        final ArrayList<BlockDescription> sortStates = new ArrayList<>();
        final ArrayList<Integer> doors = new ArrayList<>();

        try {
            // vanilla runtime block
            for (Object elem : BlockNukkitIdConverter.json) {
                final JSONObject e = (JSONObject) elem;

                final int newId = e.getInt("newId");
                final int legacyData = e.getInt("data");
                final JSONObject states = e.getJSONObject("states");

                if(!doors.contains(newId) && e.getString("nameId").contains("_door"))
                    doors.add(newId);

                sortStates.add(new BlockDescription(e.getInt("oldId"), newId, states, legacyData, "minecraft:" + e.getString("nameId")));
            }

            // custom runtime block
            final JSONObject EMPTY = new JSONObject();
            JSONObject blockMapping = EMPTY;
            try {
                blockMapping = new JSONObject(Files.readString(new File("block_mapping.json").toPath()));
            } catch (Exception ex) {
                Logger.warning("Malformed block state mappings: " + ex.getMessage());
            }

            JSONObject statesValues = blockMapping.optJSONObject("states_values", EMPTY);

            final Int2ObjectMap<BlockStateSnapshot> runtime2State = ReflectHelper.getField(blockStateMapping, "runtime2State");
            final Object2ObjectMap<NbtMap, BlockStateSnapshot> palette = ReflectHelper.getField(blockStateMapping, "paletteMap");
            final Int2IntMap runtimeIdToLegacyLeveldb = ReflectHelper.getField(blockPalette, "runtimeIdToLegacy");

            AtomicReference<Integer> RUNTIME_ID = new AtomicReference<>(runtimeIdToLegacyLeveldb.size());
            runtimeIdToLegacyLeveldb.forEach((id, legacy) -> RUNTIME_ID.set(Math.max(RUNTIME_ID.get(), id + 1)));
            CustomBlock.blocks.forEach((id, manager) -> {
                final List<JSONObject> states = variantGenerations(CustomBlock.getStates(manager));

                for (int data = 0; data < states.size(); data++) {
                    var state = states.get(data);
                    sortStates.add(new BlockDescription(id, id, state, data, manager.textId));

                    int runtimeId = RUNTIME_ID.getAndSet(RUNTIME_ID.get() + 1);

                    var builder = NbtMap.builder();

                    var builderState = NbtMap.builder();
                    state.keySet().forEach((name) -> {
                        JSONObject convert = statesValues.optJSONObject(name, EMPTY);
                        int value = state.getInt(name);
                        builderState.put(name, convert.has(String.valueOf(value)) ? convert.get(String.valueOf(value)) : value);
                    });
                    builder.put("states", builderState.build());
                    builder.putString("name", manager.textId);
                    builder.putInt("version", VERSION_STATE);

                    var nbt = builder.build();
                    nbt.hashCode();

                    if(blockStateMapping.containsState(nbt)) {
                        BlockStateSnapshot snapshot = blockStateMapping.getState(nbt);
                        runtime2State.remove(snapshot.getRuntimeId());
                        palette.remove(snapshot.getVanillaState());
                    }

                    blockStateMapping.registerState(runtimeId, nbt);
                    blockPalette.registerState(id, data, runtimeId, null);

                    levelDbRuntimeIdToData.put(nbt, data);
                    paletteMap.put(nbt, BlockStateSnapshot.builder()
                            .version(VERSION_STATE)
                            .vanillaState(nbt)
                            .runtimeId(runtimeId)
                            .build());
                }
            });

            final List<String> sortedStates = new ArrayList<>(stateIdByName.keySet());
            Collections.sort(sortedStates);

            sortStates.sort((a, b) -> {
                if (a.newId != b.newId) {
                    return Integer.compare(a.newId, b.newId);
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

                return 1;
            });

            String[] wallConnectionType = new String[] {"none", "short", "tall"};

            runtimeIdToLegacy = new int[sortStates.size()];
            for (int runtimeId = 0; runtimeId < sortStates.size(); runtimeId++) {
                final BlockDescription description = sortStates.get(runtimeId);
                final int blockMeta = description.data;

                int blockId = description.newId;
                int legacyId = (blockId << Block.DATA_BITS) | blockMeta;

                if(doors.contains(blockId)) {
                    int doorMeta = description.states.getInt("direction");

                    if(description.states.getInt("open_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_OPEN_BIT;

                    if(description.states.getInt("upper_block_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_TOP_BIT;

                    if(description.states.getInt("door_hinge_bit") == 1)
                        doorMeta = doorMeta | BlockDoor.DOOR_HINGE_BIT;

                    legacyId = (blockId << Block.DATA_BITS) | doorMeta;
                } else if(blockId == BlockID.COBBLESTONE_WALL || blockId == BlockID.BLACKSTONE_WALL || blockId == BlockID.POLISHED_BLACKSTONE_WALL || blockId == BlockID.POLISHED_BLACKSTONE_BRICK_WALL) {
                    int wallMeta = 0;

                    int east = description.states.getInt("wall_connection_type_east");
                    int west = description.states.getInt("wall_connection_type_west");
                    int north = description.states.getInt("wall_connection_type_north");
                    int south = description.states.getInt("wall_connection_type_south");
                    int postBit = description.states.getInt("wall_post_bit");

                    wallMeta = ((wallMeta & ~BlockWall.NORTH_MASK) | ((north << Integer.numberOfTrailingZeros(BlockWall.NORTH_MASK)) & BlockWall.NORTH_MASK));
                    wallMeta = ((wallMeta & ~BlockWall.WEST_MASK) | ((west << Integer.numberOfTrailingZeros(BlockWall.WEST_MASK)) & BlockWall.WEST_MASK));
                    wallMeta = ((wallMeta & ~BlockWall.EAST_MASK) | ((east << Integer.numberOfTrailingZeros(BlockWall.EAST_MASK)) & BlockWall.EAST_MASK));
                    wallMeta = ((wallMeta & ~BlockWall.SOUTH_MASK) | ((south << Integer.numberOfTrailingZeros(BlockWall.SOUTH_MASK)) & BlockWall.SOUTH_MASK));
                    wallMeta = ((wallMeta & ~BlockWall.POST_MASK) | ((postBit << Integer.numberOfTrailingZeros(BlockWall.POST_MASK)) & BlockWall.POST_MASK));

                    NbtMapBuilder vanillaState = NbtMap.builder();
                    NbtMapBuilder states = NbtMap.builder();

                    states.put("wall_connection_type_east", wallConnectionType[east]);
                    states.put("wall_connection_type_north", wallConnectionType[north]);
                    states.put("wall_connection_type_south", wallConnectionType[south]);
                    states.put("wall_connection_type_west", wallConnectionType[west]);
                    states.put("wall_post_bit", postBit == 1);

                    vanillaState.put("name", switch (blockId) {
                        case BlockID.COBBLESTONE_WALL -> "minecraft:cobblestone_wall";
                        case BlockID.BLACKSTONE_WALL -> "minecraft:blackstone_wall";
                        case BlockID.POLISHED_BLACKSTONE_WALL -> "minecraft:polisheed_blackstone_wall";
                        case BlockID.POLISHED_BLACKSTONE_BRICK_WALL -> "minecraft:polisheed_blackstone_brick_wall";
                        default -> throw new RuntimeException("Unknown block id " + blockId);
                    });

                    if(description.states.has("wall_block_type")) {
                        BlockWall.WallType wallType = BlockWall.WallType.values()[description.states.getInt("wall_block_type")];

                        states.put("wall_block_type", wallType.name().toLowerCase());

                        blockId = wallType.id;
                    }

                    vanillaState.put("states", states.build());
                    vanillaState.put("version", VERSION_STATE);

                    int runtimeIdLevelDb = RUNTIME_ID.getAndSet(RUNTIME_ID.get() + 1);
                    NbtMap buildStates = vanillaState.build();
                    buildStates.hashCode();
                    if(blockStateMapping.containsState(buildStates)) {
                        runtime2State.remove(palette.remove(buildStates).getRuntimeId());
                    }
                    blockStateMapping.registerState(runtimeIdLevelDb, vanillaState.build());
                    blockPalette.registerState(blockId, wallMeta, runtimeIdLevelDb, null);
                    legacyId = (blockId << Block.DATA_BITS) | wallMeta;
                }


                if (blockMeta < Block.DATA_SIZE) {
                    //if(!doors.contains(blockId) && legacyToRuntimeId.containsKey(legacyId))
                        //throw new RuntimeException("Confilict: " + Arrays.toString(BlockStateStorage.getBlockForRuntimeId(legacyToRuntimeId.get(legacyId))) + ", blockId: " + blockId + ", runtimeId: " + runtimeId + ", status: " + description.states);

                    legacyToRuntimeId.put(legacyId, runtimeId);
                    legacyToRuntimeId.putIfAbsent(legacyId, runtimeId);
                    runtimeIdToLegacy[runtimeId] = legacyId;

                    BlockStateStorage.addState(description.states, runtimeId, description.id, blockMeta, description.newId);
                } else {
                    throw new RuntimeException("Block " + description.id + " is too big, size: " + Block.DATA_SIZE + ", meta: " + blockMeta);
                }
            }

            runtimeIdToLegacyLevelDb = new int[RUNTIME_ID.get()];
            runtimeIdToLegacyLeveldb.forEach((runtimeId, legacyId) -> {
                runtimeIdToLegacyLevelDb[runtimeId] = legacyId;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Loading runtime blocks, time: " + (System.currentTimeMillis() - start) + "...");
    }

    public static int get(int id, int meta) {
        final int legacyId = id << Block.DATA_BITS | meta;

        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << Block.DATA_BITS);

            if (runtimeId == -1 && id != BlockID.INFO_UPDATE) {
                logger.info("failed to locate state for id: " + id + ":" + meta);

                runtimeId = legacyToRuntimeId.get(BlockID.INFO_UPDATE << Block.DATA_BITS);
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
        if(protocol == LevelDBConstants.PALETTE_VERSION) {
            if(runtimeId >= 0 && runtimeId < runtimeIdToLegacyLevelDb.length) {
                return runtimeIdToLegacyLevelDb[runtimeId];
            }
            return -1;
        }

        if(runtimeId >= 0 && runtimeId < runtimeIdToLegacy.length) {
            return runtimeIdToLegacy[runtimeId];
        }
        return -1;
    }

    @Inject
    public static int getLegacyFullId(int runtimeId) {
        return getLegacyFullId(InnerCoreServer.PROTOCOL, runtimeId);
    }

    @Inject
    public static int getLegacyFullId(int protocol, CompoundTag tag) {
        throw new RuntimeException("use getLegacyFullId(int, CompoundTag)");
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int id, int meta) {
        if(protocol == LevelDBConstants.PALETTE_VERSION) {
            return blockPalette.getRuntimeId(id, meta);
        }
        return get(id, meta);
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int legacyId) {
        return getOrCreateRuntimeId(protocol, legacyId >> 4, legacyId & 0xf);
    }

    @Inject
    public static int getOrCreateRuntimeId(int legacyId) {
        return getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, legacyId >> 4, legacyId & 0xf);
    }

    @Inject(className = "cn.nukkit.level.format.leveldb.BlockStateMapping")
    public static BlockStateSnapshot getStateUnsafe(BlockStateMapping self, NbtMap vanilla) {
        String id = vanilla.getString("name");
        if(CustomBlock.customBlocks.containsKey(id)) {
            BlockStateSnapshot snapshot = CACHE_PALETTE.get(vanilla);

            if(snapshot == null) {
                var customVanilla = vanilla.toBuilder();
                customVanilla.putInt("version", VERSION_STATE);

                snapshot = paletteMap.get(customVanilla.build());
                if(snapshot != null) {
                    CACHE_PALETTE.put(vanilla, snapshot);
                } else {
                    int damage = self.getState(self.updateVanillaState(vanilla)).getLegacyData();

                    for (NbtMap key : paletteMap.keySet()) {
                        if (key.getString("name").equals(id) && damage == levelDbRuntimeIdToData.getOrDefault(key, 0)) {
                            logger.warning("Replacing a unit with low compatibility: " + id + " " + customVanilla.build() + "  " + key);
                            snapshot = paletteMap.get(key);
                            CACHE_PALETTE.put(vanilla, snapshot);
                            break;
                        }
                    }
                }
            }

            if(snapshot != null) {
                return snapshot;
            } else {
                logger.warning("State not found: " + vanilla);
            }
        }

        return self.getState(self.updateVanillaState(vanilla));
    }
}
