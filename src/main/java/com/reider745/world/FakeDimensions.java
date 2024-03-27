package com.reider745.world;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimension;

import java.util.ArrayList;
import java.util.HashMap;

public class FakeDimensions {
    private static int CURRENT_FAKE_ID = Integer.MIN_VALUE;
    private static final HashMap<Integer, Integer> fakeIdForLevelId = new HashMap<>();
    private static final HashMap<Integer, Level> levelForFakeId = new HashMap<>();
    private static final ArrayList<Integer> ignore = new ArrayList<>();

    public static int getFakeIdForLevel(final Level level){
        /*final int dimensionId = level.getDimension();
        if((dimensionId >= Level.DIMENSION_OVERWORLD && dimensionId <= Level.DIMENSION_THE_END) || CustomDimension.getDimensionById(dimensionId) != null) {
            levelForFakeId.put(dimensionId, level);
            return dimensionId;
        }*/

        if(ignore.contains(level.getDimension()))
            return level.getDimension();

        //Проверка для псевдо-измерений накита
        Integer fakeId = fakeIdForLevelId.get(level.getId());
        if(fakeId == null){
            while (levelForFakeId.containsKey(CURRENT_FAKE_ID))
                CURRENT_FAKE_ID++;
            fakeId = CURRENT_FAKE_ID;

            fakeIdForLevelId.put(level.getId(), fakeId);
            levelForFakeId.put(fakeId, level);
        }

        return fakeId;
    }

    public static void registerIgnore(int id){
        ignore.add(id);
        levelForFakeId.put(id, null);
    }

    public static void init(){
        registerIgnore(Level.DIMENSION_OVERWORLD);
        registerIgnore(Level.DIMENSION_NETHER);
        registerIgnore(Level.DIMENSION_THE_END);
    }

    public static Level getLevelForFakeId(int fakeId){
        return !ignore.contains(fakeId) ? levelForFakeId.get(fakeId) : Server.getInstance().getLevels().values().stream()
                .filter(level -> level.getDimension() == fakeId)
                .findFirst().orElse(null);
    }
}