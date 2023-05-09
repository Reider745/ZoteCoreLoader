package com.zhekasmirnov.apparatus.ecs.types.item;

import com.zhekasmirnov.apparatus.ecs.core.TypeIndexMap;

public class ItemComponent {
    public static final int COMPONENT_ID = TypeIndexMap.getTypeIndex("ItemComponent");

    public final int id;
    public final String nameId;
    public final String name;

    public ItemComponent(int id, String nameId, String name) {
        this.id = id;
        this.nameId = nameId;
        this.name = name;
    }
}
