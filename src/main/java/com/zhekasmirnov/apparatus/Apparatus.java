package com.zhekasmirnov.apparatus;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.apparatus.api.container.ItemContainer;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerPackVersionChecker;
import com.zhekasmirnov.apparatus.multiplayer.mod.RuntimeIdDataPacketSender;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity;

public class Apparatus {
    static {
        NetworkEntity.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        MultiplayerModList.loadClass();
        IdConversionMap.loadClass();
        RuntimeIdDataPacketSender.loadClass();
        ItemContainer.loadClass();
    }

    public static void loadClasses() {
        // forces all api classes to load
    }

    public static boolean isDevelop() {
        return InnerCoreServer.isDeveloperMode();
    }
}
