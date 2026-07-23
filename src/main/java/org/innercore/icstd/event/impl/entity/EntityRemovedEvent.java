package org.innercore.icstd.event.impl.entity;

import org.innercore.icstd.event.IScriptEvent;
import org.innercore.icstd.event.ScriptEventName;

@ScriptEventName("EntityRemoved")
public class EntityRemovedEvent implements IScriptEvent {
    private final long entityUid;

    public EntityRemovedEvent(Object[] args) {
        this.entityUid = ((Number) args[0]).longValue();
    }

    public EntityRemovedEvent(long entityUid) {
        this.entityUid = entityUid;
    }

    public long getEntityUid() {
        return entityUid;
    }

    @Override
    public Object[] asScriptArgs(String name) {
        return new Object[] {entityUid};
    }
}