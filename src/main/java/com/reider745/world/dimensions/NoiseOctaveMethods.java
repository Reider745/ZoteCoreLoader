package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

public class NoiseOctaveMethods {
    private static final PointersStorage<NoiseOctaveDescription> pointers = new PointersStorage<>("noise_octave", new PointerGenFastest(), false);

    public static class NoiseOctaveDescription {
        public final int type;
        public float translateX, translateY, translateZ;
        public float scaleX, scaleY, scaleZ;
        public float weight;
        public int seed;
        public NoiseConversionMethods.NoiseConversionDescription conversion;

        interface INoise {
            float apply(Noise.Data noise_data, float x, float y, float z, int seed);
        }

        private final INoise noise;

        public NoiseOctaveDescription(int type){
            this.type = type;

            switch (type){
                case 0 -> noise = NoiseOctaveDescription::base_noise_value;
                case 1 -> noise = NoiseOctaveDescription::gray_noise_value;
                case 2 -> noise = NoiseOctaveDescription::chess_noise_value;
                case 10 -> noise = NoiseOctaveDescription::sine_noise_x;
                case 11 -> noise = NoiseOctaveDescription::sine_noise_y;
                case 12 -> noise = NoiseOctaveDescription::sine_noise_z;
                case 13 -> noise = NoiseOctaveDescription::sine_noise_xy;
                case 14 -> noise = NoiseOctaveDescription::sine_noise_yz;
                case 15 -> noise = NoiseOctaveDescription::sine_noise_xz;
                case 16 -> noise = NoiseOctaveDescription::sine_noise_xyz;
                default -> noise = NoiseOctaveDescription::base_noise_value;
            }
        }

        private static final float VALUE = 2147483648f * 2f;
        private static float fast_val_3d(int x, int y, int z, int seed) {
            int n = seed;
            n ^= 1619 * x;
            n ^= 31337 * y;
            n ^= 6971 * z;
            return Math.abs(((n * n * n * 60493f) % VALUE) / VALUE);
        }

        public float noise_value(Noise.Data noise_data, float x, float y, float z, int seed){
            return noise.apply(noise_data, x, y, z, seed);
        }

        public static float gray_noise_value(Noise.Data noise_data, float x, float y, float z, int seed) {
            return fast_val_3d((int) (x * 10), (int) (y * 10), (int) (z * 10), seed);
        }

        public static float chess_noise_value(Noise.Data noise_data, float x, float y, float z, int seed) {
            int X = (int) Math.floor(x);
            int Y = (int) Math.floor(y);
            int Z = (int) Math.floor(z);
            return (X & 1) ^ (Y & 1) ^ (Z & 1);
        }

        public static float sine_noise_x(Noise.Data noise_data, float x, float y, float z, int seed) {
            return (float) Math.sin(x);
        }

        public static float sine_noise_y(Noise.Data noise_data, float x, float y, float z, int seed) {
            return (float) Math.sin(y);
        }

        public static float sine_noise_z(Noise.Data noise_data, float x, float y, float z, int seed) {
            return (float) Math.sin(z);
        }

        public static float sine_noise_xy(Noise.Data noise_data, float x, float y, float z, int seed) {
            return ((float) (Math.sin(x) * Math.sin(y)));
        }

        public static float sine_noise_xz(Noise.Data noise_data, float x, float y, float z, int seed) {
            return ((float) (Math.sin(x) * Math.sin(z)));
        }

        public static float sine_noise_yz(Noise.Data noise_data, float x, float y, float z, int seed) {
            return ((float) (Math.sin(y) * Math.sin(z)));
        }

        public static float sine_noise_xyz(Noise.Data noise_data, float x, float y, float z, int seed) {
            return ((float) (Math.sin(x) * Math.sin(y) * Math.sin(z)));
        }

        public static float base_noise_value(Noise.Data noise_data, float x, float y, float z, int seed) {
            final int X = (int) Math.floor(x);
            final int Y = (int) Math.floor(y);
            final int Z = (int) Math.floor(z);
            x -= X;
            y -= Y;
            z -= Z;
            final float u = x * x * x * (x * (x * 6f - 15) + 10);
            final float v = y * y * y * (y * (y * 6f - 15) + 10);
            final float w = z * z * z * (z * (z * 6f - 15) + 10);

            final float p000 = fast_val_3d(X, Y, Z, seed);
            final float p001 = fast_val_3d(X, Y, Z + 1, seed);
            final float p100 = fast_val_3d(X + 1, Y, Z, seed);
            final float p101 = fast_val_3d(X + 1, Y, Z + 1, seed);
            final float p010 = fast_val_3d(X, Y + 1, Z, seed);
            final float p011 = fast_val_3d(X, Y + 1, Z + 1, seed);
            final float p110 = fast_val_3d(X + 1, Y + 1, Z, seed);
            final float p111 = fast_val_3d(X + 1, Y + 1, Z + 1, seed);
            final float _u = 1 - u;
            final float p00 = p000 * _u + p100 * u;
            final float p10 = p010 * _u + p110 * u;
            final float p01 = p001 * _u + p101 * u;
            final float p11 = p011 * _u + p111 * u;
            final float p0 = p00 * (1 - v) + p10 * v;
            final float p1 = p01 * (1 - v) + p11 * v;
            return p0 * (1 - w) + p1 * w;
        }
    }

    public static NoiseOctaveDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(int type){
        return pointers.addPointer(new NoiseOctaveDescription(type));
    }
    public static void nativeSetTranslate(long ptr, float x, float y, float z){
        NoiseOctaveDescription description = pointers.get(ptr);
        description.translateX = x;
        description.translateY = y;
        description.translateZ = z;
    }
    public static void nativeSetScale(long ptr, float x, float y, float z){
        NoiseOctaveDescription description = pointers.get(ptr);
        description.scaleX = x;
        description.scaleY = y;
        description.scaleZ = z;
    }
    public static void nativeSetWeight(long ptr, float w){
        pointers.get(ptr).weight = w;
    }
    public static void nativeSetSeed(long ptr, int seed){
        pointers.get(ptr).seed = seed;
    }
    public static void nativeSetConversion(long ptr, long conversion){
        pointers.get(ptr).conversion = NoiseConversionMethods.get(conversion);
    }
}
