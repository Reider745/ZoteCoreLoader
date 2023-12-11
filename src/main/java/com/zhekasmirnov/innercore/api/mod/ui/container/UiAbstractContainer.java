package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;

public interface UiAbstractContainer {
    void addElementInstance(UIElement element, String name);

    void onWindowClosed();

    void openAs(IWindow window);

    void close();

    Object getParent();

    UIElement getElement(String elementName);

    UiVisualSlotImpl getSlotVisualImpl(String elementName);

    void setBinding(String elementName, String bindingName, Object value);

    Object getBinding(String elementName, String bindingName);

    void handleBindingDirty(String elementName, String bindingName);

    void handleInventoryToSlotTransaction(int inventorySlot, String slot, int amount);

    void handleSlotToSlotTransaction(String from, String to, int amount);

    void handleSlotToInventoryTransaction(String slot, int amount);
}
