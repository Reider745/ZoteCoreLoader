package com.zhekasmirnov.horizon.util;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class JSONUtils {

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(JSONArray var0) {
        ArrayList<T> var2 = new ArrayList<>();

        for(int var1 = 0; var1 < var0.length(); ++var1) {
            var2.add((T) var0.opt(var1));
        }

        return var2;
    }
}
