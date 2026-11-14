package com.example.wol.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {NetworkEntity.class, DeviceEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NetworkDao networkDao();
    public abstract DeviceDao deviceDao();
}