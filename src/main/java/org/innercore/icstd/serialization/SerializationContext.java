package org.innercore.icstd.serialization;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SerializationContext {
    public int recursionDepth = 0;
    public boolean hasLegacyFormat = true;
    public boolean forceLegacyFormat = false;
    public HashSet<Object> visitedObjects = new HashSet<>();

    public static class ErrorRecord {
        private String message;
        private Object object;
        private Throwable exception;

        private ErrorRecord(String message, Object o, Throwable exception) {
            this.message = message;
            object = o;
            this.exception = exception;
        }

        public String getMessage() { return message; }
        public Object getObject() { return object; }
        public Throwable getException() { return exception; }

        public String objectToString(boolean inline, int inlineLimit) {
            String str = object != null ? object.toString() : "null";
            if (inlineLimit > 0 && str.length() > inlineLimit) {
                str = str.substring(0, inlineLimit);
            }
            return str;
        }

        public boolean isEmpty() {
            return object == null && exception == null && (message == null || message.isEmpty());
        }

        public String formatMsg(boolean inline, int inlineLimit, int stackLimit) {
            // TODO: improve this
            boolean hasMessage = !(message == null || message.isEmpty());
            if (exception == null && object == null && !hasMessage) {
                return "*no message*";
            }
            StringBuilder fmt = new StringBuilder();
            if (hasMessage) {
                fmt.append(message);
            }
            if (object != null) {
                fmt.append(" at ").append(objectToString(inline, inlineLimit));
            }
            if (exception != null) {
                fmt.append(" with ").append(ICLog.exceptionToString(exception, inline, stackLimit >= 0, stackLimit, -1));
            }
            return fmt.toString();
        }
    }

    public List<ErrorRecord> errorsList = null;
    public List<String> verboseMsgList = null;
    public boolean logWhenAddingErrors = true;

    public void addError(Throwable err, Object o, String msg) {
        if (errorsList == null) errorsList = new ArrayList<>();
        ErrorRecord r = new ErrorRecord(msg, o, err);
        if (r.isEmpty())
            r.message = "*no message*";
        if (r.exception == null)
            r.exception = new RuntimeException();
        if (logWhenAddingErrors)
            ICLog.i("Serializer", "error " + r.formatMsg(true, 256, 3));
        errorsList.add(r);
    }
    public void addError(String msg) { addError(null, null, msg); }
    public boolean hasErrors() { return errorsList != null && !errorsList.isEmpty(); }
}
