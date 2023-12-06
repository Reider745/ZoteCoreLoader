package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

/**
 * Created by zheka on 20.09.2017.
 */

public class PrintStacking {
	public static void prepare() {
	}

	public static void print(String message) {
		Logger.debug("PrintStacking/print", message);
	}
}
