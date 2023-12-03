package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.block.BlockStateRegisters;
import com.reider745.block.CustomBlock;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;

import java.util.ArrayList;

public class CustomBlocksCommands extends Command {
    public CustomBlocksCommands() {
        super("custom_blocks", "get list custom blocks");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp()) return false;

        StringBuilder message = new StringBuilder("===CustomBlocks===");
        for(Integer id : CustomBlock.blocks.keySet()){
            ArrayList<String> variants = CustomBlock.getOrgVariants(id);
            for(int data = 0;data < variants.size();data++)
                message.append("\n" + ItemMethod.getNameForId(id, data, 0) + ", id: " + id+", data: "+data+", runtimeId: "+ BlockStateRegisters.getStateFor(id, data)+", texId: "+CustomBlock.getTextIdForNumber(id));
        }
        commandSender.sendMessage(message.toString());
        return true;
    }
}
