package com.example.wol.data.repository;

import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;

import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.NetworkDao;
import com.example.wol.data.db.NetworkEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkRepository {

    private final NetworkDao networkDao;
    private final ExecutorService executor;

    @Inject
    public NetworkRepository(NetworkDao networkDao, ExecutorService executor) {
        this.networkDao = networkDao;
        this.executor = executor;
    }

    public LiveData<List<NetworkEntity>> getAllNetworks() {
        return networkDao.getAllNetworks();
    }

    public LiveData<NetworkEntity> getNetworkById(long id) {
        return networkDao.getNetworkById(id);
    }

    public void insert(NetworkEntity network, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                networkDao.insert(network);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError("Network name already exists");
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Insert failed" : e.getMessage());
            }
        });
    }

    public void update(NetworkEntity network, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                networkDao.update(network);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError("Network name already exists");
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Update failed" : e.getMessage());
            }
        });
    }

    public void delete(NetworkEntity network, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                networkDao.delete(network);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Delete failed" : e.getMessage());
            }
        });
    }

    public void getDeviceCount(long networkId, Consumer<Integer> callback) {
        executor.execute(() -> {
            int count = networkDao.getDeviceCountForNetwork(networkId);
            callback.accept(count);
        });
    }
}