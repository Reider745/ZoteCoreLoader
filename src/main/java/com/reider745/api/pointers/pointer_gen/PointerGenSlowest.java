package com.reider745.api.pointers.pointer_gen;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PointerGenSlowest implements IBasePointerGen {
    public final ConcurrentLinkedDeque<Long> usePointers = new ConcurrentLinkedDeque<>();
    public final Random random = new Random();

    @Override
    public final long next() {
        while (true){
            final long ptr = random.nextLong();
            if (ptr != 0 && !usePointers.contains(ptr)) {
                usePointers.add(ptr);
                return ptr;
            }
        }
    }

    @Override
    public final void remove(long ptr) {
        usePointers.remove(ptr);
    }
}