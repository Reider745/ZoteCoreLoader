package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.defaults.VersionCommand;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.JavaPluginLoader;
import cn.nukkit.plugin.PluginClassLoader;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Utils;

import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.commands.TpsCommand;
import com.reider745.entity.EntityMotion;
import com.reider745.event.EventListener;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;

@Hooks(className = "cn.nukkit.Server")
public class ServerHooks {
    public static InnerCoreServer innerCoreServer = new InnerCoreServer();
    private static final org.apache.logging.log4j.Logger zoteLogger = LogManager.getLogger(MainLogger.class);

    @Inject
    public static void start(Server self) throws Exception {
        innerCoreServer.afterload();
    }

    @Inject(className = "cn.nukkit.Nukkit")
    public static String getVersion() {
        return "git-null"; // ignore nukkit updates
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

        final Config properties = server.getProperties();
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

        if(variable.equals("enable-raw-ores") || variable.equals("enable-new-paintings"))// Исключительно для новых версий, поэтому без права на включение
            return false;

        final Config properties = server.getProperties();
        Object value = properties.exists(variable) ? properties.get(variable) : defaultValue;

        if (value instanceof Boolean)
            return (Boolean) value;

        return switch (String.valueOf(value)) {
            case "on", "true", "1", "yes" -> true;
            default -> false;
        };
    }

    @Inject
    public static String getPropertyString(Server server, String variable, String defaultValue) {
        final Config properties = server.getProperties();
        final String value = properties.exists(variable) ? properties.get(variable).toString() : defaultValue;

        if(variable.equals("level-type") && (value.isEmpty() || value.equals("normal") || value.equals("default"))) {
            return "oldnormal";
        }
        return value;
    }

    private static int indexTime = 0;

    @Inject
    public static void checkTickUpdates(Server server, int tick) {
        indexTime = (indexTime + 1) % 20;

        try {
            try {
                long start = System.currentTimeMillis();

                EntityMotion.tick();
                NativeCallback.onTick();

                //for(Block block : postApply) {
                //    EventListener.onRedstone(block);
                //}

                TpsCommand.innerCoreTimes[indexTime] = System.currentTimeMillis() - start;

                EventListener.isFirstTickEnd = true;
            } catch (Exception e) {
                Logger.warning("IC-TICK", e);
            }
        }catch (Throwable e){
            Logger.warning("IC-TICK", e);
        }
    }

    @Inject(type = TypeHook.AFTER)
    public static void reload(Server server) {
        InnerCoreServer.singleton.reload();
    }

    @Inject
    public static String getVersion(Server server) {
        return 'v' + Utils.getVersionByProtocol(ProtocolInfo.v1_16_200);
    }

    @Inject(type = TypeHook.AFTER_NOT_REPLACE, method = "<init>", className = "cn.nukkit.plugin.PluginClassLoader")
    public static void newPluginClassLoader(PluginClassLoader self, JavaPluginLoader loader, ClassLoader parent, File file) {
        ZoteClassLoaderPool.addClassLoader(self);
    }

    @Inject(
        className = "cn.nukkit.command.defaults.VersionCommand",
        signature = "(Lcn/nukkit/command/CommandSender;Ljava/lang/String;[Ljava/lang/String;)Z"
    )
    public static void execute(VersionCommand self, CommandSender sender, String commandLabel, String[] args){
        if (sender.hasPermission("nukkit.command.version.plugins")) {
            List<String> ranges = new ArrayList<>();
            List<Object> specs = InnerCoreServer.getVersionCodeSpecs();
            if (specs.size() == 0) {
                ranges.add("any that supports multiplayer");
            } else {
                for (Object spec : specs) {
                    if (spec instanceof int[] range) {
                        if (range[0] == 0) {
                            ranges.add("<" + range[1]);
                        } else if (range[1] == 0) {
                            ranges.add(">=" + range[0]);
                        } else {
                            ranges.add(range[0] + " to " + range[1]);
                        }
                    } else if (spec instanceof Integer code) {
                        ranges.add(code < 0 ? "not " + code : "" + code);
                    } else if (spec != null) {
                        ranges.add(spec.toString());
                    }
                }
            }
            sender.sendMessage("#########################################\n" +
                "Zote Core\n" +
                "Inner Core v" + InnerCoreServer.getVersionName() + " (code " + InnerCoreServer.getVersionCode() + ")\n" +
                "Supported client versions are: " + String.join(", ", ranges));
        }
    }

    @Inject(className = "cn.nukkit.utils.MainLogger", type = TypeHook.BEFORE_REPLACE)
    public static void warning(MainLogger nukkitLogger, String message) {
        if (message == null || !message.startsWith("Ignoring InnerCorePacket from ")) {
            zoteLogger.warn(message);
        }
    }
}
