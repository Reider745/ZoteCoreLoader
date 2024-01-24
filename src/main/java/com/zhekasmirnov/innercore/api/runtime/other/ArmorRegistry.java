package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ScriptableParams;
import com.zhekasmirnov.innercore.api.log.ICLog;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.08.2017.
 */

public class ArmorRegistry {
    public interface IArmorCallback {
        boolean tick(ItemInstance slot, int index, ArmorInfo armorInfo);

        boolean hurt(ScriptableParams params, ItemInstance slot, int index, ArmorInfo armorInfo);
    }

    public interface IJSArmorCallback {
        boolean tick(ItemInstance slot, int index, int durability);

        boolean hurt(ScriptableParams params, ItemInstance slot, int index, int durability);
    }

    public static class DefaultArmorCallback implements IArmorCallback {
        @Override
        public boolean tick(ItemInstance slot, int index, ArmorInfo armorInfo) {
            return false;
        }

        @Override
        public boolean hurt(ScriptableParams params, ItemInstance slot, int index, ArmorInfo armorInfo) {
            return false;
        }
    }

    public static class ScriptableArmorCallbacks implements IArmorCallback {
        private final IJSArmorCallback callbacks;

        public ScriptableArmorCallbacks(ScriptableObject obj) {
            callbacks = (IJSArmorCallback) Context.jsToJava(obj, IJSArmorCallback.class);
        }

        @Override
        public boolean tick(ItemInstance slot, int index, ArmorInfo armorInfo) {
            try {
                return callbacks.tick(slot, index, armorInfo.durability);
            } catch (Throwable err) {
                ICLog.e("ARMOR", "error in armor tick", err);
            }
            return false;
        }

        @Override
        public boolean hurt(ScriptableParams params, ItemInstance slot, int index, ArmorInfo armorInfo) {
            try {
                return callbacks.hurt(params, slot, index, armorInfo.durability);
            } catch (Throwable err) {
                ICLog.e("ARMOR", "error in armor tick", err);
            }
            return false;
        }
    }

    public static class ArmorInfo {
        public IArmorCallback callback;
        public int durability;

        public ArmorInfo(int durability) {
            this.durability = durability;
        }

        public ArmorInfo(IArmorCallback callback, int durability) {
            this.callback = callback;
            this.durability = durability;
        }
    }

    public static void onTick() {
    }

    public static void onHurt(long attacker, int damage, int type, boolean b1, boolean b2) {
    }

    public static void registerArmor(int id, ScriptableObject obj) {
        registerArmor(id, new ScriptableArmorCallbacks(obj));
    }

    public static void preventArmorDamaging(int id) {
        NativeItem item = NativeItem.getItemById(id);
        if (item != null) {
            item.setArmorDamageable(false);
        }
    }

    public static void registerArmor(int id, IArmorCallback callback) {
        NativeItem item = NativeItem.getItemById(id);
        if (item != null) {
            item.setArmorDamageable(callback instanceof DefaultArmorCallback);
        }
    }
}
