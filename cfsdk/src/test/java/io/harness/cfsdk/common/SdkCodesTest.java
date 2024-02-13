package io.harness.cfsdk.common;


import static org.junit.Assert.fail;
import static io.harness.cfsdk.common.SdkCodes.*;

import org.junit.Test;

import io.harness.cfsdk.cloud.model.Target;

public class SdkCodesTest {

    @Test
    public void testAllLogs() {
        try {
            errorMissingSdkKey();
            infoPollStarted(123);
            infoSdkInitOk();
            infoSdkWaitingForInit();
            infoSdkAuthOk();
            infoPollingStopped();
            infoStreamConnected();
            infoStreamEventReceived(null);
            infoStreamEventReceived("dummy data");
            infoMetricsThreadStarted(321);
            infoMetricsThreadExited();
            warnAuthFailedSrvDefaults(null);
            warnAuthFailedSrvDefaults("error 1");
            warnAuthRetying(1);
            warnAuthRetying(-1);
            warnStreamDisconnected("error 2");
            warnStreamDisconnected(null);
            warnPostMetricsFailed(null);
            warnPostMetricsFailed("error 3");
            warnDefaultVariationServed("id1", null, null);
            warnDefaultVariationServed("id1", null, "defaultVal");

            Target target = new Target();
            target.identifier("test");


            //.builder().identifier("test").isPrivate(false).build();
            warnDefaultVariationServed("id2", "defaultVal2", "reason");
        } catch (Exception e) {
             fail("should not throw exception" + e);
        }
    }
}
