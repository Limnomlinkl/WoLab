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
public interface DeviceDao {

    @Query("SELECT * FROM devices WHERE networkId = :networkId ORDER BY createdAt ASC")
    LiveData<List<DeviceEntity>> getDevicesForNetwork(long networkId);

    @Query("SELECT * FROM devices WHERE id = :id")
    LiveData<DeviceEntity> getDeviceById(long id);

    @Query("SELECT * FROM devices WHERE id = :id")
    DeviceEntity getDeviceByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(DeviceEntity device);

    @Update
    void update(DeviceEntity device);

    @Delete
    void delete(DeviceEntity device);
}