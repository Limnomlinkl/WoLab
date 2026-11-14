package com.example.wol.data.repository;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;

import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.DeviceDao;
import com.example.wol.data.db.DeviceEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DeviceRepository {

    private final DeviceDao deviceDao;
    private final ExecutorService executor;

    @Inject
    public DeviceRepository(DeviceDao deviceDao, ExecutorService executor) {
        this.deviceDao = deviceDao;
        this.executor = executor;
    }

    public LiveData<List<DeviceEntity>> getDevicesForNetwork(long networkId) {
        return deviceDao.getDevicesForNetwork(networkId);
    }

    public LiveData<DeviceEntity> getDeviceById(long id) {
        return deviceDao.getDeviceById(id);
    }

    public DeviceEntity getDeviceByIdSync(long id) {
        return deviceDao.getDeviceByIdSync(id);
    }

    public void insert(DeviceEntity device, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                deviceDao.insert(device);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError("Device name already exists in this network");
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Insert failed" : e.getMessage());
            }
        });
    }

    public void update(DeviceEntity device, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                deviceDao.update(device);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError("Device name already exists in this network");
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Update failed" : e.getMessage());
            }
        });
    }

    public void delete(DeviceEntity device, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                deviceDao.delete(device);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Delete failed" : e.getMessage());
            }
        });
    }
}