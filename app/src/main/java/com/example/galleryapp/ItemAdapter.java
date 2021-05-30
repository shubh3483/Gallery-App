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
    onClickListener listener;
    onLongItemClickListener mOnLongItemClickListener;
    //public int lastSelected;

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
    public ItemAdapter(Context context, List<Item> itemsList, onClickListener listener){
        this.itemsList = itemsList;
        requiredNewItemList = itemsList;
        this.context = context;
        this.listener = listener;
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


        ItemCardBinding tempBinding = ((ItemViewHolder)holder).b;
        Item item = requiredNewItemList.get(position);
        finalUriOrUrl = checkUrlOrURi(item);
        Glide.with(context)
                .load(finalUriOrUrl)
                .into(holder.b.imageView);
        holder.b.title.setText(item.label);
        holder.b.title.setBackgroundColor(item.color);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //lastSelected = holder.getAdapterPosition();
                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.ItemLongClicked(v, position);
                    return true;
                }
                return false;
            }
        });
        //tempMethod(tempBinding.getRoot());
        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.ItemLongClicked(v, position);
                }

                return true;
            }
        });*/
        /*TODO : holder.b.cardShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.position(position);
            }
        });*/

    }

    /*private void tempMethod(ConstraintLayout tempBinding) {
        tempBinding.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MainActivity activity = (MainActivity)context;
                //activity.getMenuInflater();
                activity.getMenuInflater().inflate(R.menu.context_menu, menu);
            }
        });
    }*/

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

    static class ItemViewHolder extends RecyclerView.ViewHolder /*implements View.OnLongClickListener, View.OnCreateContextMenuListener*/ {
        ItemCardBinding b;
        public ItemViewHolder(ItemCardBinding b){
            super(b.getRoot());
            this.b = b;
            //this.b.imageView.setOnCreateContextMenuListener(this);
        }

        /*@Override
        public boolean onLongClick(View v) {
            return false;
        }*/

        /*@Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater inflater = ((MainActivity) context).getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        }*/
    }

    public void setOnLongItemClickListener(onLongItemClickListener mOnLongItemClickListener) {
        this.mOnLongItemClickListener = mOnLongItemClickListener;
    }

    public interface onLongItemClickListener {
        void ItemLongClicked(View v, int position);
    }

    interface onClickListener {
        void position(int position);
    }
}
