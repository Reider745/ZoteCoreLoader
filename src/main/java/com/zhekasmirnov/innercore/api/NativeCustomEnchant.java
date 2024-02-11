package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.reider745.item.CustomEnchantMethods;
import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mod.ContentIdSource;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;
import java.util.Map;

public class NativeCustomEnchant {
    private static final Map<Integer, NativeCustomEnchant> enchantById = new HashMap<>();
    private static final Map<String, NativeCustomEnchant> enchantByNameId = new HashMap<>();

    public final long pointer;
    public final int id;
    public final String nameId;

    private String description;

    private NativeCustomEnchant(int id, String nameId) {
        this.id = id;
        this.nameId = nameId;
        this.pointer = constructNew(id, nameId);
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

    public static void updateAllEnchantsDescriptions() {
        for (NativeCustomEnchant enchant : enchantById.values()) {
            enchant.setDescription(enchant.description);
        }
    }

    public NativeCustomEnchant setDescription(String description) {
        this.description = description;
        setDescription(pointer, NameTranslation.translate(description));
        return this;
    }

    public NativeCustomEnchant setFrequency(int frequency) {
        setFrequency(pointer, frequency);
        return this;
    }

    public NativeCustomEnchant setIsLootable(boolean value) {
        setIsLootable(pointer, value);
        return this;
    }

    public NativeCustomEnchant setIsDiscoverable(boolean value) {
        setIsDiscoverable(pointer, value);
        return this;
    }

    public NativeCustomEnchant setIsTreasure(boolean value) {
        setIsTreasure(pointer, value);
        return this;
    }

    public NativeCustomEnchant setMasks(int mask1, int mask2) {
        setMasks(pointer, mask1, mask2);
        return this;
    }

    public NativeCustomEnchant setMask(int mask) {
        return setMasks(mask, 0);
    }

    public NativeCustomEnchant setMinMaxLevel(int minLevel, int maxLevel) {
        setMinMaxLevel(pointer, minLevel, maxLevel);
        return this;
    }

    public NativeCustomEnchant setMinMaxCost(float aMin, float bMin, float cMin, float aMax, float bMax, float cMax) {
        setMinMaxCostPoly(pointer, aMin, bMin, cMin, aMax, bMax, cMax);
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
        setIsMeleeDamageEnchant(pointer, true);
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
        setIsMeleeDamageEnchant(pointer, true);
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
        setIsProtectionEnchant(pointer, true);
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
        setIsProtectionEnchant(pointer, true);
        doPostHurtListener = listener;
        return this;
    }

    private static long constructNew(int id, String nameId) {
        return CustomEnchantMethods.constructNew(id, nameId);
    }

    private static void setDescription(long pointer, String description) {
        CustomEnchantMethods.setDescription(pointer, description);
    }

    private static void setFrequency(long pointer, int frequency) {
        CustomEnchantMethods.setFrequency(pointer, frequency);
    }

    private static void setIsLootable(long pointer, boolean isLootable) {
        CustomEnchantMethods.setIsLootable(pointer, isLootable);
    }

    private static void setIsDiscoverable(long pointer, boolean isDiscoverable) {
        CustomEnchantMethods.setIsDiscoverable(pointer, isDiscoverable);
    }

    private static void setIsTreasure(long pointer, boolean isTreasure) {
        CustomEnchantMethods.setIsTreasure(pointer, isTreasure);
    }

    private static void setIsMeleeDamageEnchant(long pointer, boolean value) {
        CustomEnchantMethods.setIsMeleeDamageEnchant(pointer, value);
    }

    private static void setIsProtectionEnchant(long pointer, boolean value) {
        CustomEnchantMethods.setIsProtectionEnchant(pointer, value);
    }

    private static void setMasks(long pointer, int mask1, int mask2) {
        CustomEnchantMethods.setMasks(pointer, mask1, mask2);
    }

    private static void setMinMaxLevel(long pointer, int minLevel, int maxLevel) {
        CustomEnchantMethods.setMinMaxLevel(pointer, minLevel, maxLevel);
    }

    private static void setMinMaxCostPoly(long pointer, float aMin, float bMin, float cMin, float aMax, float bMax,
            float cMax) {
        CustomEnchantMethods.setMinMaxCostPoly(pointer, aMin, bMin, cMin, aMax, bMax, cMax);
    }

    public static void passCurrentDamageBonus(float bonus) {
       // CustomEnchantMethods.passCurrentDamageBonus(bonus);
    }

    public static void passCurrentProtectionBonus(float bonus) {
        //CustomEnchantMethods.passCurrentProtectionBonus(bonus);
    }
}
/*
float simple(float x) {
    int i;
    for (i = 1; i < x + 1; i++) {

    }
    return x;
}
void t1() {

}
int main() {
    setlocale(LC_ALL, "RU");
    const int a = 10;
    int A[a];
    int g;
    srand(time(NULL));
    for (int i = 0; i < a; i++) {
        A[i] = rand() % 51;
        cout « A[a];
    }
    for (int i = 0; i < a; i++) {
        g = A[i]++;
    }
    cout « g;
}
 */