package com.example.walkingmate.feature.walk.data;

import android.content.Context;
import android.content.SharedPreferences;

public class BpmPreferenceStore {
    private static final String PREF_NAME = "BPM_PREFS";
    private static final String SAVED_BPM_KEY = "saved_bpm";

    private final SharedPreferences sharedPreferences;

    public BpmPreferenceStore(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveBpm(float bpm) {
        sharedPreferences.edit().putFloat(SAVED_BPM_KEY, bpm).apply();
    }

    public float getSavedBpm() {
        return sharedPreferences.getFloat(SAVED_BPM_KEY, 0f);
    }

    public int getSavedBpmRounded() {
        return Math.round(getSavedBpm());
    }

    public void resetSavedBpm() {
        sharedPreferences.edit().putFloat(SAVED_BPM_KEY, 0f).apply();
    }
}
