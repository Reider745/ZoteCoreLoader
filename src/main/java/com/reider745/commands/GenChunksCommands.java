package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;
import com.reider745.world.BlockSourceMethods;

public class GenChunksCommands extends Command {
    public GenChunksCommands(){
        super("gen_chunks", "load chunks");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("dimension", CommandParamType.INT, false),
                new CommandParameter("x1", CommandParamType.INT, false),
                new CommandParameter("z1", CommandParamType.INT, false),
                new CommandParameter("x2", CommandParamType.INT, false),
                new CommandParameter("z2", CommandParamType.INT, false),
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if(!commandSender.isOp() && args.length < 5) return false;

        int dimension = Integer.parseInt(args[0]);
        int x1 = Integer.parseInt(args[1]);
        int z1 = Integer.parseInt(args[2]);
        int x2 = Integer.parseInt(args[3]);
        int z2 = Integer.parseInt(args[4]);

        Level level = BlockSourceMethods.getLevelForDimension(dimension);
        long start = System.currentTimeMillis();

        for(int x = Math.min(x1, x2);x < Math.max(x1, x2);x++)
            for(int z = Math.min(z1, z2);z < Math.max(z1, z2);z++)
                level.loadChunk(x, z);

        commandSender.sendMessage("End loaded chunks "+(System.currentTimeMillis()-start));
        return true;
    }
}
