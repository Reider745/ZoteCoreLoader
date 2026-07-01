package org.innercore.icstd.updatable.impl;

/**
 * A handle that encodes an index (24 bits), a generation (8 bits), and a scope ID (16 bits)
 * into a single 64‑bit long.
 */
final class UpdatableRef {
    private final long handle;

    // Masks for bit fields (assumed order: idx in low 24 bits, gen in next 8, scope in next 16)
    private static final long IDX_MASK  = 0xFFFFFFL;
    private static final long GEN_MASK  = 0xFFL << 24;
    private static final long SCOPE_MASK = 0xFFFFL << 32;

    public UpdatableRef() {
        this.handle = ~0L;   // all bits set (~0u in C++)
    }

    public UpdatableRef(long handle) {
        this.handle = handle;
    }

    public UpdatableRef(int idx, int gen, short scope) {
        this.handle = (idx & IDX_MASK)
                | ((gen & 0xFFL) << 24)
                | ((long)(scope & 0xFFFF) << 32);
    }

    public long getHandle() { return handle; }

    public int getIdx() {
        return (int)(handle & IDX_MASK);
    }

    public int getGen() {
        return (int)((handle >> 24) & 0xFF);
    }

    public short getScope() {
        return (short)((handle >> 32) & 0xFFFF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdatableRef)) return false;
        return handle == ((UpdatableRef) o).handle;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(handle);
    }

    // Comparison operators (used by containers)
    public boolean lt(UpdatableRef rhs) { return handle < rhs.handle; }
    public boolean gt(UpdatableRef rhs) { return handle > rhs.handle; }
}