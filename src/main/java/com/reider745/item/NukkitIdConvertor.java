package com.reider745.item;

import com.zhekasmirnov.innercore.api.NativeItemInstance;

import java.util.*;

public class NukkitIdConvertor {

    public static void init() {
        // cn.nukkit.item.ItemBucket
        put(325, 2, 838, 0); // ItemID.cod_bucket
        put(325, 10, 843, 0); // ItemID.lava_bucket
        put(325, 4, 846, 0); // ItemID.tropical_fish_bucket
        put(325, 3, 847, 0); // ItemID.salmon_bucket
        put(325, 8, 850, 0); // ItemID.water_bucket
        put(325, 5, 871, 0); // ItemID.pufferfish_bucket
        put(325, 1, 876, 0); // ItemID.milk_bucket
        // cn.nukkit.utils.DyeColor, cn.nukkit.item.ItemDye
        put(351, 7, 730, 0); // ItemID.light_gray_dye
        put(351, 17, 736, 0); // ItemID.brown_dye
        put(351, 12, 758, 0); // ItemID.light_blue_dye
        put(351, 2, 760, 0); // ItemID.green_dye
        put(351, 11, 784, 0); // ItemID.yellow_dye
        put(351, 18, 819, 0); // ItemID.blue_dye
        put(351, 4, 821, 0); // ItemID.lapis_lazuli
        put(351, 0, 822, 0); // ItemID.ink_sac
        put(351, 19, 823, 0); // ItemID.white_dye
        put(351, 14, 824, 0); // ItemID.orange_dye
        put(351, 13, 825, 0); // ItemID.magenta_dye
        put(351, 8, 826, 0); // ItemID.gray_dye
        put(351, 6, 827, 0); // ItemID.cyan_dye
        put(351, 5, 828, 0); // ItemID.purple_dye
        put(351, 1, 829, 0); // ItemID.red_dye
        put(351, 16, 831, 0); // ItemID.black_dye
        put(351, 9, 837, 0); // ItemID.pink_dye
        put(351, 3, 848, 0); // ItemID.cocoa_beans
        put(351, 15, 858, 0); // ItemID.bone_meal
        put(351, 10, 868, 0); // ItemID.lime_dye
        // cn.nukkit.item.ItemID
        put(350, 0, 877, 0); // ItemID.cooked_cod
        put(263, 1, 866, 0); // ItemID.charcoal
        put(401, 0, 836, 0); // ItemID.firework_rocket
        put(349, 0, 809, 0); // ItemID.cod
        put(461, 0, 815, 0); // ItemID.tropical_fish
        put(752, 0, 724, 0); // ItemID.netherite_scrap
        put(750, 0, 725, 0); // ItemID.netherite_leggings
        put(744, 0, 726, 0); // ItemID.netherite_shovel
        put(743, 0, 727, 0); // ItemID.netherite_sword
        put(742, 0, 728, 0); // ItemID.netherite_ingot
        put(748, 0, 764, 0); // ItemID.netherite_helmet
        put(745, 0, 804, 0); // ItemID.netherite_pickaxe
        put(751, 0, 813, 0); // ItemID.netherite_boots
        put(749, 0, 834, 0); // ItemID.netherite_chestplate
        put(746, 0, 835, 0); // ItemID.netherite_axe
        put(747, 0, 880, 0); // ItemID.netherite_hoe
        put(757, 0, 885, 0); // ItemID.warped_fungus_on_a_stick
        put(736, 0, 869, 0); // ItemID.honeycomb
        put(737, 0, 731, 0); // ItemID.honey_bottle
        put(753, 0, 739, 0); // ItemID.crimson_sign
        put(450, 0, 741, 0); // ItemID.totem_of_undying
        put(754, 0, 755, 0); // ItemID.warped_sign
        put(801, 0, 530, 0); // ItemID.soul_campfire
        put(734, 0, 757, 0); // ItemID.suspicious_stew
        put(741, 0, 729, 0); // ItemID.lodestone_compass
        put(759, 0, 857, 0); // ItemID.music_disc_pigstep
        // cn.nukkit.item.ItemBoat
        put(333, 0, 873, 0); // ItemID.oak_boat
        put(333, 1, 844, 0); // ItemID.spruce_boat
        put(333, 2, 875, 0); // ItemID.birch_boat
        put(333, 3, 845, 0); // ItemID.jungle_boat
        put(333, 4, 842, 0); // ItemID.acacia_boat
        put(333, 5, 841, 0); // ItemID.dark_oak_boat
        // TODO: IDs conversion for Blocks, Patterns, Spawn Eggs, etc.
    }

    public static final class EntryItem {
        public int id, data;

        private EntryItem(int id, int data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            EntryItem entryItem = (EntryItem) o;
            return id == entryItem.id && data == entryItem.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, data);
        }

        @Override
        public String toString() {
            return "EntryItem{id=" + id + ", data=" + data + '}';
        }
    }

    private static final Map<EntryItem, EntryItem> nukkitToInnerCore = new HashMap<>();

    public static void put(int nukkitId, int nukkitData, int targetId, int targetData) {
        final EntryItem nukkit = new EntryItem(nukkitId, nukkitData);
        final EntryItem target = new EntryItem(targetId, targetData);
        nukkitToInnerCore.put(nukkit, target);
    }

    public static void convert(NativeItemInstance instance) {
        final EntryItem target = getInnerCoreForNukkit(instance.id, instance.data);
        if (target != null) {
            instance.id = target.id;
            instance.data = target.data;
        }
    }

    public static EntryItem getInnerCoreForNukkit(int id, int data) {
        EntryItem entryItem = new EntryItem(id, data);
        return nukkitToInnerCore.containsKey(entryItem) ? nukkitToInnerCore.get(entryItem) : entryItem;
    }

    public static EntryItem getNukkitForInnerCore(int id, int data) {
        final EntryItem target = new EntryItem(id, data);
        return nukkitToInnerCore.entrySet().stream()
                .filter(entry -> target.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(target);
    }
}
