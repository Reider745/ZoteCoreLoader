package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

@Deprecated(since = "Zote")
public class ScriptableUiVisualSlotImpl implements UiVisualSlotImpl {
    private final ScriptableObject scriptable;

    public ScriptableUiVisualSlotImpl(ScriptableObject scriptable) {
        this.scriptable = scriptable;
    }

    @Override
    public int getId() {
        return (int) ScriptableObjectHelper.getFloatProperty(scriptable, "id", 0);
    }

    @Override
    public int getCount() {
        return (int) ScriptableObjectHelper.getFloatProperty(scriptable, "count", 0);
    }

    @Override
    public int getData() {
        return (int) ScriptableObjectHelper.getFloatProperty(scriptable, "data", 0);
    }

    @Override
    public NativeItemInstanceExtra getExtra() {
        return NativeItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null));
    }
}
