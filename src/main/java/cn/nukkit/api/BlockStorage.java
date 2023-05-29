package cn.nukkit.api;

import cn.nukkit.Server;
import cn.nukkit.block.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockStorage implements BlockID {
    private static HashMap<Integer, Class<? extends Block>> classes = new HashMap<>();

    private static HashMap<String, Block> blocks = new HashMap<>();

    private static HashMap<String, Block> hashStringIds = new HashMap<>();

    private static HashMap<Integer, Integer> light = new HashMap<>();
    private static HashMap<Integer, Integer> lightFilter = new HashMap<>();
    private static HashMap<Integer, Boolean> solid = new HashMap<>();
    private static HashMap<Integer, Double> hardness = new HashMap<>();
    private static HashMap<Integer, Boolean> transparent = new HashMap<>();
    private static HashMap<Integer, Boolean> hasMeta = new HashMap<>();
    private static HashMap<Integer, Block> fullId = new HashMap<>();

    public static Integer getLight(int id){
        return light.get(id);
    }

    public static Integer getLightFilter(int id){
        return lightFilter.get(id);
    }
    public static boolean canSolid(int id){
        Boolean result = solid.get(id);
        return result == null ? false : result;
    }
    public static double getHardness(int id){
        return hardness.get(id);
    }
    public static boolean canTransparent(int id){
        Boolean result = transparent.get(id);
        return result == null ? false : result;
    }

    public static boolean canMeta(int id){
        Boolean result = hasMeta.get(id);
        return result == null ? false : result;
    }

    public static Block get(String id){
        return hashStringIds.get(id);
    }

    public static Block get(int id, int data){
        return blocks.get(id+":"+data);
    }

    public static Block get(int id){
        return get(id, 0);
    }

    //public static Block getFullStete

    public static void registerBlock(int id, int data, Block block){
        blocks.put(id+":"+data, block);
        fullId.put((id << 4) | data, block);

        hasMeta.put(id, true);

        solid.put(id, block.isSolid());
        transparent.put(id, block.isTransparent());
        hardness.put(id, block.getHardness());
        light.put(id, block.getLightLevel());

        if (block.isSolid()) {
            if (block.isTransparent()) {
                if (block instanceof BlockLiquid || block instanceof BlockIce) {
                    lightFilter.put(id, 2);
                } else {
                    lightFilter.put(id, 1);
                }
            } else {
                lightFilter.put(id, 15);
            }
        } else {
            lightFilter.put(id, 1);
        }
    }

    public static void registerBlock(int id, Class<? extends Block> block_class){
        classes.put(id, block_class);

        Block block;
        try {
            block = (Block) block_class.newInstance();
            try {
                Constructor constructor = block_class.getDeclaredConstructor(int.class);
                constructor.setAccessible(true);
                for (int data = 0; data < 16; ++data) {
                    Block bl = (Block) constructor.newInstance(data);
                    blocks.put(id+":"+data, bl);
                    fullId.put((id << 4) | data, bl);
                }
                hasMeta.put(id, true);
            } catch (NoSuchMethodException ignore) {
                for (int data = 0; data < 16; ++data) {
                    blocks.put(id+":"+data, block);
                    fullId.put((id << 4) | data, block);
                }
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().error("Error while registering " + block_class.getName(), e);
            for (int data = 0; data < 16; ++data) {
                blocks.put(id+":"+data, new BlockUnknown(id, data));
            }
            return;
        }

        solid.put(id, block.isSolid());
        transparent.put(id, block.isTransparent());
        hardness.put(id, block.getHardness());
        light.put(id, block.getLightLevel());

        if (block.isSolid()) {
            if (block.isTransparent()) {
                if (block instanceof BlockLiquid || block instanceof BlockIce) {
                    lightFilter.put(id, 2);
                } else {
                    lightFilter.put(id, 1);
                }
            } else {
                lightFilter.put(id, 15);
            }
        } else {
            lightFilter.put(id, 1);
        }
    }

    public static void forEach(BiConsumer<Integer, Class<? extends Block>> block){
        classes.forEach(block);
    }

    public static void init(){
        Server.getInstance().getLogger().info("Loaded blocks...");

        Class<? extends Block>[] list = new Class[256];

        list[AIR] = BlockAir.class; //0
        list[STONE] = BlockStone.class; //1
        list[GRASS] = BlockGrass.class; //2
        list[DIRT] = BlockDirt.class; //3
        list[COBBLESTONE] = BlockCobblestone.class; //4
        list[PLANKS] = BlockPlanks.class; //5
        list[SAPLING] = BlockSapling.class; //6
        list[BEDROCK] = BlockBedrock.class; //7
        list[WATER] = BlockWater.class; //8
        list[STILL_WATER] = BlockWaterStill.class; //9
        list[LAVA] = BlockLava.class; //10
        list[STILL_LAVA] = BlockLavaStill.class; //11
        list[SAND] = BlockSand.class; //12
        list[GRAVEL] = BlockGravel.class; //13
        list[GOLD_ORE] = BlockOreGold.class; //14
        list[IRON_ORE] = BlockOreIron.class; //15
        list[COAL_ORE] = BlockOreCoal.class; //16
        list[WOOD] = BlockWood.class; //17
        list[LEAVES] = BlockLeaves.class; //18
        list[SPONGE] = BlockSponge.class; //19
        list[GLASS] = BlockGlass.class; //20
        list[LAPIS_ORE] = BlockOreLapis.class; //21
        list[LAPIS_BLOCK] = BlockLapis.class; //22
        list[DISPENSER] = BlockDispenser.class; //23
        list[SANDSTONE] = BlockSandstone.class; //24
        list[NOTEBLOCK] = BlockNoteblock.class; //25
        list[BED_BLOCK] = BlockBed.class; //26
        list[POWERED_RAIL] = BlockRailPowered.class; //27
        list[DETECTOR_RAIL] = BlockRailDetector.class; //28
        list[STICKY_PISTON] = BlockPistonSticky.class; //29
        list[COBWEB] = BlockCobweb.class; //30
        list[TALL_GRASS] = BlockTallGrass.class; //31
        list[DEAD_BUSH] = BlockDeadBush.class; //32
        list[PISTON] = BlockPiston.class; //33
        list[PISTON_HEAD] = BlockPistonHead.class; //34
        list[WOOL] = BlockWool.class; //35
        list[DANDELION] = BlockDandelion.class; //37
        list[FLOWER] = BlockFlower.class; //38
        list[BROWN_MUSHROOM] = BlockMushroomBrown.class; //39
        list[RED_MUSHROOM] = BlockMushroomRed.class; //40
        list[GOLD_BLOCK] = BlockGold.class; //41
        list[IRON_BLOCK] = BlockIron.class; //42
        list[DOUBLE_STONE_SLAB] = BlockDoubleSlabStone.class; //43
        list[STONE_SLAB] = BlockSlabStone.class; //44
        list[BRICKS_BLOCK] = BlockBricks.class; //45
        list[TNT] = BlockTNT.class; //46
        list[BOOKSHELF] = BlockBookshelf.class; //47
        list[MOSS_STONE] = BlockMossStone.class; //48
        list[OBSIDIAN] = BlockObsidian.class; //49
        list[TORCH] = BlockTorch.class; //50
        list[FIRE] = BlockFire.class; //51
        list[MONSTER_SPAWNER] = BlockMobSpawner.class; //52
        list[WOOD_STAIRS] = BlockStairsWood.class; //53
        list[CHEST] = BlockChest.class; //54
        list[REDSTONE_WIRE] = BlockRedstoneWire.class; //55
        list[DIAMOND_ORE] = BlockOreDiamond.class; //56
        list[DIAMOND_BLOCK] = BlockDiamond.class; //57
        list[WORKBENCH] = BlockCraftingTable.class; //58
        list[WHEAT_BLOCK] = BlockWheat.class; //59
        list[FARMLAND] = BlockFarmland.class; //60
        list[FURNACE] = BlockFurnace.class; //61
        list[BURNING_FURNACE] = BlockFurnaceBurning.class; //62
        list[SIGN_POST] = BlockSignPost.class; //63
        list[WOOD_DOOR_BLOCK] = BlockDoorWood.class; //64
        list[LADDER] = BlockLadder.class; //65
        list[RAIL] = BlockRail.class; //66
        list[COBBLESTONE_STAIRS] = BlockStairsCobblestone.class; //67
        list[WALL_SIGN] = BlockWallSign.class; //68
        list[LEVER] = BlockLever.class; //69
        list[STONE_PRESSURE_PLATE] = BlockPressurePlateStone.class; //70
        list[IRON_DOOR_BLOCK] = BlockDoorIron.class; //71
        list[WOODEN_PRESSURE_PLATE] = BlockPressurePlateWood.class; //72
        list[REDSTONE_ORE] = BlockOreRedstone.class; //73
        list[GLOWING_REDSTONE_ORE] = BlockOreRedstoneGlowing.class; //74
        list[UNLIT_REDSTONE_TORCH] = BlockRedstoneTorchUnlit.class;
        list[REDSTONE_TORCH] = BlockRedstoneTorch.class; //76
        list[STONE_BUTTON] = BlockButtonStone.class; //77
        list[SNOW_LAYER] = BlockSnowLayer.class; //78
        list[ICE] = BlockIce.class; //79
        list[SNOW_BLOCK] = BlockSnow.class; //80
        list[CACTUS] = BlockCactus.class; //81
        list[CLAY_BLOCK] = BlockClay.class; //82
        list[SUGARCANE_BLOCK] = BlockSugarcane.class; //83
        list[JUKEBOX] = BlockJukebox.class; //84
        list[FENCE] = BlockFence.class; //85
        list[PUMPKIN] = BlockPumpkin.class; //86
        list[NETHERRACK] = BlockNetherrack.class; //87
        list[SOUL_SAND] = BlockSoulSand.class; //88
        list[GLOWSTONE_BLOCK] = BlockGlowstone.class; //89
        list[NETHER_PORTAL] = BlockNetherPortal.class; //90
        list[LIT_PUMPKIN] = BlockPumpkinLit.class; //91
        list[CAKE_BLOCK] = BlockCake.class; //92
        list[UNPOWERED_REPEATER] = BlockRedstoneRepeaterUnpowered.class; //93
        list[POWERED_REPEATER] = BlockRedstoneRepeaterPowered.class; //94
        list[INVISIBLE_BEDROCK] = BlockBedrockInvisible.class; //95
        list[TRAPDOOR] = BlockTrapdoor.class; //96
        list[MONSTER_EGG] = BlockMonsterEgg.class; //97
        list[STONE_BRICKS] = BlockBricksStone.class; //98
        list[BROWN_MUSHROOM_BLOCK] = BlockHugeMushroomBrown.class; //99
        list[RED_MUSHROOM_BLOCK] = BlockHugeMushroomRed.class; //100
        list[IRON_BARS] = BlockIronBars.class; //101
        list[GLASS_PANE] = BlockGlassPane.class; //102
        list[MELON_BLOCK] = BlockMelon.class; //103
        list[PUMPKIN_STEM] = BlockStemPumpkin.class; //104
        list[MELON_STEM] = BlockStemMelon.class; //105
        list[VINE] = BlockVine.class; //106
        list[FENCE_GATE] = BlockFenceGate.class; //107
        list[BRICK_STAIRS] = BlockStairsBrick.class; //108
        list[STONE_BRICK_STAIRS] = BlockStairsStoneBrick.class; //109
        list[MYCELIUM] = BlockMycelium.class; //110
        list[WATER_LILY] = BlockWaterLily.class; //111
        list[NETHER_BRICKS] = BlockBricksNether.class; //112
        list[NETHER_BRICK_FENCE] = BlockFenceNetherBrick.class; //113
        list[NETHER_BRICKS_STAIRS] = BlockStairsNetherBrick.class; //114
        list[NETHER_WART_BLOCK] = BlockNetherWart.class; //115
        list[ENCHANTING_TABLE] = BlockEnchantingTable.class; //116
        list[BREWING_STAND_BLOCK] = BlockBrewingStand.class; //117
        list[CAULDRON_BLOCK] = BlockCauldron.class; //118
        list[END_PORTAL] = BlockEndPortal.class; //119
        list[END_PORTAL_FRAME] = BlockEndPortalFrame.class; //120
        list[END_STONE] = BlockEndStone.class; //121
        list[DRAGON_EGG] = BlockDragonEgg.class; //122
        list[REDSTONE_LAMP] = BlockRedstoneLamp.class; //123
        list[LIT_REDSTONE_LAMP] = BlockRedstoneLampLit.class; //124
        //TODO: list[DROPPER] = BlockDropper.class; //125
        list[ACTIVATOR_RAIL] = BlockRailActivator.class; //126
        list[COCOA] = BlockCocoa.class; //127
        list[SANDSTONE_STAIRS] = BlockStairsSandstone.class; //128
        list[EMERALD_ORE] = BlockOreEmerald.class; //129
        list[ENDER_CHEST] = BlockEnderChest.class; //130
        list[TRIPWIRE_HOOK] = BlockTripWireHook.class;
        list[TRIPWIRE] = BlockTripWire.class; //132
        list[EMERALD_BLOCK] = BlockEmerald.class; //133
        list[SPRUCE_WOOD_STAIRS] = BlockStairsSpruce.class; //134
        list[BIRCH_WOOD_STAIRS] = BlockStairsBirch.class; //135
        list[JUNGLE_WOOD_STAIRS] = BlockStairsJungle.class; //136

        list[BEACON] = BlockBeacon.class; //138
        list[STONE_WALL] = BlockWall.class; //139
        list[FLOWER_POT_BLOCK] = BlockFlowerPot.class; //140
        list[CARROT_BLOCK] = BlockCarrot.class; //141
        list[POTATO_BLOCK] = BlockPotato.class; //142
        list[WOODEN_BUTTON] = BlockButtonWooden.class; //143
        list[SKULL_BLOCK] = BlockSkull.class; //144
        list[ANVIL] = BlockAnvil.class; //145
        list[TRAPPED_CHEST] = BlockTrappedChest.class; //146
        list[LIGHT_WEIGHTED_PRESSURE_PLATE] = BlockWeightedPressurePlateLight.class; //147
        list[HEAVY_WEIGHTED_PRESSURE_PLATE] = BlockWeightedPressurePlateHeavy.class; //148
        list[UNPOWERED_COMPARATOR] = BlockRedstoneComparatorUnpowered.class; //149
        list[POWERED_COMPARATOR] = BlockRedstoneComparatorPowered.class; //149
        list[DAYLIGHT_DETECTOR] = BlockDaylightDetector.class; //151
        list[REDSTONE_BLOCK] = BlockRedstone.class; //152
        list[QUARTZ_ORE] = BlockOreQuartz.class; //153
        list[HOPPER_BLOCK] = BlockHopper.class; //154
        list[QUARTZ_BLOCK] = BlockQuartz.class; //155
        list[QUARTZ_STAIRS] = BlockStairsQuartz.class; //156
        list[DOUBLE_WOOD_SLAB] = BlockDoubleSlabWood.class; //157
        list[WOOD_SLAB] = BlockSlabWood.class; //158
        list[STAINED_TERRACOTTA] = BlockTerracottaStained.class; //159
        list[STAINED_GLASS_PANE] = BlockGlassPaneStained.class; //160

        list[LEAVES2] = BlockLeaves2.class; //161
        list[WOOD2] = BlockWood2.class; //162
        list[ACACIA_WOOD_STAIRS] = BlockStairsAcacia.class; //163
        list[DARK_OAK_WOOD_STAIRS] = BlockStairsDarkOak.class; //164
        list[SLIME_BLOCK] = BlockSlime.class; //165

        list[IRON_TRAPDOOR] = BlockTrapdoorIron.class; //167
        list[PRISMARINE] = BlockPrismarine.class; //168
        list[SEA_LANTERN] = BlockSeaLantern.class; //169
        list[HAY_BALE] = BlockHayBale.class; //170
        list[CARPET] = BlockCarpet.class; //171
        list[TERRACOTTA] = BlockTerracotta.class; //172
        list[COAL_BLOCK] = BlockCoal.class; //173
        list[PACKED_ICE] = BlockIcePacked.class; //174
        list[DOUBLE_PLANT] = BlockDoublePlant.class; //175
        list[STANDING_BANNER] = BlockBanner.class; //176
        list[WALL_BANNER] = BlockWallBanner.class; //177
        list[DAYLIGHT_DETECTOR_INVERTED] = BlockDaylightDetectorInverted.class; //178
        list[RED_SANDSTONE] = BlockRedSandstone.class; //179
        list[RED_SANDSTONE_STAIRS] = BlockStairsRedSandstone.class; //180
        list[DOUBLE_RED_SANDSTONE_SLAB] = BlockDoubleSlabRedSandstone.class; //181
        list[RED_SANDSTONE_SLAB] = BlockSlabRedSandstone.class; //182
        list[FENCE_GATE_SPRUCE] = BlockFenceGateSpruce.class; //183
        list[FENCE_GATE_BIRCH] = BlockFenceGateBirch.class; //184
        list[FENCE_GATE_JUNGLE] = BlockFenceGateJungle.class; //185
        list[FENCE_GATE_DARK_OAK] = BlockFenceGateDarkOak.class; //186
        list[FENCE_GATE_ACACIA] = BlockFenceGateAcacia.class; //187

        list[SPRUCE_DOOR_BLOCK] = BlockDoorSpruce.class; //193
        list[BIRCH_DOOR_BLOCK] = BlockDoorBirch.class; //194
        list[JUNGLE_DOOR_BLOCK] = BlockDoorJungle.class; //195
        list[ACACIA_DOOR_BLOCK] = BlockDoorAcacia.class; //196
        list[DARK_OAK_DOOR_BLOCK] = BlockDoorDarkOak.class; //197
        list[GRASS_PATH] = BlockGrassPath.class; //198
        list[ITEM_FRAME_BLOCK] = BlockItemFrame.class; //199
        list[CHORUS_FLOWER] = BlockChorusFlower.class; //200
        list[PURPUR_BLOCK] = BlockPurpur.class; //201

        list[PURPUR_STAIRS] = BlockStairsPurpur.class; //203

        list[UNDYED_SHULKER_BOX] = BlockUndyedShulkerBox.class; //205
        list[END_BRICKS] = BlockBricksEndStone.class; //206

        list[END_ROD] = BlockEndRod.class; //208
        list[END_GATEWAY] = BlockEndGateway.class; //209

        list[MAGMA] = BlockMagma.class; //213
        list[BLOCK_NETHER_WART_BLOCK] = BlockNetherWartBlock.class; //214
        list[RED_NETHER_BRICK] = BlockBricksRedNether.class; //215
        list[BONE_BLOCK] = BlockBone.class; //216

        list[SHULKER_BOX] = BlockShulkerBox.class; //218
        list[PURPLE_GLAZED_TERRACOTTA] = BlockTerracottaGlazedPurple.class; //219
        list[WHITE_GLAZED_TERRACOTTA] = BlockTerracottaGlazedWhite.class; //220
        list[ORANGE_GLAZED_TERRACOTTA] = BlockTerracottaGlazedOrange.class; //221
        list[MAGENTA_GLAZED_TERRACOTTA] = BlockTerracottaGlazedMagenta.class; //222
        list[LIGHT_BLUE_GLAZED_TERRACOTTA] = BlockTerracottaGlazedLightBlue.class; //223
        list[YELLOW_GLAZED_TERRACOTTA] = BlockTerracottaGlazedYellow.class; //224
        list[LIME_GLAZED_TERRACOTTA] = BlockTerracottaGlazedLime.class; //225
        list[PINK_GLAZED_TERRACOTTA] = BlockTerracottaGlazedPink.class; //226
        list[GRAY_GLAZED_TERRACOTTA] = BlockTerracottaGlazedGray.class; //227
        list[SILVER_GLAZED_TERRACOTTA] = BlockTerracottaGlazedSilver.class; //228
        list[CYAN_GLAZED_TERRACOTTA] = BlockTerracottaGlazedCyan.class; //229

        list[BLUE_GLAZED_TERRACOTTA] = BlockTerracottaGlazedBlue.class; //231
        list[BROWN_GLAZED_TERRACOTTA] = BlockTerracottaGlazedBrown.class; //232
        list[GREEN_GLAZED_TERRACOTTA] = BlockTerracottaGlazedGreen.class; //233
        list[RED_GLAZED_TERRACOTTA] = BlockTerracottaGlazedRed.class; //234
        list[BLACK_GLAZED_TERRACOTTA] = BlockTerracottaGlazedBlack.class; //235
        list[CONCRETE] = BlockConcrete.class; //236
        list[CONCRETE_POWDER] = BlockConcretePowder.class; //237

        list[CHORUS_PLANT] = BlockChorusPlant.class; //240
        list[STAINED_GLASS] = BlockGlassStained.class; //241
        list[PODZOL] = BlockPodzol.class; //243
        list[BEETROOT_BLOCK] = BlockBeetroot.class; //244
        list[STONECUTTER] = BlockStonecutter.class; //245
        list[GLOWING_OBSIDIAN] = BlockObsidianGlowing.class; //246
        //list[NETHER_REACTOR] = BlockNetherReactor.class; //247 Should not be removed

        //TODO: list[PISTON_EXTENSION] = BlockPistonExtension.class; //250

        list[OBSERVER] = BlockObserver.class; //251

        for (int id = 0; id < list.length; id++) {
            Class block = list[id];
            if(block != null)
                registerBlock(id, block);
            else {
                for (int data = 0; data < 16; ++data)
                    registerBlock(id, data, new BlockUnknown(id, data));
                Server.getInstance().getLogger().info("Error register block " + id);
            }
        }
    }

    public static Block getFullId(int id) {
        return fullId.get(id);
    }
}
