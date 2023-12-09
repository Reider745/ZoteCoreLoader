package com.zhekasmirnov.innercore.api.commontypes;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 09.08.2017.
 */

public class Coords extends ScriptableObject {
    @Override
    public String getClassName() {
        return "Coords";
    }

    public Coords(int x, int y, int z, int side, boolean relativeCoordsAreSame) {
        super();

        int relX = x, relY = y, relZ = z;
        if (!relativeCoordsAreSame) {
            switch (side) {
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
        }

        put("x", this, x);
        put("y", this, y);
        put("z", this, z);
        put("side", this, side);

        ScriptableObject rel = ScriptableObjectHelper.createEmpty();
        rel.put("x", rel, relX);
        rel.put("y", rel, relY);
        rel.put("z", rel, relZ);
        put("relative", this, rel);
    }

    public Coords(int x, int y, int z, int side) {
        this(x, y, z, side, false);
    }

    public Coords(double x, double y, double z) {
        super();
        put("x", this, x);
        put("y", this, y);
        put("z", this, z);
    }

    public Coords(int x, int y, int z) {
        super();
        put("x", this, x);
        put("y", this, y);
        put("z", this, z);
    }

    public Coords setSide(int side) {
        put("side", this, side);
        return this;
    }
}
