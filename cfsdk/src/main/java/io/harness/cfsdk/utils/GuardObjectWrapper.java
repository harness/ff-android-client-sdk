package io.harness.cfsdk.utils;

import java.util.concurrent.CountDownLatch;

import io.harness.cfsdk.CfClient;
import io.harness.cfsdk.logging.CfLog;

/* Can be used to ensure method cannot be called on uninitialized object.*/
public class GuardObjectWrapper {

    private Object object;
    private String tag = GuardObjectWrapper.class.getSimpleName();

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public Object get(){
        try {
            CfLog.OUT.v(tag, "Awaiting for lock release");
            countDownLatch.await(); // wait for 0
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    public void set(Object newObject){

        // this will release other thread when 0
        CfLog.OUT.v(tag, "Releasing lock "+ newObject.getClass().getSimpleName());
        this.object = newObject;
        countDownLatch.countDown();
    }
}
