package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.lang.TextContainer;
import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import java.util.List;

@Hooks(className = "cn.nukkit.Player")
public class PlayerHooks implements HookClass {
    private static boolean isAddress(String addres1, String addres2) {
        String[] split1 = addres1.split(":");
        String[] split2 = addres2.split(":");

        if (split1.length == split2.length)
            return addres1.equals(addres2);

        InnerCoreServer.server.getLogger().warning("It is not possible to get the player's port");
        return split1[0].equals(split2[0]);
    }

    public static ConnectedClient getForPlayer(Player player) {
        List<ConnectedClient> clients = Network.getSingleton().getServer().getInitializingClients();
        for (ConnectedClient client : clients)
            if (isAddress(player.getSocketAddress().toString(), client.getChannelInterface().getChannel().getClient()))
                return client;

        clients = Network.getSingleton().getServer().getConnectedClients();
        for (ConnectedClient client : clients)
            if (isAddress(player.getSocketAddress().toString(), client.getChannelInterface().getChannel().getClient()))
                return client;

        return null;
    }

    @Inject(type = TypeHook.AFTER)
    public static void completeLoginSequence(Player self) {
        if (getForPlayer(self) == null)
            self.kick("Failed to connection Inner Core 1.16.201");
    }

    @Inject
    public static void close(Player self, TextContainer message, String reason, boolean notify) {
        ConnectedClient client = getForPlayer(self);
        try{
            if(client != null)
                client.getChannelInterface().close();
        }catch (Exception ignore){}
    }
}
