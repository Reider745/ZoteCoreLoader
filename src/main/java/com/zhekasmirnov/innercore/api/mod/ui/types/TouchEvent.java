package com.zhekasmirnov.innercore.api.mod.ui.types;

import android.graphics.Rect;
import android.view.MotionEvent;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

@Deprecated(since = "Zote")
public class TouchEvent {
    public float _x, _y, x, y, localX, localY, downX, downY;

    public TouchEventType type;

    public TouchEvent(ITouchEventListener listener) {
    }

    public void update(MotionEvent motionEvent) {
    }

    public void preparePosition(UIWindow win, Rect rect) {
    }

    public ScriptableObject posAsScriptable() {
        ScriptableObject pos = new ScriptableObject() {
            @Override
            public String getClassName() {
                return "position";
            }
        };
        pos.put("x", pos, x);
        pos.put("y", pos, y);
        return pos;
    }

    public ScriptableObject localPosAsScriptable() {
        ScriptableObject pos = new ScriptableObject() {
            @Override
            public String getClassName() {
                return "position";
            }
        };
        pos.put("x", pos, localX);
        pos.put("y", pos, localY);
        return pos;
    }
}
