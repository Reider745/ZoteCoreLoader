package com.reider745.block;

import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import com.google.common.io.ByteStreams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class BlockNukkitIdConverter {
    public static final JSONArray json;

    private static final HashMap<Integer, Integer> legacyForNewId = new HashMap<>();
    private static final HashMap<Integer, Integer> newIdForLegacyId = new HashMap<>();

    static {
        try{
            json = new JSONArray(new String(ByteStreams
                    .toByteArray(Server.class.getClassLoader().getResourceAsStream("network-id-dump.json"))));

            for(int i = 0;i < json.length();i++) {
                final JSONObject block = json.getJSONObject(i);

                final int legacyId = block.getInt("oldId");
                final int newId = block.getInt("newId");

                if(legacyId != newId){
                    legacyForNewId.put(legacyId, newId);
                    newIdForLegacyId.put(newId, legacyId);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static int getNewId(int legacyId) {
        return legacyForNewId.getOrDefault(legacyId, legacyId);
    }

    public static int getLegacyId(int newId) {
        return newIdForLegacyId.getOrDefault(newId, newId);
    }
}
