package io.harness.cfsdk.logging;

public class CfLogStrategyTest implements CfLogging {

    @Override
    public void v(String tag, String message) {

        sout(tag, message);
    }

    @Override
    public void d(String tag, String message) {

        sout(tag, message);
    }

    @Override
    public void i(String tag, String message) {

        sout(tag, message);
    }

    @Override
    public void w(String tag, String message) {

        serr(tag, message);
    }

    @Override
    public void e(String tag, String message) {

        serr(tag, message);
    }

    @Override
    public void v(String tag, String message, Throwable throwable) {

        sout(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void d(String tag, String message, Throwable throwable) {

        sout(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void i(String tag, String message, Throwable throwable) {

        sout(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void w(String tag, String message, Throwable throwable) {

        serr(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void e(String tag, String message, Throwable throwable) {

        serr(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void w(String tag, Throwable throwable) {

        throwable.printStackTrace();
    }

    @Override
    public void wtf(String tag, String message) {

        serr(tag, message);
    }

    @Override
    public void wtf(String tag, String message, Throwable throwable) {

        serr(tag, message);
        throwable.printStackTrace();
    }

    @Override
    public void wtf(String tag, Throwable throwable) {

        throwable.printStackTrace();
    }

    private void sout(String tag, String message) {

        final String output = getOutput(tag, message);
        System.out.println(output);
    }

    private void serr(String tag, String message) {

        final String output = getOutput(tag, message);
        System.err.println(output);
    }

    private String getOutput(String tag, String message) {

        return String.format("%s :: %s", tag, message);
    }
}
