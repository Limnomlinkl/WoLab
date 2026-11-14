package com.example.wol.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wol.R;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                }
        );
        requestPermissionsIfNeeded();
    }

    private void requestPermissionsIfNeeded() {
        List<String> permissions = new ArrayList<>();

        // NOTE: для WoL это разрешение не обязательно, но добавлено по ТЗ
        // и запрашивается только на API 33+.
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        }

        if (!permissions.isEmpty()) {
            permissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }
}