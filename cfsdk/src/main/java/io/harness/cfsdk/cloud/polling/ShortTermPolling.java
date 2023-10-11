package io.harness.cfsdk.cloud.polling;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.common.SdkCodes;

public class ShortTermPolling implements EvaluationPolling{
    private static final int MINIMUM_POLLING_INTERVAL_MS = 60_000;
    private final long pollingIntervalMs;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> runningTask = null;

    public ShortTermPolling(int time, TimeUnit unit) {
        this.pollingIntervalMs = Math.max(unit.toMillis(time), MINIMUM_POLLING_INTERVAL_MS);
    }

    @Override
    public synchronized void start(Runnable runnable) {
        if (isRunning()) {
            return;
        }

        runningTask = scheduler.scheduleAtFixedRate(runnable, 0, pollingIntervalMs, MILLISECONDS);

        SdkCodes.infoPollStarted((int)pollingIntervalMs/1000);
    }

    @Override
    public synchronized void stop() {
        if (scheduler.isShutdown()) {
            return;
        }

        if (runningTask == null) {
            return;
        }

        runningTask.cancel(false);
        runningTask = null;

        SdkCodes.infoPollingStopped();
    }

    public boolean isRunning() {
        return runningTask != null && !runningTask.isCancelled();
    }
}
