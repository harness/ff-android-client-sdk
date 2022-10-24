package io.harness.cfsdk.utils;

import java.util.concurrent.CountDownLatch;

/* Can be used to ensure method cannot be called on uninitialized object.*/
public class GuardObjectWrapper {

    private Object object;


    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    public Object get(){
        try {
            countDownLatch.await(); // wait for 0
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    public void set(Object newObject){
        // this will release other thread when 0
        this.object = newObject;
        countDownLatch.countDown();
    }
}
