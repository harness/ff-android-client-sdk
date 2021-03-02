package io.harness.cfsdk.cloud.polling;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ShortTermPolling implements EvaluationPolling{
    private static final int MINIMUM_POLLING_INTERVAL = 10_000;
    private final long pollingInterval;
    private Timer timer;

    public ShortTermPolling(int time, TimeUnit unit) {
        this.pollingInterval = Math.max(unit.toMillis(time), MINIMUM_POLLING_INTERVAL);
    }

    @Override
    public synchronized void start(Runnable runnable) {
        if (timer != null) {
            System.out.println("timer - stopping before start");
            timer.cancel();
            timer.purge();
        }
        System.out.println("timer - scheduling new one");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, pollingInterval, pollingInterval);
    }

    @Override
    public synchronized void stop() {
        if (timer != null) {
            System.out.println("timer - stopping on exit");
            timer.cancel();
            timer.purge();
        }
        System.out.println("timer - stopping, new created");
        timer = new Timer();
    }
}
