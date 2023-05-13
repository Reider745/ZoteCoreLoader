package com.zhekasmirnov.apparatus.api.player.armor;

import com.zhekasmirnov.apparatus.adapter.innercore.game.entity.EntityActor;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.job.JobExecutor;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;

import java.util.HashMap;
import java.util.Map;

public class ActorArmorHandler {
    public interface OnTickListener {
        Object onTick(ItemStack item, int slot, long player);
    }

    public interface OnHurtListener {
        Object onHurt(ItemStack item, int slot, long player, int damageValue, int damageType, long attacker, boolean bool1, boolean bool2);
    }

    public interface OnTakeOffListener {
        void onTakeOff(ItemStack item, int slot, long player);
    }

    public interface OnTakeOnListener {
        void onTakeOn(ItemStack item, int slot, long player);
    }

    private static final Map<Integer, OnTickListener> onTickListenerMap = new HashMap<>();
    private static final Map<Integer, OnTickListener> onLocalTickListenerMap = new HashMap<>();
    private static final Map<Integer, OnHurtListener> onHurtListenerMap = new HashMap<>();
    private static final Map<Integer, OnTakeOnListener> onTakeOnListenerMap = new HashMap<>();
    private static final Map<Integer, OnTakeOnListener> onLocalTakeOnListenerMap = new HashMap<>();
    private static final Map<Integer, OnTakeOffListener> onTakeOffListenerMap = new HashMap<>();
    private static final Map<Integer, OnTakeOffListener> onLocalTakeOffListenerMap = new HashMap<>();

    public static void registerOnTickListener(int id, OnTickListener listener) {
        onTickListenerMap.put(id, listener);
    }

    public static void registerOnLocalTickListener(int id, OnTickListener listener) {
        onLocalTickListenerMap.put(id, listener);
    }

    public static void registerOnHurtListener(int id, OnHurtListener listener) {
        onHurtListenerMap.put(id, listener);
    }

    public static void registerOnTakeOnListener(int id, OnTakeOnListener listener) {
        onTakeOnListenerMap.put(id, listener);
    }

    public static void registerLocalOnTakeOnListener(int id, OnTakeOnListener listener) {
        onLocalTakeOnListenerMap.put(id, listener);
    }

    public static void registerOnTakeOffListener(int id, OnTakeOffListener listener) {
        onTakeOffListenerMap.put(id, listener);
    }

    public static void registerLocalOnTakeOffListener(int id, OnTakeOffListener listener) {
        onLocalTakeOffListenerMap.put(id, listener);
    }


    private final boolean isLocal;
    private final EntityActor actor;
    private final ItemStack[] armorItems = new ItemStack[4];

    private final JobExecutor instantExecutor = Network.getSingleton().getInstantJobExecutor();
    private final JobExecutor delayedExecutor = Network.getSingleton().getServerThreadJobExecutor();

    public ActorArmorHandler(EntityActor actor, boolean isLocal) {
        this.isLocal = isLocal;
        this.actor = actor;
        for (int slot = 0; slot < 4; slot++) {
            armorItems[slot] = new ItemStack();
        }
    }

    public void onTick() {
        long actorUid = actor.getUid();
        for (int slot = 0; slot < 4; slot++) {
            ItemStack currentArmor = actor.getArmorSlot(slot);
            ItemStack lastArmor = armorItems[slot];
            int finalSlot = slot;
            if (currentArmor.id != lastArmor.id) {
                instantExecutor.add(() -> {
                    OnTakeOffListener takeOffListener = (isLocal ? onLocalTakeOffListenerMap : onTakeOffListenerMap).get(lastArmor.id);
                    if (takeOffListener != null) {
                        takeOffListener.onTakeOff(lastArmor, finalSlot, actorUid);
                    }
                    OnTakeOnListener takeOnListener = (isLocal ? onLocalTakeOnListenerMap : onTakeOnListenerMap).get(currentArmor.id);
                    if (takeOnListener != null) {
                        takeOnListener.onTakeOn(currentArmor, finalSlot, actorUid);
                    }
                });
            }

            OnTickListener tickListener = (isLocal ? onLocalTickListenerMap : onTickListenerMap).get(currentArmor.id);
            if (tickListener != null) {
                instantExecutor.add(() -> {
                    Object resultItem = tickListener.onTick(currentArmor, finalSlot, actorUid);
                    if (resultItem != null) {
                        ItemStack resultItemStack = ItemStack.parse(resultItem);
                        // do not save this item in armorItems, so on next tick callbacks will be triggered in case of id change
                        if (resultItemStack != null) {
                            actor.setArmorSlot(finalSlot, resultItemStack);
                        }
                    }
                });
            }

            armorItems[slot] = currentArmor;
        }
    }

    public void onHurt(long attacker, int damageValue, int damageType, boolean bool1, boolean bool2) {
        if (isLocal) return;

        long actorUid = actor.getUid();
        for (int slot = 0; slot < 4; slot++) {
            ItemStack armor = actor.getArmorSlot(slot);
            OnHurtListener listener = onHurtListenerMap.get(armor.id);
            if (listener != null) {
                int finalSlot = slot;
                instantExecutor.add(() -> {
                    Object resultItem = listener.onHurt(armor, finalSlot, actorUid, damageValue, damageType, attacker, bool1, bool2);
                    if (resultItem != null) {
                        delayedExecutor.add(() -> {
                            if (actor.getHealth() > 0) {
                                ItemStack resultItemStack = ItemStack.parse(resultItem);
                                if (resultItemStack != null) {
                                    actor.setArmorSlot(finalSlot, resultItemStack);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}
