package org.innercore.icstd.event.impl.level;

import org.innercore.icstd.event.IScriptEvent;
import org.innercore.icstd.event.ScriptEventName;

@ScriptEventName("LevelLeft")
public class LevelLeftEvent implements IScriptEvent {
    private final boolean isServer;

    public LevelLeftEvent(Object[] args) {
        this.isServer = ((Boolean) args[0]);
    }

    public LevelLeftEvent(boolean entityUid) {
        this.isServer = entityUid;
    }

    public boolean isServer() {
        return isServer;
    }

    @Override
    public Object[] asScriptArgs(String name) {
        return new Object[] {isServer};
    }
}