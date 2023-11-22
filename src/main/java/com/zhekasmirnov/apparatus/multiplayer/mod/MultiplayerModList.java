package com.zhekasmirnov.apparatus.multiplayer.mod;

import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.apparatus.modloader.ApparatusModInfo;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.InitializationPacketException;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.mod.build.Mod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplayerModList {
    private static final MultiplayerModList singleton = new MultiplayerModList();

    public static MultiplayerModList getSingleton() {
        return singleton;
    }

    public static void loadClass() {
        // forces class to load and register listeners
    }

    static {
        Network.getSingleton().addClientInitializationPacket("system.mod_list", () -> getSingleton().toJson(),
            (client, data, dataType) -> {
                if (data instanceof JSONObject) {
                    String compareResult = getSingleton().compareToJson((JSONObject) data);
                    if (compareResult != null) {
                        throw new InitializationPacketException(compareResult);
                    }
                } else {
                    throw new InitializationPacketException("system.mod_list received invalid packet of type " + dataType);
                }
            });
    }


    private final List<ApparatusMod> modList = new ArrayList<>();

    private MultiplayerModList() {

    }

    public void add(ApparatusMod mod) {
        modList.add(mod);
    }

    public void clear() {
        modList.clear();
    }


    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            JSONArray list = new JSONArray();
            json.put("list", list);
            for (ApparatusMod mod : modList) {
                ApparatusModInfo info = mod.getInfo();
                if (!info.getBoolean("client_only")) {
                    JSONObject modJson = new JSONObject();
                    modJson.put("name", info.getString("name"));
                    modJson.put("version", info.getString("version"));
                    list.put(modJson);
                }
            }
            return json;
        } catch (JSONException ignore) { }
        return json;
    }

    public String compareToJson(JSONObject json) {
        JSONArray list = json.optJSONArray("list");
        if (list != null) {
            Map<String, String> thisMods = new HashMap<>();
            Map<String, String> otherMods = new HashMap<>();

            for (int i = 0; i < list.length(); i++) {
                JSONObject modJson = list.optJSONObject(i);
                if (modJson != null) {
                    thisMods.put(modJson.optString("name"), modJson.optString("version"));
                } else {
                    return "invalid mod list packet " + json;
                }
            }

            for (ApparatusMod mod : modList) {
                ApparatusModInfo info = mod.getInfo();
                if (!info.getBoolean("client_only")) {
                    otherMods.put(info.getString("name"), info.getString("version"));
                }
            }

            boolean match = true;
            StringBuilder missingMods = new StringBuilder();
            StringBuilder excessMods = new StringBuilder();
            StringBuilder incompatibleMods = new StringBuilder();
            for (Map.Entry<String, String> entry : thisMods.entrySet()) {
                String name = entry.getKey();
                String version = entry.getValue();
                String otherVersion = otherMods.get(name);
                if (otherVersion == null) {
                    excessMods.append(name).append(":").append(version).append("\n");
                    match = false;
                } else if (!otherVersion.equals(version)) {
                    incompatibleMods.append(name).append(":").append(version).append(", server version: ").append(otherVersion).append("\n");
                    match = false;
                }
            }

            for (Map.Entry<String, String> entry : otherMods.entrySet()) {
                String name = entry.getKey();
                String otherVersion = entry.getValue();
                String version = thisMods.get(name);
                if (version == null) {
                    missingMods.append(name).append(":").append(otherVersion).append("\n");
                    match = false;
                }
            }

            if (!match) {
                return "{{loc: multiplayer_mod_mismatch}}\n\n" +
                        (missingMods.length() > 0 ? "{{loc: multiplayer_mod_missing}}\n" + missingMods + "\n" : "") +
                        (excessMods.length() > 0 ? "{{loc: multiplayer_mod_excess}}\n" + excessMods + "\n" : "") +
                        (incompatibleMods.length() > 0 ? "{{loc: multiplayer_mod_different_version}}\n" + incompatibleMods + "\n" : "");
            } else {
                return null;
            }
        }
        return "invalid mod list packet " + json;
    }

    public boolean checkMultiplayerAllowed() {
        boolean allSupported = true;
        StringBuilder unsupportedMods = new StringBuilder();
        for (ApparatusMod mod : modList) {
            ApparatusModInfo info = mod.getInfo();
            if (!info.getBoolean("multiplayer_supported")) {
                unsupportedMods.append(info.getString("displayed_name")).append("\n");
                allSupported = false;
            }
        }
        if (allSupported) {
            return true;
        }
        return !false;
    }

}
