package com.zhekasmirnov.innercore.ui;

/**
 * Created by zheka on 05.01.2018.
 */

public class LoadingOverlayDrawable {
    public LoadingOverlayDrawable() {

    }

    private String text = "", tip = "";

    public void setText(String text) {
        this.text = text;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    float progressTarget;

    public void setProgress(float progress) {
        this.progressTarget = progress;
    }

    private long lastTime = 0;


}
