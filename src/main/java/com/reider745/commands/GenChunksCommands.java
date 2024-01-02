package com.reider745.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.Level;
import com.reider745.world.BlockSourceMethods;

public class GenChunksCommands extends Command {
    public GenChunksCommands() {
        super("gen_chunks", "Loads or generates chunks in specified area");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("dimension", CommandParamType.INT, false),
                new CommandParameter("x1", CommandParamType.INT, false),
                new CommandParameter("z1", CommandParamType.INT, false),
                new CommandParameter("x2", CommandParamType.INT, true),
                new CommandParameter("z2", CommandParamType.INT, true),
        });
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if (!commandSender.isOp() && args.length < 3)
            return false;

        long start = System.currentTimeMillis();
        int dimension = Integer.parseInt(args[0]);
        int x1 = Integer.parseInt(args[1]);
        int z1 = Integer.parseInt(args[2]);
        int x2 = x1;
        int z2 = z1;
        if (args.length > 3) {
            x2 = Integer.parseInt(args[3]);
            if (args.length > 4) {
                z2 = Integer.parseInt(args[4]);
            }
        }

        Level level;
        try {
            level = BlockSourceMethods.getLevelForDimension(dimension);
        } catch (UnsupportedOperationException e) {
            return false;
        }
        int chunks = 0;
        for (int x = Math.min(x1, x2), lx = Math.max(x1, x2); x <= lx; x++) {
            for (int z = Math.min(z1, z2), lz = Math.max(z1, z2); z <= lz; z++) {
                level.loadChunk(x, z);
                chunks++;
            }
        }

        commandSender.sendMessage("Loaded " + chunks + " chunks in " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }
}
