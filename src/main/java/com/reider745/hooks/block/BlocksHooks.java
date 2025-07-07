package com.reider745.hooks.block;

import cn.nukkit.Server;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.Utils;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.api.hooks.annotation.ShadowGetField;
import com.reider745.api.hooks.annotation.ShadowSetField;
import com.reider745.block.BlockMethods;
import com.reider745.block.wall.*;
import com.reider745.block.wall.BlockWall;

import java.lang.reflect.Constructor;

@Hooks(className = "cn.nukkit.block.Block")
public class BlocksHooks {
    private static void registerWall(BlockWall.WallType wallType, Class<? extends BlockWall> clazz) {
        Block.list[wallType.id] = clazz;
    }

    @Inject(type = TypeHook.AFTER_REPLACE)
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

        if (Block.list == null) {
            Block.list = new Class[Block.MAX_BLOCK_ID];
            Block.fullList = new Block[Block.MAX_BLOCK_ID * (1 << Block.DATA_BITS)];
            Block.light = new int[Block.MAX_BLOCK_ID];
            Block.lightFilter = new int[Block.MAX_BLOCK_ID];
            Block.solid = new boolean[Block.MAX_BLOCK_ID];
            Block.hardness = new double[Block.MAX_BLOCK_ID];
            Block.transparent = new boolean[Block.MAX_BLOCK_ID];
            Block.diffusesSkyLight = new boolean[Block.MAX_BLOCK_ID];
            Block.hasMeta = new boolean[Block.MAX_BLOCK_ID];

            ReflectHelper.invoke(Blocks.class, "init", new Object[0]);

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

            final BlockUnknown BLOCK_UNKNOWN = new BlockUnknown(Block.MAX_BLOCK_ID, 0);

            for (int id = 0; id < Block.MAX_BLOCK_ID; id++) {
                Class<?> c = Block.list[id];
                if (c != null) {
                    Block block;
                    try {
                        block = (Block) c.getDeclaredConstructor().newInstance();
                        try {
                            @SuppressWarnings("rawtypes")
                            Constructor constructor = c.getDeclaredConstructor(int.class);
                            constructor.setAccessible(true);
                            for (int data = 0; data < (1 << Block.DATA_BITS); ++data) {
                                int fullId = (id << Block.DATA_BITS) | data;
                                Block b;
                                try {
                                    b = (Block) constructor.newInstance(data);
                                    if (b.getDamage() != data) {
                                        b = BLOCK_UNKNOWN;
                                    }
                                } catch (Exception e) {
                                    Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
                                    b = BLOCK_UNKNOWN;
                                }
                                Block.fullList[fullId] = b;
                            }
                            Block.hasMeta[id] = true;
                        } catch (NoSuchMethodException ignore) {
                            for (int data = 0; data < Block.DATA_SIZE; ++data) {
                                int fullId = (id << Block.DATA_BITS) | data;
                                Block.fullList[fullId] = block;
                            }
                        }
                    } catch (Exception e) {
                        Server.getInstance().getLogger().error("Error while registering " + c.getName(), e);
                        for (int data = 0; data < Block.DATA_SIZE; ++data) {
                            Block.fullList[(id << Block.DATA_BITS) | data] = BLOCK_UNKNOWN;
                        }
                        return;
                    }

                    Block.solid[id] = block.isSolid();
                    Block.transparent[id] = block.isTransparent();
                    Block.diffusesSkyLight[id] = block.diffusesSkyLight();
                    Block.hardness[id] = block.getHardness();
                    Block.light[id] = block.getLightLevel();

                    if (block.isSolid()) {
                        if (block.isTransparent()) {
                            if (block instanceof BlockLiquid || block instanceof BlockIce) {
                                Block.lightFilter[id] = 2;
                            } else {
                                Block.lightFilter[id] = 1;
                            }
                        } else if (block instanceof BlockSlime) {
                            Block.lightFilter[id] = 1;
                        } else if (id == Block.CAULDRON_BLOCK) {
                            Block.lightFilter[id] = 3;
                        } else {
                            Block.lightFilter[id] = 15;
                        }
                    } else {
                        Block.lightFilter[id] = 1;
                    }
                } else {
                    Block.lightFilter[id] = 1;
                    for (int data = 0; data < Block.DATA_SIZE; ++data) {
                        Block.fullList[(id << Block.DATA_BITS) | data] = BLOCK_UNKNOWN;
                    }
                }
            }
        }
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
