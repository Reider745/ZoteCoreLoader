package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.reider745.block.BlockStateRegisters;
import com.reider745.block.CustomBlock;
import com.reider745.item.ItemMethod;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

import java.util.ArrayList;
import java.util.List;

public class InnerCoreNetworkCommand extends Command {
    public InnerCoreNetworkCommand() {
        super("inner_core_network", "debug network command");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if(!commandSender.isOp()) return false;

        StringBuilder message = new StringBuilder("===Network===");
        List<ConnectedClient> clients = Network.getSingleton().getServer().getConnectedClients();
        for(ConnectedClient client : clients)
            message.append("\n"+client.getPlayerUid()+" "+client.getClientState().name());

        clients = Network.getSingleton().getServer().getInitializingClients();
        for(ConnectedClient client : clients)
            message.append("\n"+client.getPlayerUid()+" "+client.getClientState().name());

        commandSender.sendMessage(message.toString());
        return true;
    }
}
