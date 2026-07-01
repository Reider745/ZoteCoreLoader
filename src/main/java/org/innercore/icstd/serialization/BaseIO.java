package org.innercore.icstd.serialization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;

public class BaseIO {

    public static ESerializedType getSerializedTypeFromObject(Object value) {
        if (value instanceof Number) {
            if (value instanceof Integer) {
                return ESerializedType.INT;
            } else if (value instanceof Double || value instanceof BigDecimal) {
                return ESerializedType.DOUBLE;
            } else if (value instanceof Long) {
                return ESerializedType.LONG;
            } else if (value instanceof Float) {
                return ESerializedType.FLOAT;
            } else if (value instanceof Short || value instanceof Byte) {
                return ESerializedType.INT;
            }
        } else if (value instanceof String) {
            return ESerializedType.STRING;
        } else if (value instanceof Boolean) {
            return ESerializedType.BOOLEAN;
        } else if (value instanceof JSONObject) {
            return ESerializedType.OBJECT_SCOPE;
        } else if (value instanceof JSONArray) {
            return ESerializedType.ARRAY_SCOPE;
        } else if (value == null || value == JSONObject.NULL) {
            return ESerializedType.EMPTY_SCOPE;
        }
        throw new IllegalArgumentException("unknown serialized value type: " + value.getClass() + " " + value);
    }

    public static abstract class ScopedWriter implements IScopedWriter {
        private final BaseWriter parent;
        protected Object resultObj;
        private BaseWriter cur;
        private boolean inScope = true;

        public ScopedWriter() { parent = null;}
        protected ScopedWriter(BaseWriter parent) { this.parent = parent; }

        @Override
        public IObjectWriter asObject() {
            if (!inScope) throw new IllegalStateException("not in scope");
            if (cur == null) {
                cur = newObjectWriter(this);
            }
            if (cur instanceof IObjectWriter) {
                return (IObjectWriter) cur;
            }
            throw new IllegalStateException("scoped writer was already used as non-object");
        }

        protected abstract BaseWriter newObjectWriter(ScopedWriter parent);
        protected abstract BaseWriter newArrayWriter(ScopedWriter parent);
        protected abstract ScopedWriter newScopedWriter(BaseWriter parent);

        @Override
        public IArrayWriter asArray() {
            if (!inScope) throw new IllegalStateException("not in scope");
            if (cur == null) {
                cur = newArrayWriter(this);
            }
            if (cur instanceof IArrayWriter) {
                return (IArrayWriter) cur;
            }
            throw new IllegalStateException("scoped writer was already used as non-array");
        }

        @Override
        public void endScope() {
            if (!inScope) throw new IllegalStateException("not in scope");
            if (parent != null) {
                if (parent.scope == null) throw new IllegalStateException("internal error: scope is closed");
                if (parent.childScope == null) throw new IllegalStateException("internal error: not in child scope");
                parent.writeEndedChildScope(cur != null ? cur.endThisScope() : null);
                parent.childScope = null;
            } else {
                resultObj = cur != null ? cur.endThisScope() : null;
            }
            inScope = false;
        }

        @Override
        public boolean isInScope() {
            return inScope;
        }

        @Override
        public void forceEndScope(SerializationContext ctx) {
            if (!inScope) return;
            if (cur == null) {
                endScope();
                return;
            }
            cur.forceEndScope(ctx);
            inScope = false;
        }
    }

    public static abstract class BaseWriter {
        protected ScopedWriter scope;
        protected ScopedWriter childScope;

        protected BaseWriter(ScopedWriter scope) { this.scope = scope; }

        protected abstract void writeEndedChildScope(Object childData);
        protected abstract Object endThisScope();

        public void endScope() {
            if (scope == null) throw new IllegalStateException("scope is already closed");
            scope.endScope();
            scope = null;
        }

        protected void validateStateForWrite() {
            if (childScope != null) throw new IllegalStateException("in child scope");
            else if (scope == null) throw new IllegalStateException("scope is closed");
        }

        protected ScopedWriter beginScopeImpl() {
            validateStateForWrite();
            return (childScope = scope.newScopedWriter(this));
        }

        protected void forceEndScope(SerializationContext ctx) {
            if (scope == null) return;
            if (childScope != null) {
                childScope.forceEndScope(ctx);
                childScope = null;
            }
            endScope();
        }
    }



    public static abstract class ScopedReader implements IScopedReader {
        protected final Object data;
        private BaseReader parent;
        private BaseReader cur;
        private boolean inScope = true;

        protected ScopedReader(BaseReader parent, Object data) {
            this(data);
            this.parent = parent;
        }

        protected ScopedReader(Object data) {
            this.data = data;
        }

        protected void validateInScope() {
            if (!inScope) throw new IllegalStateException("not in scope");
        }

        @Override
        public IObjectReader asObject() {
            validateInScope();
            if (isObject()) {
                if (cur == null) cur = newObjectReader(this, data);
                return (IObjectReader) cur;
            }
            return null;
        }

        @Override
        public IArrayReader asArray() {
            validateInScope();
            if (isArray()) {
                if (cur == null) cur = newArrayReader(this, data);
                return (IArrayReader) cur;
            }
            return null;
        }

        @Override
        public void endScope() {
            if (!inScope) throw new IllegalStateException("not in scope");
            inScope = false;
            if (parent != null) {
                if (parent.scope == null) throw new IllegalStateException("internal error: scope is closed");
                if (parent.childScope == null) throw new IllegalStateException("internal error: not in child scope");
                parent.endReadingChildScope();
                parent.childScope = null;
            }
            inScope = false;
        }

        @Override
        public boolean isInScope() {
            return inScope;
        }

        @Override
        public void forceEndScope(SerializationContext ctx) {
            if (!inScope) return;
            if (cur == null) {
                endScope();
                return;
            }
            cur.forceEndScope(ctx);
            inScope = false;
        }

        protected abstract BaseReader newObjectReader(ScopedReader parent, Object data);
        protected abstract BaseReader newArrayReader(ScopedReader parent, Object data);
        protected abstract ScopedReader newScopedReader(BaseReader parent, Object data);
    }

    public static abstract class BaseReader {
        protected ScopedReader scope;
        protected ScopedReader childScope;

        protected BaseReader(ScopedReader scope) { this.scope = scope; }

        protected void validateStateForRead() {
            if (childScope != null) throw new IllegalStateException("in child scope");
            else if (scope == null) throw new IllegalStateException("scope is closed");
        }

        protected void endReadingChildScope() {}

        public void endScope() {
            if (scope == null) throw new IllegalStateException("scope is already closed");
            scope.endScope();
            scope = null;
        }

        protected ScopedReader beginScopeImpl(Object data) {
            validateStateForRead();
            return (childScope = scope.newScopedReader(this, data));
        }

        protected void forceEndScope(SerializationContext ctx) {
            if (scope == null) return;
            if (childScope != null) {
                childScope.forceEndScope(ctx);
                childScope = null;
            }
            endScope();
        }

        public void endChildScope() {
            if (childScope == null) throw new IllegalStateException("in not in child scope");
            else if (scope == null) throw new IllegalStateException("scope is closed");
            childScope.endScope();
        }
    }

}
