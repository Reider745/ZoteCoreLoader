package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import com.reider745.block.BlockStateRegisters;
import com.zhekasmirnov.apparatus.adapter.innercore.game.block.BlockState;

public class StateCommand extends Command {
    public StateCommand() {
        super("state", "get list mod");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("runtimeId", CommandParamType.INT, false),
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if(!commandSender.isOp() || args.length < 1) return false;

        commandSender.sendMessage(BlockStateRegisters.getStateFor(Integer.parseInt(args[0])).toString());
        return true;
    }
}
