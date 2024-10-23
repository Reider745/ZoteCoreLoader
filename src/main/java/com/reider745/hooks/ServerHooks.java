package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

import com.reider745.InnerCoreServer;
import com.reider745.Main;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.commands.TpsCommand;
import com.reider745.entity.EntityMotion;
import com.reider745.event.EventListener;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;

@Hooks(className = "cn.nukkit.Server")
public class ServerHooks {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();

    @Inject
    public static void start(Server self) throws Exception {
        innerCoreServer.afterload();
        innerCoreServer.start();
    }

    @Inject(className = "cn.nukkit.dispenser.DispenseBehaviorRegister", type = TypeHook.AFTER)
    public static void init() {
        try {
            innerCoreServer.preload(Server.getInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Inject
    public static int getPropertyInt(Server server, String variable, Integer defaultValue) {
        if (!InnerCoreServer.isUnsupportedOptionsAllowed()
                && (variable.equals("multiversion-min-protocol") || variable.equals("multiversion-max-protocol")))
            return InnerCoreServer.PROTOCOL;

        if(variable.equals("rak-packet-limit"))
            return InnerCoreServer.getMaxPacketSending();

        Config properties = server.getProperties();
        return properties.exists(variable)
                ? (!properties.get(variable).equals("") ? Integer.parseInt(String.valueOf(properties.get(variable)))
                        : defaultValue)
                : defaultValue;
    }

    @Inject
    public static boolean getPropertyBoolean(Server server, String variable, Object defaultValue) {
        if (!InnerCoreServer.isUnsupportedOptionsAllowed() && (variable.equals("xbox-auth")))
            return false;

        if(variable.equals("load-all-worlds"))// Увы это может сломать сохраенения TileEntity, поэтому настройка заблокировна
            return true;

        if(variable.equals("enable-raw-ores"))// Исключительно для новых версий, поэтому без права на включение
            return false;

        Config properties = server.getProperties();
        Object value = properties.exists(variable) ? properties.get(variable) : defaultValue;

        if (value instanceof Boolean)
            return (Boolean) value;

        return switch (String.valueOf(value)) {
            case "on", "true", "1", "yes" -> true;
            default -> false;
        };
    }

    private static long start_pre = 0;
    private static int indexTime = 0;

    @Inject
    public static void checkTickUpdates(Server server, int tick) {
        indexTime = (indexTime + 1) % 20;
        TpsCommand.nukkitTimes[indexTime] = System.currentTimeMillis() - start_pre;

        try {
            try {
                long start = System.currentTimeMillis();

                EntityMotion.tick();
                NativeCallback.onTick();

                TpsCommand.innerCoreTimes[indexTime] = System.currentTimeMillis() - start;

                EventListener.isFirstTickEnd = true;
            } catch (Exception e) {
                Logger.warning("IC-TICK", e.getCause());
            }
        }catch (Throwable e){
            Logger.warning("IC-TICK", e);
        }

        start_pre = System.currentTimeMillis();
    }

    @Inject(type = TypeHook.AFTER)
    public static void reload(Server server) {
        InnerCoreServer.singleton.reload();
    }

    @Inject
    public static String getVersion(Server server) {
        return 'v' + Utils.getVersionByProtocol(ProtocolInfo.v1_16_200);
    }

    @Inject(
            className = "cn.nukkit.level.generator.Normal",
            type = TypeHook.AFTER_NOT_REPLACE
    )
    public static void populateChunk(Normal self, int chunkX, int chunkZ) {
        self.populateStructure(chunkX, chunkZ);
    }
}
