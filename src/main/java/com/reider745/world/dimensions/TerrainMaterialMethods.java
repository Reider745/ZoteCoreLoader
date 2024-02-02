package com.reider745.world.dimensions;

import cn.nukkit.block.Block;
import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

public class TerrainMaterialMethods {
    private static final PointersStorage<TerrainMaterialDescription> pointers = new PointersStorage<>("terrain_material", new PointerGenFastest(), false);
    public static final long ptr_main = new TerrainMaterialDescription().ptr;

    public static class TerrainMaterialDescription {
        public int baseId, baseData;
        public int coverId, coverData;
        public int surfaceWidth, surfaceId, surfaceData;
        public int fillingWidth, fillingId, fillingData;

        public float diffuse;
        public boolean isComplex = false;

        public final long ptr;

        public TerrainMaterialDescription(){
            ptr = pointers.addPointer(this);
        }

        public void prepareMaterial(){
            isComplex = coverId != 0 || surfaceId != 0 || fillingId != 0;
        }

        public Block getBlock(int depth, int maxDepth) {
            if (isComplex) {
                if (depth == 0) {
                    return Block.get(coverId, coverData);
                }
                if (depth < surfaceWidth) {
                    return Block.get(surfaceId, surfaceData);
                }
                if (maxDepth - depth < fillingWidth) {
                    return Block.get(fillingId, fillingData);
                }
            }
            return Block.get(baseId, baseData);
        }
    }


    public static TerrainMaterialDescription newMaterial(){
        return new TerrainMaterialDescription();
    }

    public static void nativeSetBase(long pointer, int id, int data){
        TerrainMaterialDescription description = pointers.get(pointer);
        description.baseId = id;
        description.baseData = data;
    }
    public static void nativeSetCover(long pointer, int id, int data){
        TerrainMaterialDescription description = pointers.get(pointer);
        description.coverId = id;
        description.coverData = data;
        description.prepareMaterial();
    }
    public static void nativeSetSurface(long pointer, int width, int id, int data){
        TerrainMaterialDescription description = pointers.get(pointer);
        description.surfaceWidth = width;
        description.surfaceId = id;
        description.surfaceData = data;
        description.prepareMaterial();
    }
    public static void nativeSetFilling(long pointer, int width, int id, int data){
        TerrainMaterialDescription description = pointers.get(pointer);
        description.fillingWidth = width;
        description.fillingId = id;
        description.fillingData = data;
        description.prepareMaterial();
    }
    public static void nativeSetDiffuse(long pointer, float diffuse){
        pointers.get(pointer).diffuse = diffuse;
    }
}
