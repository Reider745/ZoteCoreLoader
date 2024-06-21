package com.reider745.behavior.api;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.AddEntityPacket;
import com.reider745.InnerCoreServer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class VanillaMobsFamily {
    private static HashMap<String, List<String>> vanills = new HashMap<>();
    private static HashMap<Integer, String> mapping = new HashMap<>();

    static {
        AddEntityPacket.setupLegacyIdentifiers(mapping, InnerCoreServer.PROTOCOL);

        try {
            final JSONObject json = new JSONObject(
                    new String(VanillaMobsFamily.class.getClassLoader().getResourceAsStream("addon_family_dump.json").readAllBytes())
            );
            var it = json.keys();
            while(it.hasNext()){
                final String key = it.next();
                final ArrayList<String> list = new ArrayList<>();
                final JSONArray array = json.getJSONArray(key);

                for(int i = 0;i < array.length();i++)
                    list.add(array.getString(i));

                vanills.put(key, list);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getFor(String id){
        final List<String> EMPTY = new ArrayList<>();
        if(!vanills.containsKey(id))
            return EMPTY;
        return vanills.get(id);
    }

    public static List<String> getFor(int networkId){
        final String key = mapping.get(networkId);
        final List<String> EMPTY = new ArrayList<>();
        if(key == null) return EMPTY;
        return getFor(key);
    }

    public static List<String> getFor(Entity entity){
        if(entity instanceof Player)
            return getFor("minecraft:player");
        return getFor(entity.getNetworkId());
    }
}
