package com.reider745.world.dimensions;

import com.reider745.api.pointers.PointersStorage;
import com.reider745.api.pointers.pointer_gen.PointerGenFastest;

import java.util.ArrayList;
import java.util.Comparator;

public class NoiseConversionMethods {

    private static final PointersStorage<NoiseConversionDescription> pointers = new PointersStorage<>("noise_conversion", new PointerGenFastest(), false);
    private static final int noise_conversion_quality = 128;

    public static class NoiseConversionDescription {
        public final ArrayList<float[]> nodes = new ArrayList<>();
        public final float[] values = new float[noise_conversion_quality];

        public void convert(float[] buff, float len) {
            for (int i = 0; i < len; i++) {
                float value = buff[i] * (noise_conversion_quality - 1);
                int index = (int) Math.floor(value);
                if (index < 0) {
                    buff[i] = values[0];
                } else if (index >= noise_conversion_quality - 1) {
                    buff[i] = values[noise_conversion_quality - 1];
                } else {
                    float f = value - index;
                    float val1 = values[index];
                    float val2 = values[index + 1];
                    buff[i] = (val2 - val1) * f + val1;
                }
            }
        }

        public NoiseConversionDescription(){
            rebuild();
        }

        private void rebuild(){
            nodes.sort(Comparator.comparingDouble(array -> array[0]));

            float[] last = new float[]{0, 0};
            float[] next = new float[]{1, 1};

            int index = 0;
            for (int i = 0; i < noise_conversion_quality; i++) {
                float x = i / (noise_conversion_quality - 1f);
                while (index < this.nodes.size() && i < noise_conversion_quality - 1) {
                    float[] node = this.nodes.get(index);
                    if (node[0] <= x) {
                        last = node;
                        index++;
                        if (index >= this.nodes.size()) {
                            next = new float[]{1, 1};
                        } else {
                            next = this.nodes.get(index);
                        }
                    } else {
                        break;
                    }
                }
                float f = (x - last[0]) / (next[0] - last[0]);
                this.values[i] = next[1] * f + last[1] * (1f - f);
            }
        }

        public void add(float x, float y){
            nodes.add(new float[]{x, y});
            rebuild();
        }

        public float convert(float value) {
            value = value * (noise_conversion_quality - 1);
            int index = (int) Math.floor(value);
            if (index < 0) {
                return values[0];
            } else if (index >= noise_conversion_quality - 1) {
                return values[noise_conversion_quality - 1];
            } else {
                float f = value - index;
                float val1 = values[index];
                float val2 = values[index + 1];
                return (val2 - val1) * f + val1;
            }
        }
    }

    public static NoiseConversionDescription get(long ptr){
        return pointers.get(ptr);
    }

    public static long nativeConstruct(){
        return pointers.addPointer(new NoiseConversionDescription());
    }
    public static void nativeAddNode(long ptr, float x, float y){
        pointers.get(ptr).add(x, y);
    }
}
