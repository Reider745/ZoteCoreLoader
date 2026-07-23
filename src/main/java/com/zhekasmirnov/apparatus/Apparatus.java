package com.zhekasmirnov.apparatus;

import com.reider745.InnerCoreServer;
import org.innercore.icstd.entity.EntityModule;
import org.innercore.icstd.inventory.ItemContainer;
import com.zhekasmirnov.apparatus.api.player.NetworkPlayerRegistry;
import com.zhekasmirnov.apparatus.multiplayer.mod.IdConversionMap;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerModList;
import com.zhekasmirnov.apparatus.multiplayer.mod.MultiplayerPackVersionChecker;
import com.zhekasmirnov.apparatus.multiplayer.util.entity.NetworkEntity;
import com.zhekasmirnov.innercore.api.runtime.Updatable;
import org.innercore.icstd.scene.GameObjectManager;

public class Apparatus {
    static {
        NetworkEntity.loadClass();
        NetworkPlayerRegistry.loadClass();
        MultiplayerPackVersionChecker.loadClass();
        MultiplayerModList.loadClass();
        IdConversionMap.loadClass();
        ItemContainer.loadClass();
        GameObjectManager.loadClass();
        Updatable.loadClass();
        EntityModule.loadClass();
    }

    public static void loadClasses() {
        // forces all api classes to load
    }

    public static boolean isDevelop() {
        return InnerCoreServer.isDeveloperMode();
    }
}
