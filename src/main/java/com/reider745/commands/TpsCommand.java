package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

public class TpsCommand extends Command {
    public static long[] innerCoreTimes = new long[20];
    public static long[] nukkitTimes = new long[20];
    public TpsCommand() {
        super("tps");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp())
            return false;

        long innerCoreSum = 0;
        for(long time : innerCoreTimes)
            innerCoreSum += Math.min(20, 1000 / time);

        long nukkitSum = 0;
        for(long time : nukkitTimes)
            nukkitSum += Math.min(20, 1000 / time);

        commandSender.sendMessage("Inner core tps: "+(innerCoreSum/20.0f));
        commandSender.sendMessage("Nukkit tps: "+(nukkitSum/20.0f));

        return true;
    }
}
