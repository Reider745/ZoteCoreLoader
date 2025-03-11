package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockMeta;
import cn.nukkit.level.GlobalBlockPalette;
import com.reider745.InnerCoreServer;
import com.reider745.block.BlockStateRegisters;
import com.zhekasmirnov.apparatus.minecraft.enums.EnumsContainer;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.Map;

public class BlockState {
    private static final HashMap<String, Integer> ALL_STATES = new HashMap<>();
    private static final HashMap<String, Integer> VANILLA_STATES = new HashMap<>();
    private static final HashMap<Integer, Integer> CAPACITY_STATES = new HashMap<>();

    static {
        final EnumsContainer.Scope scope = GameEnums.getSingleton().getOrAddContainerForVersion(MinecraftVersions.getCurrent())
                .getScope("block_states");

        for(String name : scope.getAllEnumNames()) {
            final int id = ((Number) scope.getEnum(name)).intValue();

            ALL_STATES.put(name, id);
            VANILLA_STATES.put(name, id);
        }
    }

    public static int STATE_ID = 114;

    public static int registerBlockState(String name, int capacity) {
        if(VANILLA_STATES.containsKey(name))
            throw new RuntimeException("You can't create a state with a vanilla name");

        if(capacity <= 0)
            throw new RuntimeException("capacity state <= 0");

        final int id = ALL_STATES.computeIfAbsent(name, n -> STATE_ID++);
        final Integer maxCapacity = CAPACITY_STATES.get(id);

        if(maxCapacity == null) {
            //registerBlockState(name, id, capacity);
            CAPACITY_STATES.put(id, capacity);
        } else if(maxCapacity < capacity) {
            //setBlockStateCapacity(id, capacity);
        }

        return id;
    }

    @JSStaticFunction
    public static int getIdByName(String name) {
        return ALL_STATES.getOrDefault(name, -1);
    }

    @JSStaticFunction
    public static String getNameById(int id) {
        for(String name : ALL_STATES.keySet())
            if(ALL_STATES.get(name) == id)
                return name;
        return null;
    }

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
        this.runtimeId = GlobalBlockPalette.getOrCreateRuntimeId(InnerCoreServer.PROTOCOL, id, data);
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
                key = getIdByName(key0.toString());
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

    @SuppressWarnings("unused")
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

            int state = getIdByName(key);
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
                namedStates.put(getNameById(entry.getKey()), entry.getValue());
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

    private static int getStateFromId(int runtimeId, int state){
        return BlockStateRegisters.getStateFromId(runtimeId, state);
    }
    private static int[] getAllStatesFromId(int runtimeId){
        return BlockStateRegisters.getAllStatesFromId(runtimeId);
    }
    public static int runtimeIdByStates(int id, int[] statesAndValues){
        return BlockStateRegisters.runtimeIdByStates(id, statesAndValues);
    }
    public static int getIdFromRuntimeId(int runtimeId){
        InnerCoreServer.useIncomprehensibleMethod("getIdFromRuntimeId");
        return 0;
    }
    public static int getDataFromRuntimeId(int runtimeId){
        return BlockStateRegisters.getDataForRuntimeId(runtimeId);
    }
}
