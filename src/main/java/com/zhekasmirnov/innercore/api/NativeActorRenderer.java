package com.zhekasmirnov.innercore.api;

import java.util.HashMap;
import java.util.Map;

public class NativeActorRenderer {
    private static final Map<String, Integer> templateNameToId = new HashMap<>();

    static {
        templateNameToId.put("helmet", 0);
        templateNameToId.put("chestplate", 1);
        templateNameToId.put("leggings", 2);
        templateNameToId.put("boots", 3);
    }

    public final long pointer;
    private final NativeShaderUniformSet uniformSet;

    private final Map<String, ModelPart> parts = new HashMap<>();

    public NativeActorRenderer(long pointer) {
        this.pointer = pointer;
        long uniformSetPtr = nativeGetUniformSet(pointer);
        uniformSet = uniformSetPtr != 0 ? new NativeShaderUniformSet(uniformSetPtr) : null;
    }

    public NativeActorRenderer() {
        this.pointer = nativeConstruct();
        long uniformSetPtr = nativeGetUniformSet(pointer);
        uniformSet = uniformSetPtr != 0 ? new NativeShaderUniformSet(uniformSetPtr) : null;
        setMaterial("entity_alphatest_custom");
    }

    public NativeActorRenderer(String template) {
        Integer templateId = templateNameToId.get(template);
        pointer = templateId != null ? nativeConstructFromTemplate(templateId) : nativeConstruct();
        long uniformSetPtr = nativeGetUniformSet(pointer);
        uniformSet = uniformSetPtr != 0 ? new NativeShaderUniformSet(uniformSetPtr) : null;
        setMaterial("entity_alphatest_custom");
    }

    public NativeShaderUniformSet getUniformSet() {
        return uniformSet;
    }

    public NativeActorRenderer setTexture(String name) {
        nativeSetRendererTexture(pointer, name);
        return this;
    }

    public NativeActorRenderer setMaterial(String name) {
        nativeSetRendererMaterial(pointer, name);
        return this;
    }

    public ModelPart getPart(String name) {
        synchronized (parts) {
            ModelPart part = parts.get(name);
            if (part == null) {
                long partPointer = nativeGetPart(pointer, name);
                if (partPointer != 0) {
                    part = new ModelPart(partPointer);
                    parts.put(name, part);
                }
            }
            return part;
        }
    }

    public ModelPart addPart(String name, String parentName) {
        if (getPart(name) != null) {
            throw new IllegalArgumentException("cannot add part " + name + ", part with this name already exists");
        }
        ModelPart parent = getPart(parentName);
        if (parent == null) {
            throw new IllegalArgumentException("cannot add part " + name + ", parent part " + parentName + " does not exist");
        }
        synchronized (parts) {
            long partPointer = nativeAddPart(pointer, parent.pointer, name);
            if (partPointer != 0) {
                ModelPart part = new ModelPart(partPointer);
                parts.put(name, part);
                return part;
            }
        }
        return null;
    }

    public ModelPart addPart(String name) {
        if (getPart(name) != null) {
            throw new IllegalArgumentException("cannot add part " + name + ", part with this name already exists");
        }
        synchronized (parts) {
            long partPointer = nativeAddRootPart(pointer, name);
            if (partPointer != 0) {
                ModelPart part = new ModelPart(partPointer);
                parts.put(name, part);
                return part;
            }
        }
        return null;
    }

    public ModelPart addPart(String name, NativeRenderMesh mesh) {
        return addPart(name).setMesh(mesh);
    }

    public ModelPart addPart(String name, String parentName, NativeRenderMesh mesh) {
        return addPart(name, parentName).setMesh(mesh);
    }

    public class ModelPart {
        public final long pointer;

        private ModelPart(long pointer) {
            this.pointer = pointer;
        }

        public NativeActorRenderer endPart() {
            return NativeActorRenderer.this;
        }

        public ModelPart setTexture(String name) {
            nativePartSetTexture(pointer, name);
            return this;
        }

        public ModelPart setMaterial(String name) {
            nativePartSetMaterial(pointer, name);
            return this;
        }

        public ModelPart setTextureSize(float width, float height) {
            nativePartSetTextureSize(pointer, width, height);
            return this;
        }

        public ModelPart setOffset(float x, float y, float z) {
            nativePartSetOffset(pointer, x, y, z);
            return this;
        }

        public ModelPart setRotation(float x, float y, float z) {
            nativePartSetRotation(pointer, x, y, z);
            return this;
        }

        public ModelPart setPivot(float x, float y, float z) {
            nativePartSetPivot(pointer, x, y, z);
            return this;
        }

        public ModelPart setMirrored(boolean mirrored) {
            nativePartSetMirrored(pointer, mirrored);
            return this;
        }

        public ModelPart addBox(float x, float y, float z, float xs, float ys, float zs, float inflate, float u, float v) {
            nativePartAddBox(pointer, x, y, z, xs, ys, zs, inflate, u, v);
            return this;
        }

        public ModelPart setMesh(NativeRenderMesh renderMesh) {
            nativePartSetMesh(pointer, renderMesh != null ? renderMesh.getPtr() : 0);
            return this;
        }

        public ModelPart clear() {
            nativePartClear(pointer);
            return this;
        }
    }


    private static native long nativeConstruct();
    private static native long nativeGetUniformSet(long pointer);
    private static native long nativeConstructFromTemplate(int template);
    private static native void nativeSetRendererTexture(long pointer, String name);
    private static native void nativeSetRendererMaterial(long pointer, String name);

    private static native long nativeGetPart(long pointer, String name);
    private static native long nativeAddRootPart(long pointer, String name);
    private static native long nativeAddPart(long pointer, long parent, String name);

    private static native void nativePartClear(long part);
    private static native void nativePartAddBox(long part, float x, float y, float z, float xs, float ys, float zs, float inflate, float u, float v);
    private static native void nativePartSetMesh(long part, long mesh);
    private static native void nativePartSetTexture(long part, String name);
    private static native void nativePartSetMaterial(long part, String name);
    private static native void nativePartSetTextureSize(long part, float width, float height);
    private static native void nativePartSetOffset(long part, float x, float y, float z);
    private static native void nativePartSetRotation(long part, float x, float y, float z);
    private static native void nativePartSetPivot(long part, float x, float y, float z);
    private static native void nativePartSetMirrored(long part, boolean mirrored);
}
