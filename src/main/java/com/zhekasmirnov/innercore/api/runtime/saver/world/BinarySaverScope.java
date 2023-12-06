package com.zhekasmirnov.innercore.api.runtime.saver.world;

import org.json.JSONObject;

import java.io.*;
import java.util.Base64;

public abstract class BinarySaverScope implements WorldDataScopeRegistry.SaverScope {
    @Override
    public void readJson(Object json) throws Exception {
        String data = ((JSONObject) json).optString("data");
        if (data == null) {
            throw new IOException("missing binary data for BinarySaverScope");
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
            read(inputStream);
        }
    }

    @Override
    public Object saveAsJson() throws Exception {
        JSONObject json = new JSONObject();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            save(outputStream);
            json.put("data", Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        }
        return json;
    }

    public abstract void read(InputStream inputStream);

    public abstract void save(OutputStream outputStream);
}
