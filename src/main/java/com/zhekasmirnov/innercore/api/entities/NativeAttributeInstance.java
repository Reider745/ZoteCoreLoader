package com.zhekasmirnov.innercore.api.entities;

public class NativeAttributeInstance {
    private final long entity;
    private final String attribute;

    public NativeAttributeInstance(long entity, String attribute){
        this.entity = entity;
        this.attribute = attribute;
    }

    public float getValue(){
        return getEntityAttributeValue(entity, attribute);
    }

    public float getMaxValue(){
        return getEntityAttributeMaxValue(entity, attribute);
    }

    public float getMinValue(){
        return getEntityAttributeMinValue(entity, attribute);
    }

    public float getDefaultValue(){
        return getEntityAttributeDefaultValue(entity, attribute);
    }

    public void setMaxValue(float value){
        setEntityAttributeMaxValue(entity, attribute, value);
    }

    public void setMinValue(float value){
        setEntityAttributeMinValue(entity, attribute, value);
    }

    public void setDefaultValue(float value){
        setEntityAttributeDefaultValue(entity, attribute, value);
    }

    public void setValue(float value){
        setEntityAttributeDefaultValue(entity, attribute, value);
    }

    private static native float getEntityAttributeValue(long entity, String attribute);
    private static native float getEntityAttributeMaxValue(long entity, String attribute);
    private static native float getEntityAttributeMinValue(long entity, String attribute);
    private static native float getEntityAttributeDefaultValue(long entity, String attribute);
    private static native void setEntityAttributeMaxValue(long entity, String attribute, float value);
    private static native void setEntityAttributeMinValue(long entity, String attribute, float value);
    private static native void setEntityAttributeDefaultValue(long entity, String attribute, float value);
    private static native void setEntityAttributeValue(long entity, String attribute, float value);
}