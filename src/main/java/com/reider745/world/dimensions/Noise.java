package com.reider745.world.dimensions;

public class Noise {
    public static class Helper {
        public static int divide_floor(int a, int b) {
            return a > 0 ? a / b : (a - b + 1) / b;
        }

        public static int divide_ceil(int a, int b) {
            return a > 0 ? (a + b - 1) / b : a / b;
        }

        public static float interpolate_cube(float p000, float p001, float p010, float p011, float p100, float p101, float p110, float p111, float x, float y, float z) {
            final float _x = 1 - x;
            final float _y = 1 - y;
            return ((p000 * _x + p100 * x) * _y + (p010 * _x + p110 * x) * y) * (1f - z) + ((p001 * _x + p101 * x) * _y + (p011 * _x + p111 * x) * y) * z;
        }
    }

    public static class Buffer {
        public int offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, grid, length;
        public float[] buffer, tmp_buffer;

        public Buffer() {
        }

        public Buffer(int sizeX, int sizeY, int sizeZ, int grid) {
            realloc(0, 0, 0, sizeX, sizeY, sizeZ, grid);
        }

        public Buffer(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, int grid) {
            realloc(offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, grid);
        }

        private float __INTERNAL_NOISE_BUFFER_POS(int x, int y, int z){
            return buffer[(x * sizeZ + z) * sizeY + y];
        }

        public void realloc(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, int grid) {
            if (grid != 1) {
                int x1 = Helper.divide_floor(offsetX, grid) * grid;
                int y1 = Helper.divide_floor(offsetY, grid) * grid;
                int z1 = Helper.divide_floor(offsetZ, grid) * grid;
                int x2 = Helper.divide_ceil(offsetX + sizeX, grid) * grid;
                int y2 = Helper.divide_ceil(offsetY + sizeY, grid) * grid;
                int z2 = Helper.divide_ceil(offsetZ + sizeZ, grid) * grid;
                offsetX = x1;
                offsetY = y1;
                offsetZ = z1;
                sizeX = Helper.divide_ceil(x2 - x1, grid) + 1;
                sizeY = Helper.divide_ceil(y2 - y1, grid) + 1;
                sizeZ = Helper.divide_ceil(z2 - z1, grid) + 1;
            }

            this.grid = grid;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            int new_length = sizeX * sizeY * sizeZ;
            if (length != new_length) {
                length = new_length;
                buffer = new float[length];
                tmp_buffer = new float[length];
            }
        }

        public void swap() {
            final float[] tmp = tmp_buffer;
            tmp_buffer = buffer;
            buffer = tmp;
        };

        public void fill_zeroes() {
            buffer = new float[buffer.length];
            //memset(buffer, 0, sizeof(float) * length);
        };

        void fill_zeroes_tmp() {
            tmp_buffer = new float[tmp_buffer.length];
           // memset(tmp_buffer, 0, sizeof(float) * length);
        };

        public float get(int x, int y, int z) {
            x -= offsetX;
            y -= offsetY;
            z -= offsetZ;
            if (grid == 1) {
                return __INTERNAL_NOISE_BUFFER_POS(x, y, z);
            }
            int x0 = x / grid;
            int y0 = y / grid;
            int z0 = z / grid;

            return Helper.interpolate_cube(
                    __INTERNAL_NOISE_BUFFER_POS(x0, y0, z0),
                    __INTERNAL_NOISE_BUFFER_POS(x0, y0, z0 + 1),
                    __INTERNAL_NOISE_BUFFER_POS(x0, y0 + 1, z0),
                    __INTERNAL_NOISE_BUFFER_POS(x0, y0 + 1, z0 + 1),
                    __INTERNAL_NOISE_BUFFER_POS(x0 + 1, y0, z0),
                    __INTERNAL_NOISE_BUFFER_POS(x0 + 1, y0, z0 + 1),
                    __INTERNAL_NOISE_BUFFER_POS(x0 + 1, y0 + 1, z0),
                    __INTERNAL_NOISE_BUFFER_POS(x0 + 1, y0 + 1, z0 + 1),
                    (x - x0 * grid) / (float) grid,
                    (y - y0 * grid) / (float) grid,
                    (z - z0 * grid) / (float) grid
            );
        }
    }

    private static final int noise_permutations_size = 256;

    public static class Data {
        int seed;
        int random;
        int[] permutations = new int[noise_permutations_size];

        public void prepare_noise_data(int seed){
            this.seed = seed;
            /*
            int* permutations = noise_data.permutations;
            memset(permutations, -1, sizeof(int) * noise_permutations_size);
            for (int i = 0; i < noise_permutations_size; i++) {
                int index = rand() % noise_permutations_size;
                if (permutations[index] != -1) {
                    permutations[i] = permutations[index];
                }
                permutations[index] = i;
            }
             */
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
    public static float noise_value(Noise.Data noise_data, float x, float y, float z, int seed) {
        final int X = (int) Math.floor(x); //x > 0 ? (int) x : (int) x - 1;
        final int Y = (int) Math.floor(y); //y > 0 ? (int) y : (int) y - 1;
        final int Z = (int) Math.floor(z); //z > 0 ? (int) z : (int) z - 1;
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

    public static void generate_noise_buffer(NoiseGeneratorMethods.NoiseGeneratorDescription generator, Data data, Buffer bufferRef){
        generate_noise_buffer(generator, data, bufferRef, bufferRef.offsetX, bufferRef.offsetY, bufferRef.offsetZ);
    }

    public static void generate_noise_buffer(NoiseGeneratorMethods.NoiseGeneratorDescription generator, Data data, Buffer bufferRef, float offsetX, float offsetY, float offsetZ){
        // transfer all required values into local storage
        final int grid = bufferRef.grid;
        final int sizeX = bufferRef.sizeX;
        final int sizeY = bufferRef.sizeY;
        final int sizeZ = bufferRef.sizeZ;
        final int buffer_len = bufferRef.length;
        final boolean single_layer = generator.layers.size() < 2;

        if (!single_layer) {
            bufferRef.fill_zeroes();
        }

        for(var layer : generator.layers){
            bufferRef.fill_zeroes_tmp();

            for(var octave : layer.octaves){
                int index = 0;
                final int seed = octave.seed + data.seed;
                final float xf0 = offsetX * octave.scaleX + octave.translateX;
                final float yf0 = offsetY * octave.scaleY + octave.translateY;
                final float zf0 = offsetZ * octave.scaleZ + octave.translateZ;
                float xf = xf0, yf, zf;
                final float xs = grid * octave.scaleX, ys = grid * octave.scaleY, zs = grid * octave.scaleZ;

                for (int x = 0; x < sizeX; x++) {
                    zf = zf0;
                    for (int z = 0; z < sizeZ; z++) {
                        yf = yf0;
                        for (int y = 0; y < sizeY; y++) {
                            final float value = noise_value(data, xf, yf, zf, seed) * octave.weight;
                            if (octave.conversion != null) {
                                bufferRef.tmp_buffer[index++] += octave.conversion.convert(value);
                            } else {
                                bufferRef.tmp_buffer[index++] += value;
                            }
                            yf += ys;
                        }
                        zf += zs;
                    }
                    xf += xs;
                }
            }
            if (layer.conversion != null) {
                layer.conversion.convert(bufferRef.tmp_buffer, buffer_len);
            }
            if (!single_layer) {
                for (int k = 0; k < buffer_len; k++) {
                    bufferRef.buffer[k] *= bufferRef.tmp_buffer[k];
                }
            }
        }

        // in case of single layer multiplication buffer is ignored and all data is stored in temp buffer
        if (single_layer)
            bufferRef.swap();

        if (generator.conversion != null) {
            generator.conversion.convert(bufferRef.buffer, buffer_len);
        }
    }
}
