package org.innercore.icstd.common.script;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GenericPos3BindGenBase;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.BlockPosAndSide;
import org.innercore.icstd.common.Vec3f;
import org.innercore.icstd.common.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@ApiBinding("GenericPos3")
public class GenericScriptPos3 extends GenericPos3BindGenBase {
    @ApiBinding public double x;
    @ApiBinding public double y;
    @ApiBinding public double z;
    @ApiBinding public int side;

    @ApiBinding @Nullable
    public GenericScriptPos3 vec = null;
    @ApiBinding @Nullable
    public GenericScriptPos3 relative = null;

    @ApiBinding
    public GenericScriptPos3(double x, double y, double z, int side) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
    }

    @ApiBinding
    public GenericScriptPos3(double x, double y, double z) {
        this(x, y, z, -1);
    }

    public GenericScriptPos3(Vec3f pos) { this(pos.x, pos.y, pos.z); }
    public GenericScriptPos3(Vec3i pos) { this(pos.x, pos.y, pos.z); }
    public GenericScriptPos3(BlockPos pos) { this(pos.x, pos.y, pos.z); }
    public GenericScriptPos3(BlockPosAndSide pos) { this(pos.x, pos.y, pos.z, pos.side); }

    @ApiBinding
    public GenericScriptPos3 makeRelativePos() {
        double relX = x, relY = y, relZ = z;
        switch(side) {
            case 0:
                relY--;
                break;
            case 1:
                relY++;
                break;
            case 2:
                relZ--;
                break;
            case 3:
                relZ++;
                break;
            case 4:
                relX--;
                break;
            case 5:
                relX++;
                break;
        }
        return new GenericScriptPos3(relX, relY, relZ);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("GenericPos{x=" + x + ", y=" + y + ", z=" + z);
        if (side != -1) {
            str.append(", side=").append(side);
        }
        if (relative != null) {
            str.append(", relative=").append(relative);
        }
        if (vec != null) {
            str.append(", vec=").append(vec);
        }
        str.append('}');
        return str.toString();
    }

    @ApiBinding
    public static GenericScriptPos3 makeWithSideAndRelative(double x, double y, double z, int side) {
        GenericScriptPos3 pos = new GenericScriptPos3(x, y, z, side);
        pos.relative = pos.makeRelativePos();
        return pos;
    }

    @ApiBinding
    public static GenericScriptPos3 parse(Object o, boolean nullable) {
        if (o instanceof GenericScriptPos3 s) {
            return s;
        }
        if (o instanceof Scriptable s) {
            double x = ScriptableObjectHelper.getDoubleProperty(s, "x", 0);
            double y = ScriptableObjectHelper.getDoubleProperty(s, "y", 0);
            double z = ScriptableObjectHelper.getDoubleProperty(s, "z", 0);
            int side = ScriptableObjectHelper.getIntProperty(s, "side", -1);
            Scriptable rel = ScriptableObjectHelper.getScriptableProperty(s, "relative", null);
            Scriptable vec = ScriptableObjectHelper.getScriptableProperty(s, "vec", null);
            GenericScriptPos3 pos = new GenericScriptPos3(x, y, z, side);
            pos.relative = rel != null ? new GenericScriptPos3(Vec3f.parse(rel, false)) : null;
            pos.vec = vec != null ? new GenericScriptPos3(Vec3f.parse(vec, false)) : null;
            return pos;
        }
        return nullable ? null : new GenericScriptPos3(0, 0, 0, -1);
    }

    @ApiBinding
    public Object toScriptableObject() {
        ScriptableObject obj = ScriptableObjectHelper.createEmpty();
        obj.put("x", obj, x);
        obj.put("y", obj, y);
        obj.put("z", obj, z);
        if (side >= 0) obj.put("side", obj, side);
        if (relative != null) {
            obj.put("relative", obj, relative.toScriptableObject());
        }
        if (vec != null) {
            obj.put("vec", obj, vec.toScriptableObject());
        }
        return obj;
    }
}
