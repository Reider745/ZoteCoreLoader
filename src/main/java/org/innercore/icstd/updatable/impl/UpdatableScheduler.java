package org.innercore.icstd.updatable.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UpdatableScheduler {
    final short scopeId;
    final ReentrantReadWriteLock extLock = new ReentrantReadWriteLock();
    int curTick = 0;

    // Internal record
    private static class UpdatableRecord {
        boolean isAlive;
        boolean isTouchedThisTick;
        int generation;        // uint8_t, but range 0‑255

        // Union: scheduledAtBegin / freeListNext
        int scheduledAtBegin;  // uint32_t
        int freeListNext;      // used only when record is in free list
        int scheduledAtEnd;    // uint32_t
        int tickedAt;          // uint32_t (~0u initially)

        int rescheduleOnTickMin; // uint32_t
        int rescheduleOnTickMax; // uint32_t

        void reset() {
            isAlive = true;
            isTouchedThisTick = false;
            tickedAt = ~0;   // ~0u in C++ (all bits 1)
            scheduledAtBegin = scheduledAtEnd = ~0;
            rescheduleOnTickMin = rescheduleOnTickMax = 0;
        }
    }

    private final List<UpdatableRecord> updatables = new ArrayList<>();
    private int freeListHead = -1;
    private int freeListTail = -1;
    private int freeListCnt = 0;

    public UpdatableScheduler(short scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * Creates a new updatable and returns its handle.
     * Reuses a record from the free list if freeListCnt > 64,
     * otherwise appends a new one.
     */
    public UpdatableRef create() {
        extLock.writeLock().lock();
        try {
            if (freeListCnt > 64) {
                int idx = freeListHead;
                UpdatableRecord u = updatables.get(idx);
                freeListHead = u.freeListNext;
                freeListCnt--;
                u.reset();
                u.generation++;          // overflow is acceptable (wraps like uint8_t)
                return new UpdatableRef(idx, u.generation & 0xFF, scopeId);
            } else {
                int idx = updatables.size();
                UpdatableRecord u = new UpdatableRecord();
                u.reset();
                u.generation = 0;
                updatables.add(u);
                return new UpdatableRef(idx, 0, scopeId);
            }
        } finally {
            extLock.writeLock().unlock();
        }
    }

    /**
     * Checks whether a handle is still valid and alive.
     */
    public boolean exists(UpdatableRef ref) {
        extLock.readLock().lock();
        try {
            int idx = ref.getIdx();
            if (idx >= updatables.size()) return false;
            UpdatableRecord u = updatables.get(idx);
            return u.isAlive && (ref.getGen() == (u.generation & 0xFF));
        } finally {
            extLock.readLock().unlock();
        }
    }

    /**
     * Destroys an alive updatable, returning true on success.
     */
    public boolean destroy(UpdatableRef ref) {
        extLock.writeLock().lock();
        try {
            if (!exists(ref)) return false;
            int idx = ref.getIdx();
            UpdatableRecord u = updatables.get(idx);
            if (freeListCnt > 0) {
                updatables.get(freeListTail).freeListNext = idx;
            } else {
                freeListHead = idx;
            }
            freeListTail = idx;
            freeListCnt++;
            u.isAlive = false;
            return true;
        } finally {
            extLock.writeLock().unlock();
        }
    }

    /**
     * Schedules an updatable to be ticked between tick_in and tick_until (inclusive)
     * relative to current tick. Only changes the schedule if it would be sooner.
     */
    public void schedule(UpdatableRef ref, int tickIn, int tickUntil) {
        if (tickUntil < tickIn) tickUntil = tickIn;
        extLock.readLock().lock();   // C++ uses shared_lock for schedule
        try {
            if (!exists(ref) || tickIn <= 0) return;
            UpdatableRecord u = updatables.get(ref.getIdx());
            u.scheduledAtBegin = Math.min(u.scheduledAtBegin, curTick + tickIn);
            u.scheduledAtEnd   = Math.min(u.scheduledAtEnd,   curTick + tickUntil);
        } finally {
            extLock.readLock().unlock();
        }
    }

    /**
     * Sets a recurring schedule that will be re‑applied after each tick.
     */
    public void setRecurringSchedule(UpdatableRef ref, int tickIn, int tickUntil) {
        if (tickUntil < tickIn) tickUntil = tickIn;
        extLock.readLock().lock();
        try {
            if (!exists(ref)) return;
            UpdatableRecord u = updatables.get(ref.getIdx());
            u.rescheduleOnTickMin = tickIn;
            u.rescheduleOnTickMax = tickUntil;
        } finally {
            extLock.readLock().unlock();
        }
    }

    /**
     * Clears the immediate schedule without changing the recurring settings.
     */
    public void clearSchedule(UpdatableRef ref) {
        extLock.readLock().lock();
        try {
            if (!exists(ref)) return;
            UpdatableRecord u = updatables.get(ref.getIdx());
            u.tickedAt = ~0;
            u.scheduledAtBegin = u.scheduledAtEnd = ~0;
        } finally {
            extLock.readLock().unlock();
        }
    }

    /**
     * Marks all records as dead and builds a free list of the entire array.
     */
    public void clearAll() {
        extLock.writeLock().lock();
        try {
            int size = updatables.size();
            for (int i = 0; i < size; i++) {
                UpdatableRecord u = updatables.get(i);
                u.isAlive = false;
                u.freeListNext = i + 1;   // last element may point to garbage – never accessed
            }
            freeListHead = 0;
            freeListTail = size - 1;
            freeListCnt = size;
        } finally {
            extLock.writeLock().unlock();
        }
    }

    public int getAliveCnt() {
        extLock.readLock().lock();
        try {
            return updatables.size() - freeListCnt;
        } finally {
            extLock.readLock().unlock();
        }
    }

    // ---------------------------------------------------------
    // Tick processing (was JNI, now pure Java)
    // ---------------------------------------------------------

    /**
     * Called after a tick has finished – re‑schedules the record according to its
     * recurring settings (if any).
     */
    private void onTicked(UpdatableRecord u) {
        u.scheduledAtBegin = (u.rescheduleOnTickMin == 0) ? ~0 : curTick + u.rescheduleOnTickMin;
        u.scheduledAtEnd   = (u.rescheduleOnTickMax == 0) ? ~0 : curTick + u.rescheduleOnTickMax;
        u.tickedAt = curTick;
    }

    /**
     * Fills the provided array with indices of updatables that should be ticked now
     * (placed at the beginning) or later (placed at the end). The array must be
     * large enough. The first two elements hold the “now” and “exhaustive” positions
     * after the call. This mimics the C++ JNI helper.
     */
    public void scheduleAtTickForJava(int[] tickArr) {
        extLock.writeLock().lock();   // original does not lock – we lock for safety
        try {
            int nowPos = 2;                     // start after meta fields
            int exhaustivePos = tickArr.length; // start from the end

            for (int i = 0; i < updatables.size(); i++) {
                UpdatableRecord u = updatables.get(i);
                if (!u.isAlive) continue;

                u.isTouchedThisTick = true;   // mark for later verification

                if (u.scheduledAtEnd == curTick) {
                    // Should be ticked right now
                    tickArr[nowPos]     = i;
                    tickArr[nowPos + 1] = (u.tickedAt > curTick) ? 0 : curTick - u.tickedAt;
                    nowPos += 2;
                    onTicked(u);
                } else if (u.scheduledAtBegin >= curTick) {
                    // Will be ticked in the future
                    exhaustivePos -= 2;
                    tickArr[exhaustivePos]     = i;
                    tickArr[exhaustivePos + 1] = (u.tickedAt > curTick) ? 0 : curTick - u.tickedAt;
                }
            }

            // Store the current positions for the caller
            tickArr[0] = nowPos;
            tickArr[1] = exhaustivePos;
        } finally {
            extLock.writeLock().unlock();
        }
    }

    /**
     * Marks a batch of indices as ticked. Only affects records that were touched
     * during the current scheduling round and are still alive.
     */
    public void markBatchTickedForJava(int[] tickedIndices, int begin, int end) {
        extLock.writeLock().lock();
        try {
            for (int i = begin; i < end; i += 2) {
                int idx = tickedIndices[i];
                if (idx < 0 || idx >= updatables.size()) continue;
                UpdatableRecord u = updatables.get(idx);
                if (u.isTouchedThisTick && u.isAlive) {
                    onTicked(u);
                }
            }
        } finally {
            extLock.writeLock().unlock();
        }
    }
}