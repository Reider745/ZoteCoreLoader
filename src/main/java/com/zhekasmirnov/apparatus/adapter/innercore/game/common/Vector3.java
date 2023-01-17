package com.zhekasmirnov.apparatus.adapter.innercore.game.common;

public class Vector3 {
    public final float x, y, z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(float[] arr) {
        this(arr[0], arr[1], arr[2]);
    }

    public float lengthSqr() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float distanceSqr(Vector3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distance(Vector3 other) {
        return (float) Math.sqrt(distanceSqr(other));
    }
}
