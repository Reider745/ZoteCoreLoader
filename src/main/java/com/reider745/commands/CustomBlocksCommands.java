package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import com.reider745.block.BlockStateRegisters;
import com.reider745.block.CustomBlock;
import com.reider745.item.ItemMethod;
import java.util.ArrayList;

public class CustomBlocksCommands extends Command {
    public CustomBlocksCommands() {
        super("custom_blocks", "Gets list of defined custom blocks");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp())
            return false;

        StringBuilder list = new StringBuilder();
        for (Integer id : CustomBlock.blocks.keySet()) {
            ArrayList<String> variants = CustomBlock.getOrgVariants(id);
            for (int data = 0; data < variants.size(); data++)
                list.append("\n" + ItemMethod.getNameForId(id, data, 0) + " -> " + id + ":" + data
                        + " (runtime=" + BlockStateRegisters.getStateFor(id, data) + ", named="
                        + CustomBlock.getTextIdForNumber(id) + ")");
        }

        if (list.length() == 0) {
            commandSender.sendMessage(TextFormat.YELLOW + "There are no custom blocks.");
        } else {
            commandSender.sendMessage("Custom blocks (" + CustomBlock.blocks.size() + "):" + list.toString());
        }
        return true;
    }
}
