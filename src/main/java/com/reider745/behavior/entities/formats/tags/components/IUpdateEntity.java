package com.reider745.behavior.entities.formats.tags.components;

import com.reider745.behavior.entities.CustomEntity;

public interface IUpdateEntity {
    void onUpdate(CustomEntity entity);
    default int getPriority(){return 0;}
}
