package com.zhekasmirnov.innercore.api.runtime;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;

import java.util.HashMap;

/**
 * Created by zheka on 24.08.2017.
 */

public class LoadingStage {
    public static final int STAGE_COUNT = 8;

    public static final int STAGE_IDLE = 0;
    public static final int STAGE_START = 1;
    public static final int STAGE_RESOURCES_LOADING = 2;
    public static final int STAGE_MODS_PRELOAD = 3;
    public static final int STAGE_MCPE_STARTING = 4;
    public static final int STAGE_MCPE_INITIALIZING = 5;
    public static final int STAGE_FINAL_LOADING = 6;
    public static final int STAGE_COMPLETE = 7;

    private static int stage = STAGE_IDLE;

    private static HashMap<Integer, Long> timeMap = new HashMap<>();
    private static final long startupTime = System.currentTimeMillis();

    public static int getStage() {
        return stage;
    }

    public static void setStage(int stage) {
        LoadingStage.stage = stage;
        timeMap.put(stage, System.currentTimeMillis());

        ICLog.i("PROFILING", "switched into new loading stage: stage=" + stageToString(stage));
    }

    public static boolean isPassed(int stage) {
        return LoadingStage.stage > stage;
    }

    public static boolean isInOrPassed(int stage) {
        return LoadingStage.stage >= stage;
    }

    public static String stageToString(int stage) {
        switch (stage) {
            case STAGE_IDLE:
                return "STAGE_IDLE";
            case STAGE_START:
                return "STAGE_START";
            case STAGE_RESOURCES_LOADING:
                return "STAGE_RESOURCES_LOADING";
            case STAGE_MODS_PRELOAD:
                return "STAGE_MODS_PRELOAD";
            case STAGE_MCPE_STARTING:
                return "STAGE_MCPE_STARTING";
            case STAGE_MCPE_INITIALIZING:
                return "STAGE_MCPE_INITIALIZING";
            case STAGE_FINAL_LOADING:
                return "STAGE_FINAL_LOADING";
            case STAGE_COMPLETE:
                return "STAGE_COMPLETE";
            default:
                return "STAGE_UNKNOWN";
        }
    }

    public static long getStageBeginTime(int stage) {
        return timeMap.containsKey(stage) ? timeMap.get(stage) : -1;
    }

    public static void outputTimeMap() {
        ICLog.d("PROFILING", "showing startup time map");
        for (int stage = 0; stage < STAGE_COUNT; stage++) {
            long time = getStageBeginTime(stage);
            if (time != -1) {
               Logger.debug("PROFILING", "stage " + stageToString(stage) + " started at " + (time - startupTime) * .001 + "s");
            }
            else {
               Logger.debug("PROFILING", "stage " + stageToString(stage) + " was ignored");
            }
        }
    }
}
