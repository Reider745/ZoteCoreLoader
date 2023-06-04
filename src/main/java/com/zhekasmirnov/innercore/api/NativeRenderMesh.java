package com.zhekasmirnov.innercore.api;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import org.mozilla.javascript.Scriptable;

import java.io.File;

/**
 * Created by zheka on 23.02.2018.
 */

public class NativeRenderMesh {
    private final long ptr;

    public NativeRenderMesh() {
        this.ptr = nativeConstructNew();
    }

    public long getPtr() {
        return ptr;
    }

    public void rebuild() {
        nativeRebuild(ptr);
        invalidate();
    }

    public void clear() {
        nativeClear(ptr);
        invalidate();
    }
    
    public void invalidate() {
        nativeInvalidate(ptr);
    }


    public void setLightParams(float min, float max, float smooth) {
        nativeSetLightParams(ptr, min, max, smooth);
    }

    public void setLightDir(float x, float y, float z) {
        nativeSetLightDir(ptr, x, y, z);
    }

    public void setLightIgnore(boolean block, boolean sky) {
        nativeSetLightIgnore(ptr, block, sky);
    }

    public void setLightPos(int x, int y, int z) {
        nativeSetLightPos(ptr, x, y, z);
    }

    public void setFoliageTinted() {
        setFoliageTinted(0);
    }

    public void setFoliageTinted(int data) {
        nativeSetTintSource(ptr, true, false, false, data);
    }

    public void setGrassTinted() {
        nativeSetTintSource(ptr, false, true, false, 0);
    }

    public void setWaterTinted() {
        nativeSetTintSource(ptr, false, false, true, 0);
    }

    public void setNoTint() {
        nativeSetTintSource(ptr, false, false, false, 0);
    }


    public void addVertex(float x, float y, float z, float u, float v) {
        nativeAddVertex(ptr, x, y, z, u, v);
    }

    public void addVertex(float x, float y, float z) {
        nativeAddVertex(ptr, x, y, z, .5f, .5f);
    }

    public void addMesh(NativeRenderMesh mesh, float tx, float ty, float tz, float sx, float sy, float sz) {
        if (mesh != null) {
            nativeAddMeshToMesh(ptr, mesh.ptr, tx, ty, tz, sx, sy, sz);
        }
    }

    public void addMesh(NativeRenderMesh mesh, float x, float y, float z) {
        addMesh(mesh, x, y, z, 1, 1, 1);
    }

    public void addMesh(NativeRenderMesh mesh) {
        addMesh(mesh, 0, 0, 0, 1, 1, 1);
    }

    public void setColor(float r, float g, float b, float a) {
        nativeSetColor(ptr, r, g, b, a);
    }

    public void setColor(float r, float g, float b) {
        setColor(r, g, b, 1);
    }
    
    public void setNormal(float x, float y, float z) {
        nativeSetNormal(ptr, x, y, z);
    }

    public void resetColor() {
        nativeResetColor(ptr);
    }


    private String mBlockTextureName = null;
    private int mBlockTextureId = 0;

    public void setBlockTexture(String name, int id) {
        if (ResourcePackManager.isValidBlockTexture(name, id)) {
            nativeSetTargetBlockTexture(ptr, name, id);
            mBlockTextureName = name;
            mBlockTextureId = id;
        }
        else  {
            nativeSetTargetBlockTexture(ptr, "missing_block", 0);
            mBlockTextureName = "missing_block";
            mBlockTextureId = 0;
        }
    }

    public void resetTexture() {
        nativeResetTargetTexture(ptr);
        mBlockTextureName = null;
        mBlockTextureId = 0;
    }


    public void translate(float x, float y, float z) {
        nativeTranslate(ptr, x, y, z);
        invalidate();
    }

    public void scale(float x, float y, float z) {
        nativeScale(ptr, x, y, z);
        invalidate();
    }
    
    public void rotate(float x, float y, float z, float rx, float ry, float rz) {
        nativeRotate(ptr, x, y, z, rx, ry, rz);
        invalidate();
    }
    
    public void rotate(float rx, float ry, float rz) {
        rotate(0, 0, 0, rx, ry, rz);
    }

    public void fitIn(float x1, float y1, float z1, float x2, float y2, float z2, boolean keepRatio) {
        nativeFitIn(ptr, x1, y1, z1, x2, y2, z2, keepRatio);
        invalidate();
    }

    public void fitIn(float x1, float y1, float z1, float x2, float y2, float z2) {
        fitIn(x1, y1, z1, x2, y2, z2, true);
    }

    public NativeRenderMesh clone() {
        NativeRenderMesh mesh = new NativeRenderMesh();
        if (mBlockTextureName != null) {
            mesh.setBlockTexture(mBlockTextureName, mBlockTextureId);
        }
        mesh.addMesh(this);
        return mesh;
    }

    public void importFromFile(String path, String type, Scriptable _params) {

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
        int dataSize = nativeGetMeshDataSize(ptr);
        ReadOnlyVertexData data = new ReadOnlyVertexData(dataSize);
        nativeGetMeshData(ptr, data.indices, data.vertices, data.colors, data.uvs);
        return data;
    }

    public Object newGuiRenderMesh() {
       return null;
    }


    private static long nativeConstructNew(){
        return 0;
    }
    private static void nativeClear(long ptr){

    }
    private static void nativeInvalidate(long ptr){

    }
    private static void nativeRebuild(long ptr){

    }
    private static int nativeGetMeshDataSize(long ptr){
        return 0;
    }
    private static void nativeGetMeshData(long ptr, short[] indices, float[] vertices, float[] colors, float[] uvs){

    }

    private static void nativeSetLightParams(long ptr, float min, float max, float smooth){

    }
    private static void nativeSetLightIgnore(long ptr, boolean block, boolean sky){

    }
    private static void nativeSetLightDir(long ptr, float x, float y, float z){

    }
    private static void nativeSetLightPos(long ptr, int x, int y, int z){

    }
    private static void nativeSetTintSource(long ptr, boolean foliage, boolean grass, boolean water, int foliageData){

    }

    private static void nativeAddVertex(long ptr, float x, float y, float z, float u, float v){

    }
    private static void nativeAddMeshToMesh(long ptr, long mesh, float tx, float ty, float tz, float sx, float sy, float sz){

    }
    private static void nativeSetColor(long ptr, float r, float g, float b, float a){

    }
    private static void nativeSetNormal(long ptr, float x, float y, float z){

    }
    private static void nativeResetColor(long ptr){

    }
    private static void nativeSetTargetBlockTexture(long ptr, String name, int id){

    }
    private static void nativeResetTargetTexture(long ptr){

    }

    private static void nativeTranslate(long ptr, float x, float y, float z){

    }
    private static void nativeScale(long ptr, float x, float y, float z){

    }
    private static void nativeRotate(long ptr, float x, float y, float z, float rx, float ry, float rz){

    }
    private static void nativeFitIn(long ptr, float x1, float y1, float z1, float x2, float y2, float z2, boolean keepRatio){

    }

    private static void nativeImportFromFile(long ptr, String name, String type, float tx, float ty, float tz, float sx, float sy, float sz, boolean invertV){

    }
}
