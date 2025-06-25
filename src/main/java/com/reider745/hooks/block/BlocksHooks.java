package com.reider745.hooks.block;

import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.Utils;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.ShadowGetField;
import com.reider745.api.hooks.annotation.ShadowSetField;
import com.reider745.block.BlockMethods;
import com.reider745.block.wall.*;

@Hooks(className = "cn.nukkit.block.Block")
public class BlocksHooks {
    @Inject
    public static void init() {
        ReflectHelper.setField(Block.class, "MAX_BLOCK_ID", 16000);

        /*
        Ломает совместимость с некоторыми плагинами!
        Необходимо для расширения максимального количество состояний на блок, с 64 до 512, что нужно заборам и пользовательских состояний для блоков
        Увеличение дата битов на блок влечет уменьшением максимально зарегистрированных блоков, иза-чего выше 16 ставить не стоит, что-бы для блоков был доступен хотя-бы short
        Так-же генерация Block.fullList не очень любит большое количество дата-битов, для увелечения состояний с 512 до 64к нужно переделать генерацию fullList
         */
        ReflectHelper.setField(Block.class, "DATA_BITS", Utils.dynamic(9));
        ReflectHelper.setField(Block.class, "DATA_SIZE", Utils.dynamic(1 << Block.DATA_BITS));
        ReflectHelper.setField(Block.class, "DATA_MASK", Utils.dynamic(Block.DATA_SIZE - 1));
    }

    private static void registerWall(BlockWall.WallType wallType, Class<? extends BlockWall> clazz) {
        Block.list[wallType.id] = clazz;
    }

    @Inject(className = "cn.nukkit.block.Blocks", method = "init")
    public static void initBlocks() {
        registerWall(BlockWall.WallType.DIORITE, BlockDioriteWall.class);
        registerWall(BlockWall.WallType.COBBLESTONE, BlockCobblestoneWall.class);
        registerWall(BlockWall.WallType.BRICK, BlockBrickWall.class);
        registerWall(BlockWall.WallType.GRANITE, BlockGraniteWall.class);
        registerWall(BlockWall.WallType.ANDESITE, BlockAndesiteWall.class);
        registerWall(BlockWall.WallType.SANDSTONE, BlockSandstoneWall.class);
        registerWall(BlockWall.WallType.PRISMARINE, BlockPrismarineWall.class);
        registerWall(BlockWall.WallType.STONE_BRICK, BlockStoneBrickWall.class);
        registerWall(BlockWall.WallType.NETHER_BRICK, BlockNetherBrickWall.class);
        registerWall(BlockWall.WallType.END_BRICK, BlockEndstoneBrickWall.class);
        registerWall(BlockWall.WallType.RED_NETHER_BRICK, BlockRedNetherBrickWall.class);
        registerWall(BlockWall.WallType.MOSSY_STONE_BRICK, BlockMossyStoneBrickWall.class);
        registerWall(BlockWall.WallType.MOSSY_COBBLESTONE, BlockMossyCobblestoneWall.class);
        registerWall(BlockWall.WallType.RED_SANDSTONE, BlockRedSandstoneWall.class);
        registerWall(BlockWall.WallType.BLACKSTONE, BlockBlackstoneWall.class);
        registerWall(BlockWall.WallType.POLISHED_BLACKSTONE, BlockPolishedBlackstoneWall.class);
        registerWall(BlockWall.WallType.POLISHED_BLACKSTONE_BRICK, BlockPolishedBlackstoneBrickWall.class);
    }

    @ShadowGetField(className = "cn.nukkit.item.Item", field = "block")
    public static Block getBlockForSelf(Item self) {throw new RuntimeException();};

    @ShadowSetField(className = "cn.nukkit.item.Item", field = "block")
    public static void setBlockForSelf(Item self, Block block) {throw new RuntimeException();};

    @Inject(className = "cn.nukkit.item.ItemBlock")
    public static Block getBlock(ItemBlock self) {
        Block block = getBlockForSelf(self);
        if(block.getId() == 139) {
            final BlockWall.WallType type = BlockWall.WallType.values()[self.getDamage()];
            block = Block.get(type.id);
            setBlockForSelf(self, block);
            return block;
        }
        return block.clone();
    }

    public static Boolean isSolid(Block self) {
        return (Boolean) BlockMethods.getOrDefault(self.getId(), BlockMethods.SOLID, null);
    }

    public static Double getHardness(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.DESTROY_TIME, null);
    }

    public static Double getResistance(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.EXPLOSION_RESISTANCE, null);
    }

    public static Double getFrictionFactor(Block self) {
        return (Double) BlockMethods.getOrDefault(self.getId(), BlockMethods.FRICTION, null);
    }

    public static Integer getLightLevel(Block self) {
        return (Integer) BlockMethods.getOrDefault(self.getId(), BlockMethods.LIGHT_LEVEL, null);
    }
}
