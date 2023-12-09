package com.zhekasmirnov.innercore.api.commontypes;

import com.zhekasmirnov.apparatus.mcpe.NativeBlockSource;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 09.08.2017.
 */

public class FullBlock extends ScriptableObject {
    public int id, data;

    @Override
    public String getClassName() {
        return "FullBlock";
    }

    public FullBlock(int id, int data) {
        this.id = id;
        put("id", this, this.id);
        this.data = data;
        put("data", this, this.data);
    }

    public FullBlock(int idData) {
        this.id = idData & 0xFFFF;
        if (idData >> 24 == 1) {
            this.id = -this.id;
        }
        put("id", this, this.id);
        this.data = (idData >> 16) & 0xFF;
        put("data", this, this.data);
    }

    public FullBlock(NativeBlockSource blockSource, int x, int y, int z) {
        if (blockSource != null) {
            this.id = blockSource.getBlockId(x, y, z);
            this.data = blockSource.getBlockData(x, y, z);
        } else {
            id = data = 0;
        }
        put("id", this, this.id);
        put("data", this, this.data);
    }

    public FullBlock(long actor, int x, int y, int z) {
        this(NativeBlockSource.getDefaultForActor(actor), x, y, z);
    }
}
