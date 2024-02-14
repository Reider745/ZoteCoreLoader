package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.utils.TextFormat;

import com.reider745.InnerCoreServer;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.entity.EntityMethod;
import com.reider745.event.EventListener;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.network.InnerCorePacket;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.RhinoException;

@Hooks(className = "cn.nukkit.Player")
public class PlayerHooks implements HookClass {

    private static boolean equalsAddress(String address1, String address2) {
        if (address1 == null || address2 == null || address1.isBlank() || address2.isBlank()) {
            return false;
        }
        if (address1.equalsIgnoreCase(address2)) {
            return true;
        }
        int offset;
        if ((offset = address1.lastIndexOf('/')) != -1) {
            address1 = address1.substring(offset + 1);
        }
        if ((offset = address2.lastIndexOf('/')) != -1) {
            address2 = address2.substring(offset + 1);
        }
        String[] ports = new String[2];
        if (address1.charAt(address1.length() - 1) != ']' && (offset = address1.lastIndexOf(':')) != -1) {
            if ((ports[0] = address1.substring(offset + 1)).equals("-1")) {
                ports[0] = null;
            }
            address1 = address1.substring(0, offset);
        }
        if (address2.charAt(address2.length() - 1) != ']' && (offset = address2.lastIndexOf(':')) != -1) {
            if ((ports[1] = address2.substring(offset + 1)).equals("-1")) {
                ports[1] = null;
            }
            address2 = address2.substring(0, offset);
        }
        if (address1.charAt(0) == '[' && address1.charAt((offset = address1.length() - 1)) == ']') {
            address1 = address1.substring(1, offset);
        }
        if (address2.charAt(0) == '[' && address2.charAt((offset = address2.length() - 1)) == ']') {
            address2 = address2.substring(1, offset);
        }
        return address1.equalsIgnoreCase(address2)
                && ((ports[0] == null || ports[1] == null) || ports[0].equalsIgnoreCase(ports[1]));
    }

    public static ConnectedClient getForPlayer(Player player) {
        List<ConnectedClient> clients = Network.getSingleton().getServer().getInitializingClients();
        for (ConnectedClient client : clients)
            if (equalsAddress(player.getSocketAddress().toString(),
                    client.getChannelInterface().getChannel().getClient()))
                return client;

        clients = Network.getSingleton().getServer().getConnectedClients();
        for (ConnectedClient client : clients)
            if (equalsAddress(player.getSocketAddress().toString(),
                    client.getChannelInterface().getChannel().getClient()))
                return client;

        return null;
    }

    @Inject(type = TypeHook.BEFORE_NOT_REPLACE)
    public static void close(Player player, TextContainer message, String reason, boolean notify) {
        ConnectedClient client = getForPlayer(player);
        try {
            if (client != null)
                client.disconnect();
        } catch (Throwable ignore) {
        }
        InnerCorePacket.closePlayer(player);
    }

    @Inject
    public static void dataPacket(Player player, DataPacket packet) {
        if (InnerCoreServer.isDebugInnerCoreNetwork() && !player.isOnline())
            System.out.println("sending packet player=" + player.getId() + " id=" + packet.packetId()
                    + " pid=" + packet.pid() + " channel=" + packet.getChannel());
    }

    public static void showExceptionForm(Player player, String title, String message, Throwable exc) {
        if (!InnerCoreServer.shouldPlayersReceiveExceptionForms() || !EntityMethod.isValid(player)) {
            return;
        }
        FormWindowSimple form = new FormWindowSimple(title, message, Arrays.asList(new ElementButton("Rejoin")));
        if (exc instanceof RhinoException rhino) {
            form.setContent(form.getContent() + "\n\n" + TextFormat.RED + exc.getLocalizedMessage() + "\n"
                    + rhino.getScriptStackTrace().replace('\t', ' '));
        }
        player.removeAllWindows();
        player.showFormWindow(form, EventListener.FORM_REJOIN_EXCEPTION);
    }
}
