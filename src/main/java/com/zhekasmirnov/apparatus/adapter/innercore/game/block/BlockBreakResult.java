package com.zhekasmirnov.apparatus.adapter.innercore.game.block;

import com.zhekasmirnov.apparatus.adapter.innercore.game.item.ItemStack;
import com.zhekasmirnov.apparatus.util.Java8BackComp;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockBreakResult {
    private final List<ItemStack> items = new ArrayList<>();
    private int experience;

    public List<ItemStack> getItems() {
        return items;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addExperience(int experience) {
        this.experience += experience;
    }


    public ScriptableObject asScriptable() {
        ScriptableObject result = ScriptableObjectHelper.createEmpty();
        result.put("experience", result, experience);
        result.put("items", result, ScriptableObjectHelper.createArray(Java8BackComp.stream(items).map(ItemStack::asScriptable).collect(Collectors.toList())));
        return result;
    }
}
