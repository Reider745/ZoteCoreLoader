package cn.nukkit.level;

import android.util.Pair;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import com.google.common.io.ByteStreams;
import com.reider745.block.CustomBlock;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;

@Log4j2
public class GlobalBlockPalette {
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private static final ArrayList<Pair<Integer, Long>> assignedRuntimeIds = new ArrayList<>();

    private static final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);

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
                log.error("state not found: " + key);
            states.add(new int[] {
                    stateIdByName.get(key),
                    statesJson.getInt(key)
            });
        }
        states.sort(Comparator.comparingInt(a -> a[0]));
        return computeStateHash(id, states.toArray(new int[0][0]));
    }

    static {
        Server.getInstance().getLogger().info("Loading runtime blocks...");
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        HashMap<String, Integer> stateIdByName = new HashMap<>();
        try {
            JSONObject json = new JSONObject(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("state-name-to-id.json"))));
            for (String key : json.keySet()) {
                stateIdByName.put(key, json.getInt(key));
            }
        }catch (Exception e){log.error(e.getMessage());}

        HashMap<Long, Integer> stateHashToLegacyIdData = new HashMap<>();
        HashMap<Long, JSONObject> debug_hash = new HashMap<>();
        try {
            JSONArray json = new JSONArray(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("network-id-dump.json"))));
            for (Object elem : json) {
                JSONObject e = (JSONObject) elem;
                int legacyId = e.getInt("oldId");
                int legacyData = e.getInt("data");

                if(legacyId > 8000)
                    throw new IOException("Ты чё еблан? Какова хуя в дампе блок с id "+legacyId);

                long hash = computeStateHash(e.getInt("newId"), e.getJSONObject("states"), stateIdByName);
                if (stateHashToLegacyIdData.containsKey(hash))
                    throw new RuntimeException("hash collision: " + hash + " "+e.toString());
                debug_hash.put(hash, e);
                // log.info(e.getString("nameId") + legacyId + ":" + legacyData + " -> " + hash);
                stateHashToLegacyIdData.put(hash, (legacyId << 16) | legacyData);
            }
        }catch (Exception e){log.throwing(e);}

        try {
            CustomBlock.blocks.forEach((id, manager) -> {
                ArrayList<String> variants = CustomBlock.getVariants(manager);

                for(int i = 0;i < variants.size();i++) {
                    long hash = computeStateHash(id, new JSONObject().put("color", i), stateIdByName);
                    if (stateHashToLegacyIdData.containsKey(hash))
                        throw new RuntimeException("hash collision: " + hash + " "+debug_hash.get(hash).toString());
                    debug_hash.put(hash, new JSONObject().put("block", id+":"+i));
                    stateHashToLegacyIdData.put(hash, (id << 16) | i);
                }
            });
        }catch (Exception e){log.throwing(e);}

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

            // log.info("assigned runtime id " + i + " to " + (legacyIdData >> 16) + ":" + (legacyIdData & 0xffff) + " hash=" + hash);
        }
    }

    public static ArrayList<Pair<Integer, Long>> getAssignedRuntimeIds() {
        return assignedRuntimeIds;
    }

    public static int getOrCreateRuntimeId(int id, int meta) {
        int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << 6);
            if (runtimeId == -1 && id != BlockID.INFO_UPDATE) {
                log.error("failed to locate state for id: " + id + ":" + meta);
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

    public static int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> 4, legacyId & 0xf);
    }

    public static int getLegacyFullId(int runtimeId) {
        return runtimeIdToLegacy.get(runtimeId);
    }
}
