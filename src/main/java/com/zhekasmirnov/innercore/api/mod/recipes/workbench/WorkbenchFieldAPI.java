package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import com.zhekasmirnov.innercore.api.mod.ui.container.AbstractSlot;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchFieldAPI {
    public final WorkbenchField container;

    private boolean isPrevented = false;

    public WorkbenchFieldAPI(WorkbenchField field) {
        this.container = field;
    }

    public AbstractSlot getFieldSlot(int i) {
        return container.getFieldSlot(i);
    }

    public void decreaseFieldSlot(int i) {
        AbstractSlot slot = getFieldSlot(i);
        slot.set(slot.getId(), Math.max(0, slot.getCount() - 1), slot.getData(), slot.getExtra());
        //slot.validate();
    }

    public void prevent() {
        isPrevented = true;
    }

    public boolean isPrevented() {
        return isPrevented;
    }

    public int getFieldSize() {
        return this.container.getWorkbenchFieldSize();
    }
}
