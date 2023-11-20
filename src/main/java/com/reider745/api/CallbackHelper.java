package com.reider745.api;

import cn.nukkit.event.Event;
import cn.nukkit.level.Level;
import com.reider745.api.hooks.HookController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CallbackHelper {
    public enum Type {
        GENERATION_CHUNK_OVER_WORLD,
        GENERATION_CHUNK_NETHER,
        GENERATION_CHUNK_END;
    }

    private static final HashMap<Type, ThreadRegion> types = new HashMap<>();

    public interface ICallbackApply {
        void apply();
    }

    private static class ThreadRegion extends Thread {
        private Level region;
        private final ArrayList<ICallbackApply> applys = new ArrayList<>();

        private Level getRegion() {
            return region;
        }

        public void add(ICallbackApply apply){
            synchronized (applys) {
                this.applys.add(apply);
            }
        }

        @Override
        public void run() {
            while (true) {
                synchronized (applys) {
                    Iterator<ICallbackApply> it = applys.iterator();
                    while (it.hasNext())
                        it.next().apply();
                    applys.clear();
                }
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void registerCallback(Type type){
        ThreadRegion threadRegion = new ThreadRegion();
        threadRegion.start();

        types.put(type, threadRegion);
    }

    public static void init(){
        for(Type type : Type.values())
            registerCallback(type);
    }

    private static class ThreadCallback extends Thread {
        protected final ICallbackApply apply;

        protected boolean prevent_status;

        public boolean isPrevent() {
            return prevent_status;
        }

        public void prevent() {
            this.prevent_status = true;
        }


        public ThreadCallback(ICallbackApply apply){
            this.apply = apply;
        }
    }

    private static class ThreadCallbackEvent extends ThreadCallback {
        protected final Event event;
        public ThreadCallbackEvent(Event event, ICallbackApply apply) {
            super(apply);
            this.event = event;
        }

        @Override
        public void run() {
            apply.apply();
            event.setCancelled(isPrevent());
        }
    }

    private static class ThreadCallbackController extends ThreadCallback {
        protected final HookController event;
        public ThreadCallbackController(HookController event, ICallbackApply apply) {
            super(apply);
            this.event = event;
        }

        @Override
        public void run() {
            apply.apply();
            event.setResult(isPrevent());
        }
    }

    public static void applyRegion(Type type, Level level, ICallbackApply apply){
        ThreadRegion threadRegion = types.get(type);
        threadRegion.region = level;
        threadRegion.add(apply);
    }

    public static void apply(Event event, ICallbackApply apply){
        Thread thread = new ThreadCallbackEvent(event, apply);
        thread.start();
        while(thread.isAlive()){}
    }

    public static void apply(HookController controller, ICallbackApply apply){
        Thread thread = new ThreadCallbackController(controller, apply);
        thread.start();
        while(thread.isAlive()){}
    }


    public static void prevent(){
        Thread thread = Thread.currentThread();

        if(thread instanceof ThreadCallback threadCallback)
            threadCallback.prevent();
    }

    public static boolean isPrevent(){
        Thread thread = Thread.currentThread();

        if(thread instanceof ThreadCallback threadCallback)
            return threadCallback.isPrevent();
        return false;
    }

    public static Level getForCurrentThread(){
        Thread thread = Thread.currentThread();

        if(thread instanceof ThreadRegion threadCallback)
            return threadCallback.getRegion();
        return null;
    }
}
