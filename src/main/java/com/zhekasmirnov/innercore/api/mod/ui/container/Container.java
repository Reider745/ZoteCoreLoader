package com.zhekasmirnov.innercore.api.mod.ui.container;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import org.innercore.icstd.inventory.ItemInstanceExtra;
import com.zhekasmirnov.innercore.api.log.DialogHelper;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectWrapper;
import com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchField;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UIElement;
import com.zhekasmirnov.innercore.api.mod.ui.elements.UISlotElement;
import com.zhekasmirnov.innercore.api.mod.ui.window.IWindow;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.runtime.MainThreadQueue;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaver;
import com.zhekasmirnov.innercore.api.runtime.saver.ObjectSaverRegistry;

import org.innercore.icstd.inventory.ItemInstance;
import org.innercore.icstd.modules.ItemModule;
import org.innercore.icstd.serialization.*;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zheka on 02.08.2017.
 */

public class Container implements WorkbenchField, UiAbstractContainer, ISerializableObject {
    public static final boolean isContainer = true;

    private static final String saverId = ObjectSerializer.registerType("ic:old_container", Container.class, new IObjectSerializer<Container>() {
        @Override
        public Container read(IScopedReader reader, SerializationContext ctx, String typeId) {
            Container container = new Container();
            IObjectReader slots = reader.asObject();
            if (slots == null) {
                ctx.addError("container serializer expected a slot map");
                reader.endScope();
                return null;
            }
            for (String name : slots.iterKeys()) {
                IArrayReader slot = slots.beginScope(name).asArray();
                if (slot != null && slot.size() > 2) {
                    ScriptableObject s = (ScriptableObject) container.getSlot(name);
                    s.put("id", s, slot.getInt(0, 0));
                    s.put("count", s, slot.getInt(1, 0));
                    s.put("data", s, slot.getInt(2, 0));
                    if (slot.size() > 3) {
                        ItemInstanceExtra extra = ObjectSerializer.deserializeObject(slot.beginScope(3), ItemInstanceExtra.class, ctx, true);
                        if (extra != null) {
                            s.put("extra", s, extra);
                        }
                    }
                } else if (slot != null && slot.size() == 0) {
                    container.getSlot(name);
                } else {
                    ctx.addError("invalid container slot format " + name);
                }
                slots.endChildScope();
            }
            reader.endScope();
            return container;
        }

        @Override
        public void write(IScopedWriter writer, Container container, SerializationContext ctx, String typeId) {
            if (container == null) throw new IllegalArgumentException("container is null");
            IObjectWriter slots = writer.asObject();
            for (Object key : container.slots.getIds()) {
                if (!(key instanceof String)) continue;
                Object s_ = container.getSlot((String) key);
                if (!(s_ instanceof ScriptableObject)) continue;

                ScriptableObject s = (ScriptableObject) s_;
                int id = ScriptableObjectHelper.getIntProperty(s, "id", 0);
                int count = ScriptableObjectHelper.getIntProperty(s, "count", 0);
                int data = ScriptableObjectHelper.getIntProperty(s, "data", 0);
                Object extra = ScriptableObjectHelper.getProperty(s, "extra", null);

                IArrayWriter slot = slots.beginScope((String) key).asArray();
                if (id == 0 && count == 0 && data == 0) {
                    slot.endScope(); // empty slot
                    continue;
                }
                slot.addInt(id);
                slot.addInt(count);
                slot.addInt(data);
                if (extra instanceof ItemInstanceExtra) {
                    ObjectSerializer.serializeObject(slot.beginScope(), extra, ctx, true);
                }
                slot.endScope();
            }
            writer.endScope();
        }
    });

    private static final String legacySaverId = ObjectSaverRegistry.registerSaverStr("_container", new ObjectSaver() {
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

    @Override
    public String getSerializationTypeId() {
        return saverId;
    }

    public static void initSaverId() {
        // forces class to load
    }

    public Container() {
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
            }
            else if (_slot instanceof ScriptableObject) {
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
        MainThreadQueue.serverThread.enqueue(() -> {
            int amount = amount0;
            UIElement element = getElement(slotName);
            UISlotElement slotElement = element instanceof UISlotElement ? (UISlotElement) element : null;
            if (amount > 0 && (slotElement == null || !slotElement.isVisual)) {
                Object slotObj = getSlot(slotName);
                if (slotObj instanceof ScriptableObject) {
                    ItemInstance invSlot = ItemInstance.makeFromPointer(NativeAPI.getInventorySlot(inventorySlotId));
                    ScriptableObjectWrapper slot = new ScriptableObjectWrapper((ScriptableObject) slotObj);
                    if (slot.getInt("id") == 0 || slot.getInt("id") == invSlot.id && slot.getInt("data") == invSlot.data && slot.get("extra") == null) {
                        if (amount > invSlot.count) {
                            amount = invSlot.count;
                        }
                        if (slotElement == null || slotElement.isValidItem(invSlot.id, invSlot.count, invSlot.data, invSlot.extra)) {
                            int maxStackSize = slotElement != null && slotElement.maxStackSize > 0 ? slotElement.maxStackSize : ItemModule.getMaxStack(invSlot.id, invSlot.data);
                            amount = Math.min(amount, Math.min(invSlot.count, maxStackSize - slot.getInt("count")));
                            slot.put("count", slot.getInt("count") + amount);
                            slot.put("id", invSlot.id);
                            slot.put("data", invSlot.data);
                            slot.put("extra", ItemInstanceExtra.cloneExtra(invSlot.extra));
                            if (amount < invSlot.count) {
                                NativeAPI.setInventorySlot(inventorySlotId, invSlot.id, invSlot.count - amount, invSlot.data, ItemInstanceExtra.getValueOrNullPtr(invSlot.extra));
                            } else {
                                NativeAPI.setInventorySlot(inventorySlotId, 0, 0, 0, 0);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void handleSlotToSlotTransaction(String from, String to, int amount) {
        // TODO: ???
    }

    @Override
    public void handleSlotToInventoryTransaction(String slotName, int amount0) {
        MainThreadQueue.serverThread.enqueue(() -> {
            int amount = amount0;
            UIElement element = getElement(slotName);
            UISlotElement slotElement = element instanceof UISlotElement ? (UISlotElement) element : null;
            if (amount > 0 && (slotElement == null || !slotElement.isVisual)) {
                Object slotObj = getSlot(slotName);
                if (slotObj instanceof ScriptableObject) {
                    ScriptableObjectWrapper slot = new ScriptableObjectWrapper((ScriptableObject) slotObj);
                    int id = slot.getInt("id");
                    int count = slot.getInt("count");
                    int data = slot.getInt("data");
                    ItemInstanceExtra extra = ItemInstanceExtra.unwrapObject(slot.get("extra"));
                    if (id != 0 && count > 0) {
                        amount = Math.min(amount, count);
                        count -= amount;
                        NativeAPI.addItemToInventory(id, amount, data, ItemInstanceExtra.getValueOrNullPtr(extra), true);
                        if (count <= 0) {
                            id = data = count = 0;
                            extra = null;
                        }
                        slot.put("id", id);
                        slot.put("count", count);
                        slot.put("data", data);
                        slot.put("extra", extra);
                    }
                }
            }
        });
    }

    public void setSlot(String name, int id, int count, int data) {
        getFullSlot(name).set(id, count, data);
    }

    public void setSlot(String name, int id, int count, int data, ItemInstanceExtra extra) {
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


    public void refreshSlots() {}

    public void applyChanges() {}


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
