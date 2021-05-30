package com.example.galleryapp;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    String finalUriOrUrl = "";
    List<Item> itemsList, requiredNewItemList;
    ActivityMainBinding b;
    int position = 0;
    onClickListener listener;
    boolean checkDragHandle;

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
    public ItemAdapter(Context context, List<Item> itemsList, boolean checkDragHandle, onClickListener listener){
        this.itemsList = itemsList;
        requiredNewItemList = itemsList;
        this.context = context;
        this.listener = listener;
        this.checkDragHandle = checkDragHandle;
        b = ActivityMainBinding.inflate(LayoutInflater.from(context));
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

    @Override
    public int getItemCount() {
        return requiredNewItemList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnCreateContextMenuListener {
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b){
            super(b.getRoot());
            this.b = b;
            this.b.imageView.setOnCreateContextMenuListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if(!checkDragHandle) {
                MenuInflater inflater = ((MainActivity) context).getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                position = getAdapterPosition();
                listener.position(position);
            }
        }
    }

    /**
     * This function will check whether image is from internet or from the gallery and accordingly
     * it will load the "URI" or "URL".
     * @param item
     * @return
     */
    private String checkUrlOrURi(Item item) {
        if(item.imageRedirectedUrl != null){
            return item.imageRedirectedUrl;
        }
        return item.uri;
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

    /**
     * This function will notify to sort the items.
     */
    public void showSortedItems() {
        notifyDataSetChanged();
    }

    /**
     * This function is for drag and drop functionality.
     * @param fromPosition
     * @param toPosition
     */
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

    /*public void setOnLongItemClickListener(onLongItemClickListener mOnLongItemClickListener) {
        this.mOnLongItemClickListener = mOnLongItemClickListener;
    }

    public interface onLongItemClickListener {
        void ItemLongClicked(View v, int position);
    }*/

    /**
     * This will send the callback to the MainActivity about the position of the item for context menu.
     */
    interface onClickListener {
        void position(int position);
    }
}
