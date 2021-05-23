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
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private Item item;
    String finalUriOrUrl = "";
    List<Item> itemsList = new ArrayList<>();
    static int count = 0;

    public ItemAdapter(Context context, Item item, List<Item> itemsList){
        this.itemsList = itemsList;
        this.context = context;
        this.item = item;
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

        Item item = itemsList.get(position);
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
        return itemsList.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b){
            super(b.getRoot());
            this.b = b;
        }
    }
}
