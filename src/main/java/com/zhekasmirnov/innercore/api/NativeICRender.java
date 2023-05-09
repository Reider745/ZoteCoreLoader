package com.zhekasmirnov.innercore.api;

import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * Created by zheka on 22.09.2017.
 */

public class NativeICRender {
    public static native long constructICRender();
    public static native long constructICRenderGroup();
    public static native void icRenderGroupAddBlock(long group, int id, int data);
    public static native long icRenderClear(long model);
    public static native long icRenderAddEntry(long model);
    public static native void icRenderEntrySetModel(long entry, int x, int y, int z, long model);
    public static native void icRenderEntrySetupCondition(long entry, long condition);

    /* GROUPS */

    private static int groupUUID = 0;
    private static HashMap<String, Group> activeGroups = new HashMap<>();

    @JSStaticFunction
    public static Group getGroup(String name) {
        if (activeGroups.containsKey(name)) {
            return activeGroups.get(name);
        }
        else {
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
        private long ptr;
        private String name;

        private Group(String name) {
            ptr = constructICRenderGroup();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void add(int id, int data) {
            NativeIdMapping.iterateMetadata(id, data, this);
        }

        @Override
        public void onIdDataIterated(int id, int data) {
            icRenderGroupAddBlock(ptr, id, data);
        }
    }

    /* RENDER MODEL */

    public static class Model {
        private long ptr;
        private List<RenderEntry> entries = new ArrayList<>();

        public long getPtr() {
            return ptr;
        }

        public Model() {
            ptr = constructICRender();
        }

        public Model(NativeBlockModel model) {
            this();
            addEntry().setModel(model);
        }

        public RenderEntry addEntry() {
            RenderEntry entry = new RenderEntry(this);
            entries.add(entry);
            return entry;
        }

        public void clear() {
            icRenderClear(ptr);
        }

        public RenderEntry addEntry(NativeBlockModel model) {
            return addEntry().setModel(model);
        }

        public RenderEntry addEntry(NativeRenderMesh mesh) {
            return addEntry().setMesh(mesh);
        }

        public Object buildGuiModel(boolean resolve) {
           return null;
        }

    }

    public static final int MODE_INCLUDE = 0;
    public static final int MODE_EXCLUDE = 1;

    public static class RenderEntry {
        private long ptr;
        private Model icRender;
        private NativeBlockModel blockModel = null;
        private CONDITION condition = null;

        private RenderEntry(Model model) {
            ptr = icRenderAddEntry(model.ptr);
            icRender = model;
        }

        public Model getParent() {
            return icRender;
        }

        public RenderEntry asCondition(int x, int y, int z, Group group, int mode) {
            setCondition(new BLOCK(x, y, z, group, mode != 0));
            return this;
        }

        public RenderEntry asCondition(int x, int y, int z, String name, int mode) {
            return asCondition(x, y, z, getGroup(name), mode);
        }

        public RenderEntry asCondition(int x, int y, int z, int id, int data, int mode) {
            Group group = getUnnamedGroup();
            group.add(id, data);
            return asCondition(x, y, z, group, mode);
        }

        public RenderEntry setCondition(CONDITION condition) {
            icRenderEntrySetupCondition(ptr, condition.ptr);
            this.condition = condition;
            return this;
        }

        public RenderEntry setModel(int x, int y, int z, NativeBlockModel model) {
            icRenderEntrySetModel(ptr, x, y, z, model.pointer);
            this.blockModel = model;
            return this;
        }

        public RenderEntry setModel(NativeBlockModel model) {
            return setModel(0, 0, 0, model);
        }

        public RenderEntry setMesh(NativeRenderMesh mesh) {
            return setModel(new NativeBlockModel(mesh));
        }
    }

    /* COLLISION SHAPE */

    public static native long constructCollisionShape();
    public static native void collisionShapeClear(long shape);
    public static native void collisionShapeAddEntry(long shape, long entry);
    public static native long constructCollisionShapeEntry();
    public static native void collisionShapeEntryAddBox(long entry, float x1, float y1, float z1, float x2, float y2, float z2);
    public static native void collisionShapeEntrySetCondition(long entry, long condition);

    public static class CollisionShape {
        private long ptr;

        public long getPtr() {
            return ptr;
        }

        public CollisionShape() {
            ptr = constructCollisionShape();
        }

        public CollisionEntry addEntry() {
            CollisionEntry entry = new CollisionEntry();
            collisionShapeAddEntry(ptr, entry.ptr);
            return entry;
        }

        public void clear() {
            collisionShapeClear(ptr);
        }
    }

    public static class CollisionEntry {
        private long ptr;

        private CollisionEntry() {
            ptr = constructCollisionShapeEntry();
        }

        public CollisionEntry addBox(float x1, float y1, float z1, float x2, float y2, float z2) {
            collisionShapeEntryAddBox(ptr, x1, y1, z1, x2, y2, z2);
            return this;
        }

        public CollisionEntry setCondition(CONDITION condition) {
            collisionShapeEntrySetCondition(ptr, condition.ptr);
            return this;
        }
    }

    /* CONDITIONS */

    public static native long newConditionBlock(int x, int y, int z, long group, boolean mode);
    public static native long newConditionRandom(int value, int max, int seed);
    public static native long conditionRandomSetAxisEnabled(long condition, int axis, boolean enabled);
    public static native long newConditionOperatorNot(long condition);
    public static native long newConditionOperatorAnd();
    public static native void addToConditionOperatorAnd(long opAnd, long condition);
    public static native long newConditionOperatorOr();
    public static native void addToConditionOperatorOr(long opOr, long condition);


    private static class CONDITION {
        protected final long ptr;

        private CONDITION(long ptr) {
            this.ptr = ptr;
        }
    }

    public static class BLOCK extends CONDITION {
        private int x, y, z;
        private Group group;
        private boolean mode;

        public BLOCK(int x, int y, int z, Group group, boolean mode) {
            super(newConditionBlock(x, y, z, group.ptr, mode));
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
            conditionRandomSetAxisEnabled(ptr, axis, enabled);
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
            super(newConditionOperatorNot(condition.ptr));
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
            }
            else {
                return "NOT [" + condition + "]";
            }
        }
    }

    public static class AND extends CONDITION {
        private CONDITION[] conditions;

        public AND(CONDITION ... conditions) {
            super(newConditionOperatorAnd());
            this.conditions = conditions;

            if (conditions.length < 2) {
                throw new IllegalArgumentException("ICRender AND condition got less than 2 parameters, it is useless");
            }
            for (CONDITION condition : conditions) {
                addToConditionOperatorAnd(ptr, condition.ptr);
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

        public OR(CONDITION ... conditions) {
            super(newConditionOperatorOr());
            this.conditions = conditions;

            if (conditions.length < 2) {
                throw new IllegalArgumentException("ICRender OR condition got less than 2 parameters, it is useless");
            }
            for (CONDITION condition : conditions) {
                addToConditionOperatorOr(ptr, condition.ptr);
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
