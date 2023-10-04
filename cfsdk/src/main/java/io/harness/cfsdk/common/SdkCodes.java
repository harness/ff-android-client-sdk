package io.harness.cfsdk.common;

import static java.lang.String.valueOf;
import java.util.HashMap;
import java.util.Map;


import io.harness.cfsdk.cloud.model.Target;


public class SdkCodes {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SdkCodes.class);

  public static void errorMissingSdkKey() {
    log.error(sdkErrMsg(1002));
  }

  public static void infoPollStarted(int durationSec) {
    log.info(sdkErrMsg(4000, valueOf(durationSec * 1000)));
  }

  public static void infoSdkInitOk() {
    log.info(sdkErrMsg(1000));
  }

  public static void infoSdkAuthOk() {
    log.info(sdkErrMsg(2000));
  }

  public static void infoPollingStopped() {
    log.info(sdkErrMsg(4001));
  }

  public static void infoStreamConnected() {
    log.info(sdkErrMsg(5000));
  }

  public static void infoStreamEventReceived(String eventJson) {
    log.info(sdkErrMsg(5002, eventJson));
  }

  public static void infoMetricsThreadStarted(int intervalSec) {
    log.info(sdkErrMsg(7000, valueOf(intervalSec * 1000)));
  }

  public static void infoMetricsThreadExited() {
    log.info(sdkErrMsg(7001));
  }

  public static void warnAuthFailedSrvDefaults(String reason) {
    log.warn(sdkErrMsg(2001, reason));
  }

  public static void warnAuthRetying(int attempt) {
    log.warn(sdkErrMsg(2003, ", attempt " + attempt));
  }

  public static void warnStreamDisconnected(String reason) {
    log.warn(sdkErrMsg(5001, null));
  }

  public static void warnPostMetricsFailed(String reason) {
    log.warn(sdkErrMsg(7002, null));
  }

  public static void warnDefaultVariationServed(String identifier, Target target, String def) {
    String targetId = (target == null) ? "null" : target.getIdentifier();
    String msg = String.format("identifier=%s, target=%s, default=%s", identifier, targetId, def);
    log.warn(sdkErrMsg(6001, null));
  }

  private static final Map<Integer, String> MAP = new HashMap<Integer, String>() {{
    put(1000, "The SDK has successfully initialized");
    put(1001, "The SDK has failed to initialize due to the following authentication error:");
    put(1002, "The SDK has failed to initialize due to a missing or empty API key");

    put(2000, "Authenticated ok");
    put(2001, "Authentication failed with a non-recoverable error - defaults will be served");
    put(2003, "Retrying to authenticate");

    put(4000, "Polling started, intervalMs:");
    put(4001, "Polling stopped");

    put(5000, "SSE stream connected ok");
    put(5001, "SSE stream disconnected, reason:");
    put(5002, "SSE event received:");
    put(5003, "SSE retrying to connect in");

    put(6000, "Evaluated variation successfully");
    put(6001, "Default variation was served");

    put(7000, "Metrics thread started, intervalMs:");
    put(7001, "Metrics thread exited");
    put(7002, "Posting metrics failed, reason:");
  }};



  private static String sdkErrMsg(int error_code) {
    return sdkErrMsg(error_code, null);
  }

  private static String sdkErrMsg(int error_code, String appendText) {
    if (appendText == null) {
      appendText = "";
    }

    return String.format(
        "SDKCODE(%s:%s): %s %s",
        getErrClass(error_code), error_code, MAP.get(error_code), appendText);
  }

  private static String getErrClass(int error_code) {
    if (error_code >= 1000 && error_code <= 1999) return "init";
    else if (error_code >= 2000 && error_code <= 2999) return "auth";
    else if (error_code >= 4000 && error_code <= 4999) return "poll";
    else if (error_code >= 5000 && error_code <= 5999) return "stream";
    else if (error_code >= 6000 && error_code <= 6999) return "eval";
    else if (error_code >= 7000 && error_code <= 7999) return "metric";
    return "";
  }
}
