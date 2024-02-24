package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.reider745.InnerCoreServer;
import com.zhekasmirnov.innercore.api.NativeItemInstanceExtra;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchField;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 02.08.2017.
 */

@Deprecated(since = "Zote")
public class Container implements WorkbenchField, UiAbstractContainer {
    public static final boolean isContainer = true;

    private static final int saverId;
    static {
        saverId = ObjectSaverRegistry.registerSaver("_container", new ObjectSaver() {
            @Override
            public Object read(ScriptableObject input) {
                Container con = new Container();
                con.slots = input;
                return con;
            }

            @Override
            public ScriptableObject save(Object input) {
                if (input instanceof Container) {
                    return ((Container) input).slots;
                }
                return null;
            }
        });
    }

    public static void initSaverId() {
        // forces class to load
    }

    public Container() {
        ObjectSaverRegistry.registerObject(this, saverId);
    }

    public Object parent, tileEntity;

    public Container(Object parent) {
        this();
        setParent(parent);
    }

    public void setParent(Object parent) {
        this.parent = this.tileEntity = parent;
    }

    public Object getParent() {
        return parent;
    }

    public ScriptableObject slots = new ScriptableObject() {
        @Override
        public String getClassName() {
            return "slot_array";
        }
    };

    public Object getSlot(String name) {
        if (slots.has(name, slots)) {
            return slots.get(name);
        }
        Slot slot = new Slot();
        slots.put(name, slots, slot);
        return slot;
    }

    public Slot getFullSlot(String name) {
        if (slots.has(name, slots)) {
            Object _slot = slots.get(name);
            if (_slot instanceof Slot) {
                return (Slot) _slot;
            } else if (_slot instanceof ScriptableObject) {
                return new Slot((ScriptableObject) _slot);
            }
        }
        Slot slot = new Slot();
        slots.put(name, slots, slot);
        return slot;
    }

    @Override
    public UiVisualSlotImpl getSlotVisualImpl(String name) {
        return new ScriptableUiVisualSlotImpl((ScriptableObject) getSlot(name));
    }

    @Override
    public void handleInventoryToSlotTransaction(int inventorySlotId, String slotName, int amount0) {
        InnerCoreServer.useClientMethod("Container.handleInventoryToSlotTransaction(inventorySlotId, slotName, amount0)");
    }

    @Override
    public void handleSlotToSlotTransaction(String from, String to, int amount) {
        // TODO: ???
    }

    @Override
    public void handleSlotToInventoryTransaction(String slotName, int amount0) {
        InnerCoreServer.useClientMethod("Container.handleSlotToInventoryTransaction(slotName, amount0)");
    }

    public void setSlot(String name, int id, int count, int data) {
        getFullSlot(name).set(id, count, data);
    }

    public void setSlot(String name, int id, int count, int data, NativeItemInstanceExtra extra) {
        getFullSlot(name).set(id, count, data, extra);
    }

    public void validateSlot(String name) {
        getFullSlot(name).validate();
    }

    public void clearSlot(String name) {
        getFullSlot(name).set(0, 0, 0);
    }

    public void dropSlot(String name, float x, float y, float z) {
        getFullSlot(name).drop(x, y, z);
    }

    public void dropAt(float x, float y, float z) {
        Object[] keys = slots.getAllIds();
        for (Object key : keys) {
            if (key instanceof String) {
                getFullSlot((String) key).drop(x, y, z);
            }
        }
    }

    public void validateAll() {
        Object[] keys = slots.getAllIds();
        for (Object key : keys) {
            if (key instanceof String) {
                getFullSlot((String) key).validate();
            }
        }
    }

    private boolean isOpened = false;
    private IWindow window;
    private HashMap<String, UIElement> elements = new HashMap<>();

    public IWindow getWindow() {
        return window;
    }

    public void _addElement(UIElement element, String name) {
        if (elements != null) {
            elements.put(name, element);
        }
        element.setupInitialBindings(this, name);
    }

    @Override
    public void addElementInstance(UIElement element, String name) {
        _addElement(element, name);
    }

    public void _removeElement(String name) {
        if (elements != null) {
            elements.remove(name);
        }
    }

    public void openAs(IWindow window) {
        if (isOpened) {
            close();
        }

        this.window = window;
        this.elements = window.getElements();
        window.setContainer(this);

        isOpened = true;

        window.open();
        setupInitialBindings();

        callOnOpenEvent();
    }

    public void close() {
        if (isOpened) {
            isOpened = false;
            this.window.close();
            callOnCloseEvent(false);
            this.window = null;
            this.elements = null;
        }
    }

    public interface OnCloseListener {
        void onClose(Container c, IWindow win);
    }

    public interface OnOpenListener {
        void onOpen(Container c, IWindow win);
    }

    private OnOpenListener openListener;
    private OnCloseListener closeListener;

    public void setOnOpenListener(OnOpenListener openListener) {
        this.openListener = openListener;
    }

    public void setOnCloseListener(OnCloseListener closeListener) {
        this.closeListener = closeListener;
    }

    private void callOnOpenEvent() {
        if (openListener != null) {
            try {
                openListener.onOpen(this, window);
            } catch (Exception e) {
                DialogHelper.reportNonFatalError("Exception in container open listener", e);
            }
        }
        Callback.invokeCallback("ContainerOpened", this, window);
    }

    private void callOnCloseEvent(boolean userCalled) {
        if (closeListener != null) {
            try {
                closeListener.onClose(this, window);
            } catch (Exception e) {
                DialogHelper.reportNonFatalError("Exception in container close listener", e);
            }
        }
        Callback.invokeCallback("ContainerClosed", this, window, userCalled);
    }

    public void onWindowClosed() {
        if (isOpened && !window.isOpened()) {
            isOpened = false;
            this.window = null;
            this.elements = null;

            callOnCloseEvent(true);
        }
    }

    public boolean isOpened() {
        return this.window != null && this.window.isOpened();
    }

    public IWindow getGuiScreen() {
        return window;
    }

    public ScriptableObject getGuiContent() {
        if (window != null) {
            return window.getContent();
        }
        return null;
    }

    public UIElement getElement(String elementName) {
        if (elements != null) {
            UIElement element = elements.get(elementName);
            if (element != null && !element.isReleased()) {
                return element;
            }
        }
        return null;
    }

    public void setBinding(String elementName, String bindingName, Object val) {
        UIElement element = getElement(elementName);
        if (element != null) {
            element.setBinding(bindingName, val);
        }
    }

    public Object getBinding(String elementName, String bindingName) {
        UIElement element = getElement(elementName);
        if (element != null) {
            return element.getBinding(bindingName);
        }
        return null;
    }

    @Override
    public void handleBindingDirty(String elementName, String bindingName) {

    }

    public void sendChanges() {

    }

    private void setupInitialBindings() {
        if (elements != null) {
            for (String name : new ArrayList<String>(elements.keySet())) {
                if (elements == null) {
                    break;
                }
                UIElement element = elements.get(name);
                if (element != null) {
                    element.setupInitialBindings(this, name);
                }
            }
        }
    }

    public void setScale(String name, float value) {
        setBinding(name, "value", value);
    }

    public float getValue(String name) {
        return (float) getBinding(name, "value");
    }

    public void setText(String name, String value) {
        setBinding(name, "text", value);
    }

    public String getText(String name) {
        return (String) getBinding(name, "text");
    }

    public boolean isElementTouched(String name) {
        UIElement element = getElement(name);
        return element != null && element.isTouched;
    }

    public void invalidateUIElements(boolean onCurrentThread) {
        if (window != null) {
            window.invalidateElements(onCurrentThread);
        }
    }

    public void invalidateUIElements() {
        invalidateUIElements(false);
    }

    public void invalidateUIDrawing(boolean onCurrentThread) {
        if (window != null) {
            window.invalidateDrawing(onCurrentThread);
        }
    }

    public void invalidateUIDrawing() {
        invalidateUIDrawing(false);
    }

    public void invalidateUI(boolean onCurrentThread) {
        if (window != null) {
            window.invalidateDrawing(onCurrentThread);
            window.invalidateElements(onCurrentThread);
        }
    }

    public void invalidateUI() {
        invalidateUI(false);
    }

    public void refreshSlots() {
    }

    public void applyChanges() {
    }

    private String wbSlotNamePrefix = "slot";

    public void setWbSlotNamePrefix(String wbSlotNamePrefix) {
        this.wbSlotNamePrefix = wbSlotNamePrefix;
    }

    /**
     * @param i
     * @return returns slot, that field slot i is linked to
     */
    @Override
    public Slot getFieldSlot(int i) {
        return getFullSlot(wbSlotNamePrefix + i);
    }

    @Override
    public AbstractSlot getFieldSlot(int x, int y) {
        if (x >= 0 && y >= 0 && x < 3 && y < 3) {
            return getFieldSlot(y * 3 + x);
        } else {
            return null;
        }
    }

    /**
     * @return returns a scriptable array of slots, that can be linked or modified
     */
    @Override
    public Scriptable asScriptableField() {
        Object[] slots = new Object[9];
        for (int i = 0; i < 9; i++) {
            slots[i] = getFieldSlot(i);
        }

        return new NativeArray(slots);
    }

    @Override
    public int getWorkbenchFieldSize() {
        return 3;
    }

    public boolean isLegacyContainer() {
        return true;
    }
}
