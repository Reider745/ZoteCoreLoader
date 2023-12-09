package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.mod.ui.GuiRenderMesh;
import org.mozilla.javascript.Scriptable;

/**
 * Created by zheka on 23.02.2018.
 */

public class NativeRenderMesh {
    public long getPtr() {
        return 0;
    }

    public void rebuild() {
        invalidate();
    }

    public void clear() {
        invalidate();
    }

    public void invalidate() {
    }

    public void setLightParams(float min, float max, float smooth) {
    }

    public void setLightDir(float x, float y, float z) {
    }

    public void setLightIgnore(boolean block, boolean sky) {
    }

    public void setLightPos(int x, int y, int z) {
    }

    public void setFoliageTinted() {
        setFoliageTinted(0);
    }

    public void setFoliageTinted(int data) {
    }

    public void setGrassTinted() {
    }

    public void setWaterTinted() {
    }

    public void setNoTint() {
    }

    public void addVertex(float x, float y, float z, float u, float v) {
    }

    public void addVertex(float x, float y, float z) {
    }

    public void addMesh(NativeRenderMesh mesh, float tx, float ty, float tz, float sx, float sy, float sz) {
    }

    public void addMesh(NativeRenderMesh mesh, float x, float y, float z) {
        addMesh(mesh, x, y, z, 1, 1, 1);
    }

    public void addMesh(NativeRenderMesh mesh) {
        addMesh(mesh, 0, 0, 0, 1, 1, 1);
    }

    public void setColor(float r, float g, float b, float a) {
    }

    public void setColor(float r, float g, float b) {
        setColor(r, g, b, 1);
    }

    public void setNormal(float x, float y, float z) {
    }

    public void resetColor() {
    }

    public void setBlockTexture(String name, int id) {
    }

    public void resetTexture() {
    }

    public void translate(float x, float y, float z) {
        invalidate();
    }

    public void scale(float x, float y, float z) {
        invalidate();
    }

    public void rotate(float x, float y, float z, float rx, float ry, float rz) {
        invalidate();
    }

    public void rotate(float rx, float ry, float rz) {
        rotate(0, 0, 0, rx, ry, rz);
    }

    public void fitIn(float x1, float y1, float z1, float x2, float y2, float z2, boolean keepRatio) {
        invalidate();
    }

    public void fitIn(float x1, float y1, float z1, float x2, float y2, float z2) {
        fitIn(x1, y1, z1, x2, y2, z2, true);
    }

    public NativeRenderMesh clone() {
        return new NativeRenderMesh();
    }

    public void importFromFile(String path, String type, Scriptable _params) {
        switch (type.toLowerCase()) {
            case "obj":
                break;
            default:
                throw new IllegalArgumentException("Cannot import RenderMesh, invalid file type given: " + type);
        }
    }

    public class ReadOnlyVertexData {
        public final int dataSize;
        public final short[] indices;
        public final float[] vertices;
        public final float[] colors;
        public final float[] uvs;

        private ReadOnlyVertexData(int dataSize) {
            this.dataSize = dataSize;
            this.indices = new short[dataSize];
            this.vertices = new float[dataSize * 3];
            this.colors = new float[dataSize * 4];
            this.uvs = new float[dataSize * 2];
        }
    }

    public ReadOnlyVertexData getReadOnlyVertexData() {
        return new ReadOnlyVertexData(0);
    }

    public GuiRenderMesh newGuiRenderMesh() {
        return new GuiRenderMesh();
    }
}
