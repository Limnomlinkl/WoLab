package com.example.wol.di;

import com.example.wol.network.HostAvailabilityChecker;
import com.example.wol.network.MacResolver;
import com.example.wol.network.WolSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public ExecutorService provideExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Provides
    @Singleton
    public WolSender provideWolSender() {
        return new WolSender();
    }

    @Provides
    @Singleton
    public HostAvailabilityChecker provideChecker() {
        return new HostAvailabilityChecker();
    }

    @Provides
    @Singleton
    public MacResolver provideMacResolver() {
        return new MacResolver();
    }
}