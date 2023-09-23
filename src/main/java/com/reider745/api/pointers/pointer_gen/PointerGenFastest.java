package com.reider745.api.pointers.pointer_gen;

public class PointerGenFastest implements IBasePointerGen {
    public long pre = Long.MIN_VALUE;

    @Override
    public final long next() {
        final long result = pre;
        pre++;
        return result;
    }

    @Override
    public final void remove(long ptr) {

    }
}
