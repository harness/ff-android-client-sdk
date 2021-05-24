package io.harness.cfsdk.utils;

/**
 * Various utils for the SDK.
 */
public class CfUtils {

    private CfUtils() {
    }

    /**
     * Text utils.
     */
    public static class Text {

        private Text() {
        }

        /**
         * Check if the text is empty.
         *
         * @param text Text to check.
         * @return True == Text is empty.
         */
        public static boolean isEmpty(String text) {

            return text == null || text.isEmpty();
        }
    }
}
