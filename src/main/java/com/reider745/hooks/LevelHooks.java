package com.reider745.hooks;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseLevelProvider;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.level.particle.ItemBreakParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.potion.Effect;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.event.EventListener;
import com.reider745.world.DimensionsMethods;
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
            } else if (!ev.getInstaBreak() && ev.isFastBreak()) {
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

    @Inject
    public static void initLevel(Level level){
        DimensionsMethods.initLevel(level);
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
