package com.zhekasmirnov.horizon.util;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class JSONUtils {
    public static <T> List<T> toList(JSONArray var0) {
        ArrayList var2 = new ArrayList();

        for(int var1 = 0; var1 < var0.length(); ++var1) {
            var2.add(var0.opt(var1));
        }

        return var2;
    }
}
