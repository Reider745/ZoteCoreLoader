package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.math.NukkitMath;

import java.util.Arrays;

public class TpsCommand extends Command {
    public static long[] innerCoreTimes = new long[20];

    public TpsCommand() {
        super("tps");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if(!commandSender.isOp())
            return false;

        try{
            float count = 0;
            long sum = 0;
            for(long time : innerCoreTimes)
                if(time != 0) {
                    sum += time;
                    count++;
                }

            if(sum != 0) {
                final float avgTime = sum / count;
                commandSender.sendMessage("Inner core tps: " + NukkitMath.round(Math.min(20, 1000 / avgTime), 2)  + ", ms: "+avgTime);
            }else
                commandSender.sendMessage("Inner core tps: 20.0");

            commandSender.sendMessage("Nukkit tps: " + NukkitMath.round(commandSender.getServer().getTicksPerSecond(), 2));
        }catch (Exception e){
            commandSender.sendMessage("Not enough information");
            commandSender.sendMessage(Arrays.toString(innerCoreTimes));
        }

        return true;
    }
}
