package com.example.wol.ui;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.wol.R;
import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.NetworkEntity;
import com.example.wol.data.repository.NetworkRepository;
import com.example.wol.data.repository.PreferencesRepository;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class NetworkEditViewModel extends ViewModel {

    private final Context context;
    private final NetworkRepository repository;
    private final PreferencesRepository preferencesRepository;

    public final LiveData<List<NetworkEntity>> networks;

    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> warning = new MutableLiveData<>();

    @Inject
    public NetworkEditViewModel(@ApplicationContext Context context,
                                NetworkRepository repository,
                                PreferencesRepository preferencesRepository) {
        this.context = context;
        this.repository = repository;
        this.preferencesRepository = preferencesRepository;
        networks = repository.getAllNetworks();
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getWarning() {
        return warning;
    }

    public void clearError() {
        error.setValue(null);
    }

    public void clearWarning() {
        warning.setValue(null);
    }

    public void addNetwork(String name) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue(context.getString(R.string.err_name_empty));
            return;
        }
        NetworkEntity network = new NetworkEntity();
        network.name = name.trim();
        network.createdAt = System.currentTimeMillis();

        repository.insert(network, new RepositoryCallback() {
            @Override
            public void onSuccess() {
                preferencesRepository.setSelectedNetworkId(network.id);
                preferencesRepository.setSelectedDeviceId(0L);
            }

            @Override
            public void onWarning(String message) {
                warning.postValue(message);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void renameNetwork(NetworkEntity network, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            error.setValue(context.getString(R.string.err_name_empty));
            return;
        }
        network.name = newName.trim();
        repository.update(network, new RepositoryCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onWarning(String message) {
                warning.postValue(message);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void deleteNetwork(NetworkEntity network) {
        repository.delete(network, new RepositoryCallback() {
            @Override
            public void onSuccess() {
                Long selected = preferencesRepository.getSelectedNetworkId().getValue();
                if (selected != null && selected == network.id) {
                    preferencesRepository.setSelectedNetworkId(0L);
                    preferencesRepository.setSelectedDeviceId(0L);
                }
            }

            @Override
            public void onWarning(String message) {
                warning.postValue(message);
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void getDeviceCount(long networkId, Consumer<Integer> callback) {
        repository.getDeviceCount(networkId, callback);
    }

    public void selectNetwork(long networkId) {
        preferencesRepository.setSelectedNetworkId(networkId);
        preferencesRepository.setSelectedDeviceId(0L);
    }
}