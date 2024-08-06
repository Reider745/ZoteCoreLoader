package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;

import java.util.Arrays;

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

        try{
            float aboba1 = 0, aboba2 = 0;
            long innerCoreSum = 0;
            for(long time : innerCoreTimes)
                if(time != 0) {
                    innerCoreSum += Math.min(20, 1000 / time);
                    aboba1++;
                }

            long nukkitSum = 0;
            for(long time : nukkitTimes)
                if(time != 0) {
                    nukkitSum += Math.min(20, 1000 / time);
                    aboba2++;
                }

            if(innerCoreSum != 0)
                commandSender.sendMessage("Inner core tps: " + (innerCoreSum / aboba1));
            else
                commandSender.sendMessage("Inner core tps: 20.0");

            if(nukkitSum != 0)
                commandSender.sendMessage("Nukkit tps: " + (nukkitSum / aboba2));
            else
                commandSender.sendMessage("Nukkit tps: 20.0");
        }catch (Exception e){
            commandSender.sendMessage("Not enough information");
            commandSender.sendMessage(Arrays.toString(innerCoreTimes));
            commandSender.sendMessage(Arrays.toString(nukkitTimes));
        }

        return true;
    }
}
