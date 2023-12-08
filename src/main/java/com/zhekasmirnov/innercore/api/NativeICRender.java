package com.zhekasmirnov.innercore.api;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.mod.ui.GuiBlockModel;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;

/**
 * Created by zheka on 22.09.2017.
 */

public class NativeICRender {
    public static long constructICRender() {
        InnerCoreServer.useClientMethod("NativeICRender.constructICRender()");
        return 0;
    }

    public static long constructICRenderGroup() {
        InnerCoreServer.useClientMethod("NativeICRender.constructICRenderGroup()");
        return 0;
    }

    public static void icRenderGroupAddBlock(long group, int id, int data) {
        InnerCoreServer.useClientMethod("NativeICRender.icRenderGroupAddBlock(group, id, data)");
    }

    public static long icRenderClear(long model) {
        InnerCoreServer.useClientMethod("NativeICRender.icRenderClear(model)");
        return 0;
    }

    public static long icRenderAddEntry(long model) {
        InnerCoreServer.useClientMethod("NativeICRender.icRenderAddEntry(model)");
        return 0;
    }

    public static void icRenderEntrySetModel(long entry, int x, int y, int z, long model) {
        InnerCoreServer.useClientMethod("NativeICRender.icRenderEntrySetModel(entry, x, y, z, model)");
    }

    public static void icRenderEntrySetupCondition(long entry, long condition) {
        InnerCoreServer.useClientMethod("NativeICRender.icRenderEntrySetupCondition(entry, condition)");
    }

    /* GROUPS */

    private static int groupUUID = 0;
    private static HashMap<String, Group> activeGroups = new HashMap<>();

    @JSStaticFunction
    public static Group getGroup(String name) {
        if (activeGroups.containsKey(name)) {
            return activeGroups.get(name);
        } else {
            Group group = new Group(name);
            activeGroups.put(name, group);
            return group;
        }
    }

    @JSStaticFunction
    public static Group getUnnamedGroup() {
        return getGroup("_anonymous" + (groupUUID++));
    }

    public static class Group implements NativeIdMapping.IIdIterator {
        private String name;

        private Group(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void add(int id, int data) {
        }

        @Override
        public void onIdDataIterated(int id, int data) {
        }
    }

    /* RENDER MODEL */

    public static class Model {
        public long getPtr() {
            return 0;
        }

        public Model() {
        }

        public Model(NativeBlockModel model) {
            addEntry();
        }

        public RenderEntry addEntry() {
            return new RenderEntry(this);
        }

        public void clear() {
        }

        public RenderEntry addEntry(NativeBlockModel model) {
            return addEntry();
        }

        public RenderEntry addEntry(NativeRenderMesh mesh) {
            return addEntry();
        }

        public GuiBlockModel buildGuiModel(boolean resolve) {
            return new GuiBlockModel.Builder().build(resolve);
        }

    }

    public static final int MODE_INCLUDE = 0;
    public static final int MODE_EXCLUDE = 1;

    public static class RenderEntry {
        private Model icRender;

        private RenderEntry(Model model) {
            icRender = model;
        }

        public Model getParent() {
            return icRender;
        }

        public RenderEntry asCondition(int x, int y, int z, Group group, int mode) {
            return this;
        }

        public RenderEntry asCondition(int x, int y, int z, String name, int mode) {
            return asCondition(x, y, z, getGroup(name), mode);
        }

        public RenderEntry asCondition(int x, int y, int z, int id, int data, int mode) {
            return asCondition(x, y, z, getUnnamedGroup(), mode);
        }

        public RenderEntry setCondition(CONDITION condition) {
            return this;
        }

        public RenderEntry setModel(int x, int y, int z, NativeBlockModel model) {
            return this;
        }

        public RenderEntry setModel(NativeBlockModel model) {
            return this;
        }

        public RenderEntry setMesh(NativeRenderMesh mesh) {
            return this;
        }
    }

    /* COLLISION SHAPE */

    public static long constructCollisionShape() {
        InnerCoreServer.useClientMethod("NativeICRender.constructCollisionShape()");
        return 0;
    }

    public static void collisionShapeClear(long shape) {
        InnerCoreServer.useClientMethod("NativeICRender.collisionShapeClear(shape)");
    }

    public static void collisionShapeAddEntry(long shape, long entry) {
        InnerCoreServer.useClientMethod("NativeICRender.collisionShapeAddEntry(shape, entry)");
    }

    public static long constructCollisionShapeEntry() {
        InnerCoreServer.useClientMethod("NativeICRender.constructCollisionShapeEntry()");
        return 0;
    }

    public static void collisionShapeEntryAddBox(long entry, float x1, float y1, float z1, float x2, float y2,
            float z2) {
        InnerCoreServer.useClientMethod("NativeICRender.collisionShapeEntryAddBox(entry, x1, y1, z1, x2, y2, z2)");
    }

    public static void collisionShapeEntrySetCondition(long entry, long condition) {
        InnerCoreServer.useClientMethod("NativeICRender.collisionShapeEntrySetCondition(entry, condition)");
    }

    public static class CollisionShape {
        public long getPtr() {
            return 0;
        }

        public CollisionShape() {
        }

        public CollisionEntry addEntry() {
            return new CollisionEntry();
        }

        public void clear() {
        }
    }

    public static class CollisionEntry {
        private CollisionEntry() {
        }

        public CollisionEntry addBox(float x1, float y1, float z1, float x2, float y2, float z2) {
            return this;
        }

        public CollisionEntry setCondition(CONDITION condition) {
            return this;
        }
    }

    /* CONDITIONS */

    public static long newConditionBlock(int x, int y, int z, long group, boolean mode) {
        InnerCoreServer.useClientMethod("NativeICRender.newConditionBlock(x, y, z, group, mode)");
        return 0;
    }

    public static long newConditionRandom(int value, int max, int seed) {
        InnerCoreServer.useClientMethod("NativeICRender.newConditionRandom(value, max, seed)");
        return 0;
    }

    public static long conditionRandomSetAxisEnabled(long condition, int axis, boolean enabled) {
        InnerCoreServer.useClientMethod("NativeICRender.conditionRandomSetAxisEnabled(condition, axis, enabled)");
        return 0;
    }

    public static long newConditionOperatorNot(long condition) {
        InnerCoreServer.useClientMethod("NativeICRender.newConditionOperatorNot(condition)");
        return 0;
    }

    public static long newConditionOperatorAnd() {
        InnerCoreServer.useClientMethod("NativeICRender.newConditionOperatorAnd()");
        return 0;
    }

    public static void addToConditionOperatorAnd(long opAnd, long condition) {
        InnerCoreServer.useClientMethod("NativeICRender.addToConditionOperatorAnd(opAnd, condition)");
    }

    public static long newConditionOperatorOr() {
        InnerCoreServer.useClientMethod("NativeICRender.newConditionOperatorOr()");
        return 0;
    }

    public static void addToConditionOperatorOr(long opOr, long condition) {
        InnerCoreServer.useClientMethod("NativeICRender.addToConditionOperatorOr(opOr, condition)");
    }

    public static class CONDITION {
        protected final long ptr;

        public CONDITION(long ptr) {
            this.ptr = ptr;
        }
    }

    public static class BLOCK extends CONDITION {
        private int x, y, z;
        private Group group;
        private boolean mode;

        public BLOCK(int x, int y, int z, Group group, boolean mode) {
            super(0);
            this.x = x;
            this.y = y;
            this.z = z;
            this.mode = mode;
            this.group = group;
        }

        @Override
        public String toString() {
            return "BLOCK [" + x + " " + y + " " + z + (mode ? " !" : " ") + group.getName() + "]";
        }
    }

    public static class RANDOM extends CONDITION {
        private int seed;
        private int max;
        private int value;

        public RANDOM(int value, int max, int seed) {
            super(newConditionRandom(value, max, seed));
            this.value = value;
            this.max = max;
            this.seed = seed;
        }

        public RANDOM(int value, int max) {
            this(value, max, 131071);
        }

        public RANDOM(int max) {
            this(0, max);
        }

        public RANDOM setAxisEnabled(int axis, boolean enabled) {
            return this;
        }

        @Override
        public String toString() {
            return "RANDOM [value=" + value + " max=" + max + " seed=" + seed + "]";
        }
    }

    public static class NOT extends CONDITION {
        private CONDITION condition;

        public NOT(CONDITION condition) {
            super(0);
            this.condition = condition;
        }

        @Override
        public String toString() {
            String condition = this.condition.toString();
            String[] lines = condition.split("\n");
            if (lines.length > 1) {
                StringBuilder str = new StringBuilder();
                str.append("NOT [\n");
                for (String line : lines) {
                    str.append("  ").append(line).append("\n");
                }
                str.append("]");
                return str.toString();
            } else {
                return "NOT [" + condition + "]";
            }
        }
    }

    public static class AND extends CONDITION {
        private CONDITION[] conditions;

        public AND(CONDITION... conditions) {
            super(0);
            this.conditions = conditions;

            if (conditions.length < 2) {
                throw new IllegalArgumentException("ICRender AND condition got less than 2 parameters, it is useless");
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("AND [\n");

            for (CONDITION condition : conditions) {
                String[] lines = condition.toString().split("\n");
                for (String line : lines) {
                    str.append("  ").append(line).append("\n");
                }
            }

            str.append("]");
            return str.toString();
        }
    }

    public static class OR extends CONDITION {
        private CONDITION[] conditions;

        public OR(CONDITION... conditions) {
            super(0);
            this.conditions = conditions;

            if (conditions.length < 2) {
                throw new IllegalArgumentException("ICRender OR condition got less than 2 parameters, it is useless");
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("OR [\n");

            for (CONDITION condition : conditions) {
                String[] lines = condition.toString().split("\n");
                for (String line : lines) {
                    str.append("  ").append(line).append("\n");
                }
            }

            str.append("]");
            return str.toString();
        }
    }

}
