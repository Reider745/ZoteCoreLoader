package org.innercore.icstd.scene;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.GameObjectHandleBindGenBase;
import org.innercore.icstd.common.BlockPos;
import org.innercore.icstd.common.script.GenericScriptPos3;
import org.innercore.icstd.script.ScriptObjectComponent;


@ApiBinding
public class GameObjectHandle extends GameObjectHandleBindGenBase {
    final GameObjectManager scene;
    final long rawHandle;

    GameObjectHandle(GameObjectManager scene, long handle) {
        this.scene = scene;
        this.rawHandle = handle;
    }

    @ApiBinding
    public GameObjectHandle placeAsGlobal() {
        scene.placeAsGlobal(rawHandle);
        return this;
    }

    @ApiBinding
    public GameObjectManager getScene() {
        return scene;
    }

    public GameObjectHandle placeAtBlockPos(BlockPos pos) {
        scene.placeAtBlockPos(rawHandle, pos);
        return this;
    }

    @ApiBinding
    public GameObjectHandle placeAtBlockPos(int x, int y, int z) {
        return placeAtBlockPos(new BlockPos(x, y, z));
    }

    @ApiBinding
    public GameObjectHandle setObject(Object object) {
        scene.setObject(rawHandle, object);
        return this;
    }

    @ApiBinding
    public boolean destroyNow() { return scene.destroyNow(rawHandle); }

    @ApiBinding
    public void destroyDeferred() { scene.destroyDeferred(rawHandle); }

    public Object discard() { return scene.discard(rawHandle); }

    @ApiBinding("discard")
    public Object discardForScript() {
        Object discarded = discard();
        return discarded instanceof ScriptObjectComponent ? ((ScriptObjectComponent) discarded).getScriptObject() : discarded;
    }

    @ApiBinding
    public boolean isAlive() { return scene.isAlive(rawHandle); }

    @ApiBinding
    public boolean isLoaded() { return scene.isLoaded(rawHandle); }

    public BlockPos getBlockPos() { return scene.getBlockPos(rawHandle); }

    @ApiBinding("getBlockPos")
    public GenericScriptPos3 getBlockPosForScript() { return new GenericScriptPos3(getBlockPos()); }

    public Object getObject() { return scene.getObject(rawHandle); }

    @ApiBinding("getObject")
    public Object getObjectForScript() {
        Object object = scene.getObject(rawHandle);
        return object instanceof ScriptObjectComponent ? ((ScriptObjectComponent) object).getScriptObject() : object;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GameObjectHandle that = (GameObjectHandle) object;
        return rawHandle == that.rawHandle;
    }

    @Override
    public int hashCode() { return (int) rawHandle; }
}
