package com.reider745.item;

import com.reider745.api.CustomManager;
import com.reider745.item.ItemMethod.PropertiesNames;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

public class CustomArmorItem extends CustomItemClass {
    private int slot;

    public CustomArmorItem(int id, Integer meta, int count) {
        super(id, meta, count);
    }

    public CustomArmorItem(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager);
    }

    @Override
    protected void initItem() {
        super.initItem();
        this.slot = parameters.get(PropertiesNames.Armors.SLOT, -1);
    }

    public int getArmorSlot() {
        return slot;
    }

    @Override
    public boolean isArmor() {
        return true;
    }

    @Override
    public boolean isHelmet() {
        return slot == 0;
    }

    @Override
    public boolean canBePutInHelmetSlot() {
        return this.isHelmet();
    }

    @Override
    public boolean isChestplate() {
        return slot == 1;
    }

    @Override
    public boolean isLeggings() {
        return slot == 2;
    }

    @Override
    public boolean isBoots() {
        return slot == 3;
    }

    @Override
    public boolean isUnbreakable() {
        return !parameters.get(PropertiesNames.ARMOR_DAMAGEABLE, true) || super.isUnbreakable();
    }

    @Override
    public int getArmorPoints() {
        return parameters.get(PropertiesNames.Armors.DEFENSE, 0);
    }

    @Override
    public int getToughness() {
        return (int) (float) parameters.get(PropertiesNames.Armors.KNOCKBACK_RESIST, 0f);
    }

    @Override
    public boolean onClickAir(Player player, Vector3 directionVector) {
        boolean equip = false;
        Item oldSlotItem = Item.get(AIR);
        if (this.isHelmet()) {
            oldSlotItem = player.getInventory().getHelmetFast();
            if (player.getInventory().setHelmet(this)) {
                equip = true;
            }
        } else if (this.isChestplate()) {
            oldSlotItem = player.getInventory().getChestplateFast();
            if (player.getInventory().setChestplate(this)) {
                equip = true;
            }
        } else if (this.isLeggings()) {
            oldSlotItem = player.getInventory().getLeggingsFast();
            if (player.getInventory().setLeggings(this)) {
                equip = true;
            }
        } else if (this.isBoots()) {
            oldSlotItem = player.getInventory().getBootsFast();
            if (player.getInventory().setBoots(this)) {
                equip = true;
            }
        }
        if (equip) {
            player.getInventory().setItem(player.getInventory().getHeldItemIndex(), oldSlotItem);
            player.getLevel().addLevelSoundEvent(player, LevelSoundEventPacket.SOUND_ARMOR_EQUIP_GENERIC);
        }

        return this.getCount() == 0;
    }
}
