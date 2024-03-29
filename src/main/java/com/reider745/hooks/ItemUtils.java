package com.reider745.hooks;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.inventory.transaction.RepairItemTransaction;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.enchantment.EnchantmentType;
import cn.nukkit.item.randomitem.EnchantmentItemSelector;
import cn.nukkit.nbt.tag.CompoundTag;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.item.CustomArmorItem;
import com.reider745.item.CustomEnchantment;
import com.reider745.item.NukkitIdConvertor;
import com.reider745.item.Repairs;
import com.zhekasmirnov.innercore.api.NativeFurnaceRegistry;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Hooks(className = "cn.nukkit.item.Item")
public class ItemUtils implements HookClass {

    @Inject(signature = "(ILjava/lang/Integer;I[B)Lcn/nukkit/item/Item;")
    public static Item get(int id, Integer meta, int count, byte[] tags) {
        try {
            Class<?> c = null;
            if (id < 0) {
                int blockId = 255 - id;
                c = Block.list[blockId];
            } else {
                c = Item.list[id];
            }

            Item item;
            if (c == null) {
                item = new Item(id, meta, count);
            } else if ((id < 256 && id != 166) || id > 8000) {
                if (meta >= 0) {
                    item = new ItemBlock(Block.get(id, meta), meta, count);
                } else {
                    item = new ItemBlock(Block.get(id), meta, count);
                }
            } else if (id >= IDRegistry.ITEM_ID_OFFSET) {
                item = ((Item) c.getConstructor(int.class, Integer.class, int.class).newInstance(id, meta, count));
            } else {
                item = ((Item) c.getConstructor(Integer.class, int.class).newInstance(meta, count));
            }

            if (tags.length != 0) {
                item.setCompoundTag(tags);
            }

            return item;
        } catch (Exception e) {
            return new Item(id, meta, count).setCompoundTag(tags);
        }
    }

    private static final Pattern ITEM_STRING_PATTERN = Pattern.compile(
            // 1:namespace 2:name 3:damage 4:num-id 5:damage
            "^(?:(?:([a-z_]\\w*):)?([a-z._]\\w*)(?::(-?\\d+))?|(-?\\d+)(?::(-?\\d+))?)$");

    @Inject
    public static Item fromString(String str) {
        String normalized = str.trim().replace(' ', '_').toLowerCase();
        Matcher matcher = ITEM_STRING_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return Item.AIR_ITEM.clone();
        }

        String name = matcher.group(2);
        OptionalInt meta = OptionalInt.empty();
        String metaGroup;
        if (name != null) {
            metaGroup = matcher.group(3);
        } else {
            metaGroup = matcher.group(5);
        }
        if (metaGroup != null) {
            meta = OptionalInt.of(Short.parseShort(metaGroup));
        }

        String numericIdGroup = matcher.group(4);
        if (name != null) {
            String namespaceGroup = matcher.group(1);
            String namespacedId;
            if (namespaceGroup != null) {
                namespacedId = namespaceGroup + ":" + name;
            } else {
                namespacedId = "minecraft:" + name;
            }
            if ("minecraft:air".equals(namespacedId)) {
                return Item.AIR_ITEM.clone();
            }

            Supplier<Item> constructor = Item.NAMESPACED_ID_ITEM.get(namespacedId);
            if (constructor != null) {
                try {
                    return constructor.get();
                } catch (Exception e) {
                    System.out.printf("Could not create a new instance of {} using the namespaced id {}", constructor,
                            namespacedId, e);
                }
            }

            // common item
            int id = RuntimeItems.getLegacyIdFromLegacyString(namespacedId);
            if (id > 0) {
                return Item.get(id, meta.orElse(0));
            } else if (namespaceGroup != null && !namespaceGroup.equals("minecraft:")) {
                return Item.AIR_ITEM.clone();
            }
        } else if (numericIdGroup != null) {
            int id = Integer.parseInt(numericIdGroup);
            return Item.get(id, meta.orElse(0));
        }

        if (name == null) {
            return Item.AIR_ITEM.clone();
        }

        int id = 0;
        try {
            id = BlockID.class.getField(name.toUpperCase()).getInt(null);
            if (id > 255) {
                id = 255 - id;
            }
        } catch (Exception ignore) {
            try {
                id = ItemID.class.getField(name.toUpperCase()).getInt(null);
            } catch (Exception ignore1) {
                try {
                    id = IDRegistry.getIdByNameId(name);
                } catch (Exception e) {
                }
            }
        }
        return Item.get(id, meta.orElse(0));
    }

    @Inject(signature = "()Ljava/lang/Short;", type = TypeHook.BEFORE)
    public static void getFuelTime(HookController<Item> controller) {
        short fuel = NativeFurnaceRegistry.getBurnTime(controller.getSelf());
        if (fuel > 0) {
            controller.setReplace(true);
            controller.setResult(Short.valueOf(fuel));
        }
    }

    public static final String INNER_CORE_TAG_NAME = "$mod";

    public static Item get(int id, int count, int meta, NativeItemInstanceExtra extra) {
        Item item = get(id, count, meta, extra != null ? extra.getValue() : null);
        if (extra != null) {
            extra.bind(item);
        }
        return item;
    }

    public static Item get(int id, int count, int meta, Item extra) {
        Item item = get(id, count, meta);
        if (extra != null) {
            CompoundTag custom = extra.getNamedTag();
            if (custom != null) {
                item.setNamedTag(custom);
            }
        }
        return item;
    }

    public static Item get(int id, int count, int meta) {
        NukkitIdConvertor.EntryItem entry = NukkitIdConvertor.getNukkitForInnerCore(id, meta);
        return Item.get(entry.id, entry.data, count);
    }

    public static Item get(int id, int meta) {
        return get(id, 1, meta);
    }

    public static Item get(int id) {
        return get(id, 0);
    }

    public static NativeItemInstanceExtra getItemInstanceExtra(Item item) {
        CompoundTag tag = item.getNamedTag();
        if (tag != null) {
            return new NativeItemInstanceExtra(item);
        }
        return null;
    }

    @Inject(className = "cn.nukkit.inventory.transaction.RepairItemTransaction", type = TypeHook.BEFORE, signature = "()Z")
    public static void isMapRecipe(HookController<RepairItemTransaction> controller, RepairItemTransaction transaction) {
        ArrayList<Integer> repairs = Repairs.getRepairs(transaction.getInputItem().getId());
        if (repairs != null)
            controller.setResult(repairs.contains(transaction.getOutputItem().getId()));
        controller.setReplace(false);
    }

    @Inject(className = "cn.nukkit.item.randomitem.EnchantmentItemSelector")
    public static List<Enchantment> getSupportEnchantments(EnchantmentItemSelector selector, Item item) {
        ArrayList<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.getEnchantments()) {
            if (item.getId() == Item.ENCHANTED_BOOK || enchantment.canEnchant(item)) {
                if (enchantment instanceof CustomEnchantment custom && !custom.isLootable()) {
                    continue;
                }
                enchantments.add(enchantment);
            }
        }
        return enchantments;
    }

    @Inject(className = "cn.nukkit.item.enchantment.EnchantmentType", type = TypeHook.AFTER, signature = "(Lcn/nukkit/item/Item;)Z")
    public static void canEnchantItem(HookController<EnchantmentType> controller, EnchantmentType type, Item item) {
        if (type == EnchantmentType.ALL || (type == EnchantmentType.BREAKABLE && item.getMaxDurability() >= 0)) {
            return;
        }
        if (item instanceof CustomArmorItem) {
            controller.setReplace(true);
            if (type == EnchantmentType.WEARABLE || (type == EnchantmentType.ARMOR && item.isArmor())
                    || (type == EnchantmentType.ARMOR_HEAD && item.isHelmet())
                    || (type == EnchantmentType.ARMOR_TORSO && item.isChestplate())
                    || (type == EnchantmentType.ARMOR_LEGS && item.isLeggings())
                    || (type == EnchantmentType.ARMOR_FEET && item.isBoots())) {
                controller.setResult(true);
                return;
            }
            controller.setResult(false);
        }
    }
}
