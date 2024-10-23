package com.zhekasmirnov.innercore.api.runtime;

import com.reider745.event.EventListener;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.LinkedList;

public class ChunkManager {
    public static void addChunkStateChagedListener(int state){
        EventListener.addChunkStateChagedListener(state);
    }

    public interface IChunkStateChanged {
        void onChunkStateChaged(int chunkX, int chunkZ, int dimension, int preState, int state, boolean discarded);
    }

    private static final HashMap<Integer, LinkedList<IChunkStateChanged>> serverStatesListener = new HashMap<>();
    private static final HashMap<Integer, LinkedList<IChunkStateChanged>> localStatesListener = new HashMap<>();

    private static final HashMap<Integer, LinkedList<Function>> serverJsStatesListener = new HashMap<>();
    private static final HashMap<Integer, LinkedList<Function>> localJsStatesListener = new HashMap<>();

    public static void addListenerChunkStateChaged(IChunkStateChanged changed, int[] states){
        for(int state : states)
            Java8BackComp.computeIfAbsent(serverStatesListener, state, _state -> {
                addChunkStateChagedListener(state);
                return new LinkedList<>();
            }).add(changed);
    }

    public static void addListenerChunkStateChaged(Function changed, NativeArray states){
        for(int i = 0;i < states.size();i++) {
            int state = ((Number) states.get(i)).intValue();

            Java8BackComp.computeIfAbsent(serverJsStatesListener, state, _state -> {
                addChunkStateChagedListener(state);
                return new LinkedList<>();
            }).add(changed);
        }
    }

    public static void addLocalListenerChunkStateChaged(IChunkStateChanged changed, int[] states){
        for(int state : states)
            Java8BackComp.computeIfAbsent(localStatesListener, state, _state -> {
                addChunkStateChagedListener(state);
                return new LinkedList<>();
            }).add(changed);
    }

    public static void addLocalListenerChunkStateChaged(Function changed, NativeArray states){
        for(int i = 0;i < states.size();i++) {
            int state = ((Number) states.get(i)).intValue();

            Java8BackComp.computeIfAbsent(localJsStatesListener, state, _state -> {
                addChunkStateChagedListener(state);
                return new LinkedList<>();
            }).add(changed);
        }
    }

    private static final LinkedList<IChunkStateChanged> EMPTY_LISTENERS = new LinkedList<>();
    private static final LinkedList<Function> EMPTY_FUNCTION = new LinkedList<>();

    public static void onChunkStateChaged(int dimension, int chunkX, int chunkZ, int state, int preState, boolean discarded){
        for(IChunkStateChanged changed : serverStatesListener.getOrDefault(state, EMPTY_LISTENERS))
            changed.onChunkStateChaged(chunkX, chunkZ, dimension, preState, state, discarded);

        final Context context = Compiler.assureContextForCurrentThread();
        final Object[] args = new Object[]{chunkX, chunkZ, dimension, preState, state, discarded};

        for(Function changed : serverJsStatesListener.getOrDefault(state, EMPTY_FUNCTION)) {
            final Scriptable parent = changed.getParentScope();
            changed.call(context, parent, parent, args);
        }
    }

    public static void onLocalChunkStateChaged(int dimension, int chunkX, int chunkZ, int state, int preState, boolean discarded){
        for(IChunkStateChanged changed : localStatesListener.getOrDefault(state, EMPTY_LISTENERS))
            changed.onChunkStateChaged(chunkX, chunkZ, dimension, preState, state, discarded);

        final Context context = Compiler.assureContextForCurrentThread();
        final Object[] args = new Object[]{chunkX, chunkZ, dimension, preState, state, discarded};

        for(Function changed : localJsStatesListener.getOrDefault(state, EMPTY_FUNCTION)) {
            final Scriptable parent = changed.getParentScope();
            changed.call(context, parent, parent, args);
        }
    }
}