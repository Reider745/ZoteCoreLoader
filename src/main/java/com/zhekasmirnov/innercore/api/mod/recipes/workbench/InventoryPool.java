package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.mcpe.NativePlayer;
import com.zhekasmirnov.innercore.api.NativeItem;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 14.09.2017.
 */

public class InventoryPool {
    private final NativePlayer player;

    public class PoolEntry {
        public int slot, id, count, data;
        public NativeItemInstanceExtra extra;

        public PoolEntry(int slot, int id, int count, int data) {
            this.slot = slot;
            this.id = id;
            this.count = count;
            this.data = data;
        }

        public PoolEntry(int slot) {
            ItemStack item = player.getInventorySlot(slot);
            this.slot = slot;
            this.id = item.id;
            this.count = item.count;
            this.data = item.data;
            this.extra = item.extra;
        }

        public boolean isMatchesWithExtra(PoolEntry entry) {
            return id == entry.id && data == entry.data && (
                (extra != null && entry.extra != null && extra.getValue() == entry.extra.getValue()) || // same extra 
                ((extra == null || extra.isEmpty()) && (entry.extra == null || entry.extra.isEmpty())) // or no extra on both
            );
        }

        public boolean isMatches(PoolEntry entry) {
            return id == entry.id && data == entry.data;
        }

        public boolean hasExtra() {
            return extra != null && !extra.isEmpty();
        }

        public int getAmountOfItem(int amount) {
            if (amount > count) {
                amount = count;
            }
            count -= amount;
            if (count <= 0) {
                count = 0;
                player.setInventorySlot(slot, 0, 0, 0, null);
            }
            else {
                player.setInventorySlot(slot, id, count, data, extra);
            }
            return amount;
        }

        @Override
        public String toString() {
            return "{" + id + ", " + count + ", " + data + "}";
        }
    }

    public static class PoolEntrySet {
        private ArrayList<PoolEntry> entries = new ArrayList<>();

        public PoolEntrySet() {

        }

        public PoolEntrySet(ArrayList<PoolEntry> entries) {
            this.entries = entries;
        }

        public void addEntry(PoolEntry entry) {
            entries.add(entry);
        }

        public boolean isEmpty() {
            return entries.size() == 0;
        }

        public ArrayList<PoolEntry> getEntries() {
            return entries;
        }

        public PoolEntrySet getMajorEntrySet() {
            PoolEntrySet set = new PoolEntrySet();

            PoolEntry majorEntry = null;
            int majorCount = 0;

            for (PoolEntry entry : entries) {
                int count = 0;
                for (PoolEntry _entry : entries) {
                    if (entry.isMatches(_entry)) {
                        count += _entry.count;
                    }
                }

                if (majorCount < count) {
                    majorCount = count;
                    majorEntry = entry;
                }
            }

            if (majorEntry != null) {
                for (PoolEntry entry : entries) {
                    if (entry.isMatches(majorEntry)) {
                        set.addEntry(entry);
                    }
                }
            }

            return set;
        }

        public void removeMatchingEntries(PoolEntrySet set) {
            for (PoolEntry entry : set.entries) {
                entries.remove(entry);
            }
        }

        public PoolEntry getFirstEntry() {
            if (isEmpty()) {
                return null;
            }
            return entries.get(0);
        }

        public int getTotalCount() {
            int count = 0;
            for (PoolEntry entry : entries) {
                count += entry.count;
            }
            return count;
        }

        @Override
        public String toString() {
            String set = "";
            for (PoolEntry entry : entries) {
                set += entry + " ";
            }
            return "[" + set + "]";
        }

        public void spreadItems(ArrayList<AbstractSlot> slots) {
            if (isEmpty()) {
                return;
            }

            PoolEntry leadingEntry = entries.get(0);
            ArrayList<PoolEntry> remainingEntries = new ArrayList<>(entries);
            
            // first we get entries without extra
            int noExtraAmount = 0;
            ArrayList<PoolEntry> noExtraEntries = new ArrayList<>();
            for (PoolEntry entry : entries) {
                if (!entry.hasExtra()) {
                    remainingEntries.remove(entry);
                    noExtraEntries.add(entry);
                    noExtraAmount += entry.count;
                }
            }

            ArrayList<AbstractSlot> remainingSlots = new ArrayList<>();
            int countPerSlot = noExtraAmount / slots.size();
            int amountOfAdditionalSlots = noExtraAmount - countPerSlot * slots.size();
            for (int i = 0; i < slots.size(); i++) {
                AbstractSlot slot = slots.get(i);
                int amountToGet = countPerSlot + (i < amountOfAdditionalSlots ? 1 : 0);
                
                int gotAmount = 0; 
                PoolEntry first = null;
                for (PoolEntry entry : entries) {
                    if (amountToGet <= 0) {
                        break;
                    }
                    if (entry.count <= 0) {
                        continue;
                    }
                    if (entry.hasExtra()) {
                        continue;
                    }
                    if (first == null) {
                        first = entry;
                    }
                    else if (!first.isMatches(entry)) {
                        continue;
                    }
                    
                    if (NativeItem.getMaxStackForId(first.id, first.data) == 1) {
                        amountToGet = 1;
                    }
                    int got = entry.getAmountOfItem(amountToGet);
                    amountToGet -= got;
                    gotAmount += got;
                }
                if (first != null) {
                    slot.set(first.id, gotAmount, first.data, null);
                    if (gotAmount == 0) {
                        remainingSlots.add(slot);
                    }
                } else {
                    slot.set(leadingEntry.id, 0, leadingEntry.data, null);
                    remainingSlots.add(slot);
                }
            }

            for (AbstractSlot slot : remainingSlots) {
                if (remainingEntries.size() == 0) {
                    break;
                }
                PoolEntry entry = remainingEntries.remove(0);
                slot.set(entry.id, entry.getAmountOfItem(entry.count), entry.data, entry.extra);
            }
        }
    }

    public InventoryPool(long player) {
        this.player = new NativePlayer(player);
    }

    private HashMap<Long, ArrayList<PoolEntry>> entryMap = new HashMap<>();

    public void addRecipeEntry(RecipeEntry entry) {
        long code = entry.getCode();
        if (!entryMap.containsKey(code)) {
            entryMap.put(code, new ArrayList<PoolEntry>());
        }
    }

    public void addPoolEntry(PoolEntry entry) {
        ArrayList<PoolEntry> entries = getPoolEntries(new RecipeEntry(entry.id, entry.data));
        if (entries != null) {
            entries.add(entry);
        }
    }

    public PoolEntrySet getPoolEntrySet(RecipeEntry entry) {
        ArrayList<PoolEntry> entries = getPoolEntries(entry);
        return entries != null ? new PoolEntrySet(entries) : null;
    }

    public ArrayList<PoolEntry> getPoolEntries(RecipeEntry entry) {
        Long key = RecipeEntry.getCodeByItem(entry.id, entry.data);
        if (entryMap.containsKey(key)) {
            return entryMap.get(key);
        }

        key = RecipeEntry.getCodeByItem(entry.id, -1);
        if (entryMap.containsKey(key)) {
            return entryMap.get(key);
        }

        return null;
    }

    public void pullFromInventory() {
        for (int slot = 0; slot < 36; slot++) {
            addPoolEntry(new PoolEntry(slot));
        }
    }
}
