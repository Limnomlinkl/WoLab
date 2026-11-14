package com.example.wol.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class PreferencesRepository {

    private static final String PREFS_NAME = "wol_prefs";
    private static final String KEY_NETWORK_ID = "selected_network_id";
    private static final String KEY_DEVICE_ID = "selected_device_id";

    private final SharedPreferences prefs;
    private final MutableLiveData<Long> selectedNetworkId;
    private final MutableLiveData<Long> selectedDeviceId;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Inject
    public PreferencesRepository(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        selectedNetworkId = new MutableLiveData<>(prefs.getLong(KEY_NETWORK_ID, 0L));
        selectedDeviceId = new MutableLiveData<>(prefs.getLong(KEY_DEVICE_ID, 0L));

        listener = (sharedPreferences, key) -> {
            if (KEY_NETWORK_ID.equals(key)) {
                selectedNetworkId.postValue(sharedPreferences.getLong(key, 0L));
            } else if (KEY_DEVICE_ID.equals(key)) {
                selectedDeviceId.postValue(sharedPreferences.getLong(key, 0L));
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public LiveData<Long> getSelectedNetworkId() {
        return selectedNetworkId;
    }

    public LiveData<Long> getSelectedDeviceId() {
        return selectedDeviceId;
    }

    public void setSelectedNetworkId(long id) {
        prefs.edit().putLong(KEY_NETWORK_ID, id).apply();
    }

    public void setSelectedDeviceId(long id) {
        prefs.edit().putLong(KEY_DEVICE_ID, id).apply();
    }
}