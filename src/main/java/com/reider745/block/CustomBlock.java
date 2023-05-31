package com.reider745.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.api.BlockStorage;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.math.BlockFace;
import com.reider745.api.CustomManager;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomBlock extends BlockSolidMeta {
    public static HashMap<Integer, CustomManager> blocks = new HashMap<>();

    public static void init(){
        blocks.forEach((id, value) -> {
            //Block.list[id] = value.clazz;

            CustomManager manager = getBlockManager(id);
            ArrayList<String> variants = manager.get("variants", new ArrayList<>());
            for(int data = 0;data < variants.size();data++)
                BlockStorage.registerBlock(id, data, new CustomBlock(id, data, manager, variants.get(data)));
        });
    }

    public static CustomManager getBlockManager(int id){
        return blocks.get(id);
    }

    public static HashMap<String, Integer> customBlocks = new HashMap<>();

    public static CustomManager registerBlock(String textId, int id, String name, Class item){
        CustomManager manager = new CustomManager(id, item, "block");
        manager.put("name", name);

        blocks.put(id, manager);
        customBlocks.put("block_"+textId, id);;
        CustomManager.put(id, manager);

        return manager;
    }

    public static CustomManager registerBlock(String textId, int id, String name){
        return registerBlock(textId, id, name, CustomBlock.class);
    }

    private CustomManager manager;
    private int id;
    private String name;

    protected CustomBlock(int id, int meta, CustomManager manager, String name) {
        super(meta);

        this.manager = manager;
        this.id = id;
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{Item.get(id, this.getDamage(), 1)};
    }

    @Override
    public Block clone() {
        CustomBlock customBlock = (CustomBlock) super.clone();
        customBlock.id = id;
        customBlock.name = name;
        customBlock.manager = manager;
        return customBlock;
    }
}