package com.example.wol.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.wol.R;
import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.DeviceDao;
import com.example.wol.data.db.DeviceEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class DeviceRepository {

    private static final String TAG = "DeviceRepository";

    private final Context context;
    private final DeviceDao deviceDao;
    private final ExecutorService executor;

    @Inject
    public DeviceRepository(@ApplicationContext Context context,
                            DeviceDao deviceDao,
                            ExecutorService executor) {
        this.context = context;
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
                device.id = deviceDao.insert(device);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError(context.getString(R.string.err_device_exists));
            } catch (Exception e) {
                Log.e(TAG, "insert failed", e);
                callback.onError(context.getString(R.string.err_insert_failed));
            }
        });
    }

    public void update(DeviceEntity device, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                deviceDao.update(device);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError(context.getString(R.string.err_device_exists));
            } catch (Exception e) {
                Log.e(TAG, "update failed", e);
                callback.onError(context.getString(R.string.err_update_failed));
            }
        });
    }

    public void delete(DeviceEntity device, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                deviceDao.delete(device);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "delete failed", e);
                callback.onError(context.getString(R.string.err_delete_failed));
            }
        });
    }
}
