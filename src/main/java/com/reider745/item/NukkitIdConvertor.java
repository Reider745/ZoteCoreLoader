package com.reider745.item;

import com.zhekasmirnov.innercore.api.NativeItemInstance;

import java.util.*;

public class NukkitIdConvertor {
    public static void init(){
        add(351, 15, 858, 0);//bone_meal fix
        add(351, 4, 821, 0);//lapis fix

        add(325, 10, 843, 0);//lava bucket fix
        add(325, 8, 850, 0);//water bucket fix
    }

    public static class EntryItem {
        public int id, data;

        private EntryItem(int id, int data){
            this.id = id;
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryItem entryItem = (EntryItem) o;
            return id == entryItem.id && data == entryItem.data;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, data);
        }

        @Override
        public String toString() {
            return "EntryItem{" +
                    "id=" + id +
                    ", data=" + data +
                    '}';
        }
    }

    private static final List<Map.Entry<EntryItem, EntryItem>> nukkitForInenrCore = new ArrayList<>();

    public static void add(int nukkit_id, int nukkit_data, int ic_id, int ic_data){
        final EntryItem nukkit = new EntryItem(nukkit_id, nukkit_data);
        final EntryItem inner_core = new EntryItem(ic_id, ic_data);

        nukkitForInenrCore.add(new Map.Entry<>() {
            @Override
            public EntryItem getKey() {
                return nukkit;
            }

            @Override
            public EntryItem getValue() {
                return inner_core;
            }

            @Override
            public EntryItem setValue(EntryItem value) {
                return null;
            }
        });
    }

    public static void apply(final NativeItemInstance instance){
        final int id = instance.id;
        final int data = instance.data;

        for (final Map.Entry<EntryItem, EntryItem> entry : nukkitForInenrCore) {
            final EntryItem nukkit = entry.getKey();

            if (nukkit.id == id && nukkit.data == data) {
                final EntryItem inner_core = entry.getValue();

                instance.id = inner_core.id;
                instance.data = inner_core.data;
            }
        }
    }

    public static EntryItem getNukkitForInnerCore(int id, int data){
        for (final Map.Entry<EntryItem, EntryItem> entry : nukkitForInenrCore) {
            final EntryItem inner_core = entry.getValue();

            if (inner_core.id == id && inner_core.data == data)
                return entry.getKey();
        }
        return new EntryItem(id, data);
    }
}
