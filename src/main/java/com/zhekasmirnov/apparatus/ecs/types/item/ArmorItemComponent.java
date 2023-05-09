package com.zhekasmirnov.apparatus.ecs.types.item;

import com.zhekasmirnov.apparatus.ecs.core.TypeIndexMap;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ScriptableParams;
import com.zhekasmirnov.innercore.api.runtime.other.ArmorRegistry;

public class ArmorItemComponent implements ArmorRegistry.IArmorCallback {
    public static final int COMPONENT_ID = TypeIndexMap.getTypeIndex("ArmorItemComponent");

    public final int slot;
    public final int protection;
    public final float kbResist;

    public ArmorItemComponent(int slot, int protection, float kbResist) {
        this.slot = slot;
        this.protection = protection;
        this.kbResist = kbResist;
    }

    @Override
    public boolean tick(ItemInstance slot, int index, ArmorRegistry.ArmorInfo armorInfo) {
        return false;
    }

    @Override
    public boolean hurt(ScriptableParams params, ItemInstance slot, int index, ArmorRegistry.ArmorInfo armorInfo) {
        return false;
    }
}
