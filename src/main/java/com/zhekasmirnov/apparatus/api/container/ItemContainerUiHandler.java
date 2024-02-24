package com.zhekasmirnov.apparatus.api.container;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiAbstractContainer;
import com.zhekasmirnov.innercore.api.mod.ui.container.UiVisualSlotImpl;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;
import org.json.JSONObject;
import org.mozilla.javascript.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Deprecated(since = "Zote")
public class ItemContainerUiHandler implements UiAbstractContainer {
    private final ItemContainer container;

    private IWindow window = null;
    private final Map<String, Object> bindingsMap = new HashMap<>();


    public ItemContainerUiHandler(ItemContainer container) {
        this.container = container;
    }

    @Override
    public void onWindowClosed() {
        close();
    }

    public IWindow getWindow() {
        return window;
    }

    @Override
    public void openAs(IWindow window) {
        this.close();
        this.window = window;
        this.window.setContainer(this);
        this.window.open();

        Map<String, UIElement> elements = this.window.getElements();
        if (elements != null) {
            for (String name : new ArrayList<>(elements.keySet())) {
                UIElement element = elements.get(name);
                if (element != null) {
                    element.setupInitialBindings(this, name);
                }
            }
        }

        applyAllBindingsFromMap();
    }

    @Override
    public void close() {
        if (window != null) {
            window.close();
            window = null;
            Network.getSingleton().getClientThreadJobExecutor().add(container::sendClosed);
        }
    }

    @Override
    public Object getParent() {
        return container;
    }


    @Override
    public void addElementInstance(UIElement element, String name) {
        element.setupInitialBindings(this, name);
    }

    @Override
    public UIElement getElement(String elementName) {
        return window != null ? window.getElements().get(elementName) : null;
    }

    @Override
    public UiVisualSlotImpl getSlotVisualImpl(final String name) {
        return new UiVisualSlotImpl() {
            ItemContainerSlot slot;

            @Override
            public int getId() {
                slot = container.getSlot(name);
                return slot.id;
            }

            @Override
            public int getCount() {
                if (slot == null) {
                    slot = container.getSlot(name);
                }
                return slot.count;
            }

            @Override
            public int getData() {
                if (slot == null) {
                    slot = container.getSlot(name);
                }
                return slot.data;
            }

            @Override
            public NativeItemInstanceExtra getExtra() {
                if (slot == null) {
                    slot = container.getSlot(name);
                }
                return slot.extra;
            }
        };
    }


    private Object validateBindingValue(Object value) {
        while (value instanceof Wrapper) {
            value = ((Wrapper) value).unwrap();
        }
        if (value instanceof CharSequence) {
            return value.toString();
        } else if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return null;
    }

    @Override
    public Object getBinding(String elementName, String bindingName) {
        UIElement element = getElement(elementName);
        return element != null ? element.getBinding(bindingName) : null;
    }

    @Override
    public void setBinding(String elementName, String bindingName, Object value) {
        Object validatedValue = validateBindingValue(value);
        if (validatedValue == null) {
            throw new IllegalArgumentException("invalid binding value for " + elementName + "::" + bindingName + " value=" + value + " required types: number, string or boolean");
        }
        UIElement element = getElement(elementName);
        if (element != null) {
            element.setBinding(bindingName, validatedValue);
        }
        synchronized (bindingsMap) {
            bindingsMap.put(elementName + "::" + bindingName, validatedValue);
        }
    }

    @Override
    public void handleBindingDirty(String elementName, String bindingName) {
        Object bindingValue = validateBindingValue(getBinding(elementName, bindingName));
        if (bindingValue != null) {
            synchronized (bindingsMap) {
                bindingsMap.put(elementName + "::" + bindingName, bindingValue);
            }
        }
    }

    void applyAllBindingsFromMap() {
        synchronized (bindingsMap) {
            for (Map.Entry<String, Object> entry : bindingsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    String[] parts = key.split("::");
                    if (parts.length == 2) {
                        UIElement element = getElement(parts[0]);
                        if (element != null) {
                            element.setBinding(parts[1], value);
                        }
                    }
                }
            }
        }
    }

    void setBindingByComposedName(String name, Object value) {
        String[] parts = name.split("::");
        if (parts.length == 2) {
            setBinding(parts[0], parts[1], value);
        }
    }

    void receiveBindingsFromServer(JSONObject bindings) {
        if (bindings == null) {
            return;
        }
        for (Iterator<String> it = bindings.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = validateBindingValue(bindings.opt(key));
            if (value != null) {
                setBindingByComposedName(key, value);
            }
        }
    }



    @Override
    public void handleInventoryToSlotTransaction(int inventorySlot, String slot, int amount) {
        Network.getSingleton().getClientThreadJobExecutor().add(() -> container.sendInventoryToSlotTransaction(inventorySlot, slot, amount));
    }

    @Override
    public void handleSlotToSlotTransaction(String from, String to, int amount) {
        Network.getSingleton().getClientThreadJobExecutor().add(() -> container.sendSlotToSlotTransaction(from, to, amount));
    }

    @Override
    public void handleSlotToInventoryTransaction(String slot, int amount) {
        Network.getSingleton().getClientThreadJobExecutor().add(() -> container.sendSlotToInventoryTransaction(slot, amount));
    }
}
