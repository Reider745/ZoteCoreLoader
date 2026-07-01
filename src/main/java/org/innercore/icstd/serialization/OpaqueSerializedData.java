package org.innercore.icstd.serialization;

import org.innercore.internal.serialization.JsonIO;

public class OpaqueSerializedData {
    private Object data = null;
    private boolean hasData = false;
    private boolean isWriting = false;
    private int readerCount = 0;

    public OpaqueSerializedData() {

    }

    public boolean isEmpty() {
        return !hasData;
    }

    public void clear() {
        if (isWriting) throw new IllegalStateException("already writing");
        if (readerCount != 0) throw new IllegalStateException("can't write while readers are present");
        hasData = false;
        data = null;
    }

    private void onEndWrite(Object data) {
        this.data = data;
        hasData = true;
        isWriting = false;
    }

    public IScopedWriter clearAndWrite() {
        clear();
        return beginWrite();
    }

    public IScopedWriter beginWrite() {
        if (isWriting) throw new IllegalStateException("already writing");
        if (readerCount != 0) throw new IllegalStateException("can't write while readers are present");
        if (hasData) throw new IllegalStateException("must be cleaned before writing");
        isWriting = true;

        return new JsonIO.ScopedWriter() {
            @Override
            public void endScope() {
                super.endScope();
                onEndWrite(getResult());
            }
        };
    }

    private void onEndRead() {
        readerCount--;
    }

    public IScopedReader beginRead() {
        if (isWriting) throw new IllegalStateException("already writing");
        if (!hasData) throw new IllegalStateException("no data is stored");
        readerCount++;

        return new JsonIO.ScopedReader(data) {
            @Override
            public void endScope() {
                super.endScope();
                onEndRead();
            }
        };
    }
}
