package org.innercore.icstd.component;

import org.innercore.icstd.scene.GameObjectHandle;

public interface SceneHandleListenerComponent {
    void onAddedToScene(GameObjectHandle handle);
    void onRemovedFromScene(GameObjectHandle handle);
}
