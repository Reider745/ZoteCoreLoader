package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.entity.EntityMethod;

public class GetAllPlayersIdCommand extends Command {
    public GetAllPlayersIdCommand() {
        super("get_all_players_id", "");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp())
            return false;

        commandSender.getServer().getOnlinePlayers().values().forEach(player -> commandSender.sendMessage("Name: "+player.getDisplayName()+", Id: "+EntityMethod.getIdForEntity(player)));
        return true;
    }
}
