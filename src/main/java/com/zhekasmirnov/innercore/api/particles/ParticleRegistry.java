package com.zhekasmirnov.innercore.api.particles;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;

/**
 * Created by zheka on 08.02.2018.
 */

public class ParticleRegistry {
    private static HashMap<Integer, ParticleType> registeredParticleTypes = new HashMap<>();

    public static long nativeRegisterNewParticleType(String textureName, float minU, float minV, float maxU, float maxV,
            int texCountH, int texCountV, boolean isUsingBlockLight) {
        InnerCoreServer.useNotSupport(
                "ParticleRegistry.nativeRegisterNewParticleType(textureName, minU, minV, maxU, maxV, texCountH, texCountV, isUsingBlockLight)");
        return 0;
    }

    public static int nativeParticleTypeGetID(long type) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeGetID(type)");
        return 0;
    }

    public static void nativeParticleTypeSetRenderType(long type, int renderType) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetRenderType(type, renderType)");
    }

    public static void nativeParticleTypeSetRebuildDelay(long type, int delay) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetRebuildDelay(type, delay)");
    }

    public static void nativeParticleTypeSetColor(long type, float r, float g, float b, float a) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetColor(type, r, g, b, a)");
    }

    public static void nativeParticleTypeSetColorNew(long type, float r, float g, float b, float a, float r2, float g2,
            float b2, float a2) {
        InnerCoreServer
                .useNotSupport("ParticleRegistry.nativeParticleTypeSetColorNew(type, r, g, b, a, r2, g2, b2, a2)");
    }

    public static void nativeParticleTypeSetSize(long type, float min, float max) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetSize(type, min, max)");
    }

    public static void nativeParticleTypeSetLifetime(long type, int min, int max) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetLifetime(type, min, max)");
    }

    public static void nativeParticleTypeSetFriction(long type, float air, float block) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetFriction(type, air, block)");
    }

    public static void nativeParticleTypeSetCollisionParams(long type, boolean collision, boolean keepVel,
            int addLifetimeOnCollision) {
        InnerCoreServer.useNotSupport(
                "ParticleRegistry.nativeParticleTypeSetCollisionParams(type, collision, keepVel, addLifetimeOnCollision)");
    }

    public static void nativeParticleTypeSetDefaultVelocity(long type, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetDefaultVelocity(type, x, y, z)");
    }

    public static void nativeParticleTypeSetDefaultAcceleration(long type, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetDefaultAcceleration(type, x, y, z)");
    }

    public static void nativeParticleTypeSetSubEmitters(long type, long idle, long impact, long death) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetSubEmitters(type, idle, impact, death)");
    }

    public static void nativeParticleTypeSetAnimators(long type, long size, long alpha, long texture) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleTypeSetAnimators(type, size, alpha, texture)");
    }

    public static void nativeParticleTypeSetAnimatorsNew(long type, long size, long alpha, long texture, long color) {
        InnerCoreServer.useNotSupport(
                "ParticleRegistry.nativeParticleTypeSetAnimatorsNew(type, size, alpha, texture, color)");
    }

    public static class ParticleType {
        public ParticleType(String textureName, float minU, float minV, float maxU, float maxV, int texCountH,
                int texCountV, boolean isUsingBlockLight) {
        }

        public ParticleType(String locationName, boolean isUsingBlockLight, float[] uv, int texCountH, int texCountV) {
        }

        public ParticleType(ScriptableObjectWrapper scriptable) {
        }

        public ParticleType(Scriptable scriptable) {
        }

        public int getId() {
            return 0;
        }

        public void setRenderType(int renderType) {
        }

        public void setRebuildDelay(int delay) {
        }

        public void setColor(float r, float g, float b, float a) {
        }

        public void setColor(float r, float g, float b, float a, float r2, float g2, float b2, float a2) {
        }

        public void setCollisionParams(boolean collision, boolean keepVelocity, int addAfterCollision) {
        }

        public void setFriction(float air, float block) {
        }

        public void setSize(float min, float max) {
        }

        public void setLifetime(int min, int max) {
        }

        public void setDefaultVelocity(float x, float y, float z) {
        }

        public void setDefaultAcceleration(float x, float y, float z) {
        }

        public void setSubEmitter(String name, ParticleSubEmitter emitter) {
        }

        public void setAnimator(String name, ParticleAnimator animator) {
        }
    }

    public static long nativeNewParticleAnimator(float fadeInTime, float fadeInValue, float fadeOutTime,
            float fadeOutValue, int period) {
        InnerCoreServer.useNotSupport(
                "ParticleRegistry.nativeNewParticleAnimator(fadeInTime, fadeInValue, fadeOutTime, fadeOutValue, period)");
        return 0;
    }

    public static class ParticleAnimator {
        public ParticleAnimator(int period, float fadeInTime, float fadeInValue, float fadeOutTime,
                float fadeOutValue) {
        }

        public ParticleAnimator(ScriptableObjectWrapper scriptable) {
        }

        public ParticleAnimator(Scriptable scriptable) {
        }
    }

    public static long nativeNewParticleSubEmitter(float chance, int count, int type, int data) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeNewParticleSubEmitter(chance, count, type, data)");
        return 0;
    }

    public static void nativeParticleSubEmitterSetRandom(long emitter, float maxRandVel) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleSubEmitterSetRandom(emitter, maxRandVel)");
    }

    public static void nativeParticleSubEmitterSetKeepVelocity(long emitter, boolean keep) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleSubEmitterSetKeepVelocity(emitter, keep)");
    }

    public static void nativeParticleSubEmitterSetKeepEmitter(long emitter, boolean keep) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleSubEmitterSetKeepEmitter(emitter, keep)");
    }

    public static class ParticleSubEmitter {
        public ParticleSubEmitter(float chance, int count, int type, int data) {
        }

        public ParticleSubEmitter(ScriptableObjectWrapper scriptable) {
        }

        public ParticleSubEmitter(Scriptable scriptable) {
        }

        public void setRandomVelocity(float maxRandomVelocity) {
        }

        public void setKeepVelocity(boolean keepVelocity) {
        }

        public void setKeepEmitter(boolean keepEmitter) {
        }
    }

    public static long nativeNewParticleEmitter(float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeNewParticleEmitter(x, y, z)");
        return 0;
    }

    public static void nativeParticleEmitterMove(long emitter, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterMove(emitter, x, y, z)");
    }

    public static void nativeParticleEmitterMoveTo(long emitter, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterMoveTo(emitter, x, y, z)");
    }

    public static void nativeParticleEmitterSetVelocity(long emitter, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterSetVelocity(emitter, x, y, z)");
    }

    public static void nativeParticleEmitterAttachTo(long emitter, long entity, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterAttachTo(emitter, entity, x, y, z)");
    }

    public static void nativeParticleEmitterDetach(long emitter) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterDetach(emitter)");
    }

    public static void nativeParticleEmitterRelease(long emitter) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterRelease(emitter)");
    }

    public static void nativeParticleEmitterGetPosition(long emitter, float[] pos) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmitterGetPosition(emitter, pos)");
    }

    public static void nativeParticleEmit1(long emitter, int type, int data, float x, float y, float z) {
        InnerCoreServer.useNotSupport("ParticleRegistry.nativeParticleEmit1(emitter, type, data, x, y, z)");
    }

    public static void nativeParticleEmit2(long emitter, int type, int data, float x, float y, float z, float vx,
            float vy, float vz) {
        InnerCoreServer
                .useNotSupport("ParticleRegistry.nativeParticleEmit2(emitter, type, data, x, y, z, vx, vy, vz)");
    }

    public static void nativeParticleEmit3(long emitter, int type, int data, float x, float y, float z, float vx,
            float vy, float vz, float ax, float ay, float az) {
        InnerCoreServer.useNotSupport(
                "ParticleRegistry.nativeParticleEmit3(emitter, type, data, x, y, z, vx, vy, vz, ax, ay, az)");
    }

    public static class ParticleEmitter {
        public ParticleEmitter(float x, float y, float z) {
        }

        public void setEmitRelatively(boolean b) {
        }

        public void move(float x, float y, float z) {
        }

        public void moveTo(float x, float y, float z) {
        }

        public void setVelocity(float x, float y, float z) {
        }

        public void attachTo(Object ent, float x, float y, float z) {
        }

        public void attachTo(Object ent) {
        }

        public void detach() {
        }

        public void stop() {
        }

        public void release() {
        }

        public Coords getPosition() {
            return new Coords(0, 0, 0);
        }

        public float[] getPositionArray() {
            return new float[3];
        }

        public void emit(int type, int data, float x, float y, float z) {
        }

        public void emit(int type, int data, float x, float y, float z, float vx, float vy, float vz) {
        }

        public void emit(int type, int data, float x, float y, float z, float vx, float vy, float vz, float ax,
                float ay, float az) {
        }
    }

    @JSStaticFunction
    public static ParticleType getParticleTypeById(int id) {
        if (!registeredParticleTypes.containsKey(id)) {
            registeredParticleTypes.put(id, new ParticleType((Scriptable) null));
        }
        return registeredParticleTypes.get(id);
    }

    @JSStaticFunction
    public static int registerParticleType(Scriptable descr) {
        return new ParticleType(descr).getId();
    }

    @JSStaticFunction
    public static void addParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
        NativeAPI.addParticle(id, x, y, z, vx, vy, vz, data);
    }

    @JSStaticFunction
    public static void addFarParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
        NativeAPI.addFarParticle(id, x, y, z, vx, vy, vz, data);
    }
}
