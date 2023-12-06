package com.zhekasmirnov.innercore.utils;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.mozilla.javascript.MembersPatch;

public class ReflectionPatch {

	public static void init() {
		MembersPatch.addOverride("java.lang.Class.forName", "com.zhekasmirnov.innercore.utils.ReflectionPatch.forName");
	}

	public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
		if (name.startsWith("com.zhekasmirnov.horizon.launcher.ads")) {
			throw new ClassNotFoundException("Unauthorized");
		} else if (name.startsWith("zhekasmirnov.launcher.")) {
			name = "com.zhekasmirnov.innercore." + name.substring(22);
			Logger.debug("VERY_IMPORTANT", name);
		}
		return Class.forName(name, initialize, loader);
	}
}
