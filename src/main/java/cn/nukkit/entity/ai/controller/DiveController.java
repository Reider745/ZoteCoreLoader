package cn.nukkit.entity.ai.controller;

import cn.nukkit.api.PowerNukkitXOnly;
import cn.nukkit.api.Since;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;

/**
 * 下潜运动控制器，使实体下潜
 */
@PowerNukkitXOnly
@Since("1.19.60-r1")
public class DiveController implements IController {
    @Override
    public boolean control(EntityIntelligent entity) {
        //add dive force
        if (entity.getMemoryStorage().get(CoreMemoryTypes.ENABLE_DIVE_FORCE))
            //                                                                  抵消额外的浮力即可
            entity.motionY -= entity.getGravity() * (entity.getFloatingForceFactor() - 1);
        return true;
    }
}
