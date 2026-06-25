package com.zhekasmirnov.innercore.api.mod.ui.container;

import org.innercore.icstd.inventory.ItemInstanceExtra;
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
    public ItemInstanceExtra getExtra() {
        return ItemInstanceExtra.unwrapObject(ScriptableObjectHelper.getProperty(scriptable, "extra", null));
    }
}
