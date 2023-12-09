package com.zhekasmirnov.innercore.api.log;

/**
 * Created by zheka on 09.08.2017.
 */

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.IOException;

public class LogWriter {
    private final File file;
    private String buffer = "";

    public LogWriter(File file) {
        this.file = file;
    }

    public void clear() {
        try {
            FileTools.writeFileText(file.getAbsolutePath(), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logMsg(LogType type, String prefix, String message) {
        buffer += "[" + prefix + "] " + message + "\n";
        if (buffer.length() > 2048) {
            flush();
        }
    }

    public void flush() {
        try {
            FileTools.addFileText(file.getAbsolutePath(), buffer);
            buffer = "";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
