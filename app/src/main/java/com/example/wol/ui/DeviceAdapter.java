package com.example.wol.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wol.data.db.DeviceEntity;
import com.example.wol.databinding.ItemDeviceBinding;

public class DeviceAdapter extends ListAdapter<DeviceEntity, DeviceAdapter.ViewHolder> {

    public interface Listener {
        void onSelect(DeviceEntity device);

        void onRename(DeviceEntity device);

        void onChangeIp(DeviceEntity device);

        void onDelete(DeviceEntity device);
    }

    private static final DiffUtil.ItemCallback<DeviceEntity> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull DeviceEntity oldItem,
                                               @NonNull DeviceEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DeviceEntity oldItem,
                                                  @NonNull DeviceEntity newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.ipAddress.equals(newItem.ipAddress)
                            && oldItem.macAddress.equals(newItem.macAddress)
                            && oldItem.checkPort == newItem.checkPort;
                }
            };

    private final Listener listener;

    public DeviceAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeviceBinding binding;

        ViewHolder(ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onSelect(getItem(pos));
                }
            });
            binding.buttonRename.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onRename(getItem(pos));
                }
            });
            binding.buttonChangeIp.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onChangeIp(getItem(pos));
                }
            });
            binding.buttonDelete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onDelete(getItem(pos));
                }
            });
        }

        void bind(DeviceEntity device) {
            binding.textName.setText(device.name);
            binding.textDetails.setText(
                    device.ipAddress + "  •  " + device.macAddress + "  •  :" + device.checkPort
            );
        }
    }
}