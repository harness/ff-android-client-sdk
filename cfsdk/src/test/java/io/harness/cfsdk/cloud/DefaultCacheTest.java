package io.harness.cfsdk.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static io.harness.cfsdk.cloud.cache.DefaultCache.METADATA_KEY_LAST_UPDATED;

import android.content.Context;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.harness.cfsdk.cloud.cache.DefaultCache;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public class DefaultCacheTest {


    @Test
    public void testCache() {
        DefaultCache cache = new DefaultCache();
        String env = "dummyenv";

        Evaluation eval1 = new Evaluation().flag("flag1");
        Evaluation eval2 = new Evaluation().flag("flag2");
        Evaluation eval3 = new Evaluation().flag("flag3");

        cache.saveEvaluation(env, "dummykey1", eval1);
        cache.saveEvaluation(env, "dummykey2", eval2);
        cache.saveEvaluation(env, "dummykey3", eval3);

        assertEquals(3, cache.getAllEvaluations(env).size());

        assertNotNull(cache.getEvaluation(env, "dummykey1"));
        assertNotNull(cache.getEvaluation(env, "dummykey2"));
        assertNotNull(cache.getEvaluation(env, "dummykey3"));

        assertEquals("flag1", cache.getEvaluation(env, "dummykey1").getFlag());
        assertEquals("flag2", cache.getEvaluation(env, "dummykey2").getFlag());
        assertEquals("flag3", cache.getEvaluation(env, "dummykey3").getFlag());

        List<Evaluation> newEvals = Arrays.asList(new Evaluation().flag("flag4"),
                new Evaluation().flag("flag5"),
                new Evaluation().flag("flag6"));

        cache.saveAllEvaluations(env, newEvals);

        assertEquals(6, cache.getAllEvaluations(env).size());

        cache.removeEvaluation(env, "dummykey1");

        assertEquals(5, cache.getAllEvaluations(env).size());

        cache.clear(env);

        assertEquals(0, cache.getAllEvaluations(env).size());

        assertNull(cache.getEvaluation(env, "dummykey1"));
        assertNull(cache.getEvaluation(env, "dummykey2"));
        assertNull(cache.getEvaluation(env, "dummykey3"));
        assertNull(cache.getEvaluation(env, "dummykey4"));
        assertNull(cache.getEvaluation(env, "dummykey5"));
        assertNull(cache.getEvaluation(env, "dummykey6"));
    }

}
