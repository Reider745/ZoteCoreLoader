package com.zhekasmirnov.innercore.api.mod.ui.types;

import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import com.zhekasmirnov.innercore.api.mod.ui.window.UIWindow;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by zheka on 31.07.2017.
 */

public class TouchEvent {
    public float _x, _y, x, y, localX, localY, downX, downY;

    public TouchEventType type;
    private int callbackId = 0;
    private boolean isCallbackOpen = false;

    private ITouchEventListener listener;
    private Handler handler;
    public TouchEvent(ITouchEventListener listener) {
        handler = new Handler();
        this.listener = listener;
    }

    private boolean hasMovedSinceLastDown() {
        return Math.sqrt((_x - downX) * (_x - downX) + (_y - downY) * (_y - downY)) > 75;
    }

    public void update(MotionEvent motionEvent) {
        _x = motionEvent.getX();
        _y = motionEvent.getY();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = _x;
                downY = _y;
                type = TouchEventType.DOWN;
                openCallback(TouchEventType.LONG_CLICK, 800);
                break;
            case MotionEvent.ACTION_UP:
                if (isCallbackOpen) {
                    cancelCallback();
                    if (!hasMovedSinceLastDown()) {
                        type = TouchEventType.CLICK;
                        break;
                    }
                }
                type = TouchEventType.UP;
                break;
            case MotionEvent.ACTION_CANCEL:
                type = TouchEventType.CANCEL;
                cancelCallback();
                break;
            default:
                type = TouchEventType.MOVE;
                break;
        }

        listener.onTouchEvent(this);
    }

    public void preparePosition(UIWindow win, Rect rect) {
        x = _x / win.getScale();
        y = _y / win.getScale();
        if (rect != null) {
            localX = (x - rect.left) / (float) (rect.right - rect.left);
            localY = (y - rect.top) / (float) (rect.bottom - rect.top);
        }
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

    private void cancelCallback() {
        this.isCallbackOpen = false;
        this.callbackId++;
    }

    private void openCallback(TouchEventType type, int delay) {
        this.callbackId++;
        this.isCallbackOpen = true;
        handler.postDelayed(new Callback(this, type, this.callbackId), delay);
    }

    private class Callback implements Runnable {
        private TouchEvent event;
        private TouchEventType type;
        private int id;

        Callback(TouchEvent event, TouchEventType type, int id) {
            this.event = event;
            this.type = type;
            this.id = id;
        }

        @Override
        public void run() {
            if (event.callbackId == id && !event.hasMovedSinceLastDown()) {
                event.isCallbackOpen = false;
                event.type = type;
                event.listener.onTouchEvent(event);
            }
        }
    }

}
