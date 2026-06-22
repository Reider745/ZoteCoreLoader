package org.innercore.icstd.common;

import java.util.Objects;

public class ChunkPos {
    public final int x, z;

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkPos(int blockX, int blockY, int blockZ) {
        this.x = (blockX >= 0 ? blockX / 16 : (blockX - 15) / 16);
        this.z = (blockZ >= 0 ? blockZ / 16 : (blockZ - 15) / 16);
    }

    public ChunkPos(BlockPos pos) {
        this(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) object;
        return x == chunkPos.x && z == chunkPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "ChunkPos{" + x + ", " + z + '}';
    }
}
