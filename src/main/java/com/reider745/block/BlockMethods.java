package com.reider745.block;

import cn.nukkit.block.Block;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class BlockMethods {
    private static final Int2ObjectOpenHashMap<Byte2ObjectOpenHashMap<Object>> id2typeMap = new Int2ObjectOpenHashMap<>();
    private static final byte SOLID = 0;
    private static final byte CAN_CONTAIN_LIQUID = 1;
    private static final byte DESTROY_TIME = 2;
    private static final byte TRANSLUCENCY = 3;
    private static final byte EXPLOSION_RESISTANCE = 4;
    private static final byte FRICTION = 5;
    private static final byte LIGHT_LEVEL = 6;

    private static Byte2ObjectOpenHashMap<Object> assureId2Type(int id) {
        Byte2ObjectOpenHashMap<Object> type = id2typeMap.get(id);
        if (type == null) {
            type = new Byte2ObjectOpenHashMap<Object>();
            id2typeMap.put(id, type);
        }
        return type;
    }

    private static Object getOrDefault(int id, byte property, Object defaultValue) {
        Byte2ObjectOpenHashMap<Object> type = id2typeMap.get(id);
        return type != null ? type.getOrDefault(property, defaultValue) : defaultValue;
    }

    public static int getMaterial(int id) {
        return 0;
    }

    public static boolean isSolid(int id) {
        return (boolean) getOrDefault(id, SOLID, true);
    }

    public static boolean canContainLiquid(int id) {
        return (boolean) getOrDefault(id, CAN_CONTAIN_LIQUID, false);
    }

    public static boolean canBeExtraBlock(int id) {
        return false;
    }

    public static float getDestroyTime(int id) {
        return (float) getOrDefault(id, DESTROY_TIME, 1f);
    }

    public static float getExplosionResistance(int id) {
        return (float) getOrDefault(id, EXPLOSION_RESISTANCE, 1f);
    }

    public static float getFriction(int id) {
        return (float) getOrDefault(id, FRICTION, 0.6f);
    }

    public static float getTranslucency(int id) {
        return (float) getOrDefault(id, TRANSLUCENCY, 0f);
    }

    public static int getLightLevel(int id) {
        return (int) getOrDefault(id, LIGHT_LEVEL, Block.light[id]);
    }

    public static int getMapColor(int id) {
        return 0;
    }

    public static void setMaterial(int id, int material) {
    }

    public static void setMaterialBase(int id, int material) {
    }

    public static void setSoundType(int id, String sound) {
    }

    public static void setSolid(int id, boolean solid) {
        assureId2Type(id).put(SOLID, Boolean.valueOf(solid));
    }

    public static void setRenderAllFaces(int id, boolean val) {
    }

    public static void setRedstoneTileNative(int id, int data, boolean redstone) {
    }

    public static void setRedstoneConnectorNative(int id, int data, boolean redstone) {
    }

    public static void setRedstoneEmitterNative(int id, int data, boolean redstone) {
    }

    public static void setTickingTile(int id, int data, boolean ticking) {
        CustomBlock.getBlockManager(id).put("TickingTile:" + data, ticking);
    }

    public static void setAnimatedTile(int id, int data, boolean ticking) {
    }

    public static void setReceivingEntityInsideEvent(int id, boolean value) {
    }

    public static void setReceivingEntityStepOnEvent(int id, boolean value) {
    }

    public static void setReceivingNeighbourChangeEvent(int id, boolean value) {
        CustomBlock.getBlockManager(id).put("NeighbourChange", value);
    }

    public static void setDestroyTime(int id, float val) {
        assureId2Type(id).put(DESTROY_TIME, Float.valueOf(val));
    }

    public static void setExplosionResistance(int id, float val) {
        assureId2Type(id).put(EXPLOSION_RESISTANCE, Float.valueOf(val));
    }

    public static void setTranslucency(int id, float val) {
        assureId2Type(id).put(TRANSLUCENCY, Float.valueOf(val));
    }

    public static void setFriction(int id, float val) {
        assureId2Type(id).put(FRICTION, Float.valueOf(val));
    }

    public static void setLightLevel(int id, int val) {
        assureId2Type(id).put(LIGHT_LEVEL, Integer.valueOf(val));
    }

    public static void setBlockColorSource(int id, int val) {
    }

    public static void setMapColor(int id, int mapColor) {
    }

    public static void setCanContainLiquid(int id, boolean canContainLiquid) {
        assureId2Type(id).put(CAN_CONTAIN_LIQUID, Boolean.valueOf(canContainLiquid));
    }

    public static void setCanBeExtraBlock(int id, boolean canBeExtraBlock) {
    }
}
