package com.reider745.behavior.entities;

public abstract class ComponentsTag {
    public abstract String getNameTag();
    public int getPriority(){
        return 0;
    }

    public void load(EntityContentFactory factory, Object json){

    }
}
