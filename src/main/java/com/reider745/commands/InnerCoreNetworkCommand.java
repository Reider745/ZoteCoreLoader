package com.reider745.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import com.reider745.entity.EntityMethod;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.apparatus.multiplayer.server.ModdedServer;

import java.util.ArrayList;

public class InnerCoreNetworkCommand extends Command {
    public InnerCoreNetworkCommand() {
        super("inner_core_network", "Returns connected and initializing clients");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.isOp())
            return false;

        StringBuilder list = new StringBuilder();
        ModdedServer server = Network.getSingleton().getServer();
        ArrayList<ConnectedClient> clients = new ArrayList<>();
        clients.addAll(server.getInitializingClients());
        clients.addAll(server.getConnectedClients());

        for (ConnectedClient client : clients) {
            list.append("\n");
            switch (client.getClientState()) {
                case OPEN -> list.append(TextFormat.GREEN);
                case CREATED, INITIALIZING -> list.append(TextFormat.YELLOW);
                case INIT_FAILED, CLOSED -> list.append(TextFormat.RED);
            }
            Player player = EntityMethod.getPlayerById(client.getPlayerUid());
            list.append("\n" + (player != null ? player.getName() : "...") + " -> " + client.getPlayerUid()
                    + " (state=" + client.getClientState().toString() + ", protocol="
                    + client.getChannelInterface().getChannel().getProtocolId() + ")");
        }

        if (list.length() == 0) {
            commandSender.sendMessage(TextFormat.YELLOW + "There are no clients.");
        } else {
            commandSender.sendMessage("Clients (" + clients.size() + "):" + list.toString());
        }
        return true;
    }
}
