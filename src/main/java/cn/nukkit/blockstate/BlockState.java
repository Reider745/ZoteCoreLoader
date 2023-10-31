package cn.nukkit.blockstate;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import com.google.common.base.Objects;

import java.util.HashMap;

public class BlockState {
    private final int id;
    private final int data;
    private final HashMap<String, Integer> states;

    public BlockState(final int id, final int data, final HashMap<String, Integer> states){
        this.id = id;
        this.data = data;
        this.states = states;
    }

    public final int getBlockId() {
        return id;
    }

    public final int getData() {
        return data;
    }

    public HashMap<String, Integer> getStates() {
        return states;
    }

    public Block getBlockRepairing(Level level, BlockVector3 pos, int layer){
        return level.getBlock(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        return id == that.id && data == that.data && Objects.equal(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, data, states);
    }
}
