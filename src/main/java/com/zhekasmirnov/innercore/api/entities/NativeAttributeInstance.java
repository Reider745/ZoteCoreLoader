package com.zhekasmirnov.innercore.api.entities;

import com.reider745.InnerCoreServer;

@SuppressWarnings("unused")
public class NativeAttributeInstance {
    private final long entity;
    private final String attribute;

    public NativeAttributeInstance(long entity, String attribute) {
        this.entity = entity;
        this.attribute = attribute;
    }

    public float getValue() {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.getValue()");
        return 0.0f;
    }

    public float getMaxValue() {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.getMaxValue()");
        return 0.0f;
    }

    public float getMinValue() {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.getMinValue()");
        return 0.0f;
    }

    public float getDefaultValue() {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.getDefaultValue()");
        return 0.0f;
    }

    public void setMaxValue(float value) {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.setMaxValue(value)");
    }

    public void setMinValue(float value) {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.setMinValue(value)");
    }

    public void setDefaultValue(float value) {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.setDefaultValue(value)");
    }

    public void setValue(float value) {
        InnerCoreServer.useNotCurrentSupport("NativeAttributeInstance.setValue(value)");
    }
}
