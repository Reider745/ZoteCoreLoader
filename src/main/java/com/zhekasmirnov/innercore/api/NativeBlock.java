package com.zhekasmirnov.innercore.api;

import com.reider745.api.CustomManager;
import com.reider745.block.BlockMethods;
import com.reider745.block.CustomBlock;
import com.zhekasmirnov.apparatus.ecs.ECS;
import com.zhekasmirnov.apparatus.ecs.core.ComponentCollection;
import com.zhekasmirnov.apparatus.ecs.core.EntityManager;
import com.zhekasmirnov.apparatus.ecs.types.ECSTags;
import com.zhekasmirnov.apparatus.ecs.types.block.BlockComponent;
import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import com.zhekasmirnov.innercore.api.runtime.other.NameTranslation;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Function;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 26.07.2017.
 */

public class NativeBlock {

    private CustomManager pointer;

    private int id;
    private String basicName = "Unknown Block";
    private ArrayList<Long> variantPtrs = new ArrayList<Long>();

    public final int entity;

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(BlockComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    protected NativeBlock(CustomManager ptr, int id, String nameId, String name) {
        this.pointer = ptr;
        this.id = id;
        this.basicName = name;

        EntityManager em = ECS.getEntityManager();
        entity = em.createEntity();
        em.extend(entity, initCC.setValues(new BlockComponent(id, nameId, name)));
    }

    public int getId() {
        return id;
    }

    public void addVariant(String name, String[] textureNames, int[] textureIds) {
        long ptr = addVariant(pointer, name, textureNames, textureIds);
        variantPtrs.add(ptr);
    }

    public void addVariant(String[] textureNames, int[] textureIds) {
        variantPtrs.add(addVariant(pointer, basicName, textureNames, textureIds));
    }

    public static NativeBlock createBlock(int id, String nameId, String name, int materialBase) {
        name = NameTranslation.fixUnicodeIfRequired("block." + nameId, name);
        return new NativeBlock(constructBlock(id, nameId, name, materialBase), id, nameId, name);
    }

    public static NativeBlock[] createLiquidBlock(int id1, String nameId1, int id2, String nameId2, String name, int materialBase, int tickDelay, boolean isRenewable) {
        name = NameTranslation.fixUnicodeIfRequired("block." + nameId1, name);
        CustomManager[] pointers = constructLiquidBlockPair(id1, nameId1, id2, nameId2, name, materialBase, tickDelay, isRenewable);
        return new NativeBlock[]{ new NativeBlock(pointers[0], id1, nameId1, name), new NativeBlock(pointers[1], id2, nameId2, name) };
    }

    /*
     * native part
     */

    public static CustomManager constructBlock(int id, String nameId, String name, int materialBaseId){
        return CustomBlock.registerBlock(nameId, id, name);
    }
    public static native CustomManager[] constructLiquidBlockPair(int id1, String nameId1, int id2, String nameId2, String name, int materialBaseId, int tickDelay, boolean isRenewable);
    public static long addVariant(CustomManager pointer, String name, String[] textureNames, int[] textureIds){
        ArrayList<String> variants = pointer.get("variants", new ArrayList<>());
        variants.add(name);
        pointer.put("variants", variants);
        return variants.size()-1;
    }

    public static int getMaterial(int id){
        return BlockMethods.getMaterial(id);
    }
    public static boolean isSolid(int id){
        return BlockMethods.isSolid(id);
    }
    public static boolean canContainLiquid(int id){
        return BlockMethods.canContainLiquid(id);
    }
    public static boolean canBeExtraBlock(int id){
        return BlockMethods.canBeExtraBlock(id);
    }
    public static float getDestroyTime(int id){
        return BlockMethods.getDestroyTime(id);
    }
    public static float getExplosionResistance(int id){
        return BlockMethods.getExplosionResistance(id);
    }
    public static float getFriction(int id){
        return BlockMethods.getFriction(id);
    }
    public static float getTranslucency(int id){
        return BlockMethods.getTranslucency(id);
    }
    public static int getLightLevel(int id){
        return BlockMethods.getLightLevel(id);
    }
    public static int getLightOpacity(int id){
        return BlockMethods.getLightOpacity(id);
    }
    public static int getRenderLayer(int id){
        return BlockMethods.getRenderLayer(id);
    }
    public static int getRenderType(int id){
        return BlockMethods.getRenderType(id);
    }
    public static int getMapColor(int id){
        return BlockMethods.getMapColor(id);
    }

    public static void setMaterial(int id, int material){
        BlockMethods.setMaterial(id, material);
    }
    public static void setMaterialBase(int id, int material){
        BlockMethods.setMaterialBase(id, material);
    }
    public static void setSoundType(int id, String sound){
        BlockMethods.setSoundType(id, sound);
    }
    public static void setSolid(int id, boolean solid){
        BlockMethods.setSolid(id, solid);
    }
    public static void setRenderAllFaces(int id, boolean val){
        BlockMethods.setRenderAllFaces(id, val);
    }
    public static void setRedstoneTileNative(int id, int data, boolean redstone){
        BlockMethods.setRedstoneTileNative(id, data, redstone);
    }
    public static void setRedstoneConnectorNative(int id, int data, boolean redstone){
        BlockMethods.setRedstoneConnectorNative(id, data, redstone);
    }
    public static void setRedstoneEmitterNative(int id, int data, boolean redstone){
        BlockMethods.setRedstoneEmitterNative(id, data, redstone);
    }
    public static void setTickingTile(int id, int data, boolean ticking){
        BlockMethods.setTickingTile(id, data, ticking);
    }
    public static void setAnimatedTile(int id, int data, boolean ticking){
        BlockMethods.setAnimatedTile(id, data, ticking);
    }
    public static void setReceivingEntityInsideEvent(int id, boolean value){
        BlockMethods.setReceivingEntityInsideEvent(id, value);
    }
    public static void setReceivingEntityStepOnEvent(int id, boolean value){
        BlockMethods.setReceivingEntityStepOnEvent(id, value);
    }
    public static void setReceivingNeighbourChangeEvent(int id, boolean value){
        BlockMethods.setReceivingNeighbourChangeEvent(id, value);
    }
    public static void setDestroyTime(int id, float val){
        BlockMethods.setDestroyTime(id, val);
    }
    public static void setExplosionResistance(int id, float val){
        BlockMethods.setExplosionResistance(id, val);
    }
    public static void setTranslucency(int id, float val){
        BlockMethods.setTranslucency(id, val);
    }
    public static void setFriction(int id, float val){
        BlockMethods.setFriction(id, val);
    }
    public static void setLightLevel(int id, int val){
        BlockMethods.setLightLevel(id, val);
    }
    public static void setLightOpacity(int id, int val){
        BlockMethods.setLightLevel(id, val);
    }
    public static void setRenderLayer(int id, int val){
        BlockMethods.setRenderLayer(id, val);
    }
    public static void setRenderType(int id, int val){
        BlockMethods.setRenderType(id, val);
    }
    public static void setBlockColorSource(int id, int val){
        BlockMethods.setBlockColorSource(id, val);
    }
    public static void setMapColor(int id, int mapColor){
        BlockMethods.setMapColor(id, mapColor);
    }
    public static void setCanContainLiquid(int id, boolean canContainLiquid){
        BlockMethods.setCanContainLiquid(id, canContainLiquid);
    }
    public static void setCanBeExtraBlock(int id, boolean canBeExtraBlock){
        BlockMethods.setCanBeExtraBlock(id, canBeExtraBlock);
    }
    public static void setShape(int id, int data, float x1, float y1, float z1, float x2, float y2, float z2){

    }


    private static HashMap<Integer, Float> blockDestroyTimes = new HashMap<>();
    private static HashMap<Integer, Float> realBlockDestroyTimes = new HashMap<>();

    public static void setDestroyTimeForId(int id, float time) {
        blockDestroyTimes.put(id, time);
    }

    public static float getDestroyTimeForId(int id) {
        if (blockDestroyTimes.containsKey(id)) {
            return blockDestroyTimes.get(id);
        }

        if (realBlockDestroyTimes.containsKey(id)) {
            return realBlockDestroyTimes.get(id);
        }

        return NativeBlock.getDestroyTime(id);
    }

    public static void setTempDestroyTimeForId(int id, float time) {
        float _time = NativeBlock.getDestroyTimeForId(id);
        NativeBlock.setDestroyTime(id, time);
        realBlockDestroyTimes.put(id, _time);
    }

    public static void onBlockDestroyStarted(int x, int y, int z, int side) {
        int id = NativeAPI.getTile(x, y, z);
        if (blockDestroyTimes.containsKey(id)) {
            setTempDestroyTimeForId(id, blockDestroyTimes.get(id));
            return;
        }

        if (realBlockDestroyTimes.containsKey(id)) {
            setDestroyTime(id, realBlockDestroyTimes.get(id));
        }
    }



    private static HashMap<Integer, Function> tickingCallbackForId = new HashMap<>();

    public static void setRandomTickCallback(int id, final Function callback) {
        NativeIdMapping.iterateMetadata(id, -1, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                setTickingTile(id, data, callback != null);
            }
        });

        if (callback != null) {
            tickingCallbackForId.put(id, callback);
        }
        else if (tickingCallbackForId.containsKey(id)) {
            tickingCallbackForId.remove(id);
        }
    }

    public static void onRandomTickCallback(int x, int y, int z, int id, int data, NativeBlockSource blockSource) {
        Function callback = tickingCallbackForId.get(id);
        if (callback != null) {
            callback.call(Compiler.assureContextForCurrentThread(), callback.getParentScope(), callback.getParentScope(), new Object[] {
                    x, y, z, id, data, blockSource
            });
        }
    }

    private static HashMap<Integer, Function> animateCallbackForId = new HashMap<>();

    public static void setAnimateTickCallback(int id, final Function callback) {
        NativeIdMapping.iterateMetadata(id, -1, new NativeIdMapping.IIdIterator() {
            @Override
            public void onIdDataIterated(int id, int data) {
                setAnimatedTile(id, data, callback != null);
            }
        });

        if (callback != null) {
            animateCallbackForId.put(id, callback);
        }
        else if (animateCallbackForId.containsKey(id)) {
            animateCallbackForId.remove(id);
        }
    }

    public static void onAnimateTickCallback(int x, int y, int z, int id, int data) {
        Function callback = animateCallbackForId.get(id);
        if (callback != null) {
            callback.call(Compiler.assureContextForCurrentThread(), callback.getParentScope(), callback.getParentScope(), new Object[] {
                    x, y, z, id, data
            });
        }
    }
}
