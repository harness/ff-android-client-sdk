package io.harness.cfsdk.cloud.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public class StorageCache implements CloudCache {

    private final Gson gson;
    private final WeakReference<Context> appContextWeakRef;

    {

        gson = new Gson();
    }

    public StorageCache(Context appContext) {

        this.appContextWeakRef = new WeakReference<>(appContext);
    }


    @Override
    public Evaluation getEvaluation(String key) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContextWeakRef.get());
        String result = preferences.getString(key, null);
        return gson.fromJson(result, Evaluation.class);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void saveEvaluation(String key, Evaluation evaluation) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContextWeakRef.get());
        final String json = gson.toJson(evaluation);
        preferences.edit().putString(key, json).commit();
    }

    @Override
    public List<Evaluation> getAllEvaluations(String key) {
        return Collections.emptyList();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void removeEvaluation(String key) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContextWeakRef.get());
        preferences.edit().remove(key).commit();
    }

    @Override
    public void clear() {

    }
}
