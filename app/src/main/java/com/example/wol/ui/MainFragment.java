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
import com.google.android.material.snackbar.Snackbar;

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
            if (item.getItemId() == R.id.action_exit) {
                requireActivity().finishAndRemoveTask();
                return true;
            }
            return false;
        });

        // Переход в редакторы — тапами по карточкам «Сеть» и «Устройство».
        // Пункты меню «Изменить сеть/устройство» убраны, чтобы не дублировать навигацию.
        binding.cardNetwork.setOnClickListener(v -> NavHostFragment.findNavController(this)
                .navigate(R.id.action_main_to_networkEdit));

        binding.cardDevice.setOnClickListener(v -> openDeviceEditor());

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

    /**
     * Открывает список устройств выбранной сети. Если сеть ещё не выбрана —
     * подсказываем, что сначала нужно зайти в карточку «Сеть».
     */
    private void openDeviceEditor() {
        NetworkEntity network = viewModel.selectedNetwork.getValue();
        if (network == null) {
            snackbar(getString(R.string.select_network_first));
            return;
        }
        Bundle args = new Bundle();
        args.putLong("networkId", network.id);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_main_to_deviceEdit, args);
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
