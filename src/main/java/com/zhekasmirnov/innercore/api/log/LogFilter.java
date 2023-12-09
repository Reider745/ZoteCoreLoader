package com.zhekasmirnov.innercore.api.log;

import java.util.ArrayList;

/**
 * Created by zheka on 09.08.2017.
 */

public class LogFilter {
    ArrayList<LogMessage> logMessages = new ArrayList<>();

    private static boolean isContainingErrorTags = false;

    public static boolean isContainingErrorTags() {
        return isContainingErrorTags;
    }

    public void log(LogType type, String prefix, String line) {
        if (type == LogType.ERROR || prefix.equals("ERROR")) {
            isContainingErrorTags = true;
        }
        logMessages.add(new LogMessage(type, prefix, line));
    }

    private LogMessage getMessageByIndex(int index) {
        if (index < 0 || index >= logMessages.size()) {
            return null;
        }
        return logMessages.get(index);
    }

    public String buildFilteredLog(boolean format) {
        String build = "";

        for (int i = 0; i < logMessages.size(); i++) {
            LogMessage msg = getMessageByIndex(i);
            build += msg.format(format) + (format ? "<br>" : "\n");
        }

        return build;
    }

    public static class LogMessage {
        public final LogType type;
        public final LogPrefix prefix;
        public final String strPrefix;
        public final String message;

        public LogMessage(LogType type, String prefix, String message) {
            this.type = type;
            this.strPrefix = prefix;
            this.prefix = LogPrefix.fromString(prefix);
            this.message = message;
        }

        @Override
        public String toString() {
            return "[" + strPrefix + "] " + message;
        }

        public String toHtml() {
            return "<font color='" + prefix.toFontColor() + "'><b>" + "[" + strPrefix + "]" + "</b>" + " " + message
                    + "</font>";
        }

        public String format(boolean format) {
            return format ? toHtml() : toString();
        }
    }
}
