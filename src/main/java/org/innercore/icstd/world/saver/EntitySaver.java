package org.innercore.icstd.world.saver;

import lombok.NonNull;
import org.innercore.icstd.annotations.ApiHidden;
import org.innercore.icstd.entity.EntityModule;
import org.innercore.icstd.serialization.*;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EntitySaver<T> {
    private final String id;

    public EntitySaver(String id) {
        this.id = id;
    }

    @ApiHidden
    public String getId() {
        return this.id;
    }

    @NonNull
    public IScopedWriter writeUserData(long uid) {
        return EntityModule.writeUserData(uid, this.id);
    }

    @Nullable
    public IScopedReader readUserData(long uid) {
        return EntityModule.readUserData(uid, this.id);
    }

    @Nullable
    public T getForUid(long uid, T def) {
        return EntityModule.getCustomComponent(uid, this.id, def);
    }

    @Nullable
    public T getForUid(long uid) {
        return getForUid(uid, null);
    }

    public void putForUid(long uid, T data) {
        EntityModule.putCustomComponent(uid, this.id, data);
    }

    public boolean contains(long uid) {
        Map<Long, EntityModule.EntityUserData> userData = EntityModule.getEntityUserData();
        return userData.containsKey(uid) && userData.get(uid).hasScope(this.id);
    }

    public void remove(long uid) {
        Map<Long, EntityModule.EntityUserData> userData = EntityModule.getEntityUserData();
        if(userData.containsKey(uid)) userData.get(uid).remove(this.id);
    }

    public void clearAll() {
        Map<Long, EntityModule.EntityUserData> userData = EntityModule.getEntityUserData();
        for(EntityModule.EntityUserData scopeData : userData.values()) {
            scopeData.clearAll(this.id);
        }
    }
}
