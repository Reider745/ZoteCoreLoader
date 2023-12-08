package com.reider745.block;

import cn.nukkit.block.Block;
import com.reider745.api.CustomManager;

import java.util.HashMap;

public class BlockMethods {
    public static class SpecialType {
        private boolean solid;
        private float destroyTime, translucency, explosion_resistance, friction;
        private int light_opacity, light_level;

        public SpecialType(){
            this.solid = true;
            this.destroyTime = 1;
            this.translucency = 1;
            this.explosion_resistance = 1;
            this.light_level = 0;
            this.light_opacity = 0;
        }
    }

    public static HashMap<Integer, SpecialType> customs = new HashMap<>();

    private static CustomManager getCustomManager(int id){
        return CustomBlock.getBlockManager(id);
    }

    public static int getMaterial(int id){
        return 0;
    }
    public static boolean isSolid(int id){
        SpecialType type = customs.get(id);
        if(type != null)
            return type.solid;
        return true;
    }
    public static boolean canContainLiquid(int id){
        return false;
    }
    public static boolean canBeExtraBlock(int id){
        return false;
    }
    public static float getDestroyTime(int id){
        SpecialType type = customs.get(id);
        if(type != null)
            return type.destroyTime;
        return 1;
    }
    public static float getExplosionResistance(int id){
        SpecialType type = customs.get(id);
        if(type != null)
            return type.explosion_resistance;
        return 1;
    }
    public static float getFriction(int id){
        SpecialType type = customs.get(id);
        if(type != null)
            return type.friction;
        return .6f;
    }
    public static float getTranslucency(int id){
        return 0;
    }
    public static int getLightLevel(int id){
        SpecialType type = customs.get(id);
        if(type != null)
            return type.light_level;
        return Block.light[id];
    }
    public static int getMapColor(int id){
        return 0;
    }

    public static void setMaterial(int id, int material){

    }
    public static void setMaterialBase(int id, int material){

    }
    public static void setSoundType(int id, String sound){

    }

    public static void setSolid(int id, boolean solid){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.solid = solid;
        customs.put(id, type);
    }
    public static void setRenderAllFaces(int id, boolean val){

    }
    public static void setRedstoneTileNative(int id, int data, boolean redstone){

    }
    public static void setRedstoneConnectorNative(int id, int data, boolean redstone){

    }
    public static void setRedstoneEmitterNative(int id, int data, boolean redstone){

    }
    public static void setTickingTile(int id, int data, boolean ticking){
        CustomBlock.getBlockManager(id).put("TickingTile:"+data, ticking);
    }
    public static void setAnimatedTile(int id, int data, boolean ticking){

    }
    public static void setReceivingEntityInsideEvent(int id, boolean value){

    }
    public static void setReceivingEntityStepOnEvent(int id, boolean value){

    }
    public static void setReceivingNeighbourChangeEvent(int id, boolean value){
        CustomBlock.getBlockManager(id).put("NeighbourChange", value);
    }
    public static void setDestroyTime(int id, float val){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.destroyTime = val;
        customs.put(id, type);
    }
    public static void setExplosionResistance(int id, float val){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.explosion_resistance = val;
        customs.put(id, type);
    }
    public static void setTranslucency(int id, float val){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.translucency = val;
        customs.put(id, type);
    }
    public static void setFriction(int id, float val){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.friction = val;
        customs.put(id, type);
    }
    public static void setLightLevel(int id, int val){
        SpecialType type = customs.getOrDefault(id, new SpecialType());
        type.light_level = val;
        customs.put(id, type);
    }
    public static void setBlockColorSource(int id, int val){

    }
    public static void setMapColor(int id, int mapColor){

    }
    public static void setCanContainLiquid(int id, boolean canContainLiquid){

    }
    public static void setCanBeExtraBlock(int id, boolean canBeExtraBlock){

    }
}
