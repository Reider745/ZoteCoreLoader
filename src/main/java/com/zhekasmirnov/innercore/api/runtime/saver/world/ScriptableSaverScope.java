package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.serialization.*;
import org.mozilla.javascript.Scriptable;

public abstract class ScriptableSaverScope implements WorldDataScopeRegistry.SaverScope {
    @Override
    public void read(IScopedReader io, SerializationContext ctx) {
        Object deserialized = ScriptableObjectSerializer.readScriptable(io, ctx);
        if (!(deserialized instanceof Scriptable)) {
            deserialized = getDefaultSaves();
        }
        read(deserialized);
    }

    @Override
    public void write(IScopedWriter io, SerializationContext ctx) {
        Object serialized = save();
        ScriptableObjectSerializer.writeScriptable(io, serialized, ctx);
    }

    public Scriptable getDefaultSaves(){
        return ScriptableObjectHelper.createEmpty();
    }

    public abstract void read(Object object);
    public abstract Object save();
}
