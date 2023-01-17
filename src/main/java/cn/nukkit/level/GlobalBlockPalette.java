package cn.nukkit.level;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import com.google.common.io.ByteStreams;
import com.reider745.InnerCoreServer;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class GlobalBlockPalette {
    private static final Int2IntMap legacyToRuntimeId = new Int2IntOpenHashMap();
    private static final Int2IntMap runtimeIdToLegacy = new Int2IntOpenHashMap();
    private static final AtomicInteger runtimeIdAllocator = new AtomicInteger(0);


    private static final HashMap<Long, Integer> runtimeIds = new HashMap<>();

    static {
        legacyToRuntimeId.defaultReturnValue(-1);
        runtimeIdToLegacy.defaultReturnValue(-1);

        /*ListTag<CompoundTag> tag;
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("runtime_block_states.dat")) {
            if (stream == null) {
                throw new AssertionError("Unable to locate block state nbt");
            }
            //noinspection unchecked
            tag = (ListTag<CompoundTag>) NBTIO.readTag(new ByteArrayInputStream(ByteStreams.toByteArray(stream)), ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new AssertionError("Unable to load block palette", e);
        }

        for (CompoundTag state : tag.getAll()) {
            int runtimeId = runtimeIdAllocator.getAndIncrement();
            if (!state.contains("LegacyStates")) continue;

            List<CompoundTag> legacyStates = state.getList("LegacyStates", CompoundTag.class).getAll();

            // Resolve to first legacy id
            CompoundTag firstState = legacyStates.get(0);
            runtimeIdToLegacy.put(runtimeId, firstState.getInt("id") << 6 | firstState.getShort("val"));

            for (CompoundTag legacyState : legacyStates) {
                int legacyId = legacyState.getInt("id") << 6 | legacyState.getShort("val");
                legacyToRuntimeId.put(legacyId, runtimeId);
            }
        }*/
        try {
            JSONArray jsonArray = new JSONArray(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("network-id-dump.json"))));
            jsonArray.forEach((element) -> {
                JSONObject value = (JSONObject) element;
                addRuntimeId(value.getInt("newId"), value.getInt("data"), value.getInt("dbgStateId"));
            });
        }catch (Exception e){log.error(e.getMessage());}

    }

    public static void addRuntimeId(int id, int data, Integer runtimeId){
        runtimeIds.put(InnerCoreServer.hash(id, data, new int[] {}), runtimeId);
    }

    public static int getOrCreateRuntimeId(int id, int meta) {
        Long key = InnerCoreServer.hash(id, meta, new int[] {});
        Integer runtimeId = runtimeIds.get(key);
        if(runtimeId == null){
            runtimeId = runtimeIdAllocator.getAndIncrement();
            log.info("Creating new runtime ID for unknown block {}, {}", id, meta);
            addRuntimeId(id, meta, runtimeId);
        }
        return runtimeId;
        /*int legacyId = id << 6 | meta;
        int runtimeId = legacyToRuntimeId.get(legacyId);
        if (runtimeId == -1) {
            runtimeId = legacyToRuntimeId.get(id << 6);
            if (runtimeId == -1) {
                runtimeId = runtimeIdAllocator.getAndIncrement();
                legacyToRuntimeId.put(id << 6, runtimeId);
                runtimeIdToLegacy.put(runtimeId, id << 6);
            }
        }
        return runtimeId;*/
    }

    public static int getOrCreateRuntimeId(int legacyId) throws NoSuchElementException {
        return getOrCreateRuntimeId(legacyId >> 4, legacyId & 0xf);
    }
}
