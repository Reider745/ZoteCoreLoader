package com.reider745.item;

import cn.nukkit.entity.projectile.EntityThrownTrident;
import cn.nukkit.item.Item;
import cn.nukkit.item.ProjectileItem;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.CustomManager;

public class CustomProjectileItem extends ProjectileItem {
    @Override
    public String getProjectileEntityType() {
        return "Snowball";
    }

    @Override
    public float getThrowForce() {
        return 1f;
    }

    @Override
    protected void correctNBT(CompoundTag nbt) {
        nbt.put("ItemIc", NBTIO.putItemHelper(this));
    }

    private CustomManager parameters;

    public CustomProjectileItem(int id, Integer meta, int count){
        this(id, meta, count, CustomItem.getItemManager(id));
    }

    public CustomProjectileItem(int id, Integer meta, int count, CustomManager manager) {
        super(id, meta, count, manager.get("name", "InnerCore item"));

        parameters = manager;
        this.name = parameters.get("name", "InnerCore item");
    }


    @Override
    public int getMaxDurability() {
        return parameters.get("max_damage");
    }

    @Override
    public int getMaxStackSize() {
        return parameters.get("max_stack");
    }

    @Override
    public Item clone() {
        CustomProjectileItem item = (CustomProjectileItem) super.clone();
        item.parameters = parameters;
        item.name = name;
        item.meta = meta;
        return item;
    }
}
