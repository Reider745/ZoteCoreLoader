package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.TextFormat;

import java.util.Map;

import com.reider745.block.BlockStateRegisters;

public class StateCommand extends Command {
    public StateCommand() {
        super("state", "Gets block state of requested runtime identifier");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("runtimeId", CommandParamType.INT, false),
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.isOp() || args.length < 1)
            return false;

        Map<String, Integer> states = BlockStateRegisters.getStateFor(Integer.parseInt(args[0]));
        if (states == null) {
            commandSender.sendMessage(TextFormat.RED + "There are no block with runtime identifier " + args[0] + "!");
        } else if (states.size() == 0) {
            commandSender.sendMessage(TextFormat.YELLOW + "There are no states with requested identifier.");
        } else {
            commandSender.sendMessage(states.toString());
        }
        return true;
    }
}
