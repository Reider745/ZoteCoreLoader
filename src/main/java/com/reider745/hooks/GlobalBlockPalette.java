package com.reider745.hooks;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.utils.MainLogger;
import com.google.common.io.ByteStreams;
import com.reider745.api.hooks.Arguments;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Injects;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Hooks(class_name = "cn.nukkit.level.GlobalBlockPalette")
public class GlobalBlockPalette {
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

    @Inject(signature = "()V")
    public static void init(HookController controller){
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

            /*try {
                CustomBlock.blocks.forEach((id, manager) -> {
                    final ArrayList<String> variants = CustomBlock.getVariants(manager);

                    for(int i = 0;i < variants.size();i++) {
                        final HashMap<String, Integer> state = new HashMap<>();
                        state.put("color", i);
                        BlockStateRegistry.registerState(id, i, state);

                        final long hash = computeStateHash(id, new JSONObject().put("color", i), stateIdByName);
                        if (stateHashToLegacyIdData.containsKey(hash))
                            throw new RuntimeException("hash collision: " + hash + " "+debug_hash.get(hash).toString());

                        debug_hash.put(hash, new JSONObject().put("block", id+":"+i));
                        stateHashToLegacyIdData.put(hash, (id << 16) | i);
                    }
                });
            }catch (Exception e){log.throwing(e);}*/

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

    @Injects(signature = {"(II)I", "(I)I"})
    public static int getLegacyFullId(HookController controller){
        int runtimeId = controller.getArguments().arg("runtimeId");
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject(signature = "(I)I", method = "getOrCreateRuntimeId")
    public static int getOrCreateRuntimeIdLegacy(HookController controller){
        int runtimeId = controller.getArguments().arg("legacyId");
        return runtimeIdToLegacy.get(runtimeId);
    }

    @Inject(signature = "(III)I", method = "getOrCreateRuntimeId")
    public static int getOrCreateRuntimeId(HookController controller){
        Arguments arguments = controller.getArguments();
        int id = arguments.arg("id");
        int meta = arguments.arg("meta");

        return get(id, meta);
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