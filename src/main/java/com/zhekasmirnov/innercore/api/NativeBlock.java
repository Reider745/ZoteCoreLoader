package com.zhekasmirnov.innercore.api;

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

    private long pointer;

    private int id;
    private String basicName = "Unknown Block";
    private ArrayList<Long> variantPtrs = new ArrayList<Long>();

    public final int entity;

    private static final ComponentCollection initCC = new ComponentCollection()
            .setTypes(BlockComponent.COMPONENT_ID, ECSTags.CONTENT_ID);

    protected NativeBlock(long ptr, int id, String nameId, String name) {
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
        long[] pointers = constructLiquidBlockPair(id1, nameId1, id2, nameId2, name, materialBase, tickDelay, isRenewable);
        return new NativeBlock[]{ new NativeBlock(pointers[0], id1, nameId1, name), new NativeBlock(pointers[1], id2, nameId2, name) };
    }

    /*
     * native part
     */

    public static native long constructBlock(int id, String nameId, String name, int materialBaseId);
    public static native long[] constructLiquidBlockPair(int id1, String nameId1, int id2, String nameId2, String name, int materialBaseId, int tickDelay, boolean isRenewable);
    public static native long addVariant(long pointer, String name, String[] textureNames, int[] textureIds);

    public static native int getMaterial(int id);
    public static native boolean isSolid(int id);
    public static native boolean canContainLiquid(int id);
    public static native boolean canBeExtraBlock(int id);
    public static native float getDestroyTime(int id);
    public static native float getExplosionResistance(int id);
    public static native float getFriction(int id);
    public static native float getTranslucency(int id);
    public static native int getLightLevel(int id);
    public static native int getLightOpacity(int id);
    public static native int getRenderLayer(int id);
    public static native int getRenderType(int id);
    public static native int getMapColor(int id);

    public static native void setMaterial(int id, int material);
    public static native void setMaterialBase(int id, int material);
    public static native void setSoundType(int id, String sound);
    public static native void setSolid(int id, boolean solid);
    public static native void setRenderAllFaces(int id, boolean val);
    public static native void setRedstoneTileNative(int id, int data, boolean redstone);
    public static native void setRedstoneConnectorNative(int id, int data, boolean redstone);
    public static native void setRedstoneEmitterNative(int id, int data, boolean redstone);
    public static native void setTickingTile(int id, int data, boolean ticking);
    public static native void setAnimatedTile(int id, int data, boolean ticking);
    public static native void setReceivingEntityInsideEvent(int id, boolean value);
    public static native void setReceivingEntityStepOnEvent(int id, boolean value);
    public static native void setReceivingNeighbourChangeEvent(int id, boolean value);
    public static native void setDestroyTime(int id, float val);
    public static native void setExplosionResistance(int id, float val);
    public static native void setTranslucency(int id, float val);
    public static native void setFriction(int id, float val);
    public static native void setLightLevel(int id, int val);
    public static native void setLightOpacity(int id, int val);
    public static native void setRenderLayer(int id, int val);
    public static native void setRenderType(int id, int val);
    public static native void setBlockColorSource(int id, int val);
    public static native void setMapColor(int id, int mapColor);
    public static native void setCanContainLiquid(int id, boolean canContainLiquid);
    public static native void setCanBeExtraBlock(int id, boolean canBeExtraBlock);
    public static native void setShape(int id, int data, float x1, float y1, float z1, float x2, float y2, float z2);


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
