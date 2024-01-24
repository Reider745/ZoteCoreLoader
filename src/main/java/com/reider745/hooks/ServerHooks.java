package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

import com.reider745.InnerCoreServer;
import com.reider745.Main;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.entity.EntityMotion;
import com.zhekasmirnov.innercore.api.NativeCallback;

@Hooks(className = "cn.nukkit.Server")
public class ServerHooks implements HookClass {

    @Inject
    public static void start(Server self) throws Exception {
        Main.LoadingStages.afterload(self);
        Main.LoadingStages.start(self);
    }

    @Inject(className = "cn.nukkit.dispenser.DispenseBehaviorRegister", type = TypeHook.AFTER)
    public static void init() {
        try {
			Main.LoadingStages.preload(Server.getInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Inject
    public static int getPropertyInt(Server server, String variable, Integer defaultValue) {
        if (!InnerCoreServer.isUnsupportedOptionsAllowed()
                && (variable.equals("multiversion-min-protocol") || variable.equals("multiversion-max-protocol")))
            return InnerCoreServer.PROTOCOL;

        Config properties = server.getProperties();
        return properties.exists(variable)
                ? (!properties.get(variable).equals("") ? Integer.parseInt(String.valueOf(properties.get(variable)))
                        : defaultValue)
                : defaultValue;
    }

    @Inject
    public static boolean getPropertyBoolean(Server server, String variable, Object defaultValue) {
        if (!InnerCoreServer.isUnsupportedOptionsAllowed()
                && (variable.equals("xbox-auth") || variable.equals("save-player-data-by-uuid")))
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

    @Inject
    public static void checkTickUpdates(Server server, int tick) {
        EntityMotion.tick();
        NativeCallback.onTick();
    }

    @Inject
    public static void forceShutdown(Server server, String reason) throws Exception {
        Main.LoadingStages.stop(server);
    }

    @Inject(type = TypeHook.AFTER)
    public static void reload(Server server) {
        InnerCoreServer.singleton.reload();
    }

    @Inject
    public static String getVersion(Server server) {
        return 'v' + Utils.getVersionByProtocol(ProtocolInfo.v1_16_200);
    }
}
