package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import com.reider745.api.CustomManager;
import com.reider745.item.CustomItem;
import com.reider745.item.ItemMethod;

public class CustomItemsCommands extends Command {
    public CustomItemsCommands() {
        super("custom_items", "Gets list of defined custom items");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp())
            return false;

        StringBuilder list = new StringBuilder();
        for (Integer id : CustomItem.items.keySet()) {
            CustomManager manager = CustomItem.getItemManager(id);
            list.append("\n" + manager.get(ItemMethod.PropertiesNames.NAME) + " -> " + id + " (named="
                    + CustomItem.getTextIdForNumber(id) + ")");
        }

        if (list.length() == 0) {
            commandSender.sendMessage(TextFormat.YELLOW + "There are no custom items.");
        } else {
            commandSender.sendMessage("Custom items (" + CustomItem.items.size() + "):" + list.toString());
        }
        return true;
    }
}
