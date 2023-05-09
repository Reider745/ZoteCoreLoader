package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.apparatus.api.container.ItemContainerSlot;
import org.mozilla.javascript.Scriptable;

/**
 * Created by zheka on 10.09.2017.
 */

public interface WorkbenchField {
    /**
     * @return returns slot, that field slot i is linked to
     */
    ItemContainerSlot getFieldSlot(int i);

    ItemContainerSlot getFieldSlot(int x, int y);

    /**
     * @return returns a scriptable array of slots, that can be linked or modified
     */
    Scriptable asScriptableField();

    /**
     * @return returns size of workbench field, 2x2 crafting field = 2, 3x3 = 3
     */
    int getWorkbenchFieldSize();
}
