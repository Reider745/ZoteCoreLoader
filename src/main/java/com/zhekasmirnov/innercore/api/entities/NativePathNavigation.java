package com.zhekasmirnov.innercore.api.entities;

import java.util.HashMap;

import com.zhekasmirnov.apparatus.adapter.innercore.game.common.Vector3;

public class NativePathNavigation {
    private final long entity;
    private final long pointer;
    private boolean isValid = true;

    private static HashMap<Long, NativePathNavigation> activeNavigationMap = new HashMap<>();

    private static native long nativeConstruct(long entity);
    private static native void nativeCleanup();

    public static NativePathNavigation getNavigation(long entity){
        NativePathNavigation result = activeNavigationMap.get(entity);
        if(result != null){
            return result;
        } 
        result = new NativePathNavigation(entity);
        activeNavigationMap.put(entity, result);
        return result;
    }

    public static void onNavigationResult(long entity, int result){
        NativePathNavigation navigation = getNavigation(entity);
        if(navigation.resultFunction != null){
            navigation.resultFunction.onNavigationResult(navigation, result);
        }
    }


    private NativePathNavigation(long entity){
        this.entity = entity;
        this.pointer = nativeConstruct(entity);
        if(pointer == 0){
            throw new IllegalArgumentException("Invalid mob was passed to NativePathNavigation constructor: " + entity);
        }
    }

    public long getEntity(){
        return entity;
    }

    public static void cleanup(){
        for(NativePathNavigation navigation: activeNavigationMap.values()){
            navigation.isValid = false;
        }
        activeNavigationMap.clear();
        nativeCleanup();
    }

    interface NavigationResultFunction {
        void onNavigationResult(NativePathNavigation navigation, int result);
    }

    NavigationResultFunction resultFunction;

    public NativePathNavigation setResultFunction(NavigationResultFunction resultFunction){
        if (this.resultFunction != null) {
            this.resultFunction.onNavigationResult(this, 6); // FUNCTION_REPLACED
        }
        this.resultFunction = resultFunction;
        return this;
    }

    public native NativePathNavigation moveToCoords(float x, float y, float z, float velocity);
    public native NativePathNavigation moveToEntity(long entity, float velocity);
    public native NativePathNavigation setMaxNavigationDistance(float distance);
    public native float getMaxNavigationDistance();

    public native NativePathNavigation stop();
    public native NativePathNavigation setType(int type);
    public native boolean isDone();

    public native boolean canPassDoors();
    public native NativePathNavigation setCanPassDoors(boolean value);

    public native boolean isRiverFollowing();
    public native NativePathNavigation setIsRiverFollowing(boolean value);

    public native boolean canOpenDoors();
    public native NativePathNavigation setCanOpenDoors(boolean value);

    public native boolean getAvoidSun();
    public native NativePathNavigation setAvoidSun(boolean value);

    public native boolean getAvoidWater();
    public native NativePathNavigation setAvoidWater(boolean value);
    
    public native NativePathNavigation setEndPathRadius(float value);

    public native boolean getCanSink();
    public native NativePathNavigation setCanSink(boolean value);

    public native boolean getAvoidDamageBlocks();
    public native NativePathNavigation setAvoidDamageBlocks(boolean value);

    public native boolean getCanFloat();
    public native NativePathNavigation setCanFloat(boolean value);

    public native boolean isAmphibious();
    public native NativePathNavigation setIsAmphibious(boolean value);

    public native boolean getAvoidPortals();
    public native NativePathNavigation setAvoidPortals(boolean value);

    public native boolean getCanBreach();
    public native NativePathNavigation setCanBreach(boolean value);

    public native boolean getCanJump();
    public native NativePathNavigation setCanJump(boolean value);

    public native float getSpeed();
    public native NativePathNavigation setSpeed(float value);

    public native boolean getCanPathOverLava();
    public native NativePathNavigation setCanPathOverLava(boolean value);

    public native boolean getCanWalkInLava();
    public native NativePathNavigation setCanWalkInLava(boolean value);

    public native boolean getCanOpenIronDoors();
    public native NativePathNavigation setCanOpenIronDoors(boolean value);

    public native boolean getHasEndPathRadius();
    public native NativePathNavigation setHasEndPathRadius(boolean value);

    public native float getTerminationThreshold();
    public native NativePathNavigation getTerminationThreshold(float value);

    public native int getTickTimeout();
    public native NativePathNavigation setTickTimeout(int value);

    public native boolean isStuck(int ticks);
}