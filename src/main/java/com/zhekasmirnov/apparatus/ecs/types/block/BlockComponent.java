package com.zhekasmirnov.apparatus.ecs.types.block;

import com.zhekasmirnov.apparatus.ecs.core.TypeIndexMap;

public class BlockComponent {
    public static final int COMPONENT_ID = TypeIndexMap.getTypeIndex("BlockComponent");

    public final int id;
    public final String nameId;
    public final String name;

    public BlockComponent(int id, String nameId, String name) {
        this.id = id;
        this.nameId = nameId;
        this.name = name;
    }
}
