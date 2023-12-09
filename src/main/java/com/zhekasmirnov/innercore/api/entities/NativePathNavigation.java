package com.zhekasmirnov.innercore.api.entities;

import java.util.HashMap;

import com.reider745.InnerCoreServer;

public class NativePathNavigation {
    private final long entity;
    private final long pointer = 0;
    private boolean isValid = true;

    private static HashMap<Long, NativePathNavigation> activeNavigationMap = new HashMap<>();

    public static NativePathNavigation getNavigation(long entity) {
        NativePathNavigation result = activeNavigationMap.get(entity);
        if (result != null) {
            return result;
        }
        result = new NativePathNavigation(entity);
        activeNavigationMap.put(entity, result);
        return result;
    }

    public static void onNavigationResult(long entity, int result) {
        NativePathNavigation navigation = getNavigation(entity);
        if (navigation.resultFunction != null) {
            navigation.resultFunction.onNavigationResult(navigation, result);
        }
    }

    private NativePathNavigation(long entity) {
        this.entity = entity;
        // if (pointer == 0) {
            // throw new IllegalArgumentException("Invalid mob was passed to NativePathNavigation constructor: " + entity);
        // }
    }

    public long getEntity() {
        return entity;
    }

    public static void cleanup() {
        for (NativePathNavigation navigation : activeNavigationMap.values()) {
            navigation.isValid = false;
        }
        activeNavigationMap.clear();
    }

    interface NavigationResultFunction {
        void onNavigationResult(NativePathNavigation navigation, int result);
    }

    NavigationResultFunction resultFunction;

    public NativePathNavigation setResultFunction(NavigationResultFunction resultFunction) {
        if (this.resultFunction != null) {
            this.resultFunction.onNavigationResult(this, 6); // FUNCTION_REPLACED
        }
        this.resultFunction = resultFunction;
        return this;
    }

    public NativePathNavigation moveToCoords(float x, float y, float z, float velocity) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.moveToCoords(x, y, z, velocity)");
        return this;
    }

    public NativePathNavigation moveToEntity(long entity, float velocity) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.moveToEntity(entity, velocity)");
        return this;
    }

    public NativePathNavigation setMaxNavigationDistance(float distance) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setMaxNavigationDistance(distance)");
        return this;
    }

    public float getMaxNavigationDistance() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getMaxNavigationDistance()");
        return 0;
    }

    public NativePathNavigation stop() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.stop()");
        return this;
    }

    public NativePathNavigation setType(int type) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setType(type)");
        return this;
    }

    public boolean isDone() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isDone()");
        return false;
    }

    public boolean canPassDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.canPassDoors()");
        return false;
    }

    public NativePathNavigation setCanPassDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanPassDoors(value)");
        return this;
    }

    public boolean isRiverFollowing() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isRiverFollowing()");
        return false;
    }

    public NativePathNavigation setIsRiverFollowing(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setIsRiverFollowing(value)");
        return this;
    }

    public boolean canOpenDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.canOpenDoors()");
        return false;
    }

    public NativePathNavigation setCanOpenDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanOpenDoors(value)");
        return this;
    }

    public boolean getAvoidSun() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidSun()");
        return false;
    }

    public NativePathNavigation setAvoidSun(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidSun(value)");
        return this;
    }

    public boolean getAvoidWater() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidWater()");
        return false;
    }

    public NativePathNavigation setAvoidWater(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidWater(value)");
        return this;
    }

    public NativePathNavigation setEndPathRadius(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setEndPathRadius(value)");
        return this;
    }

    public boolean getCanSink() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanSink()");
        return false;
    }

    public NativePathNavigation setCanSink(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanSink(value)");
        return this;
    }

    public boolean getAvoidDamageBlocks() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidDamageBlocks()");
        return false;
    }

    public NativePathNavigation setAvoidDamageBlocks(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidDamageBlocks(value)");
        return this;
    }

    public boolean getCanFloat() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanFloat()");
        return false;
    }

    public NativePathNavigation setCanFloat(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanFloat(value)");
        return this;
    }

    public boolean isAmphibious() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isAmphibious()");
        return false;
    }

    public NativePathNavigation setIsAmphibious(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setIsAmphibious(value)");
        return this;
    }

    public boolean getAvoidPortals() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidPortals()");
        return false;
    }

    public NativePathNavigation setAvoidPortals(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidPortals(value)");
        return this;
    }

    public boolean getCanBreach() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanBreach()");
        return false;
    }

    public NativePathNavigation setCanBreach(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanBreach(value)");
        return this;
    }

    public boolean getCanJump() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanJump()");
        return false;
    }

    public NativePathNavigation setCanJump(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanJump(value)");
        return this;
    }

    public float getSpeed() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getSpeed()");
        return 0;
    }

    public NativePathNavigation setSpeed(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setSpeed(value)");
        return this;
    }

    public boolean getCanPathOverLava() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanPathOverLava()");
        return false;
    }

    public NativePathNavigation setCanPathOverLava(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanPathOverLava(value)");
        return this;
    }

    public boolean getCanWalkInLava() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanWalkInLava()");
        return false;
    }

    public NativePathNavigation setCanWalkInLava(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanWalkInLava(value)");
        return this;
    }

    public boolean getCanOpenIronDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanOpenIronDoors()");
        return false;
    }

    public NativePathNavigation setCanOpenIronDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanOpenIronDoors(value)");
        return this;
    }

    public boolean getHasEndPathRadius() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getHasEndPathRadius()");
        return false;
    }

    public NativePathNavigation setHasEndPathRadius(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setHasEndPathRadius(value)");
        return this;
    }

    public float getTerminationThreshold() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getTerminationThreshold()");
        return 0;
    }

    public NativePathNavigation getTerminationThreshold(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getTerminationThreshold(value)");
        return this;
    }

    public int getTickTimeout() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getTickTimeout()");
        return 0;
    }

    public NativePathNavigation setTickTimeout(int value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setTickTimeout(value)");
        return this;
    }

    public boolean isStuck(int ticks) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isStuck(ticks)");
        return false;
    }
}
