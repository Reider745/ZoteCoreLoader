package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.reider745.api.CustomManager;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;


public class CustomItemsCommands extends Command {
    public CustomItemsCommands() {
        super("mods", "get list custom items");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp()) return false;

        String message = "===CustomItems===";
        for(Integer id : CustomItem.items.keySet()) {
            CustomManager manager = CustomItem.getItemManager(id);
            message += "\n"+manager.get(ItemMethod.PropertiesNames.NAME)+", id: "+id;
        }
        commandSender.sendMessage(message);
        return true;
    }
}
