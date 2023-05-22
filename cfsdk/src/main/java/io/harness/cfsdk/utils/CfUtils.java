package io.harness.cfsdk.utils;

import static io.harness.cfsdk.utils.CfUtils.Text.isNotEmpty;

import io.harness.cfsdk.cloud.core.model.Evaluation;

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

        /**
         * Check if the text is not empty.
         *
         * @param text Text to check.
         * @return True == Text is not empty.
         */
        public static boolean isNotEmpty(String text) {

            return !isEmpty(text);
        }
    }

    public static class EvaluationUtil {
        private EvaluationUtil() {
        }

        /**
         * Check if the Evaluation is valid.
         *
         * @param evaluation Evaluation to check.
         * @return True == Evaluation has all fields populated.
         */
        public static boolean isEvaluationValid(Evaluation evaluation) {
            return isNotEmpty(evaluation.getFlag()) && isNotEmpty(evaluation.getKind()) && isNotEmpty(evaluation.getIdentifier()) && isNotEmpty(evaluation.getValue());
        }
    }
}
