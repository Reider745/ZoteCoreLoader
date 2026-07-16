package org.innercore.icstd.scene;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GameObjectHandleBindGenBase;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.innercore.icstd.script.ScriptObjectComponent;


// NOTE: hashCode and equals are deliberately not overridden! handles are unique objects stored in a corresponding scope map
@ApiBinding
public class GameObjectHandle extends GameObjectHandleBindGenBase {
    final long value;
    final GameObjectScope scope;
    GameObjectManager scene = null;
    int slotIdx = -1;
    boolean isExpired = false;

    GameObjectHandle(GameObjectScope scope, long handle) {
        this.scope = scope;
        this.value = handle;
    }

    @ApiBinding
    public GameObjectManager getScene() {
        return scene;
    }

    @ApiBinding
    public long getHandleValue() {
        return value;
    }

    public GameObjectScope getScope() {
        return scope;
    }

    @ApiBinding
    public GameObjectHandle placeAsGlobal() {
        if (slotIdx < 0) {
            throw new IllegalStateException("placeAsGlobal on dead handle");
        }
        scene.placeAsGlobal(slotIdx);
        return this;
    }

    public GameObjectHandle placeAtBlockPos(BlockPos pos) {
        if (slotIdx < 0) {
            throw new IllegalStateException("placeAtBlockPos on dead handle");
        }
        scene.placeAtBlockPos(slotIdx, pos);
        return this;
    }

    @ApiBinding
    public GameObjectHandle placeAtBlockPos(int x, int y, int z) {
        return placeAtBlockPos(new BlockPos(x, y, z));
    }

    @ApiBinding
    public GameObjectHandle transferToScene(GameObjectManager toScene) {
        if (slotIdx < 0) {
            throw new IllegalStateException("transferTo on dead handle");
        }
        if (scene == toScene) {
            return this;
        }
        if (toScene.getObjectScope() != scope) {
            throw new IllegalArgumentException("transferTo scene with different scope");
        }
        Object o = scene.unlinkObject(slotIdx);
        assert slotIdx < 0; // must be unlinked at this point
        toScene.createImpl(this, o);
        assert slotIdx >= 0; // verify it is re-linked again
        return this;
    }

    @ApiBinding
    public GameObjectHandle setObject(Object object) {
        if (slotIdx < 0) {
            throw new IllegalStateException("placeAtBlockPos on dead handle");
        }
        scene.setObject(slotIdx, object);
        return this;
    }

    @ApiBinding
    public boolean destroyNow() {
        return slotIdx >= 0 && scene.destroyNow(slotIdx);
    }

    @ApiBinding
    public void destroyDeferred() {
        if (slotIdx >= 0)
            scene.destroyDeferred(slotIdx);
    }

    public Object discardNow() { return slotIdx >= 0 ? scene.discardNow(slotIdx) : null; }

    @ApiBinding("discardNow")
    public Object discardNowForScript() {
        Object discarded = discardNow();
        return discarded instanceof ScriptObjectComponent ? ((ScriptObjectComponent) discarded).getScriptObject() : discarded;
    }

    @ApiBinding
    public boolean isExpired() { return isExpired; }

    @ApiBinding
    public boolean isAlive() { return slotIdx >= 0; }

    @ApiBinding
    public boolean isLoaded() { return slotIdx >= 0 && scene.isLoaded(slotIdx); }

    public BlockPos getBlockPos() { return slotIdx >= 0 ? scene.getBlockPos(slotIdx) : null; }

    @ApiBinding("getBlockPos")
    public GenericScriptPos3 getBlockPosForScript() { return new GenericScriptPos3(getBlockPos()); }

    public Object getObject() { return slotIdx >= 0 ? scene.getObject(slotIdx) : null; }

    @ApiBinding("getObject")
    public Object getObjectForScript() {
        Object object = getObject();
        return object instanceof ScriptObjectComponent ? ((ScriptObjectComponent) object).getScriptObject() : object;
    }

    @Override
    public String toString() {
        return "[Handle " + value + "]";
    }
}
