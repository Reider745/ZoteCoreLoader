package com.reider745.hooks;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import com.reider745.InnerCoreServer;
import com.reider745.Main;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;

@Hooks(class_name = "cn.nukkit.Server")
public class ServerHooks {
    @Inject
    public static void start(Server self) throws Exception {
        Main.LoadingStages.start(self);
    }

    @Inject
    public static int getPropertyInt(Server server, String variable, Integer defaultValue){
        if(variable.equals("multiversion-min-protocol") || variable.equals("multiversion-max-protocol"))
            return InnerCoreServer.PROTOCOL;

        Config properties = server.getProperties();
        return properties.exists(variable) ? (!properties.get(variable).equals("") ? Integer.parseInt(String.valueOf(properties.get(variable))) : defaultValue) : defaultValue;
    }
}
