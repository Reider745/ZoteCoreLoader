package com.reider745.block;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockStateRegisters {
    private static final Map<Long, Map<String, Integer>> statesForHash = new HashMap<>();
    private static final Map<Integer, Map<String, Integer>> statesForRuntimeId = new HashMap<>();
    private static final Map<Integer, Map<Map<String, Integer>, Integer>> runtimeIdsForStatesAndId = new HashMap<>();
    private static final Map<Integer, Integer> dataForRuntimeId = new HashMap<>();

    private static Map<String, Integer> stateIdByName;
    private static final Map<Integer, String> nameByStateId = new HashMap<>();

    public static void init(HashMap<String, Integer> stateIdByName){
        BlockStateRegisters.stateIdByName = stateIdByName;

        for (String key : stateIdByName.keySet())
            nameByStateId.put(stateIdByName.get(key), key);
    }

    public static void addState(final long hash, JSONObject json){
        final Map<String, Integer> state = new HashMap<>();
        Iterator<String> it = json.keys();
        while (it.hasNext()){
            String key = it.next();
            state.put(key, json.getInt(key));
        }

        statesForHash.put(hash, state);
    }

    private static final HashMap<String, Integer> runtimeIdForIdAndData = new HashMap<>();

    public static void addRuntimeIdAndData(final long hash, final int runtimeId, final int id, final int data){
        Map<String, Integer> state = statesForHash.get(hash);
        Map<Map<String, Integer>, Integer> states = runtimeIdsForStatesAndId.getOrDefault(id, new HashMap<>());

        statesForRuntimeId.put(runtimeId, state);
        states.put(state, runtimeId);
        runtimeIdsForStatesAndId.put(id, states);
        dataForRuntimeId.put(runtimeId, data);
        runtimeIdForIdAndData.put(id+":"+data, runtimeId);
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
        return states.get(nameByStateId.get(state));
    }
    public static int[] getAllStatesFromId(int runtimeId){
        final Map<String, Integer> state = statesForRuntimeId.get(runtimeId);
        if(state == null) return new int[]{};

        final int size = state.size();
        final int[] states = new int[size*2];
        int index = 0;
        for(String key : state.keySet()){
            states[index] = stateIdByName.get(key);
            states[index+1] = state.get(key);
            index+=2;
        }

        return states;
    } // [state1, value1, state2, value2, ...]
    public static int runtimeIdByStates(int id, int[] statesAndValues){
        final Map<String, Integer> state = new HashMap<>();
        for(int i = 0;i < statesAndValues.length;i+=2)
            state.put(nameByStateId.get(statesAndValues[i]), statesAndValues[i+1]);
        final Integer res = runtimeIdsForStatesAndId.get(id).get(state);
        return res == null ? -1 : res;
    }// -1 is returned in case it is not found
}
