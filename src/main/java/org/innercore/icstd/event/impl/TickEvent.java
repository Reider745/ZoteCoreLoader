package org.innercore.icstd.event.impl;

import org.innercore.icstd.event.IScriptEvent;
import org.innercore.icstd.event.ScriptEventName;

@ScriptEventName("tick")
public class TickEvent implements IScriptEvent {
    private static final Object[] ARGS = new Object[0];

    public TickEvent(Object[] args) {}

    public TickEvent() {}

    @Override
    public Object[] asScriptArgs(String name) {
        return ARGS;
    }
}