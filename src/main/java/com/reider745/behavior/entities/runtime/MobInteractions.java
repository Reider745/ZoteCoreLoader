package com.reider745.behavior.entities.runtime;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import com.reider745.behavior.entities.CustomEntity;
import com.reider745.behavior.loots.MobLoot;
import com.reider745.behavior.recipes.RecipeItems;
import org.json.JSONArray;
import org.json.JSONObject;

public final class MobInteractions {
    private MobInteractions() {}

    public static boolean matches(Object input, Item item) {
        if (input instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) if (matches(array.get(i), item)) return true;
            return false;
        }
        if (input == null) return false;
        try {
            Object descriptor = input instanceof JSONObject object && object.has("item") ? object : input.toString();
            Item expected = RecipeItems.parse(descriptor);
            return expected.getId() == item.getId() && (!expected.hasMeta() || expected.getDamage() == item.getDamage());
        } catch (IllegalArgumentException e) { return false; }
    }

    public static boolean interact(CustomEntity entity, Player player, Item item) {
        NukkitMobHost host = entity.host();
        Entity previous = host.other;
        host.other = player;
        try {
            JSONObject components = entity.components();
            JSONObject tameable = components.optJSONObject("minecraft:tameable");
            if (tameable != null && !entity.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_TAMED)
                    && matches(tameable.opt("tame_items"), item)) {
                consume(player, item);
                if (host.random() < tameable.optDouble("probability", 1)) {
                    entity.namedTag.putLong("BehaviorOwner", player.getId()).putString("BehaviorOwnerUUID", player.getUniqueId().toString());
                    entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_TAMED, true);
                    entity.runtime().trigger(tameable.opt("tame_event"));
                }
                return true;
            }
            JSONObject healable = components.optJSONObject("minecraft:healable");
            if (healable != null && entity.getHealth() < entity.getMaxHealth() && MobFilters.test(healable.opt("filters"), host)) {
                JSONArray items = healable.optJSONArray("items");
                if (items != null) for (int i = 0; i < items.length(); i++) {
                    JSONObject entry = items.getJSONObject(i);
                    if (matches(entry.opt("item"), item)) {
                        entity.heal((float) entity.runtime().duration(entry.opt("heal_amount"), 1));
                        consume(player, item);
                        return true;
                    }
                }
            }
            JSONObject breedable = components.optJSONObject("minecraft:breedable");
            if (breedable != null && !entity.isBaby() && !entity.isInLove() && !entity.isInLoveCooldown()
                    && matches(breedable.opt("breed_items"), item)
                    && (!breedable.optBoolean("require_tame", true) || entity.getDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_TAMED))) {
                entity.setInLove();
                consume(player, item);
                return true;
            }
            JSONObject ageable = components.optJSONObject("minecraft:ageable");
            if (ageable != null && entity.isBaby() && matches(ageable.opt("feed_items"), item)) {
                entity.feedBaby(.1);
                consume(player, item);
                return true;
            }
            JSONObject interact = components.optJSONObject("minecraft:interact");
            Object interactions = interact == null ? null : interact.opt("interactions");
            JSONArray list = interactions instanceof JSONArray a ? a : interactions instanceof JSONObject o ? new JSONArray().put(o) : new JSONArray();
            for (int i = 0; i < list.length(); i++) {
                if (!entity.canInteract(i)) continue;
                JSONObject entry = list.getJSONObject(i);
                JSONObject onInteract = entry.optJSONObject("on_interact");
                if (onInteract != null && !MobFilters.test(onInteract.opt("filters"), host)) continue;
                if (!MobFilters.test(entry.opt("filters"), host)) continue;
                if (entry.optBoolean("use_item", false)) consume(player, item);
                if (entry.has("transform_to_item")) player.getInventory().setItemInHand(RecipeItems.result(entry.get("transform_to_item")));
                if (entry.has("play_sounds")) {
                    for (String sound : entry.getString("play_sounds").split(",")) entity.level.addSound(entity, sound.trim());
                }
                JSONObject give = entry.optJSONObject("give_item");
                if (give != null) {
                    for (Item drop : MobLoot.generate(entity.getFactory().definition.source(), give.getString("table"), host)) {
                        for (Item overflow : player.getInventory().addItem(drop)) entity.level.dropItem(entity, overflow);
                    }
                }
                if (entry.has("spawn_entities")) {
                    Object spawn = entry.get("spawn_entities");
                    if (spawn instanceof String id) entity.spawnChild(id, "", entity);
                    else if (spawn instanceof JSONArray array) for (int n = 0; n < array.length(); n++) entity.spawnChild(array.getString(n), "", entity);
                }
                if (entry.optBoolean("swing", false)) player.getLevel().addSound(player, cn.nukkit.level.Sound.RANDOM_POP);
                entity.runtime().trigger(onInteract);
                entity.interactionUsed(i, entry.optDouble("cooldown", 0));
                return true;
            }
            JSONObject rideable = components.optJSONObject("minecraft:rideable");
            if (rideable != null && entity.passengers.size() < rideable.optInt("seat_count", 1)
                    && (!rideable.optBoolean("crouching_skip_interact", true) || !player.isSneaking())) {
                entity.mountEntity(player);
                return true;
            }
            JSONObject equipment = components.optJSONObject("minecraft:equippable");
            JSONArray slots = equipment == null ? null : equipment.optJSONArray("slots");
            if (slots != null) for (int i = 0; i < slots.length(); i++) {
                JSONObject slot = slots.getJSONObject(i);
                if (!matches(slot.opt("accepted_items"), item)) continue;
                int index = slot.optInt("slot", 0);
                String name = switch (index) { case 1 -> "off_hand"; case 2 -> "head"; case 3 -> "chest"; case 4 -> "legs"; case 5 -> "feet"; default -> "main_hand"; };
                Item old = entity.equipped(name);
                Item replacement = item.clone(); replacement.setCount(1);
                entity.equip(name, replacement);
                consume(player, item);
                if (!old.isNull()) player.getInventory().addItem(old);
                entity.runtime().trigger(slot.opt("on_equip"));
                return true;
            }
            return false;
        } finally { host.other = previous; }
    }

    private static void consume(Player player, Item item) {
        if (!player.isCreative()) {
            item.setCount(Math.max(0, item.getCount() - 1));
            player.getInventory().setItemInHand(item);
        }
    }

    public static void breed(CustomEntity first, CustomEntity second) {
        JSONObject breedable = first.components().optJSONObject("minecraft:breedable");
        if (breedable == null || !first.isInLove() || !second.isInLove()) return;
        Object types = breedable.opt("breeds_with");
        JSONArray choices = types instanceof JSONArray a ? a : types instanceof JSONObject o ? new JSONArray().put(o) : new JSONArray();
        for (int i = 0; i < choices.length(); i++) {
            JSONObject type = choices.getJSONObject(i);
            String mate = type.optString("mate_type", first.getFactory().description.identifier);
            if (!mate.equals(second.getFactory().description.identifier)) continue;
            String childType = type.optString("baby_type", first.getFactory().description.identifier);
            Entity baby = first.spawnChild(childType, "minecraft:entity_born", first);
            if (baby instanceof CustomEntity custom) {
                custom.setBaby(true);
                custom.namedTag.putLong("BehaviorOwner", first.namedTag.getLong("BehaviorOwner"));
            }
            first.runtime().trigger(type.opt("breed_event"));
            first.setInLove(false); second.setInLove(false);
            first.setInLoveCooldown((short) 6000); second.setInLoveCooldown((short) 6000);
            return;
        }
    }
}
