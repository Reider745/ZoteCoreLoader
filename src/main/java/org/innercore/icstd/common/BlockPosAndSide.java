package org.innercore.icstd.common;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.mozilla.javascript.ScriptableObject;


public class BlockPosAndSide extends BlockPos {
    public static final BlockPosAndSide ZERO = new BlockPosAndSide(0, 0, 0, 0);
    public final int side;

    public BlockPosAndSide(int x, int y, int z, int side) {
        super(x, y, z);
        this.side = side;
    }

    public BlockPosAndSide(double x, double y, double z, int side) {
        super(x, y, z);
        this.side = side;
    }

    public BlockPosAndSide(GenericScriptPos3 pos) {
        this(pos.x, pos.y, pos.z, pos.side);
    }

    public static BlockPosAndSide parse(Object o, boolean nullable) {
        if (o instanceof BlockPosAndSide) {
            return (BlockPosAndSide) o;
        }
        if (o instanceof GenericScriptPos3) {
            return new BlockPosAndSide((GenericScriptPos3) o);
        }
        if (o instanceof ScriptableObject) {
            ScriptableObject s = (ScriptableObject) o;
            int x = ScriptableObjectHelper.getIntProperty(s, "x", 0);
            int y = ScriptableObjectHelper.getIntProperty(s, "y", 0);
            int z = ScriptableObjectHelper.getIntProperty(s, "z", 0);
            int side = ScriptableObjectHelper.getIntProperty(s, "side", 0);
            return new BlockPosAndSide(x, y, z, side);
        }
        return nullable ? null : ZERO;
    }

    public GenericScriptPos3 toScriptPos() { return GenericScriptPos3.makeWithSideAndRelative(x, y, z, side); }
}
