package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;

public class NativeStaticUtils {
    public static boolean isExistingEntity(long entityUid){
        for (Level level : Server.getInstance().getLevels().values())
            for (Entity entity : level.getEntities())
                if (entity.getId() == entityUid)
                    return true;
        return false;
    }
}
