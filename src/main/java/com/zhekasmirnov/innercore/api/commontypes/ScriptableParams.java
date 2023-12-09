package com.zhekasmirnov.innercore.api.commontypes;

import android.util.Pair;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 09.08.2017.
 */

public class ScriptableParams extends ScriptableObject {
    @Override
    public String getClassName() {
        return "Parameters";
    }

    @SafeVarargs
    public ScriptableParams(Pair<String, Object>... params) {
        for (Pair<String, Object> param : params) {
            put(param.first, this, param.second);
        }
    }
}
