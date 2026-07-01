package org.innercore.icstd.event;

import java.util.List;

public class EventController {
    public boolean catchExceptions = false;
    public List<Throwable> errors = null;

    // used for caching mostly
    public Object scriptCtx = null;
    public Object scriptScope = null;
    public Object scriptEvent = null;
    public Object[] scriptArgs = null;
}
