package com.example.wms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<Item> listItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item listItem);
    }

    public ListAdapter(List<Item> listItems, OnItemClickListener listener) {
        this.listItems = listItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item listItem = listItems.get(position);
        holder.bind(listItem);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemName;
        private TextView itemQuantity;
        private TextView itemLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemLocation = itemView.findViewById(R.id.itemLocation);

            // Set click listener for the item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(listItems.get(position));
                    }
                }
            });
        }

        public void bind(Item listItem) {
            itemName.setText("Item Name: " + listItem.getItemName());
            itemQuantity.setText("Quantity: " + listItem.getQuantity());
            itemLocation.setText("Location: " + listItem.getLocation());
        }
    }
}

