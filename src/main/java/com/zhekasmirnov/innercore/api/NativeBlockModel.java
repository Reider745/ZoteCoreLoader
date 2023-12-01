package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 10.08.2017.
 */

public class NativeBlockModel {

    public static long constructBlockModel(){
        return 0;
    }

    public static void clear(long model){

    }

    public static void addBoxId(long model, float x1, float y1, float z1, float x2, float y2, float z2, int id, int data){

    }

    public static void addBoxTexture(long model, float x1, float y1, float z1, float x2, float y2, float z2, String name, int index){

    }

    public static void addBoxTextureSet(long model, float x1, float y1, float z1, float x2, float y2, float z2, String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5, int id5, String tex6, int id6){

    }

    public static void addBlock(long model, int id, int data, boolean par){

    }

    public static void addMesh(long model, long mesh){

    }



    public static NativeBlockModel createBlockModel() {
        return new NativeBlockModel(constructBlockModel());
    }

    public static NativeBlockModel createTexturedBox(float x1, float y1, float z1, float x2, float y2, float z2, ScriptableObject obj) {
        NativeBlockModel model = createBlockModel();
        model.addBox(x1, y1, z1, x2, y2, z2, obj);
        return model;
    }

    public static NativeBlockModel createTexturedBlock(ScriptableObject obj) {
        return createTexturedBox(0, 0, 0, 1, 1, 1, obj);
    }

    public final long pointer;
    private NativeBlockModel(long ptr) {
        pointer = ptr;
    }

    public NativeBlockModel() {
        this(constructBlockModel());
    }

    public NativeBlockModel(NativeRenderMesh mesh) {
        this();
        addMesh(mesh);
    }

    public Object buildGuiModel(boolean resolve) {
        return null;
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, int id, int data) {
        addBoxId(pointer, x1, y1, z1, x2, y2, z2, id, data);
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5, int id5, String tex6, int id6) {
        addBoxTextureSet(pointer, x1, y1, z1, x2, y2, z2, tex1, id1, tex2, id2, tex3, id3, tex4, id4, tex5, id5, tex6, id6);
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, ScriptableObject _textureSet) {
        String[] names = new String[6];
        int[] ids = new int[6];

        if (_textureSet instanceof NativeArray) {
            Object[] textureSet = ((NativeArray) _textureSet).toArray();
            for (int i = 0; i < 6; i++) {
                Object _tex = textureSet[i > textureSet.length - 1 ? textureSet.length - 1 : i];
                if (_tex != null && _tex instanceof NativeArray) {
                    Object[] tex = ((NativeArray) _tex).toArray();
                    if (tex[0] instanceof CharSequence && tex[1] instanceof Number) {
                        names[i] = tex[0].toString();
                        ids[i] = ((Number) tex[1]).intValue();
                    }
                }
            }

            for (int i = 0; i < 6; i++) {
                if (names[i] == null) {
                    names[i] = "missing_block";
                    ids[i] = 0;
                }
            }

            for (int i = 0; i < 6; i++) {
                if (!ResourcePackManager.isValidBlockTexture(names[i], ids[i])) {
                    //Logger.debug(BlockRegistry.LOGGER_TAG, "invalid block texture will be replaced with default: " + names[i] + " " + ids[i]);
                    names[i] = "missing_block";
                    ids[i] = 0;
                }
            }
        }
        else {
            throw new IllegalArgumentException("texture set must be javascript array!");
        }

        addBox(x1, y1, z1, x2, y2, z2, names[0], ids[0], names[1], ids[1], names[2], ids[2], names[3], ids[3], names[4], ids[4], names[5], ids[5]);
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, String name, int index) {
        if (!ResourcePackManager.isValidBlockTexture(name, index)) {
            name = "missing_texture";
            index = 0;
        }
        addBoxTexture(pointer, x1, y1, z1, x2, y2, z2, name, index);
    }

    public void addBlock(int id, int data, boolean par) {
        addBlock(pointer, id, data, par);
    }

    public void addBlock(int id, int data) {
        addBlock(pointer, id, data, false);
    }

    public void addBlock(int id) {
        addBlock(pointer, id, 0, false);
    }

    public void addMesh(NativeRenderMesh mesh) {
        addMesh(pointer, mesh != null ? mesh.getPtr() : 0);
    }

    public void clear() {
        clear(pointer);
    }
}
