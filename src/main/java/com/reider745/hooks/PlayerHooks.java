package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.lang.TextContainer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.Hooks;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import java.util.List;

@Hooks(className = "cn.nukkit.Player")
public class PlayerHooks implements HookClass {
    private static boolean isAddress(String address1, String address2) {
        String[] protocol1 = address1.split(":", 2);
        String[] protocol2 = address2.split(":", 2);

        if (protocol1.length == protocol2.length) {
            return address1.equals(address2);
        }
        return protocol1[0].equals(protocol2[0]);
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
        try {
            if (client != null)
                client.getChannelInterface().close();
        } catch (Exception ignore) {
        }
    }
}
