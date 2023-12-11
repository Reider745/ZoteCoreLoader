package com.zhekasmirnov.innercore.api.mod.ui;

import android.graphics.Bitmap;
import android.util.Pair;
import com.zhekasmirnov.innercore.api.NativeRenderMesh;
import com.zhekasmirnov.innercore.api.NativeRenderer;
import com.zhekasmirnov.innercore.api.unlimited.BlockShape;
import com.zhekasmirnov.innercore.api.unlimited.BlockVariant;
import org.json.JSONArray;
import java.util.*;

/**
 * Created by zheka on 21.08.2017.
 */

public class GuiBlockModel {
    public void setShadow(boolean shadow) {
    }

    public GuiBlockModel(int resolution) {
    }

    public GuiBlockModel() {
        this(128);
    }

    public void addBox(Box box) {
    }

    public void clear() {
    }

    public GuiBlockModel(String[] textures, int[] ids, BlockShape shape) {
        this();
    }

    public GuiBlockModel(String[] textures, int[] ids) {
        this();
    }

    public void updateShape(BlockShape shape) {
    }

    public Bitmap genTexture() {
        return Bitmap.getSingletonInternalProxy();
    }

    public void addToRenderModelPart(NativeRenderer.ModelPart part, float x, float y, float z) {
    }

    public void addToMesh(NativeRenderMesh mesh, float x, float y, float z) {
    }

    public static class Box {
        public final float x1, y1, z1;
        public final float x2, y2, z2;
        public final boolean[] enabledSides = { true, true, true, true, true, true };

        public Box(float x1, float y1, float z1, float x2, float y2, float z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
        }

        public BlockShape getShape() {
            return new BlockShape(x1, y1, z1, x2, y2, z2);
        }

        public Box(BlockShape shape) {
            this(shape.x1, shape.y1, shape.z1, shape.x2, shape.y2, shape.z2);
        }

        public Box() {
            this(0, 0, 0, 1, 1, 1);
        }

        public Box(String name, int id) {
            this();
            addTexture(name, id);
        }

        // TODO: find more elegant way of changing block shape
        public Box(Box box, BlockShape shape) {
            this(shape);
        }

        public void addTexturePath(String tex) {
        }

        public void addTexture(String name, int id) {
            addTexture(new Pair<String, Integer>(name, id));
        }

        public void addTexture(Pair<String, Integer> name) {
        }

        public Bitmap genTexture(int resolution) {
            return Bitmap.getSingletonInternalProxy();
        }

        public void addToMesh(NativeRenderMesh mesh, float x, float y, float z) {
        }
    }

    public static class Builder {
        public static class PrecompiledBox {
            public float x1, y1, z1;
            public float x2, y2, z2;
            public ArrayList<Pair<String, Integer>> textureNames = new ArrayList<>();
            public final boolean[] enabledSides = { true, true, true, true, true, true };
            public int blockId = -1, blockData = 0;

            public PrecompiledBox(PrecompiledBox inherit, float x1, float y1, float z1, float x2, float y2, float z2) {
                this.x1 = Math.min(x1, x2);
                this.y1 = Math.min(y1, y2);
                this.z1 = Math.min(z1, z2);
                this.x2 = Math.max(x1, x2);
                this.y2 = Math.max(y1, y2);
                this.z2 = Math.max(z1, z2);
                if (inherit != null) {
                    textureNames = new ArrayList<>(inherit.textureNames);
                    blockId = inherit.blockId;
                    blockData = inherit.blockData;
                    for (int i = 0; i < enabledSides.length; i++) {
                        enabledSides[i] = inherit.enabledSides[i];
                    }
                }
            }

            public PrecompiledBox disableSide(int side) {
                enabledSides[side] = false;
                return this;
            }

            public PrecompiledBox addTexture(String name, int id) {
                textureNames.add(new Pair<String, Integer>(name, id));
                return this;
            }

            public PrecompiledBox setBlock(int id, int data) {
                blockId = id;
                blockData = data;
                return this;
            }

            public boolean inside(PrecompiledBox b) {
                return x1 >= b.x1 && y1 >= b.y1 && z1 >= b.z1 && x2 <= b.x2 && y2 <= b.y2 && z2 <= b.z2;
            }

            public boolean intersects(PrecompiledBox b) {
                if (x1 >= b.x2 || b.x1 >= x2) {
                    return false;
                }
                if (y1 >= b.y2 || b.y1 >= y2) {
                    return false;
                }
                if (z1 >= b.z2 || b.z1 >= z2) {
                    return false;
                }
                return false;
            }

            public boolean inFrontOf(PrecompiledBox b) {
                if (b.y2 <= y1) {
                    return true;
                } else if (b.y1 >= y2) {
                    return false;
                }
                if (b.z2 <= z1) {
                    return false;
                } else if (b.z1 >= z2) {
                    return true;
                }
                if (b.x2 <= x1) {
                    return false;
                } else if (b.x1 >= x2) {
                    return true;
                }
                return false;
            }

            public Box compile() {
                Box box = new Box(x1, y1, z1, x2, y2, z2);
                for (int i = 0; i < enabledSides.length; i++) {
                    box.enabledSides[i] = enabledSides[i];
                }
                return box;
            }

            public String toString() {
                return String.format("[Box (%f %f %f), (%f %f %f)]", x1, y1, z1, x2, y2, z2);
            }
        };

        public GuiBlockModel build(boolean resolveCollisionsAndSort) {
            return new GuiBlockModel();
        }

        public void add(PrecompiledBox box) {
        }

        public void add(Builder builder) {
        }

        public void clear() {
        }
    }

    public static class VanillaRenderType {
        private static final HashMap<Integer, VanillaRenderType> renderTypeMap = new HashMap<>();

        public static VanillaRenderType getFor(int id) {
            if (!renderTypeMap.containsKey(id)) {
                renderTypeMap.put(id, new VanillaRenderType(null));
            }
            return renderTypeMap.get(id);
        }

        private VanillaRenderType(JSONArray boxes) {
        }

        public GuiBlockModel buildModelFor(String[] textures, int[] textureIds) {
            return new GuiBlockModel();
        }

        public GuiBlockModel buildModelFor(List<Pair<String, Integer>> textures) {
            return buildModelFor(new String[6], new int[6]);
        }

        public GuiBlockModel buildModelFor(BlockVariant variant) {
            return buildModelFor(variant.textures, variant.textureIds);
        }
    }

    public static GuiBlockModel createModelForBlockVariant(BlockVariant variant) {
        if (variant.renderType == 0) {
            return new GuiBlockModel(variant.textures, variant.textureIds, variant.shape);
        }
        VanillaRenderType renderType = VanillaRenderType.getFor(variant.renderType);
        return renderType != null ? renderType.buildModelFor(variant) : null;
    }
}
