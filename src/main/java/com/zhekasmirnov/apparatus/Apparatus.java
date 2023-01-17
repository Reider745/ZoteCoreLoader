package com.zhekasmirnov.apparatus;

import android.util.Log;
import cn.nukkit.Server;
import cn.nukkit.level.GlobalBlockPalette;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerPackVersionChecker;
import com.zhekasmirnov.apparatus.multiplayer.mod.RuntimeIdDataPacketSender;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

public class Apparatus {
    public static String PATH;
    public static Server server;

    private static class EntryDump {
        int newId;
        int data;
        HashMap<String, Integer> states;

        Long dbgHash;
    }


    public static void init(Server server) throws Exception {
        long start = System.currentTimeMillis();
        server.getLogger().info("start load inner core");
        URL url = Server.class.getProtectionDomain().getCodeSource().getLocation();
        File f = new File(url.toURI());
        PATH = URLDecoder.decode(f.getParentFile().getAbsolutePath(), "UTF-8");


        Apparatus.server = server;
        ICLog.server = server;
        Logger.server = server;
        FileTools.init();

        MultiplayerModList.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        NetworkEntity.loadClass();
        IdConversionMap.loadClass();

        InnerCoreServer.RuntimeId runtimeIds = new InnerCoreServer.RuntimeId();

        JSONArray jsonArray = new JSONArray(new String(ByteStreams.toByteArray(Server.class.getClassLoader().getResourceAsStream("network-id-dump.json"))));
        jsonArray.forEach((element) -> {
            JSONObject value = (JSONObject) element;
            //runtimeIds.add(value.getLong("dbgHash"), value.getInt("dbgStateId"));
            runtimeIds.add(value.getLong("dbgHash"), GlobalBlockPalette.getOrCreateRuntimeId(value.getInt("newId"), value.getInt("data")));
        });



        RuntimeIdDataPacketSender.loadClass(runtimeIds);
        Network.getSingleton().startLanServer();
        NetworkJsAdapter.instance = new NetworkJsAdapter(Network.getSingleton());
       // Network.getSingleton().startLanServer(InnerCoreConfig.getInt("port"));
        Logger.info("INNERCORE", "end load, time: "+(System.currentTimeMillis()-start));
    }

    public static void postInit(){

    }

    public static int getVersionCode() {
        return 1;
    }

    public static boolean isDevelop() {
        return true;
    }
}
