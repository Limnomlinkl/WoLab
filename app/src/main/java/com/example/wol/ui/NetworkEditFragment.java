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
import com.example.wol.data.db.NetworkEntity;
import com.example.wol.databinding.DialogEditTextBinding;
import com.example.wol.databinding.FragmentNetworkEditBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NetworkEditFragment extends Fragment {

    private FragmentNetworkEditBinding binding;
    private NetworkEditViewModel viewModel;
    private NetworkAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNetworkEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(NetworkEditViewModel.class);

        binding.toolbar.setNavigationOnClickListener(
                v -> NavHostFragment.findNavController(this).navigateUp()
        );

        adapter = new NetworkAdapter(new NetworkAdapter.Listener() {
            @Override
            public void onSelect(NetworkEntity network) {
                viewModel.selectNetwork(network.id);
                snackbar(getString(R.string.network_selected, network.name));
            }

            @Override
            public void onRename(NetworkEntity network) {
                showRenameDialog(network);
            }

            @Override
            public void onDelete(NetworkEntity network) {
                confirmDelete(network);
            }
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.buttonAdd.setOnClickListener(v -> showAddDialog());

        viewModel.networks.observe(getViewLifecycleOwner(), list -> {
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

    private void showAddDialog() {
        DialogEditTextBinding dialogBinding = DialogEditTextBinding.inflate(getLayoutInflater());
        TextInputLayout inputLayout = dialogBinding.textInputLayout;
        inputLayout.setHint(getString(R.string.network_name));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_network)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.add, (d, w) -> {
                    String name = dialogBinding.editText.getText() != null
                            ? dialogBinding.editText.getText().toString()
                            : "";
                    viewModel.addNetwork(name);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showRenameDialog(NetworkEntity network) {
        DialogEditTextBinding dialogBinding = DialogEditTextBinding.inflate(getLayoutInflater());
        TextInputLayout inputLayout = dialogBinding.textInputLayout;
        inputLayout.setHint(getString(R.string.network_name));
        dialogBinding.editText.setText(network.name);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename_network)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    String name = dialogBinding.editText.getText() != null
                            ? dialogBinding.editText.getText().toString()
                            : "";
                    viewModel.renameNetwork(network, name);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(NetworkEntity network) {
        viewModel.getDeviceCount(network.id, count -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                String message = count > 0
                        ? getString(R.string.delete_network_with_devices, network.name, count)
                        : getString(R.string.delete_network_confirm, network.name);

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_network)
                        .setMessage(message)
                        .setPositiveButton(R.string.delete, (d, w) -> viewModel.deleteNetwork(network))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            });
        });
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