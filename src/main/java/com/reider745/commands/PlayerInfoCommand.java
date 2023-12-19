package com.reider745.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.reider745.hooks.PlayerHooks;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

public class PlayerInfoCommand extends Command {
    public PlayerInfoCommand() {
        super("player_info", "debug inner core network player");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.STRING, false),
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if(!commandSender.isOp() || args.length < 1) return false;

        Player player = commandSender.getServer().getPlayer(args[0]);
        if(player != null){
            ConnectedClient client = PlayerHooks.getForPlayer(player);
            if(client == null) return false;

            commandSender.sendMessage("=====" + args[0] + "=====\n" + "ConnectedClient protocol: " + client.getChannelInterface().getChannel().getProtocolId());
            return true;
        }

        return false;
    }
}
