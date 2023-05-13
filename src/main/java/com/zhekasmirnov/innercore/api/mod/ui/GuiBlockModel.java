package com.zhekasmirnov.innercore.api.mod.ui;

import android.util.Pair;
import com.zhekasmirnov.apparatus.minecraft.version.MinecraftVersions;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeItemModel;
import com.zhekasmirnov.innercore.api.NativeRenderMesh;
import com.zhekasmirnov.innercore.api.NativeRenderer;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.unlimited.BlockRegistry;
import com.zhekasmirnov.innercore.api.unlimited.BlockShape;
import com.zhekasmirnov.innercore.api.unlimited.BlockVariant;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.resource.ResourcePackManager;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private ArrayList<Box> boxes = new ArrayList<>();

    public void addBox(Box box) {
    }

    public void clear(){
    }

    public GuiBlockModel(String[] textures, int[] ids, BlockShape shape) {

    }

    public GuiBlockModel(String[] textures, int[] ids) {

    }

    public void updateShape(BlockShape shape){
    }

    public Object genTexture() {
        return null;
    }


    public void addToRenderModelPart(NativeRenderer.ModelPart part, float x, float y, float z) {

    }

    public void addToMesh(NativeRenderMesh mesh, float x, float y, float z) {

    }

    public static class Box {
        public final float x1, y1, z1;
        public final float x2, y2, z2;
        public final boolean[] enabledSides = {true, true, true, true, true, true};

        private boolean shadow = true;

        public void setShadow(boolean shadow) {
            this.shadow = shadow;
        }

        private boolean renderAllSides = false;

        public void setRenderAllSides(boolean renderAllSides) {
            this.renderAllSides = renderAllSides;
        }

        private int[] SIDE_SHADOW = {
                0, 0, 0, 130, 65, 0, 0
        };

        private int[][] SIDES = {
                { // bottom
                        8, 5, 6, 7
                },
                { // top
                        4, 1, 2, 3
                },
                { // back right
                        4, 1, 5, 8
                },
                { // front right
                        3, 2, 6, 7
                },
                { // front left
                        4, 3, 7, 8
                },
                { // back left
                        1, 2, 6, 5
                }
        };

        // this values are used to fix coordinates of each point by a little overlapping amount to avoid empty spaces
        private float[][][] EPSILON = {
            { // bottom
                {-1, 0, 1},
                {1, 0, 1},
                {1, 0, -1},
                {-1, 0, -1},
            },
            { // top
                {-1, 0, 1},
                {1, 0, 1},
                {1, 0, -1},
                {-1, 0, -1},
            },
            { // back right
                {-1, 1, 0},
                {1, 1, 0},
                {1, -1, 0},
                {-1, -1, 0},
            },
            { // front right
                {-1, 1, 0},
                {1, 1, 0},
                {1, -1, 0},
                {-1, -1, 0},
            },
            { // front left
                {0, 1, 1},
                {0, 1, -1},
                {0, -1, -1},
                {0, -1, 1},
            },
            { // back left
                {0, 1, 1},
                {0, 1, -1},
                {0, -1, -1},
                {0, -1, 1},
            },
        };

        private float[][] projection;

        public Box(float x1, float y1, float z1, float x2, float y2, float z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;

            buildProjection();
        }

        private void buildProjection() {

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
        public Box(Box box, BlockShape shape){
            this(shape);
            textures = box.textures;
            textureNames = box.textureNames;
        }

        private ArrayList<String> textures = new ArrayList<>();
        private ArrayList<Pair<String, Integer>> textureNames = new ArrayList<>();

        private Object loadTexture(int side) {
            return null;
        }

        private Pair<String, Integer> getTextureName(int side){
            side = Math.min(side, textureNames.size() - 1);
            if (side < 0) {
                return null;
            }
            return textureNames.get(side);
        }

        public void addTexturePath(String tex) {
            if (tex != null) {
                if (!tex.endsWith(".png")) {
                    tex += ".png";
                }
                textures.add(tex);
                return;
            }
            textures.add("missing_texture");
        }

        public void addTexture(String name, int id){
            addTexture(new Pair<String, Integer>(name, id));
        }

        public void addTexture(Pair<String, Integer> name) {
            textureNames.add(name);
            String tex = ResourcePackManager.getBlockTextureName(name.first, name.second);
            if (tex == null) {
                tex = ResourcePackManager.getBlockTextureName("missing_block", 0);
            }

            addTexturePath(tex);
        }

        private static void recycleBitmap(Object bitmap) {

        }

        // returns x, y, w, h of current side
        private float[] getSideBounds(int side) {
            float x = 0, y = 0, w = 1, h = 1;

            switch (side) {
                case 0:
                case 1:
                    x = x1; y = z1;
                    w = x2 - x1; h = z2 - z1;
                    break;
                case 2:
                case 3:
                    x = x1; y = y1;
                    w = x2 - x1; h = y2 - y1;
                    break;
                case 4:
                case 5:
                    x = z1; y = y1;
                    w = z2 - z1; h = y2 - y1;
                    break;
            }

            x = Math.max(0, Math.min(1, x));
            y = Math.max(0, Math.min(1, y));
            w = Math.max(0, Math.min(1 - x, w));
            h = Math.max(0, Math.min(1 - y, h));

            return new float[]{x, y, w, h};
        }

        private Object getSideCutout(int side) {
            return null;
        }


        private Object getSideProjection(int side, Object cutout, int res) {
            return null;
        }

        private Object genProjectedSideTex(int side, int res) {
            return null;
        }

        public Object genTexture(int resolution) {
            return null;
        }

        public void addToMesh(NativeRenderMesh mesh, float x, float y, float z) {
        }
    }

    public static class Builder {
        private static final float E = 0.025f;

        public static class PrecompiledBox {
            public float x1, y1, z1;
            public float x2, y2, z2;
            public ArrayList<Pair<String, Integer>> textureNames = new ArrayList<>(); 
            public final boolean[] enabledSides = {true, true, true, true, true, true};
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

            public PrecompiledBox disableSide(int side){
                enabledSides[side] = false;
                return this;
            }

            public PrecompiledBox addTexture(String name, int id){
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
                if (blockId != -1) {
                    BlockVariant variant = BlockRegistry.getBlockVariant(blockId, blockData);
                    if (variant != null) {
                        for (int i = 0; i < 6; i++) {
                            box.addTexture(variant.textures[i], variant.textureIds[i]);
                        }
                    } else if (blockId < IDRegistry.BLOCK_ID_OFFSET) {

                    }
                }
                for (Pair<String, Integer> textureName : textureNames) {
                    box.addTexture(textureName);
                }
                return box;
            }

            public String toString() {
                return String.format("[Box (%f %f %f), (%f %f %f)]", x1, y1, z1, x2, y2, z2);
            }
        };

        private List<PrecompiledBox> boxes = new ArrayList<>();
        
        public GuiBlockModel build(boolean resolveCollisionsAndSort) {
            // resolve intersections
            List<PrecompiledBox> resolved = new ArrayList<>(boxes);
            boolean collision;

            if (resolveCollisionsAndSort) {
                // resolve x
                collision = true;
                while (collision) {
                    collision = false;
                    for (int i = 0; i < resolved.size() && !collision; i++) {
                        PrecompiledBox box1 = resolved.get(i);
                        for (int j = 0; j < resolved.size() && !collision; j++) {
                            if (i != j) {
                                PrecompiledBox box2 = resolved.get(j);
                                if (box1.y1 + E >= box2.y2 || box1.y2 <= box2.y1 + E || box1.z1 + E >= box2.z2 || box1.z2 <= box2.z1 + E) {
                                    continue;
                                }
                                if (box1.x1 < box2.x1 && box1.x2 > box2.x1) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box2.x1, box1.y2, box1.z2).disableSide(5));
                                    resolved.add(new PrecompiledBox(box1, box2.x1, box1.y1, box1.z1, box1.x2, box1.y2, box1.z2).disableSide(4));
                                    collision = true;
                                    break;
                                }
                                if (box1.x1 < box2.x2 && box1.x2 > box2.x2) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box2.x2, box1.y2, box1.z2).disableSide(5));
                                    resolved.add(new PrecompiledBox(box1, box2.x2, box1.y1, box1.z1, box1.x2, box1.y2, box1.z2).disableSide(4));
                                    collision = true;
                                    break;
                                }
                            }
                        }    
                    }
                }
                
                
                // resolve y
                collision = true;
                while (collision) {
                    collision = false;
                    for (int i = 0; i < resolved.size() && !collision; i++) {
                        PrecompiledBox box1 = resolved.get(i);
                        for (int j = 0; j < resolved.size() && !collision; j++) {
                            if (i != j) {
                                PrecompiledBox box2 = resolved.get(j);
                                if (box1.x1 + E >= box2.x2 || box1.x2 <= box2.x1 + E || box1.z1 + E >= box2.z2 || box1.z2 <= box2.z1 + E) {
                                    continue;
                                }
                                if (box1.y1 < box2.y1 && box1.y2 > box2.y1) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box1.x2, box2.y1, box1.z2).disableSide(1));
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box2.y1, box1.z1, box1.x2, box1.y2, box1.z2).disableSide(0));
                                    collision = true;
                                    break;
                                }
                                if (box1.y1 < box2.y2 && box1.y2 > box2.y2) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box1.x2, box2.y2, box1.z2).disableSide(1));
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box2.y2, box1.z1, box1.x2, box1.y2, box1.z2).disableSide(0));
                                    collision = true;
                                    break;
                                }
                            }
                        }    
                    }
                }

                // resolve z
                collision = true;
                while (collision) {
                    collision = false;
                    for (int i = 0; i < resolved.size() && !collision; i++) {
                        PrecompiledBox box1 = resolved.get(i);
                        for (int j = 0; j < resolved.size() && !collision; j++) {
                            if (i != j) {
                                PrecompiledBox box2 = resolved.get(j);
                                if (box1.x1 + E >= box2.x2 || box1.x2 <= box2.x1 + E || box1.y1 + E >= box2.y2 || box1.y2 <= box2.y1 + E) {
                                    continue;
                                }
                                if (box1.z1 < box2.z1 && box1.z2 > box2.z1) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box1.x2, box1.y2, box2.z1).disableSide(2));
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box2.z1, box1.x2, box1.y2, box1.z2).disableSide(3));
                                    collision = true;
                                    break;
                                }
                                if (box1.z1 < box2.z2 && box1.z2 > box2.z2) {
                                    resolved.remove(i);
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box1.z1, box1.x2, box1.y2, box2.z2).disableSide(2));
                                    resolved.add(new PrecompiledBox(box1, box1.x1, box1.y1, box2.z2, box1.x2, box1.y2, box1.z2).disableSide(3));
                                    collision = true;
                                    break;
                                }
                            }
                        }    
                    }
                }

                // System.out.println("------------------");
                // for (PrecompiledBox box : resolved) {
                //     for (PrecompiledBox box2 : resolved) {
                //         if (box != box2 && box.intersects(box2) && !box.inside(box2) && !box2.inside(box)) {
                //             System.out.println("BOX INTERSECTS");
                //         }
                //     }
                //     System.out.println("compile box: " + box);
                // }

                Comparator<PrecompiledBox> comparator = new Comparator<PrecompiledBox>() {
                    public int compare(PrecompiledBox box1, PrecompiledBox box2) {
                        if (box1.x1 == box2.x1 && box1.y1 == box2.y1 && box1.z1 == box2.z1 && box1.x2 == box2.x2 && box1.y2 == box2.y2 && box1.z2 == box2.z2) {
                            return 0;
                        }
                        if (box1.inFrontOf(box2)) {
                            return 1;
                        }
                        if (box2.inFrontOf(box1)) {
                            return -1;
                        }
                        if (box1.inside(box2)) {
                            return -1;
                        }
                        if (box2.inside(box1)) {
                            return 1;
                        }
                        return 0;
                    }
                };

                // for (PrecompiledBox box1 : resolved) {
                //     for (PrecompiledBox box2 : resolved) {
                //         if (comparator.compare(box1, box2) > 0 && comparator.compare(box2, box1) > 0) {
                //             System.out.println("FLIP RULE BROKEN");
                //         }
                //         if (comparator.compare(box1, box2) == 0 && comparator.compare(box2, box1) != 0) {
                //             System.out.println("EQUALITY RULE BROKEN");
                //         }
                //         for (PrecompiledBox box3 : resolved) {
                //             if (comparator.compare(box1, box2) < 0 && comparator.compare(box2, box3) < 0 && !(comparator.compare(box1, box3) < 0)) {
                //                 System.out.println("TRANSITIVITY RULE BROKEN: " + box1 + " < " + box2 + " < " + box3 + " but box3 <= box1: " + comparator.compare(box1, box3) + " " + box3.inFrontOf(box1));
                //             }
                //         }
                //     }
                // }
                
                try {
                    Collections.sort(resolved, comparator);
                } catch (IllegalArgumentException e) {
                    ICLog.e("GuiBlockModel", "failed to compile boxes", e);
                }
            }

            GuiBlockModel model = new GuiBlockModel();
            for (PrecompiledBox box : resolved) {
                Box compiled = box.compile();
                if (compiled != null) {
                    model.addBox(compiled);
                }
            }
            return model;
        }

        public void add(PrecompiledBox box) {
            boxes.add(box);
        }

        public void add(Builder builder) {
            for (PrecompiledBox box : builder.boxes) {
                add(box);
            }
        }

        public void clear() {
            boxes.clear();
        }
    }

    public static class VanillaRenderType {
        private static final HashMap<Integer, VanillaRenderType> renderTypeMap = new HashMap<>();
        private static boolean isDataLoaded = false;

        private static JSONObject loadDescriptionJson() {
            try {
                return new JSONObject(FileTools.getAssetAsString("innercore/icons/block_rendertypes.json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        private static synchronized void loadIfRequired() {
            if (!isDataLoaded) {
                JSONObject json = loadDescriptionJson();

            }
        }

        public static VanillaRenderType getFor(int id) {
            loadIfRequired();
            return renderTypeMap.get(id);
        }


        private static class NoTextureBox {
            public float x1, y1, z1;
            public float x2, y2, z2;
            public ArrayList<Pair<String, Integer>> textureNames = new ArrayList<>(); 
            public final boolean[] enabledSides = {true, true, true, true, true, true};
            public int blockId = -1, blockData = 0;

            private NoTextureBox(float x1, float y1, float z1, float x2, float y2, float z2) {
                this.x1 = Math.min(x1, x2);
                this.y1 = Math.min(y1, y2);
                this.z1 = Math.min(z1, z2);
                this.x2 = Math.max(x1, x2);
                this.y2 = Math.max(y1, y2);
                this.z2 = Math.max(z1, z2);
            }

            private NoTextureBox(JSONArray box) {
                this((float) box.optDouble(0, 0), (float) box.optDouble(1, 0), (float) box.optDouble(2, 0), (float) box.optDouble(3, 1), (float) box.optDouble(4, 1), (float) box.optDouble(5, 1));
            }

            private Box asModelBox(String[] textures, int[] textureIds) {
                Box box = new Box(x1, y1, z1, x2, y2, z2);
                for (int i = 0; i < 6; i++) {
                    box.addTexture(textures[i], textureIds[i]);
                }
                return box;
            }
        }

        private ArrayList<NoTextureBox> boxes = new ArrayList<>();

        private VanillaRenderType(JSONArray boxes) {

        }

        public GuiBlockModel buildModelFor(String[] textures, int[] textureIds) {
            GuiBlockModel model = new GuiBlockModel();
            for (NoTextureBox box : boxes) {
                model.addBox(box.asModelBox(textures, textureIds));
            }
            return model;
        }

        public GuiBlockModel buildModelFor(List<Pair<String, Integer>> textures) {
            String[] textureNames = new String[6];
            int[] textureIds = new int[6];
            for (int i = 0; i < 6; i++) {
                Pair<String, Integer> texture = textures.size() > 0 ? textures.get(Math.min(i, textures.size() - 1)) : new Pair<>("missing_texture", 0);
                textureNames[i] = texture.first;
                textureIds[i] = texture.second;
            }
            return buildModelFor(textureNames, textureIds);
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
