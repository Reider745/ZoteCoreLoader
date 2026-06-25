package org.innercore.icstd.updatable;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.UpdatableHandleBindGenBase;

@ApiBinding("UpdatableHandle")
public class UpdatableHandle extends UpdatableHandleBindGenBase {
    private long handle;

    @ApiBinding
    public UpdatableHandle(long handle) {
        this.handle = handle;
    }

    @ApiBinding
    public long getHandle() { return handle; }

    /* @ApiBinding
    public long replaceUpdatableHandle(long handle) {
        long last = this.handle;
        this.handle = handle;
        return last;
    } */

    @ApiBinding
    public boolean isAlive() { return handle != -1 && UpdatableScheduler.isRawHandleAlive(handle); }
    @ApiBinding public UpdatableHandle schedule(int tickIn, int tickUntil) { UpdatableScheduler.scheduleByRawHandle(handle, tickIn, tickUntil); return this; }
    @ApiBinding public UpdatableHandle schedule(int tickIn) { UpdatableScheduler.scheduleByRawHandle(handle, tickIn); return this; }
    @ApiBinding public UpdatableHandle setRecurringSchedule(int tickIn, int tickUntil) { UpdatableScheduler.setRecurringScheduleByRawHandle(handle, tickIn, tickUntil); return this; }
    @ApiBinding public UpdatableHandle setRecurringSchedule(int tickIn) { UpdatableScheduler.setRecurringScheduleByRawHandle(handle, tickIn); return this;}
    @ApiBinding public UpdatableHandle clearSchedule() { UpdatableScheduler.clearScheduleByRawHandle(handle); return this; }

    public UpdatableComponent getObject() { return UpdatableScheduler.getByRawHandle(handle); }
    @ApiBinding("getObject") public Object getObjectForScript() { return UpdatableScheduler.getByRawHandleForScript(handle); }
    @ApiBinding public UpdatableSchedulerScope getScope() { return UpdatableScheduler.getScopeByRawHandle(handle); }

    @ApiBinding
    public boolean destroy() {
        boolean result = UpdatableScheduler.destroyRawHandle(handle);
        handle = -1;
        return result;
    }
}
