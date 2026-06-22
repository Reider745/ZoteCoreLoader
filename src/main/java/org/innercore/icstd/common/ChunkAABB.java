package org.innercore.icstd.common;

import java.util.Objects;

public class ChunkAABB {
    public final int x1, z1, x2, z2;

    public ChunkAABB(int x1, int z1, int x2, int z2) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
    }

    public boolean contains(int x, int z) { return x >= x1 && x < x2 && z >= z1 && z < z2; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkAABB chunkAABB = (ChunkAABB) o;
        return x1 == chunkAABB.x1 && z1 == chunkAABB.z1 && x2 == chunkAABB.x2 && z2 == chunkAABB.z2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x1, z1, x2, z2);
    }

    @Override
    public String toString() {
        return "ChunkAABB{" + x1 + "," + z1 + " ; " + x2 + "," + z2 + "}";
    }
}
