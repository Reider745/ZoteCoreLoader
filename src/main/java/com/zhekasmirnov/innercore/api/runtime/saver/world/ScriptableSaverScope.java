package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;

import org.apache.commons.math3.exception.NullArgumentException;
import org.mozilla.javascript.Scriptable;
import java.io.IOException;

public abstract class ScriptableSaverScope implements WorldDataScopeRegistry.SaverScope {
    @Override
    public void readJson(Object json) throws Exception {
        Object scriptable = ScriptableSerializer.scriptableFromJson(json);
        if (scriptable == null) {
            throw new NullArgumentException();
        } else if (scriptable instanceof Scriptable) {
            read((Scriptable) scriptable);
        } else {
            throw new IOException("scriptable saver scope readJson() de-serialized into non scriptable");
        }
    }

    @Override
    public Object saveAsJson() throws Exception {
        // an ugly solution, but a pretty functional one
        return ScriptableSerializer.scriptableToJson(save(), err -> {
            WorldDataSaver.logErrorStatic("error in serializer", err);
        });
    }

    public abstract void read(Object object);

    public abstract Object save();
}
