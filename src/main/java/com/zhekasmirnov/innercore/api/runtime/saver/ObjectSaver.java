package com.zhekasmirnov.innercore.api.runtime.saver;

import org.innercore.icstd.serialization.ScriptableObjectSerializer;

/**
 * Created by zheka on 20.08.2017.
 */

public abstract class ObjectSaver extends ScriptableObjectSerializer {
    private int saverId = 0;

    public int getSaverId() {
        return saverId;
    }

    public void setSaverId(int saverId) {
        this.saverId = saverId;
    }
}
