package org.mineprogramming.horizon.innercore.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class JSONUtils {

    public static Iterator<JSONObject> getJsonIterator(final JSONArray array){
        return new Iterator<JSONObject>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length();
            }
    
            @Override
            public JSONObject next() {
                return array.optJSONObject(i++);
            }
        };
    } 

    

    public static Iterator<JSONObject> getJsonIterator(final JSONObject obejct){
        final JSONArray names = obejct.names();

        return new Iterator<JSONObject>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < names.length();
            }
    
            @Override
            public JSONObject next() {
                String name = names.optString(i++);
                return obejct.optJSONObject(name);
            }
        };
    }
}