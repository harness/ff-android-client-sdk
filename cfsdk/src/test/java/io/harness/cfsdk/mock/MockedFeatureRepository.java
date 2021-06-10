package io.harness.cfsdk.mock;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.repository.FeatureRepository;

public class MockedFeatureRepository implements FeatureRepository {

    public static final String MOCK_BOOL;
    public static final String MOCK_NUMBER;
    public static final String MOCK_STRING;

    static {

        MOCK_BOOL = "MOCK_BOOL";
        MOCK_STRING = "MOCK_STRING";
        MOCK_NUMBER = "MOCK_NUMBER";
    }

    private final HashMap<String, Evaluation> mocks;

    {

        final Evaluation boolEval = new Evaluation();
        boolEval.setIdentifier(MOCK_BOOL);
        boolEval.setFlag(MOCK_BOOL);
        boolEval.setValue(true);

        final Evaluation stringEval = new Evaluation();
        stringEval.setIdentifier(MOCK_STRING);
        stringEval.setFlag(MOCK_STRING);
        stringEval.setValue(MOCK_STRING);

        final Evaluation numberEval = new Evaluation();
        numberEval.setIdentifier(MOCK_NUMBER);
        numberEval.setFlag(MOCK_NUMBER);
        numberEval.setValue(MOCK_NUMBER.length());

        mocks = new HashMap<>();
        mocks.put(MOCK_BOOL, boolEval);
        mocks.put(MOCK_STRING, stringEval);
        mocks.put(MOCK_NUMBER, numberEval);
    }

    @Override
    public Evaluation getEvaluation(

            String environment,
            String target,
            String evaluationId,
            String clusterIdentifier,
            boolean useCache
    ) {

        return mocks.get(evaluationId);
    }

    @Override
    public List<Evaluation> getAllEvaluations(

            String environment,
            String target,
            String clusterIdentifier,
            boolean useCache
    ) {

        return new LinkedList<>(mocks.values());
    }

    @Override
    public void remove(

            String environment,
            String target,
            String evaluationId) {

        mocks.remove(evaluationId);
    }

    @Override
    public void clear() {

        mocks.clear();
    }
}
