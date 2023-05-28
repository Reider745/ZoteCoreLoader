package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockMeta;
import cn.nukkit.level.GlobalBlockPalette;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;

public class BlockState {
    public final int id;
    public final int data;
    public final int runtimeId;

    private int[] rawStates = null;
    private Map<Integer, Integer> states = null;
    private Map<String, Integer> namedStates = null;

    public BlockState(int id, int data) {
        this.id = id;
        this.data = data;
        this.runtimeId = -1;
    }

    public BlockState(Block idDataAndState) {
        /*long idData = idDataAndState & (long) 0xFFFFFFFF;
        this.id = (int) (idData >> (long) 16) & 0xFFFF;
        this.data = (int) (idData & (long) 0xFFFF);
        this.runtimeId = (int) (idDataAndState >> (long) 32);*/
        this.id = idDataAndState.getId();
        if(idDataAndState instanceof BlockMeta) {
            BlockMeta meta = (BlockMeta) idDataAndState;
            this.data = meta.getDamage();
        }else
            this.data = 0;
        this.runtimeId = GlobalBlockPalette.getOrCreateRuntimeId(id, data);
    }

    public BlockState(int id, Map<?, Integer> map) {
        this.id = id;

        rawStates = new int[map.size() * 2];
        int index = 0;
        for (Map.Entry<?, Integer> entry : map.entrySet()) {
            Object key0 = entry.getKey();
            int key = -1;
            if (key0 instanceof Integer) {
                key = (int) key0;
            } else if (key0 instanceof CharSequence) {
                key = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_states", key0.toString()), -1);
            }
            if (key == -1) {
                continue;
            }
            rawStates[index++] = key;
            rawStates[index++] = entry.getValue();
        }

        this.runtimeId = runtimeIdByStates(id, rawStates);
        this.data = getDataFromRuntimeId(runtimeId);
    }

    private BlockState(int id, int[] rawStates) {
        this.id = id;
        this.rawStates = rawStates;
        this.runtimeId = runtimeIdByStates(id, rawStates);
        this.data = getDataFromRuntimeId(runtimeId);
    }

    private static Map<Integer, Integer> scriptableToStateMap(Scriptable scriptable) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Object key0 : scriptable.getIds()) {
            String key = key0.toString();

            int state = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_states", key), -1);
            if (state == -1) {
                try {
                    state = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            Object value = scriptable.get(key, scriptable);
            result.put(state, value instanceof Number ? ((Number) value).intValue() : 0);
        }
        return result;
    }

    public BlockState(int id, Scriptable scriptable) {
        this(id, scriptableToStateMap(scriptable));
    }

    public int getId() {
        return id;
    }

    public int getData() {
        return data;
    }

    public int getRuntimeId() {
        return runtimeId;
    }

    public boolean isValidState() {
        return runtimeId != -1;
    }

    public int getState(int state) {
        return runtimeId > 0 ? getStateFromId(runtimeId, state) : -1;
    }

    public boolean hasState(int state) {
        return getState(state) != -1;
    }

    public BlockState addState(int state, int value) {
        Map<Integer, Integer> mStates = new HashMap<>(getStates());
        mStates.put(state, value);
        return new BlockState(id, mStates);
    }

    public BlockState addStatesMap(Map<?, Integer> states) {
        Map<Object, Integer> mStates = new HashMap<>(getStates());
        mStates.putAll(states);
        return new BlockState(id, mStates);
    }

    public BlockState addStates(Scriptable states) {
        return addStatesMap(scriptableToStateMap(states));
    }

    public Map<Integer, Integer> getStates() {
        if (states == null) {
            states = new HashMap<>();
            if (runtimeId > 0) {
                rawStates = rawStates != null ? rawStates : getAllStatesFromId(runtimeId);
                for (int i = 0; i < rawStates.length / 2; i++) {
                    states.put(rawStates[i * 2], rawStates[i * 2 + 1]);
                }
            }
        }
        return states;
    }

    public Map<String, Integer> getNamedStates() {
        if (namedStates == null) {
            namedStates = new HashMap<>();
            Map<Integer, Integer> states = getStates();
            for (Map.Entry<Integer, Integer> entry : states.entrySet()) {
                namedStates.put(GameEnums.getSingleton().getKeyForEnum("block_states", entry.getKey()), entry.getValue());
            }
        }
        return namedStates;
    }

    public ScriptableObject getStatesScriptable() {
        Map<Integer, Integer> states = getStates();
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (Map.Entry<Integer, Integer> entry : states.entrySet()) {
            result.put(entry.getKey().toString(), result, entry.getValue());
        }
        return result;
    }

    public ScriptableObject getNamedStatesScriptable() {
        Map<String, Integer> states = getNamedStates();
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (Map.Entry<String, Integer> entry : states.entrySet()) {
            result.put(entry.getKey(), result, entry.getValue());
        }
        return result;
    }

    @Override
    public String toString() {
        return "BlockState{id=" + id + ", data=" + data + ", runtimeId=" + runtimeId + ", states=" + getNamedStates() + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        BlockState that = (BlockState) object;

        if (id != that.id) return false;
        if (data != that.data) return false;
        if (runtimeId != that.runtimeId) return false;
        return getStates().equals(that.getStates());
    }

    private static native int getStateFromId(int runtimeId, int state);
    private static native int[] getAllStatesFromId(int runtimeId); // [state1, value1, state2, value2, ...]
    public static native int runtimeIdByStates(int id, int[] statesAndValues); // -1 is returned in case it is not found
    public static native int getIdFromRuntimeId(int runtimeId);
    public static native int getDataFromRuntimeId(int runtimeId);
}
