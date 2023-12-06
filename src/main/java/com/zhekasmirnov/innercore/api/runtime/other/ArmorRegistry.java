package com.zhekasmirnov.innercore.api.runtime.other;

import android.util.Pair;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.NativeItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.commontypes.ScriptableParams;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

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

    private static HashMap<Integer, ArmorInfo> armorInfoMap = new HashMap<>();
    private static NativeItemInstance[] armorSlots = new NativeItemInstance[4];

    private static void refreshArmorSlots() {
        for (int slot = 0; slot < 4; slot++) {
            if (armorSlots[slot] != null) {
                armorSlots[slot].destroy();
            }
            // TODO: armorSlots[slot] = new
            // NativeItemInstance(NativeAPI.getPlayerArmor(slot));
        }
    }

    private static void postArmorApply(final int index, final NativeItemInstance checker, final ItemInstance source) {
        MainThreadQueue.serverThread.enqueue(new Runnable() {
            @Override
            public void run() {
                NativeItemInstance item = new NativeItemInstance(NativeAPI.getPlayerArmor(index));
                if (item.id == checker.id && item.data == checker.data) {
                    NativeAPI.setPlayerArmor(index, source.getId(), 1, source.getData(), source.getExtraValue());
                }
                item.destroy();
            }
        });
    }

    public static void onTick() {
        if (!NativeAPI.isValidEntity(NativeAPI.getPlayer())) {
            ICLog.d("ARMOR", "ticking with invalid player entity aborted");
            return;
        }
        refreshArmorSlots();

        for (int slot = 0; slot < 4; slot++) {
            NativeItemInstance item = armorSlots[slot];
            ArmorInfo info = armorInfoMap.get(item.id);

            if (info != null && info.callback != null) {
                ItemInstance source = new ItemInstance(item);
                if (info.callback.tick(source, slot, info)) {
                    postArmorApply(slot, item, source);
                }
            }
        }
    }

    public static void onHurt(long attacker, int damage, int type, boolean b1, boolean b2) {
        refreshArmorSlots();

        ScriptableParams params = new ScriptableParams(
                new Pair<String, Object>("attacker", attacker),
                new Pair<String, Object>("damage", damage),
                new Pair<String, Object>("type", type),
                new Pair<String, Object>("bool1", b1),
                new Pair<String, Object>("bool2", b2));

        for (int slot = 0; slot < 4; slot++) {
            NativeItemInstance item = armorSlots[slot];
            ArmorInfo info = armorInfoMap.get(item.id);

            if (info != null && info.callback != null) {
                ItemInstance source = new ItemInstance(item);
                if (info.callback.hurt(params, source, slot, info)) {
                    postArmorApply(slot, item, source);
                }
            }
        }
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
        int durability = NativeItem.getMaxDamageForId(id, 0);
        armorInfoMap.put(id, new ArmorInfo(callback, durability));

        NativeItem item = NativeItem.getItemById(id);
        if (item != null) {
            item.setArmorDamageable(callback instanceof DefaultArmorCallback);
        }
    }
}
