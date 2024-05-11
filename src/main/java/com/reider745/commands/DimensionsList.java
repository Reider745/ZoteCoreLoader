package com.reider745.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.world.FakeDimensions;

public class DimensionsList  extends Command {
    public DimensionsList() {
        super("dimensions_list", "get list dimensions");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp())
            return false;

        StringBuilder builder = new StringBuilder();

        builder.append("====Dimensions====");
        Server.getInstance().getLevels().forEach((levelId, level) ->
                builder.append("\nid: ")
                        .append(FakeDimensions.getFakeIdForLevel(level))
                        .append(", name: ")
                        .append(level.getName()));

        commandSender.sendMessage(builder.toString());
        return true;
    }
}
