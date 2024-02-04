package com.reider745.block;

import cn.nukkit.block.*;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import com.reider745.api.CustomManager;
import com.reider745.api.ReflectHelper;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItem;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class CustomBlock extends BlockSolidMeta implements RandomTick {
    public static HashMap<Integer, CustomManager> blocks = new HashMap<>();

    public static void registerBlock(int id, Block block) {
        registerBlock(id, 0, block);
    }

    public static void registerBlock(int id, int data, Block block) {
        Block.list[id] = block.getClass();
        int fullId = (id << DATA_BITS) | data;
        Block.fullList[fullId] = block;
        Block.hasMeta[id] = true;

        Block.solid[id] = block.isSolid();
        Block.transparent[id] = block.isTransparent();
        Block.hardness[id] = block.getHardness();
        Block.light[id] = block.getLightLevel();

        boolean[] randomTickBlocks = ReflectHelper.getField(Level.class, "randomTickBlocks");

        if (block instanceof RandomTick randomTick && randomTick.canRandomTickBlocks())
            randomTickBlocks[id] = true;

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

    public static String getTextIdForNumber(int id) {
        for (String texId : customBlocks.keySet())
            if (customBlocks.get(texId).equals(id))
                return texId;
        return null;
    }

    public static void init() {
        blocks.forEach((id, value) -> {
            final CustomManager manager = getBlockManager(id);
            final ArrayList<String> variants = getVariants(manager);
            try {
                final Constructor<?> constructor = manager.getClazz().getDeclaredConstructor(int.class, int.class,
                        CustomManager.class, String.class);
                for (int data = 0; data < variants.size(); data++)
                    registerBlock(id, data, (Block) constructor.newInstance(id, data, manager, variants.get(data)));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CustomManager getBlockManager(int id) {
        return blocks.get(id);
    }

    public static ArrayList<String> getVariants(int id) {
        return getVariants(getBlockManager(id));
    }

    public static ArrayList<String> getOrgVariants(int id) {
        CustomManager manager = getBlockManager(id);
        if (manager == null)
            return new ArrayList<>();
        return manager.get("variants", new ArrayList<>());
    }

    public static ArrayList<String> getOrgVariants(CustomManager manager) {
        if (manager == null)
            return new ArrayList<>();
        return manager.get("variants", new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> getVariants(CustomManager manager) {
        ArrayList<String> variants = (ArrayList<String>) manager.get("variants", new ArrayList<>()).clone();
        if (variants.size() >= 16)
            return variants;

        int size = 16 - variants.size();
        String name = variants.get(0);
        for (int i = 0; i < size; i++)
            variants.add(name);

        return variants;
    }

    public static HashMap<String, Integer> customBlocks = new HashMap<>();

    public static CustomManager registerBlock(String textId, int id, String name, Class<?> item) {
        CustomManager manager = new CustomManager(id, item, "block");
        manager.put("name", name);

        blocks.put(id, manager);
        customBlocks.put("block_" + textId, id);

        CustomManager.put(id, manager);
        NativeItem.newNativeItem(id, manager, textId, name);
        return manager;
    }

    public static CustomManager registerBlock(String textId, int id, String name) {
        return registerBlock(textId, id, name, CustomBlock.class);
    }

    public static CustomManager registerBlock(String textId, int id, String name, int tick_rate, boolean isRenewable) {
        CustomManager manager = registerBlock(textId, id, name, CustomBlockLiquid.class);
        manager.put("tick_rate", tick_rate);
        manager.put("isRenewable", isRenewable);
        return manager;
    }

    private CustomManager manager;
    private int id;
    private String name;

    private boolean TickingTile;
    private boolean NeighbourChange;

    protected CustomBlock(int id, int meta) {
        this(id, meta, getBlockManager(id));
    }

    protected CustomBlock(int id, int meta, CustomManager manager) {
        this(id, meta, manager, "blank");
    }

    protected CustomBlock(int id, int meta, CustomManager manager, String name) {
        super(meta);

        this.manager = manager;
        this.id = id;
        this.name = name;

        TickingTile = manager.get("TickingTile:" + meta, false);
        NeighbourChange = manager.get("NeighbourChange", false);
    }

    public static int getIdForText(String block) {
        return customBlocks.get(block);
    }

    @Override
    public int getToolTier() {
        return 0;
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
        return ItemTool.TYPE_NONE;
    }

    @Override
    public Item[] getDrops(Item item) {
        // Overriden by Core Engine, should be empty
        return new Item[0];
    }

    @Override
    public boolean canSilkTouch() {
        // Overriden by Core Engine, should be false
        return false;
    }

    @Override
    public void onNeighborChange(@NotNull BlockFace side) {
        if (NeighbourChange) {
            Block changeBlock = getSide(side);
            NativeCallback.onBlockEventNeighbourChange((int) x, (int) y, (int) z, (int) changeBlock.x, (int) changeBlock.y,
                    (int) changeBlock.z, level);
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            NativeCallback.onRandomBlockTick((int) this.x, (int) this.y, (int) this.z, id, this.getDamage(),
                    this.level);
            return type;
        } else if (type == Level.BLOCK_UPDATE_REDSTONE) {
            RedstoneUpdateEvent ev = new RedstoneUpdateEvent(this);
            getLevel().getServer().getPluginManager().callEvent(ev);
            if (ev.isCancelled())
                return type;
            return type;
        }
        return type;
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
