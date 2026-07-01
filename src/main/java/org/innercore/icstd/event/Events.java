package org.innercore.icstd.event;

import java.util.*;

public class Events {
    private static class EventListenerData {
        Object listener;
        private int index;

        private int priority = 0;

        public EventListenerData(Object listener, int index) {
            this.listener = listener;
            this.index = index;
        }
    }

    private static class EventListenerRecord {
        String name = null;
        boolean isManaged = false;
        boolean hasJavaListeners = false;
        boolean hasScriptListeners = false;
        List<Object> listeners = new ArrayList<>();
        List<EventListenerData> listenersData = new ArrayList<>();

        EventBuilder addListener(Object listener) {
            EventListenerData data = new EventListenerData(listener, listeners.size());
            listeners.add(listener);
            listenersData.add(data);
            isManaged = isManaged || listener instanceof IManagedEventListener<?>;
            // FIXME:
            // isManaged = isManaged || (listener instanceof IScriptEventListener && ((IScriptEventListener) listener).isEventCtrlRequired());
            hasScriptListeners = hasScriptListeners || listener instanceof IScriptEventListener;
            hasJavaListeners = hasJavaListeners || !(listener instanceof IScriptEventListener);
            return new EventBuilder(this, listener, data);
        }

        void onOrderChanged() {
            listenersData.sort(Comparator.comparingInt(data -> data.priority));
            for (int i = 0; i < listenersData.size(); i++) {
                EventListenerData data = listenersData.get(i);
                data.index = i;
                listeners.set(i, data.listener);
            }
        }
    }

    private static final HashMap<Class<?>, EventListenerRecord> eventRecordByClass = new HashMap<>();
    private static final HashMap<String, EventListenerRecord> eventRecordByName = new HashMap<>();
    private static final HashMap<String, Class<?>> scriptEventClsByName = new HashMap<>();
    private static final HashMap<String, IScriptEventConverter<?>> scriptEventConverters = new HashMap<>();

    public static int count(String name) {
        EventListenerRecord record = eventRecordByName.get(name);
        if(record == null) return 0;
        return record.listeners.size();
    }

    public static class EventBuilder {
        final EventListenerRecord eventRecord;
        final Object listener;
        final EventListenerData data;

        EventBuilder(EventListenerRecord eventRecord, Object listener, EventListenerData data) {
            this.eventRecord = eventRecord;
            this.listener = listener;
            this.data = data;
        }

        public EventBuilder setPriority(int priority) {
            this.data.priority = priority;
            this.eventRecord.onOrderChanged();
            return this;
        }
    }

    private static String getAndValidateScriptEventClassName(Class<?> eventClass) {
        ScriptEventName ann = eventClass.getAnnotation(ScriptEventName.class);
        if (ann == null || ann.value().isEmpty())
            throw new IllegalArgumentException("Event class " + eventClass + " must be annotated with @AnnotateEvent with non-empty name");
        return ann.value();
    }

    private static void reportClsNameConflict(String eventName, Class<?> newCls, Class<?> registeredCls) {
        System.out.println("Found conflicting event classes for event name '" + eventName + "' " + registeredCls.getName() + " != " + newCls.getName() + " latter will be skipped");
        // todo:
    }

    private static EventBuilder onEventImpl(Class<?> eventCls, Object listener) {
        EventListenerRecord rec = eventRecordByClass.get(eventCls);
        if (rec == null) {
            rec = new EventListenerRecord();
            // handle aliasing script events by name
            if (IScriptEvent.class.isAssignableFrom(eventCls)) {
                String name = getAndValidateScriptEventClassName(eventCls);
                Class<?> registeredCls = scriptEventClsByName.get(name);
                if (registeredCls == null) {
                    try {
                        Class.forName(eventCls.getName(), true, eventCls.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    eventRecordByName.put(name, rec);
                    scriptEventClsByName.put(name, eventCls);
                } else if (registeredCls.equals(eventCls)) {
                    rec = eventRecordByName.get(name);
                } else {
                    reportClsNameConflict(name, eventCls, registeredCls);
                }
            }
            eventRecordByClass.put(eventCls, rec);
        }

        return rec.addListener(listener);
    }

    private static EventBuilder onEventImpl(String eventName, Object listener) {
        EventListenerRecord rec = eventRecordByName.get(eventName);
        if (rec == null) {
            rec = new EventListenerRecord();
            eventRecordByName.put(eventName, rec);
        }
        return rec.addListener(listener);
    }

    private static void invokeImpl(EventListenerRecord rec, Object event, EventController ctrl, Object[] scriptArgs) {
        final boolean isManaged = rec.isManaged;
        if (isManaged && ctrl == null)
            ctrl = new EventController();
        final boolean catchExceptions = ctrl != null && ctrl.catchExceptions;

        if (event == null) {
            // separate path to script-to-script events
            for (Object listener : rec.listeners) {
                if (listener instanceof IScriptEventListener) {
                    try {
                        ((IScriptEventListener) listener).onEvent(ctrl, scriptArgs);
                    } catch (Throwable t) {
                        if (catchExceptions) {
                            if (ctrl.errors == null) ctrl.errors = new ArrayList<>();
                            ctrl.errors.add(t);
                        } else throw t;
                    }
                }
            }
            return;
        }
        final boolean isScriptEvent = event instanceof IScriptEvent;
        if ((!isScriptEvent && !rec.hasScriptListeners) && !isManaged) {
            // fast path for unmanaged and not script events
            for (Object listener : rec.listeners) {
                try {
                    ((IEventListener<Object>) listener).onEvent(event);
                } catch (Throwable t) {
                    if (catchExceptions) {
                        if (ctrl.errors == null) ctrl.errors = new ArrayList<>();
                        ctrl.errors.add(t);
                    } else throw t;
                }
            }
            return;
        }
        // slow path for mixed listeners
        for (Object listener : rec.listeners) {
            if (isManaged) {
                // todo:
            }
            try {
                if (listener instanceof IEventListener<?>) {
                    ((IEventListener<Object>) listener).onEvent(event);
                } else if (listener instanceof IManagedEventListener<?>) {
                    ((IManagedEventListener<Object>) listener).onEvent(ctrl, event);
                } else if (listener instanceof IScriptEventListener) {
                    if (isScriptEvent) {
                        if (scriptArgs == null)
                            scriptArgs = ((IScriptEvent) event).asScriptArgs(rec.name);
                        ((IScriptEventListener) listener).onEvent(ctrl, scriptArgs);
                    }
                }
            } catch (Throwable t) {
                if (catchExceptions) {
                    if (ctrl.errors == null) ctrl.errors = new ArrayList<>();
                    ctrl.errors.add(t);
                } else throw t;
            }
        }
    }

    //

    public static<T> EventBuilder on(Class<T> eventCls, IEventListener<T> listener) { return onEventImpl(eventCls, listener); }

    public static<T> EventBuilder on(Class<T> eventCls, IManagedEventListener<T> listener) { return onEventImpl(eventCls, listener); }

    public static<T> EventBuilder onScriptEvent(String eventName, IScriptEventListener listener) { return onEventImpl(eventName, listener); }

    public static<T> void invoke(EventController ctrl, T event) {
        EventListenerRecord rec = eventRecordByClass.get(event.getClass());
        if (rec != null)
            invokeImpl(rec, event, ctrl, null);
    }

    public static<T> void invoke(T event) { invoke(null, event);}

    public static<T> void addScriptEventConverter(Class<T> eventCls, IScriptEventConverter<T> cvt) {
        String name = getAndValidateScriptEventClassName(eventCls);
        if (scriptEventClsByName.containsKey(name)) {
            reportClsNameConflict(name, eventCls, scriptEventClsByName.get(name));
            return;
        }
        scriptEventConverters.put(name, cvt);
    }

    public static void invokeScriptEvent(EventController ctrl, String name, Object[] scriptArgs) {
        EventListenerRecord rec = eventRecordByName.get(name);
        if (rec == null)
            return;
        Object event = null;
        if (rec.hasJavaListeners) {
            IScriptEventConverter<?> converter = scriptEventConverters.get(name);
            if (converter != null)
                event = converter.toEvent(name, scriptArgs);
        }
        if (ctrl == null)
            ctrl = new EventController();
        ctrl.scriptArgs = scriptArgs;
        invokeImpl(rec, event, ctrl, scriptArgs);
    }
}
