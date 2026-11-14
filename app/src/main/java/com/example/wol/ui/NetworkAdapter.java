package com.example.wol.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wol.data.db.NetworkEntity;
import com.example.wol.databinding.ItemNetworkBinding;

public class NetworkAdapter extends ListAdapter<NetworkEntity, NetworkAdapter.ViewHolder> {

    public interface Listener {
        void onSelect(NetworkEntity network);

        void onRename(NetworkEntity network);

        void onDelete(NetworkEntity network);
    }

    private static final DiffUtil.ItemCallback<NetworkEntity> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull NetworkEntity oldItem,
                                               @NonNull NetworkEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull NetworkEntity oldItem,
                                                  @NonNull NetworkEntity newItem) {
                    return oldItem.name.equals(newItem.name);
                }
            };

    private final Listener listener;

    public NetworkAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNetworkBinding binding = ItemNetworkBinding.inflate(
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

        private final ItemNetworkBinding binding;

        ViewHolder(ItemNetworkBinding binding) {
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
            binding.buttonDelete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onDelete(getItem(pos));
                }
            });
        }

        void bind(NetworkEntity network) {
            binding.textName.setText(network.name);
        }
    }
}