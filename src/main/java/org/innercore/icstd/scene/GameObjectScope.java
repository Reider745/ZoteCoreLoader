package org.innercore.icstd.scene;

import org.innercore.icstd.annotations.ApiHidden;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GameObjectScope {
    private static final long HANDLE_VALUE_BITS = 48L;
    public static final GameObjectScope SERVER_SCOPE = new GameObjectScope(true, 0L);
    public static final GameObjectScope CLIENT_SCOPE = new GameObjectScope(false, 1L << HANDLE_VALUE_BITS);

    private final boolean isServerSide;
    private final AtomicLong nextUniqueHandleValue = new AtomicLong(1);

    private GameObjectScope(boolean isServerSide, long uniqueHandleOffset) {
        this.isServerSide = isServerSide;
        // NOTE: this is implementation defined, any mechanism for generating unique long handles will be suitable
        // reserve 48 bits for a range, 15 bits for offset
        assert (uniqueHandleOffset & ((1L << HANDLE_VALUE_BITS) - 1L)) == 0L;
        // seed handles with current millisecond time on init, time is monotonically increasing, assume mods won't generate more than 16 handles per millisecond on average
        // this will last us 2^44 ms or > 500 years without repeating
        // because its implementation details, this can be relaxed a lot, if some bits are needed for packing additional data, or just reworked entirely
        nextUniqueHandleValue.set((((System.currentTimeMillis() << 4) + 1) & ((1L << HANDLE_VALUE_BITS) - 1L)) + uniqueHandleOffset);
    }

    private final HashMap<Long, GameObjectHandle> handles = new HashMap<>();

    GameObjectHandle getOrCreateHandle(long value) {
        return handles.computeIfAbsent(value, (Long _key) -> new GameObjectHandle(this, value));
    }

    GameObjectHandle createUniqueHandle() {
        return getOrCreateHandle(nextUniqueHandleValue.getAndAdd(1));
    }

    void expireHandle(GameObjectHandle handle) {
        if (handle.isExpired)
            return;
        if (handle.scope != this)
            throw new IllegalArgumentException("handle of different scope");
        handle.scene = null;
        handle.slotIdx = -1;
        handle.isExpired = true;
        handles.remove(handle.value);
    }

    // NOTE: Only call after all scenes of this scope are cleared
    @ApiHidden
    public void expireAllHandles() {
        for (GameObjectHandle handle : handles.values()) {
            handle.scene = null;
            handle.slotIdx = -1;
            handle.isExpired = true;
        }
        handles.clear();
    }

    public boolean isServerSide() {
        return isServerSide;
    }
}
