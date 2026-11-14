package com.example.wol.di;

import android.content.Context;

import androidx.room.Room;

import com.example.wol.data.db.AppDatabase;
import com.example.wol.data.db.DeviceDao;
import com.example.wol.data.db.NetworkDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "wol-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public NetworkDao provideNetworkDao(AppDatabase db) {
        return db.networkDao();
    }

    @Provides
    public DeviceDao provideDeviceDao(AppDatabase db) {
        return db.deviceDao();
    }
}