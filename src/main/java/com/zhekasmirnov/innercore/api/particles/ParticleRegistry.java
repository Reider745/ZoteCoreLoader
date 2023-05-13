package com.zhekasmirnov.innercore.api.particles;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.util.HashMap;

/**
 * Created by zheka on 08.02.2018.
 */

public class ParticleRegistry {
    private static HashMap<Integer, ParticleType> registeredParticleTypes = new HashMap<>();

    public static class ParticleType {
        private long ptr;
        private int id;

        private void initPtr(long ptr) {
            this.ptr = ptr;
            this.id = 0;
            registeredParticleTypes.put(this.id, this);
        }

        public ParticleType(String textureName, float minU, float minV, float maxU, float maxV, int texCountH, int texCountV, boolean isUsingBlockLight) {
            initPtr(0);
        }

        public ParticleType(String locationName, boolean isUsingBlockLight, float[] uv, int texCountH, int texCountV) {
            String path = null;

            if(FileTools.assetExists("resource_packs/vanilla/particle-atlas/" + locationName + ".png")){
                path = "particle-atlas/" + locationName;
            } else if(FileTools.assetExists("resource_packs/vanilla/" + locationName + ".png")) {
                path = locationName;
            } 

            if (uv == null) {
                uv = new float[] {0, 0, 1, 1};
            }
            
            if(path == null) {
                ICLog.i("ERROR", "invalid particle location name: " + locationName + ", it does not exist and will be replaced with default.");
                initPtr(0);
            } else {
                // legacy support
                if (texCountH <= 0 || texCountV <= 0) {
                    texCountH = 1;
                    texCountV = 1;
                    Logger.debug("resource_packs/vanilla/" + path + ".png");
                }
                initPtr(0);
            }
        }

        public ParticleType(ScriptableObjectWrapper scriptable) {
            this(scriptable.getString("texture", "undefined"), scriptable.getBoolean("isUsingBlockLight"), scriptable.getUVTemplate("textureUV"), scriptable.getInt("framesX", -1), scriptable.getInt("framesY", -1));

            ScriptableObjectWrapper subEmitters = scriptable.getScriptableWrapper("emitters");
            if (subEmitters != null) {
                ScriptableObjectWrapper idle = subEmitters.getScriptableWrapper("idle");
                if (idle != null) {
                    setSubEmitter("idle", new ParticleSubEmitter(idle));
                }
                ScriptableObjectWrapper impact = subEmitters.getScriptableWrapper("impact");
                if (impact != null) {
                    setSubEmitter("impact", new ParticleSubEmitter(impact));
                }
                ScriptableObjectWrapper death = subEmitters.getScriptableWrapper("death");
                if (death != null) {
                    setSubEmitter("death", new ParticleSubEmitter(death));
                }
            }

            ScriptableObjectWrapper animators = scriptable.getScriptableWrapper("animators");
            if (animators != null) {
                ScriptableObjectWrapper size = animators.getScriptableWrapper("size");
                if (size != null) {
                    setAnimator("size", new ParticleAnimator(size));
                }
                ScriptableObjectWrapper alpha = animators.getScriptableWrapper("alpha");
                if (alpha != null) {
                    setAnimator("alpha", new ParticleAnimator(alpha));
                }
                ScriptableObjectWrapper icon = animators.getScriptableWrapper("icon");
                if (icon != null) {
                    setAnimator("icon", new ParticleAnimator(icon));
                }
                ScriptableObjectWrapper color = animators.getScriptableWrapper("color");
                if (color != null) {
                    setAnimator("color", new ParticleAnimator(color));
                }
            }

            ScriptableObjectWrapper friction = scriptable.getScriptableWrapper("friction");
            if (friction != null) {
                setFriction(friction.getFloat("air", 1), friction.getFloat("block", 1));
            }

            if (scriptable.has("color")) {
                float[] color = scriptable.getColorTemplate("color", 1);
                if (scriptable.has("color2")) {
                    float[] color2 = scriptable.getColorTemplate("color2", 1);
                    setColor(color[0], color[1], color[2], color[3], color2[0], color2[1], color2[2], color2[3]);
                } else {
                    setColor(color[0], color[1], color[2], color[3]);
                }
            }


            if (scriptable.has("lifetime")) {
                float[] lifetime = scriptable.getMinMaxTemplate("lifetime", 100);
                setLifetime((int) lifetime[0], (int) lifetime[1]);
            }

            if (scriptable.has("size")) {
                float[] size = scriptable.getMinMaxTemplate("size", 1);
                setSize(size[0], size[1]);
            }

            if (scriptable.has("velocity")) {
                float[] velocity = scriptable.getVec3Template("velocity", 0);
                setDefaultVelocity(velocity[0], velocity[1], velocity[2]);
            }

            if (scriptable.has("acceleration")) {
                float[] acceleration = scriptable.getVec3Template("acceleration", 0);
                setDefaultAcceleration(acceleration[0], acceleration[1], acceleration[2]);
            }

            setCollisionParams(scriptable.getBoolean("collision"), scriptable.getBoolean("keepVelocityAfterImpact"), scriptable.getInt("addLifetimeAfterImpact"));
            setRenderType(scriptable.getInt("render", 1));
            setRebuildDelay(scriptable.getInt("rebuildDelay", 10));
        }

        public ParticleType(Scriptable scriptable) {
            this(new ScriptableObjectWrapper(scriptable));
        }

        public int getId() {
            return id;
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

        private HashMap<String, ParticleSubEmitter> subEmitters = new HashMap<>();
        private long getSubEmitterPtr(String name) {
            ParticleSubEmitter emitter = subEmitters.get(name);
            return emitter != null ? emitter.ptr : 0;
        }

        public void setSubEmitter(String name, ParticleSubEmitter emitter) {
            subEmitters.put(name, emitter);
        }

        private HashMap<String, ParticleAnimator> animators = new HashMap<>();
        private long getAnimatorPtr(String name) {
            ParticleAnimator animator = animators.get(name);
            return animator != null ? animator.ptr : 0;
        }

        public void setAnimator(String name, ParticleAnimator animator) {
            animators.put(name, animator);
        }
    }



    public static native long nativeNewParticleAnimator(float fadeInTime, float fadeInValue, float fadeOutTime, float fadeOutValue, int period);

    public static class ParticleAnimator {
        private long ptr;

        public ParticleAnimator(int period, float fadeInTime, float fadeInValue, float fadeOutTime, float fadeOutValue) {
            ptr = nativeNewParticleAnimator(fadeInTime, fadeInValue, fadeOutTime, fadeOutValue, period);
        }

        public ParticleAnimator(ScriptableObjectWrapper scriptable) {
            this(scriptable.getInt("period", -1), scriptable.getFloat("fadeIn"), scriptable.getFloat("start"), scriptable.getFloat("fadeOut", 0), scriptable.getFloat("end", 0));
        }

        public ParticleAnimator(Scriptable scriptable) {
            this(new ScriptableObjectWrapper(scriptable));
        }
    }



    public static class ParticleSubEmitter {
        private long ptr;

        public ParticleSubEmitter(float chance, int count, int type, int data) {
            ptr = 0;
        }

        public ParticleSubEmitter(ScriptableObjectWrapper scriptable) {
            this(scriptable.getFloat("chance", 1), scriptable.getInt("count", 1), scriptable.getInt("type", 0), scriptable.getInt("data", 0));

            setKeepVelocity(scriptable.getBoolean("keepVelocity"));
            setKeepEmitter(scriptable.getBoolean("keepEmitter"));

            if (scriptable.getFloat("randomize") > 0.001) {
                setRandomVelocity(scriptable.getFloat("randomize"));
            }
        }

        public ParticleSubEmitter(Scriptable scriptable) {
            this(new ScriptableObjectWrapper(scriptable));
        }

        public void setRandomVelocity(float maxRandomVelocity) {
        }

        public void setKeepVelocity(boolean keepVelocity) {
        }

        public void setKeepEmitter(boolean keepEmitter) {
        }
    }

    public static class ParticleEmitter {
        private long ptr;
        private boolean isRelativeEmittingEnabled = false;

        public ParticleEmitter(float x, float y, float z) {
            ptr = 0;
        }

        public void setEmitRelatively(boolean b) {
            isRelativeEmittingEnabled = b;
        }

        public void move(float x, float y, float z) {
        }

        public void moveTo(float x, float y, float z) {
        }

        public void setVelocity(float x, float y, float z) {
        }

        public void attachTo(Object ent, float x, float y, float z) {
            long entity = (long) (ent instanceof Wrapper ? ((Wrapper) ent).unwrap() : ((Number) ent).longValue());
        }

        public void attachTo(Object ent) {
            attachTo(ent, 0, 0, 0);
        }

        public void detach() {
        }

        public void stop() {
            detach();
            setVelocity(0, 0, 0);
        }

        public void release() {
        }

        public Coords getPosition() {
            return new Coords(0, 0, 0);
        }

        public float[] getPositionArray() {
            return new float[] {0, 0, 0};
        }

        private float[] pos = new float[3];
        private void refreshPos() {
        }

        public void emit(int type, int data, float x, float y, float z) {
            if (isRelativeEmittingEnabled) {
                refreshPos();
            }
            else {
            }
        }

        public void emit(int type, int data, float x, float y, float z, float vx, float vy, float vz) {
            if (isRelativeEmittingEnabled) {
                refreshPos();
            } else {
            }
        }

        public void emit(int type, int data, float x, float y, float z, float vx, float vy, float vz, float ax, float ay, float az) {
            if (isRelativeEmittingEnabled) {
                refreshPos();
            }
            else {
            }
        }
    }



    @JSStaticFunction
    public static ParticleType getParticleTypeById(int id) {
        return registeredParticleTypes.get(id);
    }

    @JSStaticFunction
    public static int registerParticleType(Scriptable descr) {
        return new ParticleType(descr).getId();
    }

    @JSStaticFunction
    public static void addParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
    }

    @JSStaticFunction
    public static void addFarParticle(int id, double x, double y, double z, double vx, double vy, double vz, int data) {
    }
}
