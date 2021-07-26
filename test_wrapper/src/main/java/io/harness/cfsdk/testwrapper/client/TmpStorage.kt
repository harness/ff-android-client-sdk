package io.harness.cfsdk.testwrapper.client

import io.harness.cfsdk.cloud.cache.CloudCache
import io.harness.cfsdk.cloud.core.model.Evaluation
import java.util.concurrent.ConcurrentHashMap

class TmpStorage : CloudCache {

    private val map = ConcurrentHashMap<String, Evaluation>()

    override fun getEvaluation(key: String?): Evaluation? {

        return map[key]
    }

    override fun saveEvaluation(key: String, evaluation: Evaluation) {

        map[key] = evaluation
    }

    override fun getAllEvaluations(key: String?): List<Evaluation> {

        val list = mutableListOf<Evaluation>()
        list.addAll(map.values)
        return list
    }

    override fun removeEvaluation(key: String) {

        map.remove(key)
    }

    override fun clear() {

        map.clear()
    }
}