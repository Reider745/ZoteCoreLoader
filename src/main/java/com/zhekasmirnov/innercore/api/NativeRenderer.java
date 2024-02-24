package com.zhekasmirnov.innercore.api;

import org.mozilla.javascript.annotations.JSStaticFunction;

import com.reider745.InnerCoreServer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 26.07.2017.
 */

@Deprecated(since = "Zote")
public class NativeRenderer {

    public static class Renderer {
        protected long pointer;

        protected int id;
        protected Model model;

        public boolean isHumanoid = false;

        private Renderer() {
            this.model = null;
        }

        public Renderer(long ptr) {
            this.pointer = ptr;
            this.model = new Model(0, this);
            this.id = 0;
        }

        public Transform transform = new Transform();

        public class Transform {
            public Transform clear() {
                return this;
            }

            public Transform lock() {
                return this;
            }

            public Transform unlock() {
                return this;
            }

            public Transform matrix(float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7,
                    float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
                return this;
            }

            public Transform scale(float x, float y, float z) {
                return this;
            }

            public Transform rotate(double x, double y, double z) {
                return this;
            }

            public Transform translate(double x, double y, double z) {
                return this;
            }
        }

        public float getScale() {
            return 1.0f;
        }

        public void setScale(float scale) {
        }

        public void setSkin(String skin) {
        }

        public Model getModel() {
            return model;
        }

        public int getRenderType() {
            return id;
        }

        public long getPointer() {
            return pointer;
        }

        private boolean isReleased = false;
        private final ArrayList<FinalizeCallback> finalizeCallbacks = new ArrayList<>();

        // this must be call
        public void release() {
            if (!isReleased) {
                isReleased = true;
                for (FinalizeCallback callback : finalizeCallbacks) {
                    callback.onFinalized(this);
                }
            }
        }

        @Override
        protected void finalize() throws Throwable {
            release();
            super.finalize();
        }

        public void addFinalizeCallback(FinalizeCallback callback) {
            finalizeCallbacks.add(callback);
        }

        public void setFinalizeable(boolean finalizeable) {
            if (finalizeable) {
                rendererById.remove(id);
                weakRendererById.put(id, new WeakReference<>(this));
            } else {
                rendererById.put(id, this);
                weakRendererById.remove(id);
            }
        }

    }

    public static class SpriteRenderer extends Renderer {
        protected SpriteRenderer(long ptr) {
            super();
            this.pointer = ptr;
            this.id = getRendererId(pointer);
        }
    }

    public static class Model {
        protected Model(long ptr, Renderer renderer) {
        }

        public boolean hasPart(String name) {
            return false;
        }

        public ModelPart getPart(String name) {
            return new ModelPart(0, this);
        }

        public void reset() {
        }

        public void clearAllParts() {
        }
    }

    public static class ModelPart {
        protected ModelPart(long ptr, Model model) {
        }

        public void clear() {
            setMesh(null);
        }

        public void addBox(float x, float y, float z, float w, float h, float l, float add) {
        }

        public void addBox(float x, float y, float z, float w, float h, float l) {
        }

        // public void addColoredBox(float x, float y, float z, float w, float h, float
        // l, float add, float r, float g, float b, float a) {
        // }

        public ModelPart addPart(String name) {
            return new ModelPart(0, null);
        }

        public void setTextureOffset(int x, int y) {
        }

        public void setTextureOffset(int x, int y, boolean placeholder) {
            setTextureOffset(x, y);
        }

        public void setTextureSize(int x, int y) {
        }

        public void setTextureSize(int x, int y, boolean placeholder) {
            setTextureSize(x, y);
        }

        public void setOffset(float x, float y, float z) {
        }

        public void setRotation(float x, float y, float z) {
        }

        private NativeRenderMesh mesh = null;

        public void setMesh(NativeRenderMesh mesh) {
            this.mesh = mesh;
        }

        public NativeRenderMesh getMesh() {
            return mesh;
        }
    }

    private static HashMap<Integer, Renderer> rendererById = new HashMap<Integer, Renderer>();
    private static HashMap<Integer, WeakReference<Renderer>> weakRendererById = new HashMap<>();

    @JSStaticFunction
    public static Renderer createHumanoidRenderer(double scale) {
        Renderer renderer = new Renderer(0);
        renderer.isHumanoid = true;
        rendererById.put(renderer.getRenderType(), renderer);
        return renderer;
    }

    @JSStaticFunction
    public static Renderer createRendererWithSkin(String skin, double scale) {
        Renderer renderer = new Renderer(0);
        renderer.isHumanoid = true;
        rendererById.put(renderer.getRenderType(), renderer);
        return renderer;
    }

    @JSStaticFunction
    public static Renderer createItemSpriteRenderer(int id) {
        Renderer renderer = new SpriteRenderer(0);
        renderer.isHumanoid = false;
        rendererById.put(renderer.getRenderType(), renderer);
        return renderer;
    }

    @JSStaticFunction
    public static Renderer getRendererById(int id) {
        return rendererById.containsKey(id) ? rendererById.get(id)
                : (weakRendererById.containsKey(id) ? weakRendererById.get(id).get() : null);
    }

    public interface FinalizeCallback {
        void onFinalized(NativeRenderer.Renderer renderer);
    }

    public static class RenderPool {
        private final ArrayList<Renderer> pool = new ArrayList<>();
        private final IFactory renderFactory;

        public interface IFactory {
            Renderer newRender();
        }

        public RenderPool(IFactory factory) {
            renderFactory = factory;
        }

        public RenderPool() {
            renderFactory = new IFactory() {
                @Override
                public Renderer newRender() {
                    return createHumanoidRenderer(1);
                }
            };
        }

        public Renderer getRender() {
            synchronized (pool) {
                if (pool.size() > 0) {
                    return pool.remove(0);
                } else {
                    Renderer render = renderFactory.newRender();
                    render.addFinalizeCallback(new NativeRenderer.FinalizeCallback() {
                        @Override
                        public void onFinalized(NativeRenderer.Renderer render) {
                            synchronized (pool) {
                                NativeRenderer.Renderer newRender = new NativeRenderer.Renderer(render.getPointer());
                                newRender.setFinalizeable(true);
                                newRender.addFinalizeCallback(this);
                                newRender.isHumanoid = render.isHumanoid;
                                pool.add(newRender); // move to a new render object and pool it
                            }
                        }
                    });
                    render.setFinalizeable(true);
                    return render;
                }
            }
        }
    }

    /*
     * native part
     */

    public static long constructHumanoidRenderer(float scale) {
        InnerCoreServer.useClientMethod("NativeRenderer.constructHumanoidRenderer(scale)");
        return 0;
    }

    public static long constructHumanoidRendererWithSkin(String skin, float scale) {
        InnerCoreServer.useClientMethod("NativeRenderer.constructHumanoidRendererWithSkin(skin, scale)");
        return 0;
    }

    public static long constructSpriteRenderer(int id) {
        InnerCoreServer.useClientMethod("NativeRenderer.constructSpriteRenderer(id)");
        return 0;
    }

    public static long getModel(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.getModel(pointer)");
        return 0;
    }

    public static long getModelPart(long pointer, String name) {
        InnerCoreServer.useClientMethod("NativeRenderer.getModelPart(pointer, name)");
        return 0;
    }

    public static int getRendererId(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.getRendererId(pointer)");
        return 0;
    }

    public static void setScale(long pointer, float scale) {
        InnerCoreServer.useClientMethod("NativeRenderer.setScale(pointer, scale)");
    }

    public static void setSkin(long pointer, String skin) {
        InnerCoreServer.useClientMethod("NativeRenderer.setSkin(pointer, skin)");
    }

    public static void transformClear(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformClear(pointer)");
    }

    public static void transformLock(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformLock(pointer)");
    }

    public static void transformUnlock(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformUnlock(pointer)");
    }

    public static void transformAddTransform(long pointer, float f0, float f1, float f2, float f3, float f4, float f5,
            float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
        InnerCoreServer.useClientMethod(
                "NativeRenderer.transformAddTransform(pointer, f0, f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15)");
    }

    public static void transformScale(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformScale(pointer, x, y, z)");
    }

    public static void transformRotate(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformRotate(pointer, x, y, z)");
    }

    public static void transformTranslate(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeRenderer.transformTranslate(pointer, x, y, z)");
    }

    public static float getScale(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.getScale(pointer)");
        return 0;
    }

    public static boolean hasModelPart(long pointer, String name) {
        InnerCoreServer.useClientMethod("NativeRenderer.hasModelPart(pointer, name)");
        return false;
    }

    public static long addModelPart(long model, long pointer, String name) {
        InnerCoreServer.useClientMethod("NativeRenderer.addModelPart(model, pointer, name)");
        return 0;
    }

    public static void clearAllModelParts(long model) {
        InnerCoreServer.useClientMethod("NativeRenderer.clearAllModelParts(model)");
    }

    public static void addBox(long pointer, float x, float y, float z, float w, float h, float l, float add) {
        InnerCoreServer.useClientMethod("NativeRenderer.addBox(pointer, x, y, z, w, h, l, add)");
    }

    public static void addColoredBox(long pointer, float x, float y, float z, float w, float h, float l, float add,
            float r, float g, float b, float a) {
        InnerCoreServer.useClientMethod("NativeRenderer.addColoredBox(pointer, x, y, z, w, h, l, add, r, g, b, a)");
    }

    public static void setMesh(long pointer, long mesh) {
        InnerCoreServer.useClientMethod("NativeRenderer.setMesh(pointer, mesh)");
    }

    public static void setOffset(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeRenderer.setOffset(pointer, x, y, z)");
    }

    public static void setRotation(long pointer, float x, float y, float z) {
        InnerCoreServer.useClientMethod("NativeRenderer.setRotation(pointer, x, y, z)");
    }

    public static void setTextureOffset(long pointer, int x, int y) {
        InnerCoreServer.useClientMethod("NativeRenderer.setTextureOffset(pointer, x, y)");
    }

    public static void setTextureSize(long pointer, int x, int y) {
        InnerCoreServer.useClientMethod("NativeRenderer.setTextureSize(pointer, x, y)");
    }

    public static void clearModelPart(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.clearModelPart(pointer)");
    }

    public static void resetModel(long pointer) {
        InnerCoreServer.useClientMethod("NativeRenderer.resetModel(pointer)");
    }
}
