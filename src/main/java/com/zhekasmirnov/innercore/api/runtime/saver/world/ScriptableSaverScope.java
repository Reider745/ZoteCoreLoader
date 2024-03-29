package com.zhekasmirnov.innercore.api.runtime.saver.world;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.runtime.saver.serializer.ScriptableSerializer;
import org.mozilla.javascript.Scriptable;
import java.io.IOException;

public abstract class ScriptableSaverScope implements WorldDataScopeRegistry.SaverScope {
    @Override
    public void readJson(Object json) throws Exception {
        Object scriptable = ScriptableSerializer.scriptableFromJson(json);
        if (scriptable instanceof Scriptable) {
            read((Scriptable) scriptable);
        }else if(scriptable == null){
            Logger.warning("scriptable saver scope readJson() de-serialized into non scriptable, use default value");
            read(getDefaultSaves());
        }else{
            throw new IOException("scriptable saver scope readJson() de-serialized into non scriptable "+scriptable.getClass());
        }
    }

    @Override
    public Object saveAsJson() throws Exception {
        // an ugly solution, but a pretty functional one
        return ScriptableSerializer.scriptableToJson(save(), err -> {
            WorldDataSaver.logErrorStatic("error in serializer", err);
        });
    }

    public Scriptable getDefaultSaves(){
        return ScriptableObjectHelper.createEmpty();
    }
    public abstract void read(Object object);
    public abstract Object save();
}
