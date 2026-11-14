package com.example.wol.data;

public interface RepositoryCallback {
    void onSuccess();

    /** Не ошибка, но нужно показать пользователю предупреждение. */
    default void onWarning(String message) {
        onSuccess();
    }

    void onError(String message);
}