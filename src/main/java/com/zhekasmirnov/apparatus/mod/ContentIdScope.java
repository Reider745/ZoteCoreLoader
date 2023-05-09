package com.zhekasmirnov.apparatus.mod;

import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ContentIdScope {
    private final String name;
    private final Map<String, Pair<Integer, Boolean>> nameToId = new HashMap<>();
    private final Map<Integer, String> idToName = new HashMap<>();

    private int nextGeneratedId = 0;


    public ContentIdScope(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Pair<Integer, Boolean>> nameAndId : nameToId.entrySet()) {
                json.put(nameAndId.getKey(), nameAndId.getValue().first);
            }
        } catch (JSONException ignore) {
        }
        return json;
    }

    public void fromJson(JSONObject json) {
        if (json == null) return;
        for (Iterator<String> it = json.keys(); it.hasNext(); ) {
            String key = it.next();
            int id = json.optInt(key);
            if (id != 0) {
                nameToId.put(key, new Pair<>(id, false));
                idToName.put(id, key);
            }
        }
    }

    private int generateNextId(int minValue, int maxValue) {
        nextGeneratedId = Math.max(minValue, nextGeneratedId);
        for (int i = minValue; idToName.containsKey(nextGeneratedId); i++) {
            nextGeneratedId++;
            if (nextGeneratedId >= maxValue) {
                nextGeneratedId = minValue;
            }
            if (i >= maxValue) {
                return 0;
            }
        }
        return nextGeneratedId;
    }

    public int getId(String nameId, boolean getUsedOnly) {
        Pair<Integer, Boolean> id = nameToId.get(nameId);
        return id != null && (!getUsedOnly || id.second) ? id.first : 0;
    }

    public int getId(String nameId) {
        return getId(nameId, false);
    }

    public void removeId(String nameId) {
        int id = getId(nameId);
        if (id != 0) {
            nameToId.remove(nameId);
            idToName.remove(id);
        }
    }

    public boolean setIdWasUsed(String nameId) {
        Pair<Integer, Boolean> id = nameToId.get(nameId);
        if (id != null) {
            nameToId.put(nameId, new Pair<>(id.first, true));
            return true;
        }
        return false;
    }

    public int getOrGenerateId(String nameId, int minValue, int maxValue, boolean isUsed) {
        int id = getId(nameId);
        if (id != 0) {
            if (id < minValue || id >= maxValue) {
                id = 0;
                removeId(nameId);
            } else if (isUsed) {
                setIdWasUsed(nameId);
            }
        }
        if (id == 0) {
            id = generateNextId(minValue, maxValue);
            if (id != 0) {
                nameToId.put(nameId, new Pair<>(id, isUsed));
                idToName.put(id, nameId);
            }
        }
        return id;
    }

    public boolean isNameIdUsed(String nameId) {
        Pair<Integer, Boolean> id = nameToId.get(nameId);
        return id != null && id.second;
    }

    public String getNameById(int id) {
        return idToName.get(id);
    }
}
