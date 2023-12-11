package com.zhekasmirnov.innercore.api.mod.util;

import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 10.08.2017.
 */

public class ScriptableSuperclass extends ScriptableObject {
    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }
}
