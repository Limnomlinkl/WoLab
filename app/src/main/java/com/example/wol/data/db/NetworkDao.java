package com.example.wol.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NetworkDao {

    @Query("SELECT * FROM networks ORDER BY createdAt ASC")
    LiveData<List<NetworkEntity>> getAllNetworks();

    @Query("SELECT * FROM networks WHERE id = :id")
    LiveData<NetworkEntity> getNetworkById(long id);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(NetworkEntity network);

    @Update
    void update(NetworkEntity network);

    @Delete
    void delete(NetworkEntity network);

    @Query("SELECT COUNT(*) FROM devices WHERE networkId = :networkId")
    int getDeviceCountForNetwork(long networkId);
}