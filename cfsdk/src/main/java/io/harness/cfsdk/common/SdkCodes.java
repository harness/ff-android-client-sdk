package io.harness.cfsdk.common;

import static java.lang.String.valueOf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import io.harness.cfsdk.cloud.model.Target;

public class SdkCodes {

  private static final Logger log = LoggerFactory.getLogger(SdkCodes.class);

  private SdkCodes() {}

  public static void errorMissingSdkKey() {
    if (log.isErrorEnabled())
      log.error(sdkErrMsg(1002));
  }

  public static void infoPollStarted(int durationSec) {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(4000, valueOf(durationSec * 1000)));
  }

  public static void infoSdkInitOk() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(1000));
  }

  public static void infoSdkWaitingForInit() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(1003));
  }

  public static void infoSdkAuthOk(String sdkVersion) {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(2000, sdkVersion));
  }

  public static void infoPollingStopped() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(4001));
  }

  public static void infoStreamConnected() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(5000));
  }

  public static void infoStreamStopped() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(5004));
  }

  public static void infoStreamEventReceived(String eventJson) {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(5002, eventJson));
  }

  public static void infoMetricsThreadStarted(int intervalSec) {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(7000, valueOf(intervalSec * 1000)));
  }

  public static void infoMetricsThreadExited() {
    if (log.isInfoEnabled())
      log.info(sdkErrMsg(7001));
  }

  public static void warnAuthFailedSrvDefaults(String reason) {
    if (log.isWarnEnabled())
      log.warn(sdkErrMsg(2001, reason));
  }

  public static void warnAuthRetying(int attempt) {
    if (log.isWarnEnabled())
      log.warn(sdkErrMsg(2003, ", attempt " + attempt));
  }

  public static void warnStreamDisconnected(String reason) {
    if (log.isWarnEnabled())
      log.warn(sdkErrMsg(5001, reason));
  }

  public static void warnPostMetricsFailed(String reason) {
    if (log.isWarnEnabled())
      log.warn(sdkErrMsg(7002, reason));
  }

  public static void warnDefaultVariationServed(String identifier, String def, String reason) {
    if (log.isWarnEnabled()) {
      log.warn(sdkErrMsg(6001, String.format("identifier=%s, default=%s reason=%s", identifier, def, reason)));
    }
  }

  private static final Map<Integer, String> MAP = new HashMap<Integer, String>() {{
    put(1000, "The SDK has successfully initialized");
    put(1001, "The SDK has failed to initialize due to the following authentication error:");
    put(1002, "The SDK has failed to initialize due to a missing or empty API key");
    put(1003, "The SDK is waiting for initialization to complete");

    put(2000, "Authenticated ok, SDK version");
    put(2001, "Authentication failed with a non-recoverable error - defaults will be served");
    put(2003, "Retrying to authenticate");

    put(4000, "Polling started, intervalMs:");
    put(4001, "Polling stopped");

    put(5000, "SSE stream connected ok");
    put(5001, "SSE stream disconnected, reason:");
    put(5002, "SSE event received:");
    put(5003, "SSE retrying to connect in");
    put(5004, "SSE stopped");

    put(6000, "Evaluated variation successfully");
    put(6001, "Default variation was served");

    put(7000, "Metrics thread started, intervalMs:");
    put(7001, "Metrics thread exited");
    put(7002, "Posting metrics failed, reason:");
  }};

  private static String sdkErrMsg(int errorCode) {
    return sdkErrMsg(errorCode, null);
  }

  private static String sdkErrMsg(int errorCode, String appendText) {
    if (appendText == null) {
      appendText = "";
    }

    return String.format(
        "SDKCODE(%s:%s): %s %s",
        getErrClass(errorCode), errorCode, MAP.get(errorCode), appendText);
  }

  private static String getErrClass(int errorCode) {
    if (errorCode >= 1000 && errorCode <= 1999) return "init";
    else if (errorCode >= 2000 && errorCode <= 2999) return "auth";
    else if (errorCode >= 4000 && errorCode <= 4999) return "poll";
    else if (errorCode >= 5000 && errorCode <= 5999) return "stream";
    else if (errorCode >= 6000 && errorCode <= 6999) return "eval";
    else if (errorCode >= 7000 && errorCode <= 7999) return "metric";
    return "";
  }
}
