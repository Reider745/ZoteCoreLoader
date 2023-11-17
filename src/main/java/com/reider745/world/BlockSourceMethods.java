package com.reider745.world;

import cn.nukkit.level.Level;
import com.reider745.InnerCoreServer;

public class BlockSourceMethods {
    public static Level getLevelForDimension(int dimension){
        if(dimension >= 0 && dimension <= 2)
            dimension++;
        return InnerCoreServer.server.getLevel(dimension);
    }
}
