package com.reider745.hooks;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.MainLogger;
import com.google.common.io.ByteStreams;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.block.CustomBlock;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import android.animation.ValueAnimator;

import java.io.IOException;
import java.util.*;

@Hooks(class_name = "cn.nukkit.level.GlobalBlockPalette")
public class GlobalBlockPalette implements HookClass {
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private static final ArrayList<Pair<Integer, Long>> assignedRuntimeIds = new ArrayList<>();
    private static long computeStateHash(int id, int[][] allStatesSorted) {
        long hash = (long) id;
        //hash = hash * 314159L + (long) data;

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

    public static ArrayList<Pair<Integer, Long>> getAssignedRuntimeIds() {
        return assignedRuntimeIds;
    }

    public static class BlockState {
        private final int id;
        private final int data;
        private final HashMap<String, Integer> states;

        public BlockState(final int id, final int data, final HashMap<String, Integer> states){
            this.id = id;
            this.data = data;
            this.states = states;
        }

        public final int getBlockId() {
            return id;
        }

        public final int getData() {
            return data;
        }

        public HashMap<String, Integer> getStates() {
            return states;
        }

        public Block getBlockRepairing(Level level, BlockVector3 pos, int layer){
            return level.getBlock(pos.x, pos.y, pos.z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockState that = (BlockState) o;
            return id == that.id && data == that.data && Objects.equals(states, that.states);
        }
    }

    private static final HashMap<String, BlockState> statesForIdAndData = new HashMap<>();

    public static void registerState(JSONObject jsonObject){
        int stateId = jsonObject.getInt("stateId");
        String nameId = jsonObject.getString("nameId");
        JSONObject states = jsonObject.getJSONObject("states");

        final HashMap<String, Integer> states_result = new HashMap<>();

        Iterator<String> it = states.keys();
        while (it.hasNext()) {
            String key = it.next();
            states_result.put(key, states.getInt(key));
        }

        registerState(jsonObject.getInt("oldId"), jsonObject.getInt("data"), states_result);
    }

    public static void registerState(final int id, final int data, final HashMap<String, Integer> states){
        BlockState state = new BlockState(id, data, states);
        statesForIdAndData.put(id+":"+data, state);
    }

    public static BlockState getForBlock(final Block block){
        return statesForIdAndData.get(block.getId()+":"+block.getDamage());
    }

    @Inject
    public static void init(){
        MainLogger log = Server.getInstance().getLogger();
        log.info("Loading runtime blocks...");
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);



        HashMap<String, Integer> stateIdByName = new HashMap<>();
        try {
            JSONObject json = new JSONObject(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("state-name-to-id.json"))));
            for (String key : json.keySet()) {
                stateIdByName.put(key, json.getInt(key));
            }
        }catch (Exception e){log.error(e.getMessage());}

        final HashMap<Long, Integer> stateHashToLegacyIdData = new HashMap<>();
        final HashMap<Long, JSONObject> debug_hash = new HashMap<>();
        try {
            final JSONArray json = new JSONArray(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("network-id-dump.json"))));
            for (Object elem : json) {
                final JSONObject e = (JSONObject) elem;

                //BlockStateRegistry.registerState(e);

                final int legacyId = e.getInt("oldId");
                final int legacyData = e.getInt("data");

                if(legacyId > 8000)
                    throw new IOException("Ты чё еблан? Какова хуя в дампе блок с id "+legacyId);

                final long hash = computeStateHash(e.getInt("newId"), e.getJSONObject("states"), stateIdByName);
                if (stateHashToLegacyIdData.containsKey(hash))
                    throw new RuntimeException("hash collision: " + hash + " "+e);
                debug_hash.put(hash, e);

                // log.info(e.getString("nameId") + legacyId + ":" + legacyData + " -> " + hash);
                stateHashToLegacyIdData.put(hash, (legacyId << 16) | legacyData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

            try {
                CustomBlock.blocks.forEach((id, manager) -> {
                    final ArrayList<String> variants = CustomBlock.getVariants(manager);

                    for(int i = 0;i < variants.size();i++) {
                        final HashMap<String, Integer> state = new HashMap<>();
                        state.put("color", i);
                        registerState(id, i, state);

                        final long hash = computeStateHash(id, new JSONObject().put("color", i), stateIdByName);
                        if (stateHashToLegacyIdData.containsKey(hash))
                            throw new RuntimeException("hash collision: " + hash + " "+debug_hash.get(hash).toString());

                        debug_hash.put(hash, new JSONObject().put("block", id+":"+i));
                        stateHashToLegacyIdData.put(hash, (id << 16) | i);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

        ArrayList<Long> sortedStateHash = new ArrayList<>(stateHashToLegacyIdData.keySet());
        sortedStateHash.sort((a, b) -> {
            // uint64_t comparator
            if (a < 0 && b >= 0)
                return 1;
            if (a >= 0 && b < 0)
                return -1;
            return Long.compare(a, b);
        });

        for (int i = 0; i < sortedStateHash.size(); i++) {
            long hash = sortedStateHash.get(i);
            int legacyIdData = stateHashToLegacyIdData.get(hash);
            int legacyId = legacyIdData >> 16;
            int legacyData = legacyIdData & 0xffff;
            if (legacyData < 64) {
                legacyToRuntimeId.put((legacyId << 6) | legacyData, i);
                legacyToRuntimeId.putIfAbsent((legacyId << 6), i);
                runtimeIdToLegacy.put(i, (legacyId << 6) | legacyData);
            }
            assignedRuntimeIds.add(new Pair<>(i, hash));

//            log.info("assigned runtime id " + i + " to " + (legacyIdData >> 16) + ":" + (legacyIdData & 0xffff) + " hash=" + hash);
        }
    }

    @Inject
    public static int getLegacyFullId(int protocol, int runtimeId){
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject
    public static int getLegacyFullId(int runtimeId){
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject
    public static int getLegacyFullId(int protocol, CompoundTag tag){
        throw new RuntimeException("use getLegacyFullId(int, CompoundTag)");
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int id, int meta){
        return get(id, meta);
    }

    @Inject
    public static int getOrCreateRuntimeId(int protocol, int legacyId) {
        return get(legacyId >> 4, legacyId & 0xf);
    }

    @Inject
    public static int getOrCreateRuntimeId(int legacyId){
        return getOrCreateRuntimeId(ProtocolInfo.CURRENT_PROTOCOL, legacyId >> 4, legacyId & 0xf);
    }

    private static int get(int id, int meta){
        int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << 6);
            if (runtimeId == -1 && id != BlockID.INFO_UPDATE) {
                Server.getInstance().getLogger().error("failed to locate state for id: " + id + ":" + meta);
                runtimeId = legacyToRuntimeId.get(BlockID.INFO_UPDATE << 6);
                if (runtimeId == -1)
                    throw new RuntimeException("failed to fallback to BlockID.INFO_UPDATE state "+id+" "+meta);
                return runtimeId;
            } else if (id == BlockID.INFO_UPDATE){
                throw new IllegalStateException("BlockID.INFO_UPDATE state is missing! "+id+" "+meta);
            }
        }
        return runtimeId;
    }

}