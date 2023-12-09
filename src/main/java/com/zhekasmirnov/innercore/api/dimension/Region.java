package com.zhekasmirnov.innercore.api.dimension;

/**
 * Created by zheka on 13.11.2017.
 */

@Deprecated
public class Region {
    public static final int REGION_SIZE = 200000;
    public static final Region OVERWORLD = new Region(0, 0);

    public final int regionX, regionZ;

    public Region(int regionX, int regionZ) {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public boolean isOverworldRegion() {
        return regionX == 0 && regionZ == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Region) {
            return regionX == ((Region) obj).regionX && regionZ == ((Region) obj).regionZ;
        }
        return super.equals(obj);
    }

    public static class Bounds {
        public final int minX, minZ, maxX, maxZ;

        private Bounds(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
        }

        public boolean isInBounds(double x, double z) {
            return minX < x && x < maxX && minZ < z && z < maxZ;
        }
    }

    public Bounds getBounds() {
        return new Bounds(
                regionX * REGION_SIZE - REGION_SIZE / 2,
                regionZ * REGION_SIZE - REGION_SIZE / 2,
                regionX * REGION_SIZE + REGION_SIZE / 2,
                regionZ * REGION_SIZE + REGION_SIZE / 2);
    }

    public int getMiddleX() {
        return regionX * REGION_SIZE;
    }

    public int getMiddleZ() {
        return regionZ * REGION_SIZE;
    }

    public static Region getRegionAt(int x, int z) {
        return new Region((int) Math.floor(x / (double) REGION_SIZE + 0.5),
                (int) Math.floor(z / (double) REGION_SIZE + 0.5));
    }
}
