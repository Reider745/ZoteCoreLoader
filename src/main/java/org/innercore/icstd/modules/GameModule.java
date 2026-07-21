package org.innercore.icstd.modules;

import com.reider745.api.ClientOnly;
import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GameBindGenBase;

@ApiBinding("Game")
public class GameModule extends GameBindGenBase
{
    @ClientOnly
    @ApiBinding
    public static void addWorldToCache(String path) {}

    @ClientOnly
    @ApiBinding
    public static void updateWorlds() {}

    @ClientOnly
    @ApiBinding
    public static int getWorldsCount() {
        return 0;
    }
}
