package com.reider745.block;

//import cn.nukkit.blockstate.BlockStorage;
import cn.nukkit.block.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import com.reider745.api.CustomManager;
import com.reider745.api.ReflectHelper;
import com.zhekasmirnov.innercore.api.NativeBlock;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomBlock extends BlockSolidMeta implements RandomTick {
    public static HashMap<Integer, CustomManager> blocks = new HashMap<>();

    public static void registerBlock(int id, Block block){
        registerBlock(id, 0, block);
    }

    public static void registerBlock(int id, int data, Block block){
        Block.list[id] = block.getClass();
        //blocks.put(id+":"+data, block);
        int fullId = (id << DATA_BITS) | data;
        fullList[fullId] = block;
        Block.hasMeta[id] = true;

        Block.solid[id] = block.isSolid();
        Block.transparent[id] = block.isTransparent();
        hardness[id] = block.getHardness();
        Block.light[id] = block.getLightLevel();

        boolean[] randomTickBlocks = ReflectHelper.getField(Level.class, "randomTickBlocks");

        if(block instanceof RandomTick randomTick)
            randomTickBlocks[id] = randomTick.canRandomTickBlocks();

        if (block.isSolid()) {
            if (block.isTransparent()) {
                if (block instanceof BlockLiquid || block instanceof BlockIce) {
                    Block.lightFilter[id] = 2;
                } else {
                    Block.lightFilter[id] = 1;
                }
            } else {
                Block.lightFilter[id] = 15;
            }
        } else {
            Block.lightFilter[id] = 1;
        }
    }


    public static void init(){
        blocks.forEach((id, value) -> {
            CustomManager manager = getBlockManager(id);
            ArrayList<String> variants = getVariants(manager);
            for(int data = 0;data < variants.size();data++)
                registerBlock(id, data, new CustomBlock(id, data, manager));
        });
    }

    public static CustomManager getBlockManager(int id){
        return blocks.get(id);
    }

    public static ArrayList<String> getVariants(int id){
        return getVariants(getBlockManager(id));
    }

    public static ArrayList<String> getVariants(CustomManager manager){
        ArrayList<String> variants = (ArrayList<String>) manager.get("variants", new ArrayList<>()).clone();
        if(variants.size() >= 16)
            return variants;

        int size = 16-variants.size();
        String name = variants.get(0);
        for(int i = 0;i < size;i++)
            variants.add(name);

        return variants;
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

    private boolean TickingTile;

    protected CustomBlock(int id, int meta){
        this(id, meta, getBlockManager(id));
    }

    protected CustomBlock(int id, int meta, CustomManager manager) {
        super(meta);

        this.manager = manager;
        this.id = id;
        this.name = manager.get("name");

        TickingTile = manager.get("TickingTile:"+meta, false);
    }

    @Override
    public boolean canRandomTickBlocks() {
        return TickingTile;
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
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            NativeBlock.onRandomTickCallback((int) this.x, (int) this.y, (int) this.z, id, this.getDamage(), AdaptedScriptAPI.BlockSource.getFromCallbackPointer(this.level));
        }
        return 0;
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    public Block clone() {
        CustomBlock customBlock = (CustomBlock) super.clone();
        customBlock.id = id;
        customBlock.name = name;
        customBlock.manager = manager;
        customBlock.setDamage(getDamage());
        return customBlock;
    }
}
