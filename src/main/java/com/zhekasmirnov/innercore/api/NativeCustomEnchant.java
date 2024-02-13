package com.zhekasmirnov.innercore.api;

import com.reider745.api.Client;
import com.reider745.item.CustomEnchantment;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mod.ContentIdSource;

import cn.nukkit.item.enchantment.Enchantment;

import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.Map;

public class NativeCustomEnchant {
    private static final Map<Integer, NativeCustomEnchant> enchantById = new HashMap<>();
    private static final Map<String, NativeCustomEnchant> enchantByNameId = new HashMap<>();

    public final CustomEnchantment pointer;
    public final int id;
    public final String nameId;

    private String description;

    private NativeCustomEnchant(int id, String nameId) {
        this.id = id;
        this.nameId = nameId;
        Enchantment enchant = Enchantment.get(id);
        // TODO: Nukkit-MOT intersecting with vanilla 1.16, because there is swift_sneak at ID=37.
        if (id == 37 || enchant.getName().equals("%enchantment.unknown")) {
            enchant = new CustomEnchantment(id, nameId);
        }
        if (!(enchant instanceof CustomEnchantment)) {
            enchant = CustomEnchantment.inherit(enchant);
        }
        this.pointer = (CustomEnchantment) enchant;
        this.pointer.registerAsEnchantment();
        enchantById.put(id, this);
        enchantByNameId.put(nameId, this);
        setDescription("custom_enchant." + nameId);
    }

    public NativeCustomEnchant() {
        throw new UnsupportedOperationException();
    }

    @JSStaticFunction
    public static NativeCustomEnchant getEnchantById(int id) {
        return enchantById.get(id);
    }

    @JSStaticFunction
    public static NativeCustomEnchant getEnchantByNameId(String nameId) {
        return enchantByNameId.get(nameId);
    }

    @JSStaticFunction
    public static NativeCustomEnchant newEnchant(String nameId, String description) {
        int id = ContentIdSource.getGlobal().getOrCreateScope("enchant").getOrGenerateId(nameId, 37, 65536, true);
        NativeCustomEnchant enchant = getEnchantById(id);
        if (enchant == null) {
            enchant = new NativeCustomEnchant(id, nameId);
        }
        enchant.setDescription(description);
        return enchant;
    }

    @Client
    public static void updateAllEnchantsDescriptions() {
        for (NativeCustomEnchant enchant : enchantById.values()) {
            enchant.setDescription(enchant.description);
        }
    }

    @Client
    public NativeCustomEnchant setDescription(String description) {
        this.description = description;
        return this;
    }

    public NativeCustomEnchant setFrequency(int frequency) {
        pointer.setWeight(frequency);
        return this;
    }

    public NativeCustomEnchant setIsLootable(boolean value) {
        pointer.setIsLootable(value);
        return this;
    }

    @Client
    public NativeCustomEnchant setIsDiscoverable(boolean value) {
        return this;
    }

    // TODO: Currently unused in Nukkit-MOT, decreases chance to obtain enchantment.
    public NativeCustomEnchant setIsTreasure(boolean value) {
        pointer.setIsTreasure(value);
        return this;
    }

    public NativeCustomEnchant setMasks(int mask1, int mask2) {
        pointer.setEnchantingMask(mask1 | mask2);
        return this;
    }

    public NativeCustomEnchant setMask(int mask) {
        return setMasks(mask, 0);
    }

    public NativeCustomEnchant setMinMaxLevel(int minLevel, int maxLevel) {
        pointer.setMinMaxLevel(minLevel, maxLevel);
        return this;
    }

    public NativeCustomEnchant setMinMaxCost(float aMin, float bMin, float cMin, float aMax, float bMax, float cMax) {
        pointer.setMinMaxCostPoly(aMin, bMin, cMin, aMax, bMax, cMax);
        return this;
    }

    public NativeCustomEnchant setMinMaxCost(float bMin, float cMin, float bMax, float cMax) {
        return setMinMaxCost(0, bMin, cMin, 0, bMax, cMax);
    }

    public NativeCustomEnchant setMinMaxCost(float cMin, float cMax) {
        return setMinMaxCost(0, 0, cMin, 0, 0, cMax);
    }

    public interface AttackDamageBonusProvider {
        float getDamageBonus(int damage, long actor);
    }

    private AttackDamageBonusProvider attackDamageBonusProvider;

    public AttackDamageBonusProvider getAttackDamageBonusProvider() {
        return attackDamageBonusProvider;
    }

    public NativeCustomEnchant setAttackDamageBonusProvider(AttackDamageBonusProvider provider) {
        pointer.setIsMeleeDamageEnchant(true);
        attackDamageBonusProvider = provider;
        return this;
    }

    public interface DoPostAttackListener {
        void doPostAttack(ItemStack item, int damage, long actor1, long actor2);
    }

    private DoPostAttackListener doPostAttackListener;

    public DoPostAttackListener getDoPostAttackListener() {
        return doPostAttackListener;
    }

    public NativeCustomEnchant setPostAttackCallback(DoPostAttackListener listener) {
        pointer.setIsMeleeDamageEnchant(true);
        doPostAttackListener = listener;
        return this;
    }

    public interface ProtectionBonusProvider {
        float getProtectionBonus(int damage, int cause, long attacker);
    }

    private ProtectionBonusProvider protectionBonusProvider;

    public ProtectionBonusProvider getProtectionBonusProvider() {
        return protectionBonusProvider;
    }

    public NativeCustomEnchant setProtectionBonusProvider(ProtectionBonusProvider provider) {
        pointer.setIsProtectionEnchant(true);
        protectionBonusProvider = provider;
        return this;
    }

    public interface DoPostHurtListener {
        void doPostHurt(ItemStack item, int damage, long actor1, long actor2);
    }

    private DoPostHurtListener doPostHurtListener;

    public DoPostHurtListener getDoPostHurtListener() {
        return doPostHurtListener;
    }

    public NativeCustomEnchant setPostHurtCallback(DoPostHurtListener listener) {
        pointer.setIsProtectionEnchant(true);
        doPostHurtListener = listener;
        return this;
    }

    public static void passCurrentDamageBonus(float bonus) {
        CustomEnchantment.currentDamageBonus = bonus;
    }

    public static void passCurrentProtectionBonus(float bonus) {
        CustomEnchantment.currentProtectionBonus = bonus;
    }
}
