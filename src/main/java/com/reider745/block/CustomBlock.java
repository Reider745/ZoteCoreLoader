package com.reider745.block;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockIce;
import cn.nukkit.block.BlockLiquid;
import cn.nukkit.block.BlockSolidMeta;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.utils.BlockColor;
import com.reider745.api.CustomManager;
import com.reider745.api.ReflectHelper;
import com.reider745.hooks.ServerHooks;
import com.reider745.hooks.block.BlockPalette;
import com.reider745.hooks.block.BlocksHooks;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.unlimited.BlockShape;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.math3.stat.inference.BinomialTest;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomBlock extends BlockSolidMeta implements RandomTick {
    public static Int2ObjectMap<CustomManager> blocks = new Int2ObjectOpenHashMap<>();

    public static void registerBlock(int id, Block block) {
        registerBlock(id, 0, block);
    }

    public static void registerBlock(int id, int data, Block block) {
        Block.list[id] = block.getClass();
        int fullId = (id << DATA_BITS) | data;

        BlocksHooks.registerFullId(fullId, block);
        Block.hasMeta[id] = true;

        Block.solid[id] = block.isSolid();
        Block.transparent[id] = block.isTransparent();
        diffusesSkyLight[id] = block.diffusesSkyLight();
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
    public static List<Integer> getStates(CustomManager manager) {
        return (ArrayList<Integer>) manager.get("states", new ArrayList<>()).clone();
    }

    public static int getVariantCount(CustomManager manager) {
        final List<Integer> states = getStates(manager);
        int variants = 1;
        for(int id : states)
            variants *= BlockPalette.getCapacityForId(id);
        return variants;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> getVariants(CustomManager manager) {
        final ArrayList<String> variants = (ArrayList<String>) manager.get("variants", new ArrayList<>()).clone();
        final int count = getVariantCount(manager);
        if (variants.size() >= count)
            return variants;

        int size = count - variants.size();
        String name = variants.get(0);
        for (int i = 0; i < size; i++)
            variants.add(name);

        return variants;
    }

    public static HashMap<String, Integer> customBlocks = new HashMap<>();

    public static CustomManager registerBlock(String textId, int id, String name, Class<?> item) {
        textId = textId.contains(":") ? textId : "block_" + textId;

        CustomManager manager = new CustomManager(id, item, "block", textId);
        manager.put("name", name);
        manager.put("states", BlockMethods.getStatesForId(id));

        blocks.put(id, manager);
        customBlocks.put(textId, id);

        CustomManager.put(id, manager);
        NativeItem.constructBlock(id, manager, textId, name);
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

    private final boolean TickingTile;
    private final boolean NeighbourChange;
    private final Byte2ObjectOpenHashMap<Object> properties;

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

        properties = BlockMethods.getProperties(id);
    }

    public <T>T getOrDefault(byte property, T defaultValue) {
        return (T) properties.getOrDefault(property, defaultValue);
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
    public boolean isSolid() {
        return (boolean) getOrDefault(BlockMethods.SOLID, true);
    }

    @Override
    public double getResistance() {
        return (double) getOrDefault(BlockMethods.EXPLOSION_RESISTANCE, 1f);
    }

    @Override
    public double getHardness() {
        return (double) getOrDefault(BlockMethods.DESTROY_TIME, 1f);
    }

    @Override
    public double getFrictionFactor() {
        return (double) getOrDefault(BlockMethods.FRICTION, 0.6f);
    }

    @Override
    public int getLightLevel() {
        return (int) getOrDefault(BlockMethods.LIGHT_LEVEL, Block.light[id]);
    }

    @Override
    public boolean isTransparent() {
        return ((float) getOrDefault(BlockMethods.TRANSLUCENCY, 0f))  >= 1f;
    }

    @Override
    public boolean canBeFlowedInto() {
        return (boolean) getOrDefault(BlockMethods.CAN_CONTAIN_LIQUID, false);
    }

    @Override
    public int getBurnChance() {
        return getOrDefault(BlockMethods.BURN_FLAME, 0);
    }

    @Override
    public int getBurnAbility() {
        return getOrDefault(BlockMethods.BURN_CHANCE, 0);
    }

    @Override
    public BlockColor getColor() {
        return getOrDefault(BlockMethods.COLOR, BlockColor.AIR_BLOCK_COLOR);
    }

    @Override
    public double getMinX() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return x + shape.x1;
        }
        return (shape = manager.get("shape")) != null ? x + shape.x1 : x;
    }

    @Override
    public double getMinY() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return y + shape.y1;
        }
        return (shape = manager.get("shape")) != null ? y + shape.y1 : y;
    }

    @Override
    public double getMinZ() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return z + shape.z1;
        }
        return (shape = manager.get("shape")) != null ? z + shape.z1 : z;
    }

    @Override
    public double getMaxX() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return x + shape.x2;
        }
        return (shape = manager.get("shape")) != null ? x + shape.x2 : x + 1d;
    }

    @Override
    public double getMaxY() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return y + shape.y2;
        }
        return (shape = manager.get("shape")) != null ? y + shape.y2 : y + 1d;
    }

    @Override
    public double getMaxZ() {
        BlockShape shape = manager.get("shape" + getDamage());
        if (shape != null) {
            return z + shape.z2;
        }
        return (shape = manager.get("shape")) != null ? z + shape.z2 : z + 1d;
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
            NativeCallback.onBlockEventNeighbourChange((int) x, (int) y, (int) z, (int) changeBlock.x,
                    (int) changeBlock.y, (int) changeBlock.z, level);
        }
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            NativeCallback.onRandomBlockTick((int) this.x, (int) this.y, (int) this.z, id, this.getDamage(),
                    this.level);
            return type;
        } else if (type == Level.BLOCK_UPDATE_REDSTONE || type == Level.BLOCK_UPDATE_NORMAL) {
            RedstoneSystem.onChangeRedstone(this);
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
