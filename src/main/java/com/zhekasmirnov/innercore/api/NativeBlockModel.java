package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.mod.ui.GuiBlockModel;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 10.08.2017.
 */

public class NativeBlockModel {

    public static long constructBlockModel() {
        InnerCoreServer.useClientMethod("NativeBlockModel.constructBlockModel()");
        return 0;
    }

    public static void clear(long model) {
        InnerCoreServer.useClientMethod("NativeBlockModel.clear(model)");
    }

    public static void addBoxId(long model, float x1, float y1, float z1, float x2, float y2, float z2, int id,
            int data) {
        InnerCoreServer.useClientMethod("NativeBlockModel.addBoxId(model, x1, y1, z1, x2, y2, z2, id, data)");
    }

    public static void addBoxTexture(long model, float x1, float y1, float z1, float x2, float y2, float z2,
            String name, int index) {
        InnerCoreServer.useClientMethod("NativeBlockModel.addBoxTexture(model, x1, y1, z1, x2, y2, z2, name, index)");
    }

    public static void addBoxTextureSet(long model, float x1, float y1, float z1, float x2, float y2, float z2,
            String tex1, int id1, String tex2, int id2, String tex3, int id3, String tex4, int id4, String tex5,
            int id5, String tex6, int id6) {
        InnerCoreServer.useClientMethod(
                "NativeBlockModel.addBoxTextureSet(model, x1, y1, z1, x2, y2, z2, tex1, id1, tex2, id2, tex3, id3, tex4, id4, tex5, id5, tex6, id6)");
    }

    public static void addBlock(long model, int id, int data, boolean par) {
        InnerCoreServer.useClientMethod("NativeBlockModel.addBlock(model, id, data, par)");
    }

    public static void addMesh(long model, long mesh) {
        InnerCoreServer.useClientMethod("NativeBlockModel.addMesh(model, mesh)");
    }

    public static NativeBlockModel createBlockModel() {
        return new NativeBlockModel(0);
    }

    public static NativeBlockModel createTexturedBox(float x1, float y1, float z1, float x2, float y2, float z2,
            ScriptableObject obj) {
        return new NativeBlockModel(0);
    }

    public static NativeBlockModel createTexturedBlock(ScriptableObject obj) {
        return new NativeBlockModel(0);
    }

    public final long pointer = 0;
    public final GuiBlockModel.Builder guiModelBuilder = new GuiBlockModel.Builder();

    private NativeBlockModel(long ptr) {
    }

    public NativeBlockModel() {
    }

    public NativeBlockModel(NativeRenderMesh mesh) {
    }

    public GuiBlockModel buildGuiModel(boolean resolve) {
        return guiModelBuilder.build(resolve);
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, int id, int data) {
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, String tex1, int id1, String tex2,
            int id2, String tex3, int id3, String tex4, int id4, String tex5, int id5, String tex6, int id6) {
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, ScriptableObject _textureSet) {
    }

    public void addBox(float x1, float y1, float z1, float x2, float y2, float z2, String name, int index) {
    }

    public void addBlock(int id, int data, boolean par) {
    }

    public void addBlock(int id, int data) {
    }

    public void addBlock(int id) {
    }

    public void addMesh(NativeRenderMesh mesh) {
    }

    public void clear() {
    }
}
