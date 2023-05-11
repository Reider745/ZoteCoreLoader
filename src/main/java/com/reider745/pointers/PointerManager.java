package com.reider745.pointers;

import com.reider745.pointers.PointClass;

import java.util.HashMap;

public class PointerManager {
    private long start_pointer = -9223372036854775808l;
    private HashMap<Long, PointClass> pointers = new HashMap<>();

    public PointClass get(long pointer){
        return pointers.get(pointer);
    }

    public long put(PointClass object){
        long pointer = start_pointer;
        pointers.put(pointer, object);
        start_pointer++;
        return pointer;
    }

    public void clear(long pointer){
        pointers.remove(pointer);
    }
}
