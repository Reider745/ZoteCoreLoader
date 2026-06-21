package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;

public abstract class ScriptableModuleInstanceImpl implements Scriptable {
    public static final boolean VALIDATE_INVALID_WRITES = true; // turn off if mods begin to fail, allows writing extra fields to these objects

    private Scriptable prototype;
    private Scriptable parentScope;
    // slow path fallback to allow adding extra fields to an object, only enabled if VALIDATE_INVALID_WRITES = false
    private HashMap<Object, Object> extraFields;

    @Override
    public Object get(String name, Scriptable start) {
        Object o = prototype.get(name, start);
        if (o == Scriptable.NOT_FOUND && extraFields != null && extraFields.containsKey(name))
            o = extraFields.get(name);
        return o;
    }

    @Override
    public Object get(int index, Scriptable start) {
        Object o = prototype.get(index, start);
        if (o == Scriptable.NOT_FOUND && extraFields != null && extraFields.containsKey(index))
            o = extraFields.get(index);
        return o;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return prototype.has(name, start) || (extraFields != null && extraFields.containsKey(name));
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return prototype.has(index, start) || (extraFields != null && extraFields.containsKey(index));
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        if (VALIDATE_INVALID_WRITES) throw new UnsupportedOperationException("writing missing field " + name + " to " + this);
        if (extraFields == null)
            extraFields = new HashMap<>();
        extraFields.put(name, value);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (VALIDATE_INVALID_WRITES) throw new UnsupportedOperationException("writing missing field " + index + " to " + this);
        if (extraFields == null)
            extraFields = new HashMap<>();
        extraFields.put(index, value);
    }

    @Override
    public void delete(String name) {
        if (VALIDATE_INVALID_WRITES) throw new UnsupportedOperationException("deleting field " + name + " to " + this);
        if (extraFields != null)
            extraFields.remove(name);
    }

    @Override
    public void delete(int index) {
        if (VALIDATE_INVALID_WRITES) throw new UnsupportedOperationException("deleting field " + index + " to " + this);
        if (extraFields != null)
            extraFields.remove(index);
    }

    @Override
    public Scriptable getPrototype() {
        return prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Scriptable getParentScope() {
        return parentScope;
    }

    @Override
    public void setParentScope(Scriptable parent) {
        this.parentScope = parent;
    }

    @Override
    public Object[] getIds() {
        return prototype.getIds();
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return ScriptRuntime.jsDelegatesTo(instance, this);
    }
}
