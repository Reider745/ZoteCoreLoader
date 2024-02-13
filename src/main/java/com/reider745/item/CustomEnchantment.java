package com.reider745.item;

import java.util.Map;

import com.reider745.api.CallbackHelper;
import com.reider745.entity.EntityMethod;
import com.reider745.event.EventListener;
import com.reider745.hooks.ItemUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.constants.EnchantType;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemBow;
import cn.nukkit.item.ItemFishingRod;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.utils.Identifier;
import cn.nukkit.utils.Utils;

public class CustomEnchantment extends Enchantment {
	protected final Enchantment inheritable;

	public CustomEnchantment(int id, String name) {
		super(id, name, Rarity.RARE, EnchantmentType.ALL);
		this.inheritable = null;
	}

	protected CustomEnchantment(Enchantment inheritable, int id) {
		super(id, validateIdentifier(inheritable), inheritable.getRarity(), inheritable.type);
		this.inheritable = inheritable;
	}

	private static String validateIdentifier(Enchantment inheritable) {
		if (inheritable == null) {
			throw new IllegalArgumentException("inheritable == null");
		}
		if (inheritable instanceof CustomEnchantment enchant) {
			return enchant.getVanillaIdentifier();
		}
		Map.Entry<Identifier, Enchantment> enchant = customEnchantments.entrySet().stream()
				.filter(entry -> entry.getValue() != null && entry.getValue().getId() == inheritable.getId())
				.findFirst().orElse(null);
		if (enchant != null) {
			return enchant.getKey().getPath();
		}
		String name = inheritable.getName();
		return name.startsWith("%enchantment.") ? name.substring(13) : name;
	}

	public void registerAsEnchantment() {
		if (enchantments[id] != null) {
			removeCreativeEnchantedBooks(enchantments[id].getMinLevel(), enchantments[id].getMaxLevel());
		}
		enchantments[id] = this;
		customEnchantments.put(new Identifier("minecraft", name), this);
		addCreativeEnchantedBooks();
	}

	private void removeCreativeEnchantedBooks(int minLevel, int maxLevel) {
		for (int i = minLevel; i <= maxLevel; i++) {
			Item book = Item.get(Item.ENCHANTED_BOOK);
			book.addEnchantment(Enchantment.get(id).setLevel(i, false));
			Item.removeCreativeItem(407, book);
		}
	}

	private void addCreativeEnchantedBooks() {
		for (int i = getMinLevel(); i <= getMaxLevel(); i++) {
			Item book = Item.get(Item.ENCHANTED_BOOK);
			book.addEnchantment(Enchantment.get(id).setLevel(i, false));
			Item.addCreativeItem(407, book);
		}
	}

	private int weight = -1;

	@Override
	public Rarity getRarity() {
		if (weight == -1) {
			if (inheritable != null) {
				return inheritable.getRarity();
			}
			return Rarity.RARE;
		}
		return Rarity.fromWeight(weight);
	}

	@Override
	public int getWeight() {
		if (weight == -1) {
			if (inheritable != null) {
				return inheritable.getWeight();
			}
			return 3;
		}
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	private int minLevel, maxLevel;

	@Override
	public int getMinLevel() {
		if (minLevel == 0) {
			if (inheritable != null) {
				return inheritable.getMinLevel();
			}
			return 1;
		}
		return minLevel;
	}

	@Override
	public int getMaxLevel() {
		if (maxLevel == 0) {
			if (inheritable != null) {
				return inheritable.getMaxLevel();
			}
			return 5;
		}
		return maxLevel;
	}

	@Override
	public int getMaxEnchantableLevel() {
		if (maxLevel == 0 && inheritable != null) {
			return inheritable.getMaxEnchantableLevel();
		}
		return super.getMaxEnchantableLevel();
	}

	public void setMinMaxLevel(int min, int max) {
		removeCreativeEnchantedBooks(getMinLevel(), getMaxLevel());
		this.minLevel = min;
		this.maxLevel = max;
		addCreativeEnchantedBooks();
	}

	private float[] minMaxCostPoly = new float[0];

	@Override
	public int getMinEnchantAbility(int level) {
		if (minMaxCostPoly.length == 0) {
			if (inheritable != null) {
				return inheritable.getMinEnchantAbility(level);
			}
			return super.getMinEnchantAbility(level);
		}
		return (int) getPolyMinEnchantAbility(level);
	}

	protected float getPolyMinEnchantAbility(int level) {
		return Utils.rand(minMaxCostPoly[2], minMaxCostPoly[5])
				+ (float) level * Utils.rand(minMaxCostPoly[1], minMaxCostPoly[4]);
	}

	@Override
	public int getMaxEnchantAbility(int level) {
		if (minMaxCostPoly.length == 0) {
			if (inheritable != null) {
				return inheritable.getMaxEnchantAbility(level);
			}
			return super.getMaxEnchantAbility(level);
		}
		return (int) (getPolyMinEnchantAbility(level) + Utils.rand(minMaxCostPoly[0], minMaxCostPoly[3]));
	}

	public void setMinMaxCostPoly(float aMin, float bMin, float cMin, float aMax, float bMax, float cMax) {
		this.minMaxCostPoly = new float[] { aMin, bMin, cMin, aMax, bMax, cMax };
	}

	public static float currentDamageBonus;
	private boolean isMeleeDamageEnchant;

	@Override
	public double getDamageBonus(Entity entity) {
		float bonus = 0;
		if (isMeleeDamageEnchant) {
			EntityDamageEvent event = entity.getLastDamageCause();
			int damage = event != null ? (int) event.getFinalDamage() : 0;
			CallbackHelper.applyCustomValue("getDamageBonus",
					() -> NativeCallback.onEnchantGetDamageBonus(id, damage, entity.getId()), null);
			bonus = currentDamageBonus;
			currentDamageBonus = 0f;
		} else if (inheritable != null) {
			return inheritable.getDamageBonus(entity);
		}
		return bonus;
	}

	@Override
	public void doAttack(Entity attacker, Entity entity) {
		if (!isMeleeDamageEnchant && inheritable != null) {
			inheritable.doAttack(attacker, entity);
		}
	}

	@Override
	public void doPostAttack(Entity attacker, Entity entity) {
		if (isMeleeDamageEnchant) {
			EntityDamageEvent event = entity.getLastDamageCause();
			int damage = event != null ? (int) event.getFinalDamage() : 0;
			CallbackHelper.applyCustomValue("doPostAttack",
					() -> NativeCallback.onEnchantPostAttack(id, damage, attacker.getId(), entity.getId()), null);
		} else if (inheritable != null) {
			inheritable.doPostAttack(attacker, entity);
		}
	}

	public void setIsMeleeDamageEnchant(boolean enabled) {
		this.isMeleeDamageEnchant = enabled;
	}

	public static float currentProtectionBonus;
	private boolean isProtectionEnchant;

	@Override
	public float getProtectionFactor(EntityDamageEvent event) {
		float factor = 0;
		if (isProtectionEnchant) {
			int damage = (int) event.getFinalDamage();
			int cause = EventListener.convertDamageCauseToEnum(event.getCause());
			Entity attacker = event instanceof EntityDamageByEntityEvent entityDamageEvent
					? entityDamageEvent.getDamager()
					: null;
			CallbackHelper.applyCustomValue("getProtectionFactor", () -> NativeCallback.onEnchantGetProtectionBonus(id,
					damage, cause, attacker != null ? attacker.getId() : -1), null);
			factor = currentProtectionBonus;
			currentProtectionBonus = 0f;
		} else if (inheritable != null) {
			return inheritable.getProtectionFactor(event);
		}
		return factor;
	}

	@Override
	public void doPostHurt(Entity attacker, Entity entity) {
		if (isProtectionEnchant) {
			Item nukkitItem = EntityMethod.getEntityCarriedItem(attacker.getId());
			NukkitIdConvertor.EntryItem item = NukkitIdConvertor.getInnerCoreForNukkit(nukkitItem.getId(),
					nukkitItem.getDamage());
			EntityDamageEvent event = entity.getLastDamageCause();
			int damage = event != null ? (int) event.getFinalDamage() : 0;
			CallbackHelper.applyCustomValue("doPostHurt",
					() -> NativeCallback.onEnchantPostHurt(id, item.id, nukkitItem.getCount(), item.data,
							ItemUtils.getItemInstanceExtra(nukkitItem), damage, attacker.getId(), entity.getId()),
					null);
		} else if (inheritable != null) {
			inheritable.doPostHurt(attacker, entity);
		}
	}

	public void setIsProtectionEnchant(boolean enabled) {
		this.isProtectionEnchant = enabled;
	}

	@Override
	protected boolean checkCompatibility(Enchantment enchantment) {
		if (inheritable != null) {
			return inheritable.isCompatibleWith(enchantment);
		}
		return super.checkCompatibility(enchantment);
	}

	private int mask;

	@Override
	public boolean canEnchant(Item item) {
		if (mask == 0 && inheritable != null) {
			return inheritable.canEnchant(item);
		}
		if (mask == 0 || mask == EnchantType.all || mask == EnchantType.book) {
			return true;
		}
		return ((item instanceof ItemArmor || item instanceof CustomArmorItem)
				&& (((mask & EnchantType.helmet) != 0 && item.isHelmet())
						|| ((mask & EnchantType.leggings) != 0 && item.isLeggings())
						|| ((mask & EnchantType.boots) != 0 && item.isBoots())
						|| ((mask & EnchantType.chestplate) != 0 && item.isChestplate())))
				|| ((mask & EnchantType.weapon) != 0 && item.isSword())
				|| ((mask & EnchantType.bow) != 0 && item instanceof ItemBow)
				|| ((mask & EnchantType.hoe) != 0 && item.isHoe())
				|| ((mask & EnchantType.shears) != 0 && item.isShears())
				|| ((mask & EnchantType.flintAndSteel) != 0 && item.getMaxDurability() >= 0)
				|| ((mask & EnchantType.axe) != 0 && item.isAxe())
				|| ((mask & EnchantType.pickaxe) != 0 && item.isPickaxe())
				|| ((mask & EnchantType.shovel) != 0 && item.isShovel())
				|| ((mask & EnchantType.fishingRod) != 0 && item instanceof ItemFishingRod);
		// TODO: wearable, trident, crossbow, but it was actually strange...
	}

	public void setEnchantingMask(int mask) {
		this.mask = mask;
	}

	@Override
	public boolean isMajor() {
		if (inheritable != null) {
			return inheritable.isMajor();
		}
		return super.isMajor();
	}

	private boolean isTreasure, isTreasureSet;

	@Override
	public boolean isTreasure() {
		if (!isTreasureSet && inheritable != null) {
			return inheritable.isTreasure();
		}
		return isTreasure;
	}

	private boolean isLootable = true;

	public boolean isLootable() {
		return isLootable;
	}

	public void setIsLootable(boolean lootable) {
		this.isLootable = lootable;
	}

	public void setIsTreasure(boolean treasure) {
		this.isTreasure = treasure;
		this.isTreasureSet = true;
	}

	public String getVanillaIdentifier() {
		return this.name;
	}

	public static CustomEnchantment inherit(Enchantment enchantment) {
		return new CustomEnchantment(enchantment, enchantment.getId());
	}
}
