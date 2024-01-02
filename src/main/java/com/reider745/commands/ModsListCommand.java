package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.innercore.mod.build.Mod;

import java.util.List;

public class ModsListCommand extends Command {
    public ModsListCommand() {
        super("mods", "Gets a list of mods running on the server");
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp())
            return false;

        StringBuilder list = new StringBuilder();
        List<ApparatusMod> mods = ApparatusModLoader.getSingleton().getAllMods();
        for (ApparatusMod apparatus : mods) {
            if (apparatus instanceof LegacyInnerCoreMod legacyMod) {
                Mod mod = legacyMod.getLegacyModInstance();
                if (list.length() > 0) {
                    list.append(TextFormat.WHITE + ", ");
                }
                list.append(apparatus.isEnabledAndAbleToRun() ? TextFormat.GREEN : TextFormat.RED);
                list.append(mod.getName() + " v" + mod.getVersion());
                if (mod.isConfiguredForMultiplayer()) {
                    if (mod.getMultiplayerVersion() != mod.getVersion()) {
                        list.append(" (v" + mod.getMultiplayerVersion() + ")");
                    }
                    if (mod.isClientOnly()) {
                        list.append(" (client)");
                    }
                }
            }
        }

        commandSender.sendMessage("Mods (" + mods.size() + "): " + list.toString());
        return true;
    }
}
