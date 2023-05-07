package com.zhekasmirnov.apparatus.multiplayer.mod;

import com.zhekasmirnov.apparatus.adapter.innercore.PackInfo;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.InitializationPacketException;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiplayerPackVersionChecker {
    static {
        Network.getSingleton().addClientInitializationPacket("system.inner_core_build", () -> {
                JSONObject packInfo = new JSONObject();
                try {
                    packInfo.put("name", PackInfo.getPackName());
                    packInfo.put("versionName", PackInfo.getPackVersionName());
                    packInfo.put("versionCode", PackInfo.getPackVersionCode());
                } catch (JSONException ignore) { }
                return packInfo;
            }, (client, data, dataType) -> {
                if (data instanceof JSONObject) {
                    JSONObject packInfo = (JSONObject) data;
                    String packName = packInfo.optString("name");
                    String packVersionName = packInfo.optString("versionName");
                    int packVersionCode = packInfo.optInt("versionCode");
                    if (!PackInfo.getPackName().equals(packName) ||
                        !PackInfo.getPackVersionName().equals(packVersionName) ||
                        PackInfo.getPackVersionCode() != packVersionCode) {
                        String message = "\n\n{{loc: multiplayer_innercore_version_mismatch}}\n" +
                                "client (your) version: " +
                                    packName + " " + packVersionName +
                                    " (code=" + packVersionCode + ")\n" +
                                "server version: " +
                                    PackInfo.getPackName() + " " + PackInfo.getPackVersionName() +
                                    " (code=" + PackInfo.getPackVersionCode() + ")\n";
                        throw new InitializationPacketException(message);
                    }
                } else {
                    throw new InitializationPacketException("system.inner_core_build received invalid packet of type " + dataType);
                }
            });
    }

    public static void loadClass() {
        // forces class to load and register listeners
    }
}
