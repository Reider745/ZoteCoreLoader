package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import org.mozilla.javascript.Scriptable;

import java.util.Map;

// this is a stub class for DungeonUtility, it MUST NOT be used in code
@Deprecated
public class BlockState extends org.innercore.icstd.region.BlockState {
    public BlockState(int id, int data) {
        super(id, data);
    }

    public BlockState(int id, Map<?, Integer> map) {
        super(id, map);
    }

    public BlockState(long ptr, int refCnt, Object stub) {
        super(ptr, refCnt, stub);
    }

    public BlockState(int id, Scriptable scriptable) {
        super(id, scriptable);
    }

    public BlockState(long idDataAndState) {
        super(idDataAndState, null);
    }
}
