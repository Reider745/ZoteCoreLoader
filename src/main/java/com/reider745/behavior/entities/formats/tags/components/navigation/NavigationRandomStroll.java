package com.reider745.behavior.entities.formats.tags.components.navigation;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.entities.EntityContentFactory;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NavigationRandomStroll extends NavigationComponentsTag {
    // TODO: Not support
    public int priority;
    public double speed_multiplier;

    @Override
    public String getNameTag() {
        return "minecraft:behavior.random_stroll";
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void load(EntityContentFactory factory, Object json) {
        if(json instanceof JSONObject object){
            NavigationRandomStroll stroll = new NavigationRandomStroll();

            stroll.priority = object.getInt("priority");
            stroll.speed_multiplier = object.optInt("speed_multiplier", 1);

            factory.navigationRandomStroll = stroll;
        }else
            Logger.warning("Not support NavigationRandomStroll "+json.getClass());
    }

    protected boolean isSidesAir(Block block){
        for(int side = 0;side < 6;side++) {
            final Block side_block = block.getSide(BlockFace.fromIndex(side));
            if (side_block.getId() == BlockID.AIR)
                return true;
        }
        return false;
    }

    protected List<Block> getBlocksSides(Block block){
        ArrayList<Block> list = new ArrayList<>();

        for(int side = 0;side < 6;side++) {
            final Block side_block = block.getSide(BlockFace.fromIndex(side));
            if(side_block.isSolid() && isSidesAir(side_block))
                list.add(side_block);
        }
        return list;
    }

    protected Block randBlock(Random random, Block startBlock, int iterations, int max_iterations){
        List<Block> blocks = getBlocksSides(startBlock);
        if(max_iterations <= iterations || blocks.isEmpty())
            return startBlock;
        return randBlock(random, blocks.get(random.nextInt(blocks.size())), ++iterations, max_iterations);
    }

    @Override
    public void onUpdate(CustomEntity entity) {
        final Random random = new Random();
        final Vector3 pos = entity.getPosition();
        final Vector3 target = pos.clone();

        if(random.nextBoolean())
            target.x -= random.nextInt(8) + 4;
        else
            target.x += random.nextInt(8) + 4;

        if(random.nextBoolean())
            target.z -= random.nextInt(8) + 4;
        else
            target.z += random.nextInt(8) + 4;

        entity.setTargetBlock(target);
        entity.tickMove = 0;
    }
}
