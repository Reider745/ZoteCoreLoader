package com.zhekasmirnov.innercore.api.runtime.saver.world;

import org.innercore.icstd.serialization.IScopedReader;
import org.innercore.icstd.serialization.IScopedWriter;
import org.innercore.icstd.serialization.SerializationContext;
import org.json.JSONObject;

import java.io.*;
import java.util.Base64;

public abstract class BinarySaverScope implements WorldDataScopeRegistry.SaverScope {
    @Override
    public void read(IScopedReader io, SerializationContext ctx) {
        if (io.isObject()) {
            String data = io.asObject().getString("data", null);
            if (data != null) {
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
                    read(inputStream);
                } catch (IOException e) {
                    ctx.addError(e, null, "failed to read binary from base64 data");
                }
            }
        }
        io.endScope();
    }

    @Override
    public void write(IScopedWriter io, SerializationContext ctx) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            save(outputStream);
            io.asObject().putString("data",  Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        } catch (IOException e) {
            ctx.addError(e, null, "failed to save binary to base64 data");
        }
        io.endScope();
    }

    public abstract void read(InputStream inputStream);
    public abstract void save(OutputStream outputStream);
}
