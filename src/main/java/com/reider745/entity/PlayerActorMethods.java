package com.reider745.entity;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.AdventureSettings.Type;
import cn.nukkit.entity.Attribute;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemCrossbow;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import com.reider745.InnerCoreServer;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class PlayerActorMethods {

    @Nullable
    public static Player fetchOnline(long entity) {
        Map<UUID, Player> players = Server.getInstance().getOnlinePlayers();
        return players.values().stream().filter(player -> player.getId() == entity).findFirst().orElse(null);
    }

    public static boolean isValid(Player player, boolean checkOnline) {
        if (player == null || !player.isValid()) {
            return false;
        }
        if (checkOnline) {
            return fetchOnline(player.getId()) != null;
        }
        return false;
    }

    public static void invokeUseItemNoTarget(Player player, int id, int count, int data, long extra) {
        if (!isValid(player, false)) {
            return;
        }
        Item item = Item.get(id, count, data);

        NativeItemInstanceExtra instanceExtra = NativeItemInstanceExtra.getExtraOrNull(extra);
        if (instanceExtra != null) {
            CompoundTag tag = instanceExtra.getExtraProvider().getCompoundTag();
            if (tag != null) {
                item.writeCompoundTag(tag);
            }
        }

        Vector3 directionVector = player.getDirectionVector();
        if (item instanceof ItemCrossbow) {
            if (!item.onClickAir(player, directionVector)) {
                return; // Shoot
            }
        }

        PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, item, directionVector,
                player.getDirection());
        Server.getInstance().getPluginManager().callEvent(interactEvent);

        if (interactEvent.isCancelled()) {
            player.getInventory().sendHeldItem(player);
            return;
        }

        if (item.onClickAir(player, directionVector)) {
            if (player.isSurvival() || player.isAdventure()) {
                if (item.getId() == 0 || player.getInventory().getItemInHand().getId() == item.getId()) {
                    player.getInventory().setItemInHand(item);
                } else {
                    Server.getInstance().getLogger()
                            .debug("Tried to set item " + item.getId() + " but " + player.getName() + " had item "
                                    + player.getInventory().getItemInHand().getId() + " in their hand slot");
                }
            }

            if (!player.isUsingItem()) {
                player.setUsingItem(true);
                return;
            }

            // Used item
            int ticksUsed = Server.getInstance().getTick() - player.getStartActionTick();
            player.setUsingItem(false);
            if (!item.onUse(player, ticksUsed)) {
                player.getInventory().sendContents(player);
            }
        }
    }

    public static void addItemToInventory(Player player, int id, int count, int data, long extra, boolean dropLeft) {
        if (!isValid(player, false)) {
            return;
        }
        Item[] items = player.getInventory().addItem(ItemUtils.get(id, count, data, extra));
        if (dropLeft) {
            for (Item item : items) {
                player.dropItem(item);
            }
        }
    }

    public static void addItemToInventoryPtr(Player player, long itemStack, boolean dropLeft) {
        if (!isValid(player, false)) {
            return;
        }
        InnerCoreServer.useNotCurrentSupport("PlayerActor.addItemToInventoryPtr(player, itemStack, dropLeft)");
    }

    public static void addExperience(Player player, int amount) {
        if (!isValid(player, false)) {
            return;
        }
        player.addExperience(amount);
    }

    public static int getDimension(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        Level level = player.getLevel();
        if (level != null) {
            return player.getLevel().getDimension();
        }
        return 0;
    }

    public static int getGameMode(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        return player.getGamemode();
    }

    public static Item getInventorySlot(Player player, int slot) {
        if (!isValid(player, false)) {
            return Item.AIR_ITEM;
        }
        return player.getInventory().getItem(slot);
    }

    public static Item getArmor(Player player, int slot) {
        if (!isValid(player, false)) {
            return Item.AIR_ITEM;
        }
        return player.getInventory().getArmorItem(slot);
    }

    public static float getExhaustion(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        // TODO: Needs to be checked whether exhaustion exists for players or not,
        // TODO: Nukkit-MOT offers nothing to do with it.
        return 0.0f;
    }

    public static float getExperience(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        return player.getExperience();
    }

    public static float getHunger(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        PlayerFood food = player.getFoodData();
        if (food != null) {
            return food.getLevel();
        }
        return 0;
    }

    public static float getLevel(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        return player.getExperienceLevel();
    }

    public static float getSaturation(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        PlayerFood food = player.getFoodData();
        if (food != null) {
            return food.getFoodSaturationLevel();
        }
        return 0;
    }

    public static int getScore(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        return 0;
    }

    public static int getSelectedSlot(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        return player.getInventory().getHeldItemSlot();
    }

    public static void setInventorySlot(Player player, int slot, int id, int count, int data, long extra) {
        if (!isValid(player, false)) {
            return;
        }
        player.getInventory().setItem(slot, ItemUtils.get(id, count, data, extra));
    }

    public static void setArmor(Player player, int slot, int id, int count, int data, long extra) {
        if (!isValid(player, false)) {
            return;
        }
        player.getInventory().setArmorItem(slot, ItemUtils.get(id, count, data, extra));
    }

    public static void setExhaustion(Player player, float value) {
        if (!isValid(player, false)) {
            return;
        }
        player.setAttribute(Attribute.getAttribute(Attribute.EXHAUSTION).setValue(value));
    }

    public static void setExperience(Player player, float value) {
        if (!isValid(player, false)) {
            return;
        }
        // TODO: Why it was float, needs to be checked in vanilla.
        player.setExperience((int) value);
    }

    public static void setHunger(Player player, float value) {
        if (!isValid(player, false)) {
            return;
        }
        PlayerFood food = player.getFoodData();
        if (food != null) {
            // TODO: Why it was float, needs to be checked in vanilla.
            food.setLevel((int) value);
        }
    }

    public static void setLevel(Player player, float value) {
        if (!isValid(player, false)) {
            return;
        }
        // TODO: Why it was float, needs to be checked in vanilla.
        player.setExperience(player.getExperience(), (int) value);
    }

    public static void setSaturation(Player player, float value) {
        if (!isValid(player, false)) {
            return;
        }
        PlayerFood food = player.getFoodData();
        if (food != null) {
            food.setFoodSaturationLevel(value);
        }
    }

    public static void setSelectedSlot(Player player, int slot) {
        if (!isValid(player, false)) {
            return;
        }
        player.getInventory().setHeldItemSlot(slot);
    }

    public static void setRespawnCoords(Player player, int x, int y, int z) {
        if (!isValid(player, false)) {
            return;
        }
        player.setSpawn(new Vector3(x, y, z));
    }

    public static void spawnExpOrbs(Player player, float x, float y, float z, int amount) {
        if (!isValid(player, false)) {
            return;
        }
        Level level = player.getLevel();
        if (level != null) {
            level.dropExpOrb(new Vector3(x, y, z), amount);
        }
    }

    public static boolean isSneaking(Player player) {
        if (!isValid(player, false)) {
            return false;
        }
        return player.isSneaking();
    }

    public static void setSneaking(Player player, boolean sneaking) {
        if (!isValid(player, false)) {
            return;
        }
        player.setSneaking(sneaking);
    }

    public static int getItemUseDuration(Player player) {
        if (!isValid(player, false)) {
            return -1;
        }
        CompoundTag tag = player.getInventory().getItemInHand().getNamedTag();
        if (tag != null && tag.contains("components", CompoundTag.class)) {
            tag = tag.getCompound("components");
            if (tag != null && tag.contains("item_properties", CompoundTag.class)) {
                return tag.getCompound("item_properties").getInt("use_duration");
            }
        }
        return 0;
    }

    public static float getItemUseIntervalProgress(Player player) {
        if (!isValid(player, false)) {
            return -1.0f;
        }
        return 0.0f;
    }

    public static float getItemUseStartupProgress(Player player) {
        if (!isValid(player, false)) {
            return -1.0f;
        }
        // TODO: return (float) player.getBlockBreakProgress();
        return 0.0f;
    }

    public static boolean isOperator(Player player) {
        if (!isValid(player, false)) {
            return false;
        }
        return Server.getInstance().isOp(player.getName());
    }

    public static void setCanFly(Player player, boolean canFly) {
        if (!isValid(player, false)) {
            return;
        }
        player.setAllowFlight(canFly);
    }

    public static boolean canFly(Player player) {
        if (!isValid(player, false)) {
            return false;
        }
        return player.getAllowFlight();
    }

    public static void setFlying(Player player, boolean flying) {
        if (!isValid(player, false)) {
            return;
        }
        AdventureSettings settings = player.getAdventureSettings();
        settings.set(Type.FLYING, flying);
        settings.update();
    }

    public static boolean isFlying(Player player) {
        if (!isValid(player, false)) {
            return false;
        }
        return player.getAdventureSettings().get(Type.FLYING);
    }
}
