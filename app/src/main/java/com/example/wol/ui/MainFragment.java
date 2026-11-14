package com.example.wol.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.wol.R;
import com.example.wol.data.db.DeviceEntity;
import com.example.wol.data.db.NetworkEntity;
import com.example.wol.databinding.FragmentMainBinding;
import com.example.wol.domain.LaunchState;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_network) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_main_to_networkEdit);
                return true;
            }
            if (id == R.id.action_edit_device) {
                NetworkEntity network = viewModel.selectedNetwork.getValue();
                if (network == null) {
                    snackbar(getString(R.string.select_network_first));
                    return true;
                }
                Bundle args = new Bundle();
                args.putLong("networkId", network.id);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_main_to_deviceEdit, args);
                return true;
            }
            if (id == R.id.action_exit) {
                requireActivity().finishAndRemoveTask();
                return true;
            }
            return false;
        });

        binding.cardNetwork.setOnClickListener(v -> showNetworkPicker());
        binding.cardDevice.setOnClickListener(v -> showDevicePicker());
        binding.buttonLaunch.setOnClickListener(v -> viewModel.launch());

        viewModel.selectedNetwork.observe(getViewLifecycleOwner(), network -> {
            if (network != null) {
                binding.textNetwork.setText(network.name);
            } else {
                binding.textNetwork.setText(R.string.no_network);
            }
        });

        viewModel.selectedDevice.observe(getViewLifecycleOwner(), device -> {
            if (device != null) {
                binding.textDevice.setText(device.name);
            } else {
                binding.textDevice.setText(R.string.no_device);
            }
        });

        viewModel.launchState.observe(getViewLifecycleOwner(), this::renderStatus);

        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                snackbar(msg);
                viewModel.clearError();
            }
        });
    }

    private void showNetworkPicker() {
        List<NetworkEntity> networks = viewModel.networks.getValue();
        if (networks == null || networks.isEmpty()) {
            snackbar(getString(R.string.no_networks));
            return;
        }

        String[] names = new String[networks.size()];
        for (int i = 0; i < networks.size(); i++) {
            names[i] = networks.get(i).name;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_network)
                .setItems(names, (dialog, which) -> viewModel.selectNetwork(networks.get(which).id))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDevicePicker() {
        NetworkEntity network = viewModel.selectedNetwork.getValue();
        if (network == null) {
            snackbar(getString(R.string.select_network_first));
            return;
        }

        List<DeviceEntity> devices = viewModel.devices.getValue();
        if (devices == null || devices.isEmpty()) {
            snackbar(getString(R.string.no_devices));
            return;
        }

        String[] names = new String[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            names[i] = devices.get(i).name;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_device)
                .setItems(names, (dialog, which) -> viewModel.selectDevice(devices.get(which).id))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void renderStatus(LaunchState state) {
        int textRes;
        boolean progressVisible = false;

        switch (state) {
            case SENDING:
                textRes = R.string.status_sending;
                progressVisible = true;
                break;
            case WAITING:
                textRes = R.string.status_waiting;
                progressVisible = true;
                break;
            case SUCCESS:
                textRes = R.string.status_success;
                break;
            case FAILED:
                textRes = R.string.status_failed;
                break;
            case IDLE:
            default:
                textRes = R.string.status_idle;
                break;
        }

        binding.textStatus.setText(textRes);
        binding.progress.setVisibility(progressVisible ? View.VISIBLE : View.GONE);
        binding.buttonLaunch.setEnabled(!progressVisible);
    }

    private void snackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}