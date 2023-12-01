package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.api.CustomManager;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;

import java.util.ArrayList;

public class CustomBlocksCommands extends Command {
    public CustomBlocksCommands() {
        super("custom_blocks", "get list custom blocks");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp()) return false;

        StringBuilder message = new StringBuilder("===CustomBlocks===");
        for(Integer id : CustomBlock.blocks.keySet())
            ((ArrayList<String>) CustomBlock.getBlockManager(id).get("variants")).forEach(name -> message.append("\n" + name + ", id: " + id));
        commandSender.sendMessage(message.toString());
        return true;
    }
}
