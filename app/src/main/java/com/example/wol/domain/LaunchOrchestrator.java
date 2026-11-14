package com.example.wol.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.wol.network.HostAvailabilityChecker;
import com.example.wol.network.WolSender;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;

@ViewModelScoped
public class LaunchOrchestrator {

    private static final int POLL_TIMEOUT_MS = 500;
    private static final int POLL_INTERVAL_MS = 1000;
    private static final long TOTAL_POLL_MS = 30_000L;
    private static final int WOL_REPEATS = 5;
    private static final long WOL_GAP_MS = 200L;

    private final WolSender wolSender;
    private final HostAvailabilityChecker checker;

    private final MutableLiveData<LaunchState> state =
            new MutableLiveData<>(LaunchState.IDLE);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private ExecutorService executor;
    private Future<?> future;

    @Inject
    public LaunchOrchestrator(WolSender wolSender, HostAvailabilityChecker checker) {
        this.wolSender = wolSender;
        this.checker = checker;
    }

    public LiveData<LaunchState> getState() {
        return state;
    }

    public void start(String ipAddress, String macAddress, int checkPort) {
        cancel();

        cancelled.set(false);
        executor = Executors.newSingleThreadExecutor();

        future = executor.submit(() -> {
            try {
                state.postValue(LaunchState.SENDING);

                for (int i = 0; i < WOL_REPEATS; i++) {
                    if (cancelled.get()) return;
                    try {
                        wolSender.send(macAddress, ipAddress);
                    } catch (IOException ignored) {
                        // NOTE: даже если один пакет не ушёл, пробуем остальные.
                    }
                    Thread.sleep(WOL_GAP_MS);
                }

                state.postValue(LaunchState.WAITING);

                long deadline = System.currentTimeMillis() + TOTAL_POLL_MS;
                while (System.currentTimeMillis() < deadline) {
                    if (cancelled.get()) return;

                    if (checker.isAvailable(ipAddress, checkPort, POLL_TIMEOUT_MS)) {
                        state.postValue(LaunchState.SUCCESS);
                        return;
                    }
                    Thread.sleep(POLL_INTERVAL_MS);
                }

                if (!cancelled.get()) {
                    state.postValue(LaunchState.FAILED);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                if (!cancelled.get()) {
                    state.postValue(LaunchState.FAILED);
                }
            }
        });
    }

    public void cancel() {
        cancelled.set(true);
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}