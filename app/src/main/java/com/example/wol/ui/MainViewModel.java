package com.example.wol.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.wol.data.db.DeviceEntity;
import com.example.wol.data.db.NetworkEntity;
import com.example.wol.data.repository.DeviceRepository;
import com.example.wol.data.repository.NetworkRepository;
import com.example.wol.data.repository.PreferencesRepository;
import com.example.wol.domain.LaunchOrchestrator;
import com.example.wol.domain.LaunchState;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private final PreferencesRepository preferencesRepository;
    private final LaunchOrchestrator orchestrator;

    public final LiveData<List<NetworkEntity>> networks;
    public final LiveData<NetworkEntity> selectedNetwork;
    public final LiveData<List<DeviceEntity>> devices;
    public final LiveData<DeviceEntity> selectedDevice;
    public final LiveData<LaunchState> launchState;

    private final MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public MainViewModel(NetworkRepository networkRepository,
                         DeviceRepository deviceRepository,
                         PreferencesRepository preferencesRepository,
                         LaunchOrchestrator orchestrator) {
        this.preferencesRepository = preferencesRepository;
        this.orchestrator = orchestrator;

        LiveData<Long> selectedNetworkId = preferencesRepository.getSelectedNetworkId();
        LiveData<Long> selectedDeviceId = preferencesRepository.getSelectedDeviceId();

        networks = networkRepository.getAllNetworks();

        selectedNetwork = Transformations.switchMap(selectedNetworkId, id -> {
            if (id == null || id == 0L) {
                return new MutableLiveData<>();
            }
            return networkRepository.getNetworkById(id);
        });

        devices = Transformations.switchMap(selectedNetwork, network -> {
            if (network == null) {
                return new MutableLiveData<>(Collections.emptyList());
            }
            return deviceRepository.getDevicesForNetwork(network.id);
        });

        selectedDevice = Transformations.switchMap(selectedDeviceId, id -> {
            if (id == null || id == 0L) {
                return new MutableLiveData<>();
            }
            return deviceRepository.getDeviceById(id);
        });

        launchState = orchestrator.getState();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void clearError() {
        error.setValue(null);
    }

    public void selectNetwork(long networkId) {
        preferencesRepository.setSelectedNetworkId(networkId);
        preferencesRepository.setSelectedDeviceId(0L);
    }

    public void selectDevice(long deviceId) {
        preferencesRepository.setSelectedDeviceId(deviceId);
    }

    public void launch() {
        DeviceEntity device = selectedDevice.getValue();
        if (device == null) {
            error.setValue("Select a device first");
            return;
        }
        orchestrator.start(device.ipAddress, device.macAddress, device.checkPort);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        orchestrator.cancel();
    }
}