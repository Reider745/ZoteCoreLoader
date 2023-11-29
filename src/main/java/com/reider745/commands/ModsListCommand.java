package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.zhekasmirnov.apparatus.modloader.ApparatusMod;
import com.zhekasmirnov.apparatus.modloader.ApparatusModInfo;
import com.zhekasmirnov.apparatus.modloader.ApparatusModLoader;
import com.zhekasmirnov.apparatus.modloader.LegacyInnerCoreMod;
import com.zhekasmirnov.innercore.mod.build.Mod;

import java.util.List;

public class ModsListCommand extends Command {
    public ModsListCommand() {
        super("mods", "get list mod");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp()) return false;

        List<ApparatusMod> mods = ApparatusModLoader.getSingleton().getAllMods();
        String message = "===InnerCoreMods===";
        for(ApparatusMod mod : mods){
            if(mod instanceof LegacyInnerCoreMod legacyMod){
                Mod instance = legacyMod.getLegacyModInstance();
                message += "\n"+instance.getName()+", version: "+instance.getVersion()+", enabled: "+legacyMod.isEnabledAndAbleToRun();
            }
        }
        commandSender.sendMessage(message);
        return true;
    }
}
