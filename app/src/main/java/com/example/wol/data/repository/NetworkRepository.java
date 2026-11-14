package com.example.wol.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.wol.R;
import com.example.wol.data.RepositoryCallback;
import com.example.wol.data.db.NetworkDao;
import com.example.wol.data.db.NetworkEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NetworkRepository {

    private static final String TAG = "NetworkRepository";

    private final Context context;
    private final NetworkDao networkDao;
    private final ExecutorService executor;

    @Inject
    public NetworkRepository(@ApplicationContext Context context,
                             NetworkDao networkDao,
                             ExecutorService executor) {
        this.context = context;
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
                // Room не проставляет autoGenerate-ключ в объект сам — переносим
                // rowId в сущность, иначе вызывающий код видит id = 0.
                network.id = networkDao.insert(network);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError(context.getString(R.string.err_network_exists));
            } catch (Exception e) {
                Log.e(TAG, "insert failed", e);
                callback.onError(context.getString(R.string.err_insert_failed));
            }
        });
    }

    public void update(NetworkEntity network, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                networkDao.update(network);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onError(context.getString(R.string.err_network_exists));
            } catch (Exception e) {
                Log.e(TAG, "update failed", e);
                callback.onError(context.getString(R.string.err_update_failed));
            }
        });
    }

    public void delete(NetworkEntity network, RepositoryCallback callback) {
        executor.execute(() -> {
            try {
                networkDao.delete(network);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "delete failed", e);
                callback.onError(context.getString(R.string.err_delete_failed));
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
