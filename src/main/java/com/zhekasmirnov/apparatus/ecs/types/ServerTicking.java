package com.zhekasmirnov.apparatus.ecs.types;

import com.zhekasmirnov.apparatus.ecs.core.DelayedActionQueue;

public interface ServerTicking {
    void tick(DelayedActionQueue queue);
}
