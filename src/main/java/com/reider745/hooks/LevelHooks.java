package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBed;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.generic.BaseLevelProvider;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.*;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.event.EventListener;
import com.reider745.world.dimensions.DimensionsMethods;
import com.zhekasmirnov.innercore.api.dimensions.CustomDimension;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;

import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

import java.util.HashMap;
import java.util.Map;

@Hooks(className = "cn.nukkit.level.Level")
public class LevelHooks implements HookClass {
    @Override
    public void rebuildField(CtClass ctClass, CtField field) {
        if (field.getName().equals("randomTickBlocks"))
            field.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
    }

    @SuppressWarnings("unchecked")
    @Inject
    public static Item useBreakOn(Level level, Vector3 vector, BlockFace face, Item item, Player player,
            boolean createParticles) {
        if (player != null && player.getGamemode() > Player.ADVENTURE) {
            return null;
        }
        Block target = level.getBlock(vector);
        Item[] drops;
        int dropExp = target.getDropExp();

        if (item == null) {
            item = new ItemBlock(Block.get(BlockID.AIR), 0, 0);
        }

        boolean isSilkTouch = item.hasEnchantment(Enchantment.ID_SILK_TOUCH);

        if (player != null) {
            if (player.getGamemode() == Player.ADVENTURE) {
                Tag tag = item.getNamedTagEntry("CanDestroy");
                boolean canBreak = false;
                if (tag instanceof ListTag) {
                    for (Tag v : ((ListTag<? extends Tag>) tag).getAll()) {
                        if (v instanceof StringTag) {
                            Item entry = Item.fromString(((StringTag) v).data);
                            if (entry.getId() > 0 && entry.getBlockUnsafe() != null
                                    && entry.getBlockUnsafe().getId() == target.getId()) {
                                canBreak = true;
                                break;
                            }
                        }
                    }
                }
                if (!canBreak) {
                    return null;
                }
            }

            double breakTime = target.calculateBreakTime(item, player);

            if (player.isCreative() && breakTime > 0.15) {
                breakTime = 0.15;
            }

            if (player.hasEffect(Effect.HASTE)) {
                breakTime *= 1 - (0.2 * (player.getEffect(Effect.HASTE).getAmplifier() + 1));
            }

            if (player.hasEffect(Effect.MINING_FATIGUE)) {
                breakTime *= 1 - (0.3 * (player.getEffect(Effect.MINING_FATIGUE).getAmplifier() + 1));
            }

            Enchantment eff = item.getEnchantment(Enchantment.ID_EFFICIENCY);

            if (eff != null && eff.getLevel() > 0) {
                breakTime *= 1 - (0.3 * eff.getLevel());
            }

            breakTime -= 0.15;

            Item[] eventDrops;
            if (!player.isSurvival()) {
                eventDrops = Item.EMPTY_ARRAY;
            } else if (isSilkTouch && target.canSilkTouch()) {
                eventDrops = new Item[] { target.toItem() };
            } else {
                eventDrops = target.getDrops(item);
            }
            // TODO 直接加1000可能会影响其他判断，需要进一步改进
            boolean fastBreak = (player.lastBreak + breakTime * 1000) > Long.sum(System.currentTimeMillis(), 1000);
            BlockBreakEvent ev = new BlockBreakEvent(player, target, face, item, eventDrops, player.isCreative(),
                    fastBreak);

            boolean isNukkitPrevent = false;
            if ((player.isSurvival() || player.isAdventure()) && !target.isBreakable(item)) {
                ev.setCancelled();
                isNukkitPrevent = true;
            } else if (!player.isOp() && level.isInSpawnRadius(target)) {
                ev.setCancelled();
                isNukkitPrevent = true;
            // TODO: There are currently no way to prevent cheats, but it constantly breaks overpowered tools
            } else if (!ev.getInstaBreak() && ev.isFastBreak() && item.getId() < IDRegistry.ITEM_ID_OFFSET) {
                ev.setCancelled();
                isNukkitPrevent = true;
            }

            player.lastBreak = System.currentTimeMillis();

            ev.setCancelled(false);
            level.getServer().getPluginManager().callEvent(ev);

            if (!ev.isCancelled()) {
                EventListener.onBlockBreak(ev, false);
                if (isNukkitPrevent)
                    ev.setCancelled();
            }

            if (ev.isCancelled()) {
                return null;
            }

            drops = ev.getDrops();
            dropExp = ev.getDropExp();
        } else if (!target.isBreakable(item)) {
            return null;
        } else if (item.hasEnchantment(Enchantment.ID_SILK_TOUCH)) {
            drops = new Item[] { target.toItem() };
        } else {
            drops = target.getDrops(item);
        }

        Vector3 above = new Vector3(target.x, target.y + 1, target.z);
        if (level.getBlockIdAt((int) above.x, (int) above.y, (int) above.z) == Item.FIRE) {
            level.setBlock(above, Block.get(BlockID.AIR), true);
        }

        if (createParticles) {
            Map<Integer, Player> players = level.getChunkPlayers((int) target.x >> 4, (int) target.z >> 4);
            level.addParticle(new DestroyBlockParticle(target.add(0.5), target), players.values());
        }

        BlockEntity blockEntity = level.getBlockEntity(target);
        if (blockEntity != null) {
            blockEntity.onBreak();
            blockEntity.close();

            level.updateComparatorOutputLevel(target);
        }

        target.onBreak(item, player);

        item.useOn(target);
        if (item.isTool() && item.getDamage() >= item.getMaxDurability()) {
            level.addSoundToViewers(target, cn.nukkit.level.Sound.RANDOM_BREAK);
            level.addParticle(new ItemBreakParticle(target, item));
            item = new ItemBlock(Block.get(BlockID.AIR), 0, 0);
        }

        if (level.gameRules.getBoolean(GameRule.DO_TILE_DROPS)) {
            if (!isSilkTouch && player != null && drops.length != 0) { // For example no xp from redstone if it's mined
                                                                       // with stone pickaxe
                if (player.isSurvival() || player.isAdventure()) {
                    level.dropExpOrb(vector.add(0.5, 0.5, 0.5), dropExp);
                }
            }

            if (player == null || player.isSurvival() || player.isAdventure()) {
                for (Item drop : drops) {
                    if (drop.getCount() > 0) {
                        level.dropItem(vector.add(0.5, 0.5, 0.5), drop);
                    }
                }
            }
        }

        return item;
    }

    @Inject(className = "cn.nukkit.level.EnumLevel")
    public static void initLevels(){
        DimensionsMethods.initLevels();
    }

    private static Level getSyncLevel(){
        return Server.getInstance().getDefaultLevel();
    }

    private static boolean isUsePath(Level self){
        return CustomDimension.getDimensionById(self.getDimension()) != null;
    }


    //Общие правила с миром world
    /*@Inject(type = TypeHook.AFTER_NOT_REPLACE)
    public static void initLevel(Level self){
        if(isUsePath(self))
            self.gameRules = getSyncLevel().gameRules;
    }*/

    //Патч кровати
    @Inject(className = "cn.nukkit.block.BlockBed")
    public static boolean onActivate(BlockBed self, Item item, Player player){
        if (self.level.getDimension() != Level.DIMENSION_OVERWORLD && !isUsePath(self.level)) {
            self.level.useBreakOn(self);
            CompoundTag nbt = new CompoundTag()
                    .putList(new ListTag<DoubleTag>("Pos")
                            .add(new DoubleTag("", self.x))
                            .add(new DoubleTag("", self.y))
                            .add(new DoubleTag("", self.z)))
                    .putList(new ListTag<DoubleTag>("Motion")
                            .add(new DoubleTag("", 0))
                            .add(new DoubleTag("", 0))
                            .add(new DoubleTag("", 0)))
                    .putList(new ListTag<FloatTag>("Rotation")
                            .add(new FloatTag("", 0))
                            .add(new FloatTag("", 0)))
                    .putShort("Fuse", 0); // create an instant explosion
            new EntityPrimedTNT(self.level.getChunk(self.getFloorX() >> 4, self.getFloorZ() >> 4), nbt); // we don't even have to spawn the tnt entity for players
            return true;
        }

        Block blockNorth = self.north();
        Block blockSouth = self.south();
        Block blockEast = self.east();
        Block blockWest = self.west();

        Block b;
        if (self.isHeadPiece()) {
            b = self;
        } else {
            if (blockNorth.getId() == BlockID.BED_BLOCK && (blockNorth.getDamage() & 0x08) == 0x08) {
                b = blockNorth;
            } else if (blockSouth.getId() == BlockID.BED_BLOCK && (blockSouth.getDamage() & 0x08) == 0x08) {
                b = blockSouth;
            } else if (blockEast.getId() == BlockID.BED_BLOCK && (blockEast.getDamage() & 0x08) == 0x08) {
                b = blockEast;
            } else if (blockWest.getId() == BlockID.BED_BLOCK && (blockWest.getDamage() & 0x08) == 0x08) {
                b = blockWest;
            } else {
                if (player != null) {
                    player.sendTranslation("§7%tile.bed.notValid");
                }

                return true;
            }
        }

        int time = self.getLevel().getTime() % Level.TIME_FULL;
        boolean isNight = (time >= Level.TIME_NIGHT && time < Level.TIME_SUNRISE);

        if (player != null && !isNight && !self.getLevel().isThundering()) {
            Position spawn = Position.fromObject(b.add(0.5, 0.5, 0.5), player.getLevel());
            if (!player.getSpawn().equals(spawn)) {
                player.setSpawnBlock(self);
            }
            player.sendMessage(new TranslationContainer(TextFormat.GRAY + "%tile.bed.respawnSet"));
            player.sendTranslation("§7%tile.bed.noSleep");
            return true;
        }

        if (player != null) {
            if (player.distanceSquared(self) > 36) {
                player.sendTranslation("§7%tile.bed.tooFar");
                return true;
            }

            if (!player.isCreative()) {
                BlockFace secondPart = self.getBlockFace().getOpposite();
                AxisAlignedBB checkArea = new SimpleAxisAlignedBB(b.x - 8, b.y - 6.5, b.z - 8, b.x + 9, b.y + 5.5, b.z + 9).addCoord(secondPart.getXOffset(), 0, secondPart.getZOffset());

                for (Entity entity : self.getLevel().getCollidingEntities(checkArea)) {
                    if (!entity.isClosed() && Utils.monstersList.contains(entity.getNetworkId())) {
                        player.sendTranslation("§7%tile.bed.notSafe");
                        return true;
                    }
                }
            }

            if (!player.sleepOn(b)) {
                player.sendTranslation("§7%tile.bed.occupied");
            }
        }

        return true;
    }

    //Синхронизация времени
    @Inject
    public static void setTime(Level self, int time){
        if(isUsePath(self) && time != getSyncLevel().getTime())
            getSyncLevel().setTime(time);
    }

    @Inject
    public static void doTick(Level self, int currentTick){
        if(isUsePath(self))
            self.setTime(getSyncLevel().getTime());
    }

    @Inject(className = "cn.nukkit.level.format.generic.BaseLevelProvider")
    public static Map<String, Object> getGeneratorOptions(BaseLevelProvider provider) {
        return new HashMap<>() {
            {
                final String name = provider.getName();
                put("preset", provider.getLevelData().getString("generatorOptions"));
                DimensionsMethods.CustomDimensionDescription description = DimensionsMethods.descriptions.get(name);
                if(description != null){
                    put("id", description.getId());
                    put("name", name);
                }
            }
        };
    }
}
