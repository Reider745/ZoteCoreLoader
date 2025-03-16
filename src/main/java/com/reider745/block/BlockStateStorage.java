package com.reider745.block;

import com.reider745.hooks.block.BlockPalette;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// TODO: Убрать использования имен на использование id стейтов
public class BlockStateStorage {
    private static final Map<Integer, Map<String, Integer>> statesForRuntimeId = new HashMap<>();
    private static final Map<Integer, Map<Map<String, Integer>, Integer>> runtimeIdsForStatesAndId = new HashMap<>();
    private static final Map<Integer, Integer> dataForRuntimeId = new HashMap<>();

    private static final HashMap<String, Integer> runtimeIdForIdAndData = new HashMap<>();
    private static final HashMap<Integer, int[]> blockForRuntime = new HashMap<>();

    public static void addState(JSONObject json, int runtimeId, int legacyId, int data, int newId){
        final Map<String, Integer> state = new HashMap<>();
        Iterator<String> it = json.keys();
        while (it.hasNext()){
            String key = it.next();
            state.put(key, json.getInt(key));
        }

        final Map<Map<String, Integer>, Integer> states = runtimeIdsForStatesAndId.getOrDefault(legacyId, new HashMap<>());
        states.put(state, runtimeId);
        runtimeIdsForStatesAndId.put(legacyId, states);
        blockForRuntime.put(runtimeId, new int[]{newId, data});

        statesForRuntimeId.put(runtimeId, state);
        dataForRuntimeId.put(runtimeId, data);
        runtimeIdForIdAndData.put(legacyId+":"+data, runtimeId);
    }

    public static int[] getBlockForRuntimeId(final int runtimeId){
        return blockForRuntime.get(runtimeId);
    }

    public static int getDataForRuntimeId(final int runtimeId){
        return dataForRuntimeId.getOrDefault(runtimeId, 0);
    }

    public static Map<String, Integer> getStateFor(int runtimeId){
        return statesForRuntimeId.get(runtimeId);
    }

    public static int getStateFor(int id, int data){
        return runtimeIdForIdAndData.get(id+":"+data);
    }

    public static int getStateFromId(int runtimeId, int state){
        final Map<String, Integer> states = statesForRuntimeId.get(runtimeId);
        if(states == null) return 0;
        return states.get(BlockPalette.getNameForId(state));
    }
    public static int[] getAllStatesFromId(int runtimeId){
        final Map<String, Integer> state = statesForRuntimeId.get(runtimeId);
        if(state == null) return new int[]{};

        final int size = state.size();
        final int[] states = new int[size*2];
        int index = 0;
        for(String key : state.keySet()){
            states[index] = BlockPalette.getIdByName(key);
            states[index+1] = state.get(key);
            index+=2;
        }

        return states;
    } // [state1, value1, state2, value2, ...]
    public static int runtimeIdByStates(int id, int[] statesAndValues){
        final Map<String, Integer> state = new HashMap<>();
        for(int i = 0;i < statesAndValues.length;i+=2) {
            state.put(BlockPalette.getNameForId(statesAndValues[i]), statesAndValues[i + 1]);
        }
        return runtimeIdsForStatesAndId.get(id).getOrDefault(state, -1);
    }// -1 is returned in case it is not found: 47,
}
