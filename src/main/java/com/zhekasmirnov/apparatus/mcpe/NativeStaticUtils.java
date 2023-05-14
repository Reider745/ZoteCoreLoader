package com.zhekasmirnov.apparatus.mcpe;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import com.zhekasmirnov.apparatus.Apparatus;

public class NativeStaticUtils {
    public static boolean isExistingEntity(long entityUid){
        for (Level level : Apparatus.server.getLevels().values())
            for (Entity entity : level.getEntities())
                if(entity.getId() == entityUid)
                    return true;
        return false;
    }
}
