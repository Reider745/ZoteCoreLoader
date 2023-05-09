package com.zhekasmirnov.innercore.api.unlimited;

/**
 * Created by zheka on 08.08.2017.
 */

public class IDDataPair {
    public int id, data;

    public IDDataPair(int id, int data) {
        this.id = id;
        this.data = data;
    }

    @Override    
    public boolean equals(Object o) {
        if(o instanceof IDDataPair){
            IDDataPair other = (IDDataPair) o;
            return id == other.id && data == other.data;
        }
        return false;
    }

    @Override    
    public int hashCode() {
        return (id & 0xFFFF) | ((data & 0xFF) << 16);
    }

    @Override
    public String toString() {
        return id + ":" + data;
    }
}
