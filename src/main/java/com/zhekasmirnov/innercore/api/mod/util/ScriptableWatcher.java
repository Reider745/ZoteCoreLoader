package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;

/**
 * Created by zheka on 01.08.2017.
 */

public class ScriptableWatcher {
    public ScriptableObject object;

    private boolean isDirty = false;
    private ArrayList<Object> cached = new ArrayList<>();

    public ScriptableWatcher(ScriptableObject object) {
        this.object = object;
        refresh();
        validate();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void validate() {
        isDirty = false;
    }

    public void invalidate() {
        isDirty = true;
    }

    public void setTarget(ScriptableObject object) {
        this.object = object;
    }

    public void refresh() {
        checkPosition = 0;
        updateCached(object);
    }

    private void updateCached(ScriptableObject obj) {
        if (checkPosition > 128) {
            return;
        }

        if (obj == null) {
            updateSymbol("null");
            return;
        }

        Object[] keys = obj.getAllIds();
        for (Object key : keys) {
            Object val = obj.get(key);
            updateSymbol(key);
            if (val instanceof ScriptableObject) {
                updateSymbol("{");
                updateCached((ScriptableObject) val);
                updateSymbol("}");
            } else {
                updateSymbol(val);
            }
        }
    }

    private int checkPosition;

    private void updateSymbol(Object value) {
        if (value == null) {
            value = "null";
        }
        if (cached.size() <= checkPosition) {
            cached.add(value);
            isDirty = true;
        } else {
            if (!cached.get(checkPosition).equals(value)) {
                cached.set(checkPosition, value);
                isDirty = true;
            }
        }
        ++checkPosition;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Object val : cached) {
            result.append(val.toString()).append(" ");
        }
        return "{" + result + "}";
    }
}
