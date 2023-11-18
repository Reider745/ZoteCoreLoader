package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.reider745.InnerCoreServer;
import com.reider745.Main;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Server")
public class ServerHooks implements HookClass {
    @Inject
    public static void start(Server self) throws Exception {
        Main.LoadingStages.start(self);
    }
    //
    @Inject
    public static int getPropertyInt(Server server, String variable, Integer defaultValue){
        if(variable.equals("multiversion-min-protocol") || variable.equals("multiversion-max-protocol"))
            return InnerCoreServer.PROTOCOL;

        Config properties = server.getProperties();
        return properties.exists(variable) ? (!properties.get(variable).equals("") ? Integer.parseInt(String.valueOf(properties.get(variable))) : defaultValue) : defaultValue;
    }

    @Inject
    public static boolean getPropertyBoolean(Server server, String variable, Object defaultValue){
        if(variable.equals("xbox-auth") || variable.equals("save-player-data-by-uuid"))
            return false;

        Config properties = server.getProperties();
        Object value = properties.exists(variable) ? properties.get(variable) : defaultValue;

        if (value instanceof Boolean)
            return (Boolean) value;

        switch (String.valueOf(value)) {
            case "on":
            case "true":
            case "1":
            case "yes":
                return true;
        }
        return false;
    }

    @Inject
    public static void checkTickUpdates(Server server, int tick){
        Main.innerCoreServer.tick();
    }

    @Inject
    public static void forceShutdown(Server server, String reason) throws Exception {
        Main.LoadingStages.stop(server);
    }
}
