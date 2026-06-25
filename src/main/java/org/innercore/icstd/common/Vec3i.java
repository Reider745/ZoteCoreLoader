package org.innercore.icstd.common;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.mozilla.javascript.ScriptableObject;

public class Vec3i {
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    public final int x, y, z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Vec3i(Vec3f v) {
        this(v.x, v.y, v.z);
    }

    public Vec3i(GenericScriptPos3 pos) {
        this(pos.x, pos.y, pos.z);
    }

    public static Vec3i parse(Object o, boolean nullable) {
        if (o instanceof Vec3i) {
            return (Vec3i) o;
        }
        if (o instanceof Vec3f) {
            return new Vec3i((Vec3f) o);
        }
        if (o instanceof GenericScriptPos3) {
            return new Vec3i((GenericScriptPos3) o);
        }
        if (o instanceof ScriptableObject) {
            ScriptableObject s = (ScriptableObject) o;
            int x = ScriptableObjectHelper.getIntProperty(s, "x", 0);
            int y = ScriptableObjectHelper.getIntProperty(s, "y", 0);
            int z = ScriptableObjectHelper.getIntProperty(s, "z", 0);
            return new Vec3i(x, y, z);
        }
        return nullable ? null : ZERO;
    }
}
