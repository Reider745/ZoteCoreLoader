package com.zhekasmirnov.apparatus.ecs.core.collection;

public class IntFlatMap {
    private int[] data;
    private final int initialCapacity;
    private final int emptyKey;
    private final float loadFactor;
    int count = 0;

    public IntFlatMap(int emptyKey, int reserve, float loadFactor) {
        this.initialCapacity = reserve;
        this.emptyKey = emptyKey;
        this.loadFactor = loadFactor;
    }

    public IntFlatMap(int emptyKey) {
        this(emptyKey, 63, 0.5f);
    }

    public IntFlatMap() {
        this(0);
    }

    public int get(int key, int def) {
        if (data == null)
            return def;
        int len = data.length / 2;
        int idx0 = key;
//        idx0 ^= (idx0 >>> 20) ^ (idx0 >>> 12);
//        idx0 ^= (idx0 >>> 7) ^ (idx0 >>> 4);
        idx0 %= len;
        if (idx0 < 0) idx0 = len + idx0;
        int empty = emptyKey;
        int idx = idx0, i = 1, k;
        while((k = data[idx * 2]) != key && k != empty) {
            idx = (idx0 + i) % len;
            i++;
        }
        return k != empty ? data[idx * 2 + 1] : def;
    }

    public boolean put(int key, int value) {
        reserve(count + 1);
        if(put(data, key, value, emptyKey)) {
            count++;
            return true;
        }
        return false;
    }

    public boolean remove(int key) {
        int len = data.length / 2;
        int idx0 = key;
//        idx0 ^= (idx0 >>> 20) ^ (idx0 >>> 12);
//        idx0 ^= (idx0 >>> 7) ^ (idx0 >>> 4);
        idx0 %= len;
        if (idx0 < 0) idx0 = len + idx0;
        int empty = emptyKey;
        int idx = idx0, lastIdx = -1, i = 1, k;
        boolean removing = false;
        while((k = data[idx * 2]) != empty) {
            if (removing) {
                data[lastIdx * 2] = k;
                data[lastIdx * 2 + 1] = data[idx * 2 + 1];
            }
            lastIdx = idx;
            idx = (idx0 + i) % len;
            i++;
            removing = removing || k == key;
        }
        if (removing) {
            data[idx] = empty;
            count--;
        }
        return removing;
    }

    public void clearNoDealloc() {
        count = 0;
    }

    public void clear() {
        data = null;
        count = 0;
    }

    public void reserve(int newCount) {
        int newCapacity = (int) (newCount / loadFactor);
        if (data == null) {
            newCapacity = growPolicy(initialCapacity, newCapacity);
            data = new int[newCapacity * 2];
            if (emptyKey != 0) {
                for(int i = 0; i < newCapacity; i++) {
                    data[i * 2] = emptyKey;
                }
            }
            return;
        }
        int len = data.length / 2;
        if (newCapacity > len) {
            newCapacity = growPolicy(len, newCapacity);
            int[] newArray = new int[newCapacity * 2];
            if (emptyKey != 0) {
                for(int i = 0; i < newCapacity; i++) {
                    newArray[i * 2] = emptyKey;
                }
            }
            for (int i = 0; i < len; i++) {
                int key = data[i * 2];
                if (key != emptyKey) {
                    put(newArray, key, data[i * 2 + 1], emptyKey);
                }
            }
            data = newArray;
        }
    }

    private static boolean put(int[] array, int key, int value, int empty) {
        int len = array.length / 2;
        int idx0 = key;
//        idx0 ^= (idx0 >>> 20) ^ (idx0 >>> 12);
//        idx0 ^= (idx0 >>> 7) ^ (idx0 >>> 4);
        idx0 %= len;
        if (idx0 < 0) idx0 = len + idx0;
        int idx = idx0, i = 1, k;
        while((k = array[idx * 2]) != key && k != empty) {
            idx = (idx0 + i) % len;
            i++;
        }
        idx *= 2;
        array[idx] = key;
        array[idx + 1] = value;
        return k == empty;
    }

    private static int nextPowerOf2(final int a) {
        int b = 1;
        while (b < a)
            b = b << 1;
        return b;
    }

    private static int growPolicy(int count, int targetCount) {
        // TODO: better grow policy
        count = nextPowerOf2(count);
        while (count - 1 < targetCount) {
            count *= 2;
        }
        return count - 1;
    }
}
