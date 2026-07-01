package org.innercore.icstd.debug;

import com.zhekasmirnov.innercore.api.gen.SamplingProfilerBindGenBase;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;
import org.innercore.icstd.annotations.ApiBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ApiBinding
public class SamplingProfiler extends SamplingProfilerBindGenBase {
    public static class Sample {
        StackTraceElement[] trace;
        long time;

        public Sample(StackTraceElement[] trace, long time) {
            this.trace = trace;
            this.time = time;
        }
    }

    private static class RecordedThread {
        Thread thread;
        String baseFrameName;
        private List<Sample> samples = new ArrayList<>();
    }
    private final ArrayList<RecordedThread> recordedThreads = new ArrayList<>();

    private Thread samplingThread = null;
    private long samplingDelayNs = 1_000_000;

    @ApiBinding
    public SamplingProfiler() {
    }

    private volatile boolean active = false;
    private volatile boolean recording = false;
    private final Object startRecEvent = new Object();

    @ApiBinding
    public void addCurrentThread(String name) {
        Thread thread = Thread.currentThread();
        RecordedThread rec = null;
        for (RecordedThread recordedThread : recordedThreads) {
            if (recordedThread.thread == thread) {
                rec = recordedThread;
                break;
            }
        }
        if (rec == null) {
            rec = new RecordedThread();
            rec.thread = thread;
            recordedThreads.add(rec);
        }
        rec.baseFrameName = name;
    }

    @ApiBinding
    public void setSamplingDelayNs(long samplingDelayNs) {
        this.samplingDelayNs = samplingDelayNs;
    }

    @ApiBinding
    public void setSamplingRate(int samplesPerSec) {
        setSamplingDelayNs((long) 1_000_000_000 / (long) samplesPerSec);
    }

    @ApiBinding
    public void setActive(boolean newActive) {
        if (recordedThreads.isEmpty() && newActive) {
            throw new IllegalStateException("no recorded threads");
        }
        if (this.active == newActive) {
            return;
        }
        this.active = newActive;
        if (active) {
            for (RecordedThread rt : recordedThreads) {
                rt.samples.clear();
            }
            samplingThread = new Thread(() -> {
                while (active) {
                    while (!recording) {
                        try {
                            synchronized (startRecEvent) {
                                startRecEvent.wait();
                            }
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    long nextSampleTime = System.nanoTime();
                    while (recording) {
                        while (System.nanoTime() < nextSampleTime) {
                            Thread.yield();
                        }
                        nextSampleTime += samplingDelayNs;
                        for (RecordedThread rt : recordedThreads) {
                            StackTraceElement[] trace = rt.thread.getStackTrace();
                            long sampleTime = System.nanoTime();
                            rt.samples.add(new Sample(trace, sampleTime));
                        }
                    }
                }
            });
            samplingThread.start();
        } else {
            try {
                this.recording = false;
                samplingThread.join();
                samplingThread = null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @ApiBinding
    public void setRecording(boolean recording) {
        if (this.recording == recording) {
            return;
        }
        this.recording = recording;
        if (recording) {
            synchronized (startRecEvent) {
                startRecEvent.notify();
            }
        }
    }

    @ApiBinding
    public void activateAndRecord() {
        setActive(true);
        setRecording(true);
    }

    @ApiBinding
    public void stopAndDeactivate() {
        setActive(false);
    }

    private boolean cfgSkipEmptySamples = true;
    private boolean cfgFilterFrames = true;
    private boolean cfgGroupFunctions = true;
    private boolean cfgShortenPackageNames = true;
    private boolean cfgAnnotateRootTime = true;

    @ApiBinding
    public void setCfgSkipEmptySamples(boolean cfgSkipEmptySamples) {
        this.cfgSkipEmptySamples = cfgSkipEmptySamples;
    }

    @ApiBinding
    public void setCfgFilterFrames(boolean cfgFilterFrames) {
        this.cfgFilterFrames = cfgFilterFrames;
    }

    @ApiBinding
    public void setCfgGroupFunctions(boolean cfgGroupFunctions) {
        this.cfgGroupFunctions = cfgGroupFunctions;
    }

    @ApiBinding
    public void setCfgShortenPackageNames(boolean cfgShortenPackageNames) {
        this.cfgShortenPackageNames = cfgShortenPackageNames;
    }

    @ApiBinding
    public void setCfgAnnotateRootTime(boolean cfgAnnotateRootTime) {
        this.cfgAnnotateRootTime = cfgAnnotateRootTime;
    }

    private String calcAndFormatThreadStats(List<Sample> samples) {
        long last = !samples.isEmpty() ? samples.get(0).time - samplingDelayNs : 0;
        long activeTime = 0;
        long lastActiveTime = 0;
        long tickCount = 0;
        for (Sample sample : samples) {
            if (sample.time < last) {
                continue; // skip broken samples
            }
            StackTraceElement[] trace = sample.trace;
            if (trace.length == 0 && cfgSkipEmptySamples) {
                last = sample.time;
                continue;
            }
            if (sample.time - lastActiveTime > samplingDelayNs * 5) {
                tickCount++;
            }
            activeTime += sample.time - last;
            last = lastActiveTime = sample.time;
        }
        long totalTime = !samples.isEmpty() ? samples.get(samples.size() - 1).time - samples.get(0).time : 0;
        return String.format("total:%dms,tick:%.3fms(%d),active:%.1f%%",
                totalTime / 1_000_000,
                ((activeTime) / tickCount / 1000) * 0.001,
                tickCount,
                ((activeTime * 1000) / totalTime) * 0.1
        );
    }

    @ApiBinding
    public String toStatInfoStr() {
        StringBuilder sb = new StringBuilder();
        for (RecordedThread rt  : recordedThreads) {
            sb.append(rt.baseFrameName).append(": ").append(calcAndFormatThreadStats(rt.samples)).append("\n");
        }
        return sb.toString();
    }

    @ApiBinding
    public String toFoldedStacksStr() {
        if (active) {
            throw new IllegalStateException("setActive(false) must be called before export");
        }
        HashMap<String, Long> map = new HashMap<>();

        for (RecordedThread rt : recordedThreads) {
            String baseFrame = "root;" + rt.baseFrameName;
            List<Sample> samples = rt.samples;

            if (cfgAnnotateRootTime) {
                baseFrame += "(" + calcAndFormatThreadStats(samples) + ")";
            }

            final String SHORTEN_PACKAGE_SKIP_1 = "com.zhekasmirnov.innercore.api.";
            final String SHORTEN_PACKAGE_SKIP_2 = "org.innercore.icstd.";

            long lastSampleTime = !samples.isEmpty() ? samples.get(0).time - samplingDelayNs : 0;
            for (Sample sample : samples) {
                if (sample.time < lastSampleTime) {
                    continue; // skip broken samples
                }
                StackTraceElement[] trace = sample.trace;
                if (trace.length == 0 && cfgSkipEmptySamples) {
                    lastSampleTime = sample.time;
                    continue;
                }
                StringBuilder sb = new StringBuilder(baseFrame);
                for (int i = trace.length - 1; i >= 0; i--) {
                    StackTraceElement traceElement = trace[i];
                    String clsName = traceElement.getClassName();
                    boolean isJsGenFrame = clsName.startsWith("org.mozilla.javascript.gen") || clsName.startsWith("js.opt.");
                    boolean isInternalJsFrame = !isJsGenFrame && clsName.startsWith("org.mozilla.javascript");
                    boolean isInternalApiFrame = clsName.contains("BindGenBase");
                    boolean isScriptFrame = isJsGenFrame && traceElement.getLineNumber() > 0 && traceElement.getFileName() != null;
                    if (cfgFilterFrames) {
                        if (isJsGenFrame && !isScriptFrame || isInternalApiFrame || isInternalJsFrame)
                            continue;
                    }
                    if (isScriptFrame) {
                        String methodName = traceElement.getMethodName();
                        if (cfgShortenPackageNames && methodName.startsWith("_c_"))
                            methodName = methodName.substring(3);
                        if (cfgGroupFunctions)
                            sb.append(';').append(traceElement.getFileName()).append('.').append(methodName);
                        sb.append(';').append(traceElement.getFileName()).append(":").append(traceElement.getLineNumber());
                        if (!cfgGroupFunctions)
                            sb.append(" (").append(methodName).append(")");
                    } else {
                        String frameStr = traceElement.toString();
                        if (cfgShortenPackageNames) {
                            if (frameStr.startsWith(SHORTEN_PACKAGE_SKIP_1))
                                frameStr = frameStr.substring(SHORTEN_PACKAGE_SKIP_1.length());
                            else if (frameStr.startsWith(SHORTEN_PACKAGE_SKIP_2))
                                frameStr = frameStr.substring(SHORTEN_PACKAGE_SKIP_2.length());
                        }
                        int openingBracketIdx = frameStr.indexOf('(');
                        if (cfgGroupFunctions && openingBracketIdx != -1) {
                            sb.append(';').append(frameStr, 0, openingBracketIdx);
                        } else {
                            sb.append(';').append(frameStr);
                        }
                    }
                }
                map.put(sb.toString(), map.getOrDefault(sb.toString(), 0L) + (sample.time - lastSampleTime));
                lastSampleTime = sample.time;
            }
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(k).append(' ').append((v + samplingDelayNs / 2) / samplingDelayNs).append('\n');
        });
        return sb.toString();
    }

    @ApiBinding
    public static void writeStrDataToDefaultPerfFile(String data, String name) {
        try {
            FileTools.writeFileText(FileTools.DIR_PACK + "perf.txt", data);
        } catch (IOException e) {
            ICLog.e("SamplingProfiler", "Error writing perf file: " + e.getMessage());
        }
    }

    @ApiBinding
    public static void writeStrDataToDefaultPerfFile(String data) {
        writeStrDataToDefaultPerfFile(data, "perf.txt");
    }
}
