package org.mozilla.javascript;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class MembersPatch {
    private static HashMap<String, String> overrides = new HashMap<>();

    public static void addOverride(String from, String to) {
        Logger.debug("MEMBERS_PATCH", "Adding override " + from + " -> " + to);
        overrides.put(from, to);
    }

    static String getOverride(String from) {
        String result = overrides.get(from);
        if (result != null) {
            return result;
        } else {
            return from;
        }
    }

    static Method override(Class<?> cl, Method method) {
        String path = cl.getName() + "." + method.getName();

        if (overrides.containsKey(path)) {
            String overridenPath = overrides.get(path);
            int ind = overridenPath.lastIndexOf(".");
            try {
                Class<?> overridenClass = Class.forName(overridenPath.substring(0, ind));
                Method overridenMethod = overridenClass.getMethod(overridenPath.substring(ind + 1),
                        method.getParameterTypes());
                Logger.debug("MEMBERS_PATCH", "Successfully overrided method " + path);
                return overridenMethod;
            } catch (ClassNotFoundException | SecurityException e) {
                throw new RuntimeException("Exception occured while trying to override existing method. ", e);
            } catch (NoSuchMethodException ignored) {
                Logger.debug("MEMBERS_PATCH", "Could't override method " + path + " with arguments types "
                        + Arrays.toString(method.getParameterTypes()));
            }
        }

        return method;
    }
}
