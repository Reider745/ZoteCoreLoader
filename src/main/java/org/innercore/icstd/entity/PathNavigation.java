package org.innercore.icstd.entity;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.gen.PathNavigationBindGenBase;
import org.innercore.icstd.annotations.ApiBinding;

import java.util.HashMap;

@ApiBinding("PathNavigation")
public class PathNavigation extends PathNavigationBindGenBase {
    private static HashMap<Long, PathNavigation> activeNavigationMap = new HashMap<>();

    private static native long nativeConstruct(long entity);
    private static native void nativeCleanup();

    private long entity;

    public PathNavigation(long entity) {
        //super(entity);
        this.entity = entity;
    }

    public PathNavigation(long ptr, int refCnt, Object stub) {
        //super(ptr, refCnt, stub);
    }

    @ApiBinding
    public static void cleanupAll() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.cleanupAll");
    }

    @ApiBinding
    public void moveToCoords(float x, float y, float z, float dist) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.moveToCoords");
    }

    @ApiBinding
    public void moveToEntity(long entity, float dis) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.moveToEntity");
    }

    @ApiBinding
    public float getMaxNavigationDistance() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getMaxNavigationDistance");
        return 0;
    }

    @ApiBinding
    public void setMaxNavigationDistance(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setMaxNavigationDistance");
    }

    @ApiBinding
    public boolean isDone() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isDone");
        return true;
    }

    @ApiBinding
    public void stop() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.stop");
    }

    @ApiBinding
    public void setType(int type) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setType");
    }

    @ApiBinding
    public boolean canPassDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.canPassDoors");
        return false;
    }

    @ApiBinding
    public void setCanPassDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanPassDoors");
    }

    @ApiBinding
    public boolean canOpenDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.canOpenDoors");
        return false;
    }

    @ApiBinding
    public void setCanOpenDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanOpenDoors");
    }

    @ApiBinding
    public boolean isRiverFollowing() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.isRiverFollowing");
        return false;
    }

    @ApiBinding
    public void setIsRiverFollowing(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setIsRiverFollowing");
    }

    @ApiBinding
    public float getEndPathRadiusSqr() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getEndPathRadiusSqr");
        return 0;
    }

    @ApiBinding
    public void setEndPathRadius(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setEndPathRadius");
    }

    @ApiBinding
    public boolean getAvoidSun() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidSun");
        return false;
    }

    @ApiBinding
    public void setAvoidSun(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidSun");
    }

    @ApiBinding
    public boolean getAvoidWater() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidWater");
        return false;
    }

    @ApiBinding
    public void setAvoidWater(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidWater");
    }

    @ApiBinding
    public boolean getCanSink() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanSink");
        return false;
    }

    @ApiBinding
    public void setCanSink(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanSink");
    }

    @ApiBinding
    public boolean getAvoidDamageBlocks() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidDamageBlocks");
        return false;
    }

    @ApiBinding
    public void setAvoidDamageBlocks(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidDamageBlocks");
    }

    @ApiBinding
    public boolean getCanFloat() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanFloat");
        return false;
    }

    @ApiBinding
    public void setCanFloat(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanFloat");
    }

    @ApiBinding
    public boolean getAvoidPortals() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getAvoidPortals");
        return false;
    }

    @ApiBinding
    public void setAvoidPortals(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setAvoidPortals");
    }

    @ApiBinding
    public boolean getCanBreach() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanBreach");
        return false;
    }

    @ApiBinding
    public void setCanBreach(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanBreach");
    }

    @ApiBinding
    public boolean getCanJump() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanJump");
        return false;
    }

    @ApiBinding
    public void setCanJump(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanJump");
    }

    @ApiBinding
    public boolean getCanPathOverLava() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanPathOverLava");
        return false;
    }

    @ApiBinding
    public void setCanPathOverLava(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanPathOverLava");
    }

    @ApiBinding
    public boolean getCanWalkInLava() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanWalkInLava");
        return false;
    }

    @ApiBinding
    public void setCanWalkInLava(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanWalkInLava");
    }

    @ApiBinding
    public boolean getCanOpenIronDoors() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getCanOpenIronDoors");
        return false;
    }

    @ApiBinding
    public void setCanOpenIronDoors(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setCanOpenIronDoors");
    }

    @ApiBinding
    public boolean getHasEndPathRadius() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getHasEndPathRadius");
        return false;
    }

    @ApiBinding
    public void setHasEndPathRadius(boolean value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setHasEndPathRadius");
    }

    @ApiBinding
    public float getSpeed() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getSpeed");
        return 0.0f;
    }

    @ApiBinding
    public void setSpeed(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setSpeed");
    }

    @ApiBinding
    public float getTerminationThreshold() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getTerminationThreshold");
        return 0.0f;
    }

    @ApiBinding
    public void setTerminationThreshold(float value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setTerminationThreshold");
    }

    @ApiBinding
    public int getTickTimeout() {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.getTickTimeout");
        return 0;
    }

    @ApiBinding
    public void setTickTimeout(int value) {
        InnerCoreServer.useNotCurrentSupport("NativePathNavigation.setTickTimeout");
    }


    @Override
    public String toString() {
        return "[PathNavigation entity=" + entity + "]";
    }

    public static PathNavigation getNavigation(long entity) {
        PathNavigation result = activeNavigationMap.get(entity);
        if(result != null){
            return result;
        }
        result = new PathNavigation(entity);
        activeNavigationMap.put(entity, result);
        return result;
    }

    public static void onNavigationResult(long entity, int result){
        PathNavigation navigation = getNavigation(entity);
        if(navigation.resultFunction != null){
            navigation.resultFunction.onNavigationResult(navigation, result);
        }
    }

    public long getEntity() {
        return entity;
    }

    public static void cleanup() {
        for(PathNavigation navigation: activeNavigationMap.values()) {
            navigation.entity = -1;
        }
        activeNavigationMap.clear();
        cleanupAll();
    }

    public interface NavigationResultFunction {
        void onNavigationResult(PathNavigation navigation, int result);
    }

    PathNavigation.NavigationResultFunction resultFunction;

    @ApiBinding
    public PathNavigation setResultFunction(PathNavigation.NavigationResultFunction resultFunction) {
        if (this.resultFunction != null) {
            this.resultFunction.onNavigationResult(this, 6); // FUNCTION_REPLACED
        }
        this.resultFunction = resultFunction;
        return this;
    }
}
