package io.harness.cfsdk.cloud.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.logging.CfLog;

public class RepositoryTest {

    @Mock
    FeatureService mockService;
    @Mock
    CloudCache mockCache;
    @Mock
    ApiResponse demoFlagResponse;
    @Mock
    ApiResponse demoListResponse;

    Evaluation demoFlagEvaluation;
    Evaluation secondFlagEvaluation;
    private FeatureRepository repository;


    @Before
    public void setUp() {

        CfLog.testModeOn();

        MockitoAnnotations.initMocks(this);
        mockService = Mockito.mock(FeatureService.class);

        demoFlagEvaluation = new Evaluation();
        demoFlagEvaluation.setFlag("demo_flag");
        demoFlagEvaluation.value("false");

        secondFlagEvaluation = new Evaluation();
        secondFlagEvaluation.setFlag("second_flag");
        secondFlagEvaluation.value("false");

        List<Evaluation> demoEvaluationList = new LinkedList<>();
        Collections.addAll(demoEvaluationList, demoFlagEvaluation, secondFlagEvaluation);

        demoFlagResponse = Mockito.mock(ApiResponse.class);
        Mockito.when(demoFlagResponse.getCode()).thenReturn(200);
        Mockito.when(demoFlagResponse.isSuccess()).thenReturn(true);
        Mockito.when(demoFlagResponse.getRawResponse()).thenReturn("");
        Mockito.when(demoFlagResponse.body()).thenReturn(demoFlagEvaluation);

        demoListResponse = Mockito.mock(ApiResponse.class);
        Mockito.when(demoListResponse.body()).thenReturn(demoEvaluationList);
        Mockito.when(demoListResponse.getCode()).thenReturn(200);
        Mockito.when(demoListResponse.isSuccess()).thenReturn(true);

        mockCache = Mockito.mock(CloudCache.class);
        Mockito.when(mockCache.getEvaluation(Mockito.anyString())).thenReturn(demoFlagEvaluation);
        Mockito.doNothing().when(mockCache).saveEvaluation(Mockito.anyString(), Mockito.any());
        Mockito.when(mockCache.getAllEvaluations(Mockito.anyString())).thenReturn(demoEvaluationList);

        Mockito.when(mockService.getEvaluationForId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(demoFlagResponse);
        Mockito.when(mockService.getEvaluations(Mockito.anyString(), Mockito.anyString())).thenReturn(demoListResponse);
        repository = new FeatureRepositoryImpl(mockService, mockCache);
    }

    @Test
    public void repositoryTest() {

        Evaluation evaluation =
                repository.getEvaluation("demo", "target", "demo_flag", "", true);

        Assert.assertEquals(evaluation.getFlag(), demoFlagEvaluation.getFlag());
        Assert.assertEquals((String) evaluation.getValue(), demoFlagEvaluation.getValue());

        Mockito.verify(mockCache, Mockito.times(1)).getEvaluation(
                Mockito.anyString()
        );


        Evaluation cloudEvaluation =
                repository.getEvaluation("demo", "target", "demo_flag", "", false);

        Assert.assertEquals(cloudEvaluation.getFlag(), demoFlagEvaluation.getFlag());
        Assert.assertEquals((String) cloudEvaluation.getValue(), demoFlagEvaluation.getValue());


        Mockito.verify(mockService, Mockito.times(1)).getEvaluationForId(
                Mockito.eq("demo_flag"), Mockito.anyString(), Mockito.anyString()
        );
        Mockito.verify(mockCache, Mockito.times(1)).getEvaluation(
                Mockito.anyString()
        );
        Mockito.verify(mockCache, Mockito.times(1)).saveEvaluation(
                Mockito.anyString(), Mockito.eq(demoFlagEvaluation)
        );


        repository.getAllEvaluations("demo", "target", "", true);
        Mockito.verify(mockCache, Mockito.atMostOnce()).getAllEvaluations(Mockito.anyString());

        repository.getAllEvaluations("demo", "target", "", false);
        Mockito.verify(mockService, Mockito.atMostOnce()).getEvaluations(Mockito.anyString(), Mockito.anyString());

        Mockito.verify(mockCache, Mockito.times(2)).saveEvaluation(
                Mockito.anyString(), Mockito.eq(demoFlagEvaluation)
        );
        Mockito.verify(mockCache, Mockito.times(1)).saveEvaluation(
                Mockito.anyString(), Mockito.eq(secondFlagEvaluation)
        );
    }


    @Test
    public void removeTest() {

        Mockito.doNothing().when(mockCache).removeEvaluation(Mockito.anyString());
        Mockito.doNothing().when(mockCache).clear();

        repository.remove("demo", "target", "demo_flag");

        Mockito.verify(mockCache, Mockito.atMostOnce()).removeEvaluation(Mockito.eq("demo_flag"));

        repository.clear();
        Mockito.verify(mockCache, Mockito.atMostOnce()).clear();
    }
}
