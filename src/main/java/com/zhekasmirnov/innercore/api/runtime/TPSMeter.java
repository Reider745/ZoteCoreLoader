package com.zhekasmirnov.innercore.api.runtime;

import java.util.HashMap;

/**
 * Created by zheka on 20.12.2017.
 */

public class TPSMeter {
	private long lastMeasuredTime = -1;
	private long lastMeasuredFrame = 0;
	private int frame = 0;
	private float tps = 0;

	private final int maxFramesPerMeasure;
	private final int maxTimePerMeasure;

	public TPSMeter(int maxFramesPerMeasure, int maxTimePerMeasure) {
		this.maxFramesPerMeasure = maxFramesPerMeasure;
		this.maxTimePerMeasure = maxTimePerMeasure;
	}

	private static HashMap<String, TPSMeter> tpsMeterByName = new HashMap<>();

	public TPSMeter(String name, int maxFramesPerMeasure, int maxTimePerMeasure) {
		this(maxFramesPerMeasure, maxTimePerMeasure);
		tpsMeterByName.put(name, this);
	}

	public static TPSMeter getByName(String name) {
		return tpsMeterByName.get(name);
	}

	public void onTick() {
		long time = System.currentTimeMillis();

		if (frame++ % maxFramesPerMeasure == 0 || time - lastMeasuredTime > maxTimePerMeasure) {
			tps = 1000 * (frame - lastMeasuredFrame) / (float) (time - lastMeasuredTime);
			lastMeasuredFrame = frame;
			lastMeasuredTime = time;
		}
	}

	public float getTps() {
		return Math.round(tps * 10) / 10.0f;
	}
}
