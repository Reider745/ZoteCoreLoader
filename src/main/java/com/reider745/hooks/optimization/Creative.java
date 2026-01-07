package com.reider745.hooks.optimization;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Utils;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

import java.util.Map;

@Hooks(className = "cn.nukkit.item.Item")
public class Creative {
    private static Item.CreativeItems items = new Item.CreativeItems();

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void clearCreativeItems() {
        items.clear();
    }

    @Inject
    public static Item.CreativeItems getCreativeItemsAndGroups(int protocol) {
        return items;
    }

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void addCreativeItem(int protocol, Item item, CreativeItemCategory category, String group) {
        if(protocol == 407) {
            items.add(item);
        }
    }

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void registerCreativeItems(int protocol) {
        if(protocol != 407) {
            return;
        }

        for(Map map : (new Config(2)).loadFromStream(Server.class.getClassLoader().getResourceAsStream("creativeitems" + protocol + ".json")).getMapList("items")) {
            try {
                Item item = Item.fromJson(map);
                if (Utils.hasItemOrBlock(item.getId())) {
                    Item.addCreativeItem(protocol, item);
                }
            } catch (Exception e) {
                MainLogger.getLogger().logException(e);
            }
        }
    }

    @Inject(type = TypeHook.BEFORE_REPLACE)
    public static void registerCreativeItemsNew(int protocol, int blockPaletteProtocol, Item.CreativeItems creativeItems) {}
}
