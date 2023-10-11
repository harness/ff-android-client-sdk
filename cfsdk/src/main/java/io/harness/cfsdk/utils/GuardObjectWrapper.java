package io.harness.cfsdk.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/* Can be used to ensure method cannot be called on uninitialized object.*/
public class GuardObjectWrapper {
    private static final Logger log = LoggerFactory.getLogger(GuardObjectWrapper.class);

    private Object object;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public Object get(){
        try {
            log.debug("Awaiting for lock release");
            countDownLatch.await(); // wait for 0
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return object;
    }

    public void set(Object newObject){

        // this will release other thread when 0
        log.debug("Releasing lock {}", newObject.getClass().getSimpleName());
        this.object = newObject;
        countDownLatch.countDown();
    }
}
