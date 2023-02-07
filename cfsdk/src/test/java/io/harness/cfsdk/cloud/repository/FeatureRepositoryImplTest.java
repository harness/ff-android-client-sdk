package io.harness.cfsdk.cloud.repository;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.mock.MockedNetworkInfoProvider;

public class FeatureRepositoryImplTest {

    @Test
    public void getEvaluationShouldReturnNullWhenEvalNotInCacheNorNetwork() {

        final CloudCache mockCache = mock(CloudCache.class);
        final FeatureRepositoryImpl featureRepo = new FeatureRepositoryImpl(null, mockCache, MockedNetworkInfoProvider.createWithNetworkOff());

        Evaluation eval = featureRepo.getEvaluation("dummyenv", "dummytarget", "featureid", "1");

        assertNull(eval);

    }
}
