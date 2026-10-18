package com.reider745.behavior.loots;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.recipe.RecipeRegistry;
import com.reider745.behavior.entities.runtime.MobAssets;
import com.reider745.behavior.entities.runtime.MobFilters;
import com.reider745.behavior.entities.runtime.MobHost;
import com.reider745.behavior.recipes.RecipeItems;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.*;

/** Pack-local loot pools with weighted entries, nested tables, conditions and item functions. */
public final class MobLoot {
    private static final Map<Path, JSONObject> TABLES = new HashMap<>();
    private MobLoot() {}

    public static void register(String file, JSONObject json) {
        TABLES.put(Path.of(file).toAbsolutePath().normalize(), new JSONObject(json.toString()));
    }

    public static List<Item> generate(String source, String name, MobHost host) {
        List<Item> result = new ArrayList<>();
        if (name.isEmpty()) return result;
        Path root = MobAssets.packRoot(source);
        generate(root, name, host, result, new HashSet<>(), 0);
        return result;
    }

    private static void generate(Path root, String name, MobHost host, List<Item> result, Set<Path> stack, int depth) {
        Path path = root.resolve(name).normalize();
        if (!path.startsWith(root) || depth > 16 || !stack.add(path)) throw new IllegalArgumentException("Recursive or invalid loot table: " + name);
        JSONObject table = TABLES.get(path);
        if (table == null) throw new IllegalArgumentException("Missing behavior loot table: " + name);
        try {
            JSONArray pools = table.getJSONArray("pools");
            for (int i = 0; i < pools.length(); i++) {
                JSONObject pool = pools.getJSONObject(i);
                if (!conditions(pool.optJSONArray("conditions"), host)) continue;
                int rolls = (int) range(pool.opt("rolls"), 1, host);
                if (rolls < 0 || rolls > 1024) throw new IllegalArgumentException("Invalid loot roll count: " + rolls);
                JSONArray entries = pool.getJSONArray("entries");
                for (int roll = 0; roll < rolls; roll++) {
                    List<JSONObject> eligible = new ArrayList<>();
                    double weight = 0;
                    for (int n = 0; n < entries.length(); n++) {
                        JSONObject entry = entries.getJSONObject(n);
                        if (conditions(entry.optJSONArray("conditions"), host)) {
                            eligible.add(entry);
                            weight += Math.max(0, entry.optDouble("weight", 1));
                        }
                    }
                    double selected = host.random() * weight;
                    if (weight <= 0) continue;
                    for (JSONObject entry : eligible) {
                        selected -= Math.max(0, entry.optDouble("weight", 1));
                        if (selected >= 0) continue;
                        List<Item> drops = new ArrayList<>();
                        switch (entry.optString("type", "item")) {
                            case "item" -> drops.add(RecipeItems.result(entry.getString("name")));
                            case "loot_table" -> generate(root, entry.getString("name"), host, drops, stack, depth + 1);
                            case "empty" -> {}
                            default -> throw new IllegalArgumentException("Unsupported loot entry: " + entry);
                        }
                        for (Item item : drops) {
                            item = functions(item, entry.optJSONArray("functions"), host);
                            item = functions(item, pool.optJSONArray("functions"), host);
                            if (!item.isNull()) {
                                int amount = Math.min(4096, item.getCount());
                                while (amount > 0) {
                                    Item split = item.clone();
                                    split.setCount(Math.min(Math.max(1, split.getMaxStackSize()), amount));
                                    result.add(split);
                                    amount -= split.getCount();
                                    if (result.size() > 4096) throw new IllegalArgumentException("Loot output exceeds 4096 stacks");
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } finally { stack.remove(path); }
    }

    private static boolean conditions(JSONArray entries, MobHost host) {
        if (entries == null) return true;
        for (int i = 0; i < entries.length(); i++) {
            JSONObject condition = entries.getJSONObject(i);
            boolean passed = switch (condition.getString("condition")) {
                case "random_chance" -> host.random() < condition.getDouble("chance");
                case "random_chance_with_looting" -> host.random() < condition.getDouble("chance")
                        + condition.optDouble("looting_multiplier", 0) * looting(host);
                case "killed_by_player", "killed_by_player_or_pets" -> Boolean.TRUE.equals(host.filter("is_family", "damager", "", "player"));
                case "killed_by_entity" -> Boolean.TRUE.equals(host.filter("is_name", "damager", "", condition.getString("entity_type")));
                case "entity_properties" -> properties(condition, host);
                default -> throw new IllegalArgumentException("Unsupported loot condition: " + condition.getString("condition"));
            };
            if (!passed) return false;
        }
        return true;
    }

    private static Item functions(Item item, JSONArray functions, MobHost host) {
        if (functions == null) return item;
        for (int i = 0; i < functions.length(); i++) {
            JSONObject function = functions.getJSONObject(i);
            if (!conditions(function.optJSONArray("conditions"), host)) continue;
            switch (function.getString("function")) {
                case "set_count" -> item.setCount(Math.max(0, (int) range(function.opt("count"), 1, host)));
                case "set_data" -> item.setDamage((int) range(function.opt("data"), 0, host));
                case "set_damage" -> item.setDamage((int) ((1 - range(function.opt("damage"), 1, host)) * item.getMaxDurability()));
                case "looting_enchant" -> item.setCount(item.getCount() + looting(host) * (int) range(function.opt("count"), 1, host));
                case "set_name" -> item.setCustomName(function.getString("name"));
                case "set_lore" -> {
                    JSONArray lore = function.getJSONArray("lore");
                    String[] lines = new String[lore.length()];
                    for (int line = 0; line < lines.length; line++) lines[line] = lore.getString(line);
                    item.setLore(lines);
                }
                case "specific_enchants" -> {
                    JSONArray enchantments = function.getJSONArray("enchants");
                    for (int n = 0; n < enchantments.length(); n++) {
                        JSONObject enchantment = enchantments.getJSONObject(n);
                        Enchantment value = enchantment.opt("id") instanceof Number number ? Enchantment.get(number.intValue())
                                : Enchantment.getEnchantment(enchantment.getString("id"));
                        if (value != null) item.addEnchantment(value.setLevel(enchantment.optInt("level", 1)));
                    }
                }
                case "enchant_randomly" -> {
                    List<Enchantment> choices = new ArrayList<>();
                    for (Enchantment value : Enchantment.getEnchantments()) if (value != null && value.canEnchant(item)) choices.add(value);
                    if (!choices.isEmpty()) {
                        Enchantment value = Enchantment.get(choices.get((int) (host.random() * choices.size())).id);
                        item.addEnchantment(value.setLevel(1 + (int) (host.random() * value.getMaxLevel())));
                    }
                }
                case "furnace_smelt" -> {
                    var recipe = RecipeRegistry.matchFurnaceRecipe(item);
                    if (recipe != null) {
                        Item cooked = recipe.getResult(); cooked.setCount(item.getCount()); item = cooked;
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported loot function: " + function.getString("function"));
            }
        }
        return item;
    }

    private static boolean properties(JSONObject condition, MobHost host) {
        JSONObject properties = condition.getJSONObject("properties");
        if (properties.has("test") || properties.has("all_of") || properties.has("any_of")) return MobFilters.test(properties, host);
        String subject = switch (condition.optString("entity", "this")) {
            case "killer", "killer_player", "direct_killer" -> "damager";
            default -> "self";
        };
        for (String property : properties.keySet()) {
            String test = switch (property) {
                case "on_fire" -> "is_on_fire";
                case "on_ground" -> "is_on_ground";
                case "in_water" -> "is_in_water";
                default -> throw new IllegalArgumentException("Unsupported loot entity property: " + property);
            };
            Object actual = host.filter(test, subject, "", properties.get(property));
            if (actual == null || !MobFilters.compare(actual, properties.get(property), "equals")) return false;
        }
        return true;
    }

    private static int looting(MobHost host) {
        Object value = host.query("looting_level", List.of());
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static double range(Object input, double fallback, MobHost host) {
        if (input instanceof Number number) return number.doubleValue();
        if (input instanceof JSONObject range) {
            double min = range.optDouble("min", fallback);
            double max = range.optDouble("max", min);
            return min + Math.floor(host.random() * (max - min + 1));
        }
        return fallback;
    }
}
