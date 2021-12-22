package io.harness.cfsdk.testwrapper.client

import android.content.Context
import io.harness.cfsdk.cloud.cache.CloudCache
import io.harness.cfsdk.cloud.core.model.Evaluation
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TmpStorage : CloudCache {

    private var key_all: String? = null
    private var evaluations = mutableMapOf<String, HashMap<String, Evaluation>>()

    fun TmpCache(appContext: Context) {

        key_all = "all_evaluations"
        evaluations = mutableMapOf()
    }

    override fun getEvaluation(env: String, key: String): Evaluation? {

        val items = evaluations[env]
        return items?.get(key)
    }

    override fun saveEvaluation(env: String, key: String, evaluation: Evaluation) {

        var items = evaluations[env]
        if (items == null) {

            items = HashMap()
            evaluations[env] = items
        }
        items[key] = evaluation
    }

    override fun getAllEvaluations(env: String): List<Evaluation> {

        val items = evaluations[env]
        return if (items != null) {

            LinkedList(items.values)

        } else LinkedList()
    }

    override fun saveAllEvaluations(env: String, newEvaluations: List<Evaluation>) {

        val items = HashMap<String, Evaluation>()
        for (item in newEvaluations) {
            items[item.identifier] = item
        }
        evaluations[env] = items
    }

    override fun removeEvaluation(env: String, key: String) {

        val items = evaluations[env]
        items?.remove(key)
    }

    override fun clear() {

        evaluations.clear()
    }
}