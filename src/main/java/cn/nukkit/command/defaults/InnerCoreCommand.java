package cn.nukkit.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.level.GlobalBlockPalette;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class InnerCoreCommand extends VanillaCommand {
    public InnerCoreCommand(String name){
        super(name);
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("type", true, CommandParamType.STRING),
                CommandParameter.newType("data", true, CommandParamType.STRING)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(args.length < 1) {
            sender.getServer().getLogger().info("Не достаточно аргументов");
            return false;
        }
        if(args[0].equals("con")) {
            if(args.length < 2) {
                sender.getServer().getLogger().info("Не достаточно аргументов");
                return false;
            }
            Player player = sender.getServer().getPlayer(args[1]);
            if(player == null){
                sender.getServer().getLogger().info("Не найден игрок " + args[1]);
                return false;
            }
            Network.getSingleton().onConnection(player);
            sender.getServer().getLogger().info("Успешно выполнено " + args[0]);
            return true;
        }else if(args[0].equals("id")){
            if(args.length < 3) {
                sender.getServer().getLogger().info("Не достаточно аргументов");
                return false;
            }
            Logger.info("RuntimeId", String.valueOf(GlobalBlockPalette.getOrCreateRuntimeId(Integer.parseInt(args[1]), Integer.parseInt(args[2]))));
            return true;
        }
        sender.getServer().getLogger().info("Не найден тип " + args[0]);
        return false;
    }
}
