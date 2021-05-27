package com.example.galleryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    String finalUriOrUrl = "";
    List<Item> itemsList, requiredNewItemList;

    /**
     * This is needed when we are filtering to avoid reference issues.
     * @param context
     */
    public ItemAdapter(Context context) {
        this.context = context;
        itemsList = new ArrayList<>();
        requiredNewItemList = new ArrayList<>();
    }

    /**
     * This is needed when we are fetching new image.
     * @param context
     * @param itemsList
     */
    public ItemAdapter(Context context, List<Item> itemsList){
        this.itemsList = itemsList;
        requiredNewItemList = itemsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(context)
        ,parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {

        Item item = requiredNewItemList.get(position);
        finalUriOrUrl = checkUrlOrURi(item);
        Glide.with(context)
                .load(finalUriOrUrl)
                .into(holder.b.imageView);
        holder.b.title.setText(item.label);
        holder.b.title.setBackgroundColor(item.color);
    }

    private String checkUrlOrURi(Item item) {
        if(item.imageRedirectedUrl != null){
            return item.imageRedirectedUrl;
        }
        return item.uri;
    }

    @Override
    public int getItemCount() {
        return requiredNewItemList.size();
    }

    /**
     * This function will filter the particular card.
     * @param query
     * @param itemsList
     */
    public void filter(String query, List<Item> itemsList) {

        if(query.trim().isEmpty()){
            requiredNewItemList = itemsList;
            return;
        }
        query = query.trim().toLowerCase();
        requiredNewItemList.clear();
        for(Item item : itemsList){
            if(item.label.toLowerCase().contains(query)){
                requiredNewItemList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void showSortedItems() {
        notifyDataSetChanged();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(requiredNewItemList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(requiredNewItemList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b){
            super(b.getRoot());
            this.b = b;
        }
    }
}