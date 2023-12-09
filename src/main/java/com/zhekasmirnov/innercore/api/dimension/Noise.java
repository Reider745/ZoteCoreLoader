package com.zhekasmirnov.innercore.api.dimension;

import com.reider745.InnerCoreServer;

import android.graphics.Bitmap;

/**
 * Created by zheka on 12.11.2017.
 */

@Deprecated
public class Noise {
    public static class Gradient {
        public final long pointer;

        public Gradient() {
            pointer = nativeGradientConstruct();
        }

        public void add(double x, double y) {
            nativeGradientAddValue(pointer, x, y);
        }

        public double get(double x) {
            return nativeGradientGetValue(pointer, x);
        }

        public Bitmap debugGraph(boolean show) {
            return Bitmap.getSingletonInternalProxy();
        }
    }

    public static long nativeGradientConstruct() {
        InnerCoreServer.useNotSupport("Noise.nativeGradientConstruct()");
        return 0;
    }

    public static void nativeGradientAddValue(long pointer, double x, double y) {
        InnerCoreServer.useNotSupport("Noise.nativeGradientAddValue(pointer, x, y)");
    }

    public static double nativeGradientGetValue(long pointer, double x) {
        InnerCoreServer.useNotSupport("Noise.nativeGradientGetValue(pointer, x)");
        return 0.0;
    }

    public static class Octave {
        public final long pointer;

        public Octave(double weight) {
            pointer = nativeOctaveConstruct(weight);
        }

        public Octave scale(double x, double y, double z) {
            nativeOctaveScale(pointer, x, y, z);
            return this;
        }

        public Octave translate(double x, double y, double z) {
            nativeOctaveTranslate(pointer, x, y, z);
            return this;
        }
    }

    public static long nativeOctaveConstruct(double weight) {
        InnerCoreServer.useNotSupport("Noise.nativeOctaveConstruct(weight)");
        return 0;
    }

    public static void nativeOctaveScale(long pointer, double x, double y, double z) {
        InnerCoreServer.useNotSupport("Noise.nativeOctaveScale(pointer, x, y, z)");
    }

    public static void nativeOctaveTranslate(long pointer, double x, double y, double z) {
        InnerCoreServer.useNotSupport("Noise.nativeOctaveTranslate(pointer, x, y, z)");
    }

    public static class Layer {
        public final long pointer;

        public Layer() {
            pointer = nativeLayerConstruct();
        }

        public Layer addOctave(Octave octave) {
            nativeLayerAddOctave(pointer, octave.pointer);
            return this;
        }

        public Layer setGradient(Gradient gradient) {
            nativeLayerSetGradient(pointer, gradient.pointer);
            return this;
        }

        public Layer setSeed(int seed) {
            nativeLayerSetSeed(pointer, seed);
            return this;
        }
    }

    public static long nativeLayerConstruct() {
        InnerCoreServer.useNotSupport("Noise.nativeLayerConstruct()");
        return 0;
    }

    public static void nativeLayerAddOctave(long pointer, long octave) {
        InnerCoreServer.useNotSupport("Noise.nativeLayerAddOctave(pointer, octave)");
    }

    public static void nativeLayerSetSeed(long pointer, int seed) {
        InnerCoreServer.useNotSupport("Noise.nativeLayerSetSeed(pointer, seed)");
    }

    public static void nativeLayerSetGradient(long pointer, long gradient) {
        InnerCoreServer.useNotSupport("Noise.nativeLayerSetGradient(pointer, gradient)");
    }

    public static class Map {
        public final long pointer;

        public Map() {
            pointer = nativeMapConstruct();
        }

        public Map addLayer(Layer layer) {
            nativeMapAddLayer(pointer, layer.pointer);
            return this;
        }

        public Map setGradient(Gradient gradient) {
            nativeMapSetGradient(pointer, gradient.pointer);
            return this;
        }
    }

    public static long nativeMapConstruct() {
        InnerCoreServer.useNotSupport("Noise.nativeMapConstruct()");
        return 0;
    }

    public static void nativeMapAddLayer(long pointer, long layer) {
        InnerCoreServer.useNotSupport("Noise.nativeMapAddLayer(pointer, layer)");
    }

    public static void nativeMapSetGradient(long pointer, long gradient) {
        InnerCoreServer.useNotSupport("Noise.nativeMapSetGradient(pointer, gradient)");
    }
}
