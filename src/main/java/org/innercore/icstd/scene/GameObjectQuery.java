package org.innercore.icstd.scene;

import java.util.Iterator;
import java.util.List;

public interface GameObjectQuery<T> extends Iterable<T> {
    boolean isEmpty();

    T one();
    List<T> toList();

    GameObjectHandle oneHandle();
    Iterator<GameObjectHandle> iterHandles();
    List<GameObjectHandle> toHandleList();
}
