package com.reider745.event;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.math.Vector3;
import com.zhekasmirnov.innercore.api.NativeCallback;

public class EventListener implements Listener {
    @EventHandler
    public void use(PlayerInteractEvent event){
        Vector3 pos = event.getTouchVector();
        Player player = event.getPlayer();

        NativeCallback.onItemUsed((int) pos.x, (int) pos.y, (int) pos.z, event.getFace().getIndex(), (float) pos.x, (float) pos.y, (float) pos.z, true, player.getHealth() > 0, player.getId());
    }
}
