package com.zhekasmirnov.innercore.api.runtime.saver;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 20.08.2017.
 */

public abstract class ObjectSaver {
    private int saverId = 0;

    public int getSaverId() {
        return saverId;
    }

    public void setSaverId(int saverId) {
        this.saverId = saverId;
    }

    public abstract Object read(ScriptableObject input);
    public abstract ScriptableObject save(Object input);
}
