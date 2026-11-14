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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wol.R;
import com.example.wol.data.db.DeviceEntity;
import com.example.wol.databinding.DialogAddDeviceBinding;
import com.example.wol.databinding.DialogEditTextBinding;
import com.example.wol.databinding.FragmentDeviceEditBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DeviceEditFragment extends Fragment {

    private FragmentDeviceEditBinding binding;
    private DeviceEditViewModel viewModel;
    private DeviceAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDeviceEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DeviceEditViewModel.class);

        binding.toolbar.setNavigationOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp()
        );

        adapter = new DeviceAdapter(new DeviceAdapter.Listener() {
            @Override
            public void onSelect(DeviceEntity device) {
                viewModel.selectDevice(device.id);
                snackbar(getString(R.string.device_selected, device.name));
            }

            @Override
            public void onRename(DeviceEntity device) {
                showRenameDialog(device);
            }

            @Override
            public void onChangeIp(DeviceEntity device) {
                showChangeIpDialog(device);
            }

            @Override
            public void onDelete(DeviceEntity device) {
                confirmDelete(device);
            }
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.buttonAdd.setOnClickListener(v -> showAddDeviceDialog());

        viewModel.devices.observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            boolean empty = list == null || list.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                snackbar(msg);
                viewModel.clearError();
            }
        });

        viewModel.getWarning().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                snackbar(msg);
                viewModel.clearWarning();
            }
        });
    }

    private void showAddDeviceDialog() {
        DialogAddDeviceBinding dialogBinding = DialogAddDeviceBinding.inflate(getLayoutInflater());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_device)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.add, (d, w) -> {
                    String name = textOf(dialogBinding.editName);
                    String ip = textOf(dialogBinding.editIp);
                    String port = textOf(dialogBinding.editPort);
                    String mac = textOf(dialogBinding.editMac);
                    viewModel.addDevice(name, ip, port, mac);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRenameDialog(DeviceEntity device) {
        DialogEditTextBinding dialogBinding = DialogEditTextBinding.inflate(getLayoutInflater());
        dialogBinding.textInputLayout.setHint(getString(R.string.device_name));
        dialogBinding.editText.setText(device.name);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename_device)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = dialogBinding.editText.getText() != null
                            ? dialogBinding.editText.getText().toString()
                            : "";
                    viewModel.renameDevice(device, name);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showChangeIpDialog(DeviceEntity device) {
        DialogEditTextBinding dialogBinding = DialogEditTextBinding.inflate(getLayoutInflater());
        dialogBinding.textInputLayout.setHint(getString(R.string.ip_address));
        dialogBinding.editText.setText(device.ipAddress);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.change_ip)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String ip = dialogBinding.editText.getText() != null
                            ? dialogBinding.editText.getText().toString()
                            : "";
                    viewModel.updateIp(device, ip);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(DeviceEntity device) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_device)
                .setMessage(getString(R.string.delete_device_confirm, device.name))
                .setPositiveButton(R.string.delete, (d, w) -> viewModel.deleteDevice(device))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String textOf(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString() : "";
    }

    private void snackbar(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recycler.setAdapter(null);
        adapter = null;
        binding = null;
    }
}