package org.innercore.icstd.common;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.mozilla.javascript.ScriptableObject;

import java.util.Objects;

public class BlockPos {
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    public final int x, y, z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public BlockPos(Vec3f p) {
        this(p.x, p.y, p.z);
    }

    public BlockPos(GenericScriptPos3 pos) {
        this(pos.x, pos.y, pos.z);
    }

    public static BlockPos parse(Object o, boolean nullable) {
        if (o instanceof BlockPos) {
            return (BlockPos) o;
        }
        if (o instanceof GenericScriptPos3) {
            return new BlockPos((GenericScriptPos3) o);
        }
        if (o instanceof ScriptableObject) {
            ScriptableObject s = (ScriptableObject) o;
            int x = ScriptableObjectHelper.getIntProperty(s, "x", 0);
            int y = ScriptableObjectHelper.getIntProperty(s, "y", 0);
            int z = ScriptableObjectHelper.getIntProperty(s, "z", 0);
            return new BlockPos(x, y, z);
        }
        return nullable ? null : ZERO;
    }

    public GenericScriptPos3 toScriptPos() { return new GenericScriptPos3(x, y, z); }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BlockPos)) return false;
        BlockPos blockPos = (BlockPos) object;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPos{" + x + ", " + y + ", " + z + '}';
    }
}
