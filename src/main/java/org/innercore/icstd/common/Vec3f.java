package org.innercore.icstd.common;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.mozilla.javascript.ScriptableObject;

public class Vec3f {
    public static Vec3f ZERO = new Vec3f(0, 0, 0);
    public final float x, y, z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f(Vec3i v) { this(v.x, v.y, v.z); }

    public Vec3f(GenericScriptPos3 pos) {
        this((float) pos.x, (float) pos.y, (float) pos.z);
    }

    public static Vec3f parse(Object o, boolean nullable) {
        if (o instanceof Vec3f) {
            return (Vec3f) o;
        }
        if (o instanceof Vec3i) {
            return new Vec3f((Vec3i) o);
        }
        if (o instanceof GenericScriptPos3) {
            return new Vec3f((GenericScriptPos3) o);
        }
        if (o instanceof ScriptableObject) {
            ScriptableObject s = (ScriptableObject) o;
            float x = ScriptableObjectHelper.getFloatProperty(s, "x", 0);
            float y = ScriptableObjectHelper.getFloatProperty(s, "y", 0);
            float z = ScriptableObjectHelper.getFloatProperty(s, "z", 0);
            return new Vec3f(x, y, z);
        }
        return nullable ? null : ZERO;
    }

}
