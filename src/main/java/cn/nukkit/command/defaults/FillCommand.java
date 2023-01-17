package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

public class FillCommand extends VanillaCommand {
    public FillCommand(String name) {
        super(name, "Заполнит область", "/fill <x1> <y1> <z1> <x2> <y2> <z2> <block>");
        this.setPermission("Область заполнена");
        this.commandParameters.clear();
        this.commandParameters.put("default",
                new CommandParameter[]{
                        CommandParameter.newType("pos1", CommandParamType.POSITION),
                        CommandParameter.newType("pos2",  CommandParamType.POSITION),
                        CommandParameter.newEnum("block",  CommandEnum.ENUM_BLOCK),
                });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        try{
            if(!sender.isOp()) return false;
            Vector3 pos1 = new Vector3(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            Vector3 pos2 = new Vector3(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
            Level level;
            if (sender instanceof Player) level = ((Player) sender).getLevel();
            else  level = sender.getServer().getDefaultLevel();
            Block block = Block.getById(args[6]);
            for(int x = (int) Math.min(pos1.x, pos2.x);x < Math.max(pos1.x, pos2.x);x++)
                for(int y = (int) Math.min(pos1.y, pos2.y);y < Math.max(pos1.y, pos2.y);y++)
                    for(int z = (int) Math.min(pos1.z, pos2.z);z < Math.max(pos1.z, pos2.z);z++)
                        level.setBlock(new Vector3(x, y, z), block);
            return true;
        }catch (Exception e){sender.getServer().getLogger().debug(e.getMessage());}
        return false;
    }
}
