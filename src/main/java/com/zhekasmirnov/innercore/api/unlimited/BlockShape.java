package com.zhekasmirnov.innercore.api.unlimited;

import com.zhekasmirnov.innercore.api.NativeBlock;

/**
 * Created by zheka on 08.08.2017.
 */

public class BlockShape {
    public float x1, y1, z1;
    public float x2, y2, z2;

    public BlockShape(float x1, float y1, float z1, float x2, float y2, float z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    public BlockShape() {
        set(0, 0, 0, 1, 1, 1);
    }

    public void set(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public void setToBlock(int id, int data) {
        NativeBlock.setShape(id, data, x1, y1, z1, x2, y2, z2);
    }
}
