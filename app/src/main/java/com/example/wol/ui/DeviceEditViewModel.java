package com.example.wol.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.DeviceEntity;
import com.example.wol.data.repository.DeviceRepository;
import com.example.wol.data.repository.PreferencesRepository;
import com.example.wol.network.MacResolver;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DeviceEditViewModel extends ViewModel {

    private final DeviceRepository deviceRepository;
    private final MacResolver macResolver;
    private final ExecutorService executor;
    private final PreferencesRepository preferencesRepository;
    private final long networkId;

    public final LiveData<List<DeviceEntity>> devices;

    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> warning = new MutableLiveData<>();

    @Inject
    public DeviceEditViewModel(DeviceRepository deviceRepository,
                               MacResolver macResolver,
                               ExecutorService executor,
                               PreferencesRepository preferencesRepository,
                               SavedStateHandle savedStateHandle) {
        this.deviceRepository = deviceRepository;
        this.macResolver = macResolver;
        this.executor = executor;
        this.preferencesRepository = preferencesRepository;

        Long id = savedStateHandle.get("networkId");
        networkId = id != null ? id : 0L;
        devices = deviceRepository.getDevicesForNetwork(networkId);
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

    public void addDevice(String name, String ip, String portStr, String manualMac) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue("Name is empty");
            return;
        }
        if (!isValidIp(ip)) {
            error.setValue("Invalid IP address");
            return;
        }
        int port = 3389;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                error.setValue("Invalid port");
                return;
            }
        }
        if (manualMac != null && !manualMac.trim().isEmpty() && !isValidMac(manualMac)) {
            error.setValue("Invalid MAC address");
            return;
        }

        final String finalName = name.trim();
        final String finalIp = ip.trim();
        final int finalPort = port;
        final String finalManualMac = manualMac != null ? manualMac.trim() : "";

        executor.execute(() -> {
            String mac = finalManualMac.isEmpty()
                    ? macResolver.resolve(finalIp)
                    : finalManualMac;

            if (mac == null) {
                error.postValue("MAC not found. Enter it manually.");
                return;
            }

            DeviceEntity device = new DeviceEntity(
                    networkId,
                    finalName,
                    finalIp,
                    mac,
                    finalPort,
                    System.currentTimeMillis()
            );

            deviceRepository.insert(device, new RepositoryCallback() {
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
        });
    }

    public void renameDevice(DeviceEntity device, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            error.setValue("Name is empty");
            return;
        }
        device.name = newName.trim();
        deviceRepository.update(device, simpleCallback());
    }

    public void updateIp(DeviceEntity device, String newIp) {
        if (!isValidIp(newIp)) {
            error.setValue("Invalid IP address");
            return;
        }
        final String finalIp = newIp.trim();

        executor.execute(() -> {
            DeviceEntity fresh = deviceRepository.getDeviceByIdSync(device.id);
            if (fresh == null) {
                error.postValue("Device not found");
                return;
            }

            String mac = macResolver.resolve(finalIp);
            fresh.ipAddress = finalIp;
            if (mac != null) {
                fresh.macAddress = mac;
            }

            deviceRepository.update(fresh, new RepositoryCallback() {
                @Override
                public void onSuccess() {
                    if (mac == null) {
                        warning.postValue("MAC not resolved for new IP. Old MAC kept.");
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
        });
    }

    public void deleteDevice(DeviceEntity device) {
        deviceRepository.delete(device, new RepositoryCallback() {
            @Override
            public void onSuccess() {
                Long selected = preferencesRepository.getSelectedDeviceId().getValue();
                if (selected != null && selected == device.id) {
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

    public void selectDevice(long deviceId) {
        preferencesRepository.setSelectedDeviceId(deviceId);
    }

    private RepositoryCallback simpleCallback() {
        return new RepositoryCallback() {
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
        };
    }

    private boolean isValidIp(String ip) {
        if (ip == null) return false;
        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        return ip.matches(regex);
    }

    private boolean isValidMac(String mac) {
        return mac != null && mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }
}