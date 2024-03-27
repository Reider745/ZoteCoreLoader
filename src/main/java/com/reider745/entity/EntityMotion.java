package com.reider745.entity;

import cn.nukkit.entity.Entity;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// TODO: It was too good to be in ZoteCore, remote it.
public class EntityMotion {
    private static final ConcurrentHashMap<Entity, double[]> positionsLast = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Entity, double[]> positionsCurrent = new ConcurrentHashMap<>();
    private static final double[] EMPTY = new double[] { 0, 0, 0 };

    public static double[] getVelocity(final Entity entity) {
        final double[] lastPos = positionsLast.get(entity);
        final double[] currentPos = positionsCurrent.get(entity);
        if (lastPos != null && currentPos != null)
            return new double[] {
                    currentPos[0] - lastPos[0],
                    currentPos[1] - lastPos[1],
                    currentPos[2] - lastPos[2]
            };
        return EMPTY;
    }

    public static void tick() {
        for (final Entity entity : positionsCurrent.keySet())
            positionsLast.put(entity, positionsCurrent.put(entity, new double[]{entity.x, entity.y, entity.z}));
    }

    public static void added(final Entity entity) {
        final double[] pos = new double[] { entity.x, entity.y, entity.z };
        positionsLast.put(entity, pos);
        positionsCurrent.put(entity, pos);
    }

    public static void remove(final Entity entity) {
        positionsLast.remove(entity);
        positionsCurrent.remove(entity);
    }
}
