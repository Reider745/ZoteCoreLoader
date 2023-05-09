package com.zhekasmirnov.apparatus.ecs.types;

import com.zhekasmirnov.apparatus.ecs.core.DelayedActionQueue;

public interface LocalTicking {
    void tick(DelayedActionQueue queue);
}
