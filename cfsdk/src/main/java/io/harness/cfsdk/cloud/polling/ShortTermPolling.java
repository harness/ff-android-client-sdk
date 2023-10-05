package io.harness.cfsdk.cloud.polling;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.common.SdkCodes;

public class ShortTermPolling implements EvaluationPolling{
    private static final int MINIMUM_POLLING_INTERVAL_MS = 60_000;
    private final long pollingIntervalMs;
    private Timer timer;

    public ShortTermPolling(int time, TimeUnit unit) {
        this.pollingIntervalMs = Math.max(unit.toMillis(time), MINIMUM_POLLING_INTERVAL_MS);
    }

    @Override
    public synchronized void start(Runnable runnable) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        SdkCodes.infoPollStarted((int)pollingIntervalMs/1000);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, pollingIntervalMs, pollingIntervalMs);
    }

    @Override
    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        SdkCodes.infoPollingStopped();
    }
}
