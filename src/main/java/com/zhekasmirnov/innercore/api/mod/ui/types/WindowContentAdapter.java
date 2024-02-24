package com.zhekasmirnov.innercore.api.mod.ui.types;

import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindowLocation;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import org.mozilla.javascript.IdFunctionObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 04.08.2017.
 */

@Deprecated(since = "Zote")
public class WindowContentAdapter {
    ScriptableObject content, elements;
    NativeArray drawing;

    public WindowContentAdapter() {
        this(ScriptableObjectHelper.createEmpty());
    }

    public WindowContentAdapter(ScriptableObject content) {
        super();
        if (content == null) {
            content = ScriptableObjectHelper.createEmpty();
        }

        this.content = content;

        this.drawing = ScriptableObjectHelper.getNativeArrayProperty(this.content, "drawing", null);
        if (this.drawing == null) {
            this.drawing = ScriptableObjectHelper.createEmptyArray();
            this.content.put("drawing", content, this.drawing);
        }

        this.elements = ScriptableObjectHelper.getScriptableObjectProperty(this.content, "elements", null);
        if (this.elements == null) {
            this.elements = ScriptableObjectHelper.createEmpty();
            this.content.put("elements", content, this.elements);
        }
    }

    public void addDrawing(ScriptableObject drawing) {
        IdFunctionObject id = new IdFunctionObject(null, "Array", 8, 0);
        this.drawing.execIdCall(id, Compiler.assureContextForCurrentThread(), this.drawing, this.drawing,
                new Object[] { drawing });
    }

    public void insertDrawing(ScriptableObject drawing) {
        IdFunctionObject id = new IdFunctionObject(null, "Array", 11, 0);
        this.drawing.execIdCall(id, Compiler.assureContextForCurrentThread(), this.drawing, this.drawing,
                new Object[] { drawing });
    }

    public void addElement(String name, ScriptableObject element) {
        this.elements.put(name, this.elements, element);
    }

    public ScriptableObject getDrawing() {
        return drawing;
    }

    public ScriptableObject getElements() {
        return elements;
    }

    public void setLocation(UIWindowLocation location) {
        this.content.put("location", this.content, location.asScriptable());
    }

    public ScriptableObject getContent() {
        return content;
    }
}
