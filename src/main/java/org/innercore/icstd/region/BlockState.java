package org.innercore.icstd.region;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockMeta;
import cn.nukkit.level.GlobalBlockPalette;
import com.reider745.InnerCoreServer;
import com.reider745.block.BlockStateStorage;
import com.reider745.hooks.block.BlockPalette;
import com.zhekasmirnov.apparatus.minecraft.enums.EnumsContainer;
import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.NativeCallback;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.BlockStateBindGenBase;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;
import java.util.Map;

@ApiBinding
public class BlockState extends BlockStateBindGenBase {
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

    @ApiBinding
    public static int registerBlockState(String name, int capacity) {
        if(VANILLA_STATES.containsKey(name))
            throw new RuntimeException("You can't create a state with a vanilla name");

        if(capacity <= 0)
            throw new RuntimeException("capacity state <= 0");

        final int id = Java8BackComp.computeIfAbsent(ALL_STATES, name, n -> STATE_ID++);
        final Integer maxCapacity = CAPACITY_STATES.get(id);

        if(maxCapacity == null) {
            registerBlockState(name, id, capacity);
            CAPACITY_STATES.put(id, capacity);
        } else if(maxCapacity < capacity) {
            setBlockStateCapacity(id, capacity);
        }

        return id;
    }

    @ApiBinding
    public static int getIdByName(String name) {
        return ALL_STATES.getOrDefault(name, -1);
    }

    @ApiBinding
    public static String getNameById(int id) {
        for(String name : ALL_STATES.keySet())
            if(ALL_STATES.get(name) == id)
                return name;
        return null;
    }

    @ApiBinding
    public static String[] getAllStates() {
        return ALL_STATES.keySet().toArray(new String[0]);
    }

    @ApiBinding
    public static int getBlockStateCapacity(int id) {
        return CAPACITY_STATES.getOrDefault(id, 0);
    }

    @ApiBinding public final int id;
    @ApiBinding public final int data;
    private int cachedRuntimeId = -2;
    private int cachedRuntimeIdGeneration = 0;

    private int[] rawStates = null;
    private Map<Integer, Integer> states = null;
    private Map<String, Integer> namedStates = null;

    @ApiBinding
    public BlockState(int id, int data) {
        super();
        this.id = id;
        this.data = data;
        getRuntimeId();
    }

    public BlockState(int id, Map<?, Integer> map) {
        super();
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

        this.cachedRuntimeId = runtimeIdByStates(id, rawStates);
        this.data = getDataFromRuntimeId(cachedRuntimeId);
        this.cachedRuntimeIdGeneration = NativeCallback.currentGlobalWorldGeneration;
    }

    private BlockState(int id, int[] rawStates) {
        super();
        this.id = id;
        this.rawStates = rawStates;
        this.cachedRuntimeId = runtimeIdByStates(id, rawStates);
        this.cachedRuntimeIdGeneration = NativeCallback.currentGlobalWorldGeneration;
        this.data = getDataFromRuntimeId(cachedRuntimeId);
    }

    protected BlockState(Block idDataAndState) {
        super();

        this.id = idDataAndState.getId();
        this.data = idDataAndState.getDamage();
        this.cachedRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(InnerCoreServer.PROTOCOL, id, data);
        this.cachedRuntimeIdGeneration = NativeCallback.currentGlobalWorldGeneration;
    }

    BlockState(long idDataAndState) {
        long idData = idDataAndState & (long) 0xFFFFFFFF;
        this.id = (int) (idData >> (long) 16) & 0xFFFF;
        this.data = (int) (idData & (long) 0xFFFF);
        this.cachedRuntimeId = (int) (idDataAndState >> (long) 32);
        this.cachedRuntimeIdGeneration = NativeCallback.currentGlobalWorldGeneration;
    }

    protected BlockState(long idDataAndState, Object stub) {
        this(idDataAndState);
    }

    public BlockState(long ptr, int refCnt, Object stub) {
        super();
        throw new UnsupportedOperationException();
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

    @ApiBinding
    public BlockState(int id, Scriptable scriptable) {
        this(id, scriptableToStateMap(scriptable));
    }

    @ApiBinding
    public static BlockState parse(Object o, boolean nullable) {
        if (o instanceof BlockState)
            return (BlockState) o;
        if (o instanceof Scriptable) {
            Scriptable s = (Scriptable) o;
            int id = ScriptableObjectHelper.getIntProperty(s, "id", 0);
            Scriptable states = ScriptableObjectHelper.getScriptableProperty(s, "states", null);
            return states != null ? new BlockState(id, states) : new BlockState(id, ScriptableObjectHelper.getIntProperty(s, "data", 0));
        }
        return nullable ? null : new BlockState(0, 0);
    }

    @ApiBinding
    public int getId() {
        return id;
    }

    @ApiBinding
    public int getData() {
        return data;
    }

    @ApiBinding
    public int getRuntimeId() {
        if (cachedRuntimeId == -2 || NativeCallback.currentGlobalWorldGeneration != cachedRuntimeIdGeneration) {
            if (rawStates != null) {
                cachedRuntimeId = runtimeIdByStates(id, rawStates);
            } else {
                cachedRuntimeId = getRuntimeIdFromIdData(id, data);
            }
            cachedRuntimeIdGeneration = NativeCallback.currentGlobalWorldGeneration;
        }
        return cachedRuntimeId;
    }

    @ApiBinding
    public boolean isValidState() {
        return getRuntimeId() != -1;
    }

    @ApiBinding
    public int getState(int state) {
        int runtimeId = getRuntimeId();
        return runtimeId > 0 ? getStateFromId(runtimeId, state) : -1;
    }

    @ApiBinding
    public boolean hasState(int state) {
        return getState(state) != -1;
    }

    @ApiBinding
    public BlockState addState(int state, int value) {
        Map<Integer, Integer> mStates = new HashMap<>(getStates());
        mStates.put(state, value);
        return new BlockState(id, mStates);
    }

    @ApiBinding
    public BlockState addStatesMap(Map<?, Integer> states) {
        Map<Object, Integer> mStates = new HashMap<>(getStates());
        mStates.putAll(states);
        return new BlockState(id, mStates);
    }

    @ApiBinding
    public BlockState addStates(Scriptable states) {
        return addStatesMap(scriptableToStateMap(states));
    }

    @ApiBinding
    public Map<Integer, Integer> getStates() {
        if (states == null) {
            states = new HashMap<>();
            int runtimeId = getRuntimeId();
            if (runtimeId >= 0) {
                rawStates = rawStates != null ? rawStates : getAllStatesFromId(runtimeId);
                for (int i = 0; i < rawStates.length / 2; i++) {
                    states.put(rawStates[i * 2], rawStates[i * 2 + 1]);
                }
            }
        }
        return states;
    }

    @ApiBinding
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

    @ApiBinding
    public ScriptableObject getStatesScriptable() {
        Map<Integer, Integer> states = getStates();
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        for (Map.Entry<Integer, Integer> entry : states.entrySet()) {
            result.put(entry.getKey().toString(), result, entry.getValue());
        }
        return result;
    }

    @ApiBinding
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
        return "BlockState{id=" + id + ", data=" + data + ", runtimeId=" + getRuntimeId() + ", states=" + getNamedStates() + "}";
    }

    @ApiBinding
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        BlockState that = (BlockState) object;

        if (id != that.id) return false;
        if (data != that.data) return false;
        if (getRuntimeId() != that.getRuntimeId()) return false;
        return getStates().equals(that.getStates());
    }

    private static void registerBlockState(String name, int id, int capacity) {
        BlockPalette.registerBlockState(name, id, capacity);
    }

    private static void setBlockStateCapacity(int id, int capacity) {
        BlockPalette.setCapacityForId(id, capacity);
    }


    private static int getStateFromId(int runtimeId, int state){
        return BlockStateStorage.getStateFromId(runtimeId, state);
    }
    private static int[] getAllStatesFromId(int runtimeId){
        return BlockStateStorage.getAllStatesFromId(runtimeId);
    }

    @ApiBinding
    public static int runtimeIdByStates(int id, int[] statesAndValues){
        return BlockStateStorage.runtimeIdByStates(id, statesAndValues);
    }

    @ApiBinding
    public static int getIdFromRuntimeId(int runtimeId){
        InnerCoreServer.useIncomprehensibleMethod("getIdFromRuntimeId");
        return 0;
    }

    @ApiBinding
    public static int getDataFromRuntimeId(int runtimeId){
        return BlockStateStorage.getDataForRuntimeId(runtimeId);
    }

    @ApiBinding
    public static int getRuntimeIdFromIdData(int id, int data) {
        return GlobalBlockPalette.getOrCreateRuntimeId(InnerCoreServer.PROTOCOL, id, data);
    }
}
