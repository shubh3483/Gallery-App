package com.example.galleryapp;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding b;
    Bitmap bitmapFromString;
    List<Item> allItems = new ArrayList<>();
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setTitle("Gallery");
        setContentView(b.getRoot());
        Gson gson = new Gson();
        if(savedInstanceState != null){
            String json = savedInstanceState.getString(Constants.ALL_ITEMS);
            allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
            }.getType());
            if(allItems != null){
                for(Item item : allItems){
                    //Bind Data
                    ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
                    Glide.with(this)
                            .asBitmap()
                            .load(item.imageRedirectedUrl)
                            .into(binding.imageView);
                    //binding.imageView.setImageBitmap(bitmapFromString);
                    binding.title.setText(item.label);
                    binding.title.setBackgroundColor(item.color);

                    b.list.addView(binding.getRoot());
                }
            }
            else{
                allItems = new ArrayList<>();
            }
        }else{
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            String json = preferences.getString(Constants.ALL_ITEMS, null);
            allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
            }.getType());
            if(allItems != null){
                for(Item item : allItems){

                    //Bind Data
                    ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
                    Glide.with(this)
                            .asBitmap()
                            .load(item.imageRedirectedUrl)
                            .into(binding.imageView);
                    //binding.imageView.setImageBitmap(bitmapFromString);
                    binding.title.setText(item.label);
                    binding.title.setBackgroundColor(item.color);

                    b.list.addView(binding.getRoot());
                }
            }
            else{
                allItems = new ArrayList<>();
            }
        }
    }

    /**
     * This method will show the add image option in our main activity.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_app, menu);
        return true;
    }

    /**
     * The below method will show the Dialog box.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addImage){
            showAddImageDialog();
            return true;
        }
        return false;
    }


    /**
     * The below two methods will add the item card to the view.
     */
    private void showAddImageDialog() {
        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {

                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();
                    }
                });
    }

    private void inflateViewForItem(Item item) {

        //This is adding items in array list
        allItems.add(item);

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Retrieving Bitmap from its string.
        /*try {
            byte[] encodeByte = Base64.decode(item.imageRedirectedUrl, Base64.DEFAULT);
            bitmapFromString = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            
        } catch (Exception e) {
            e.getMessage();
        }*/
        //Bind Data
        Glide.with(this)
                .asBitmap()
                .load(item.imageRedirectedUrl)
                .into(binding.imageView);
        //binding.imageView.setImageBitmap(bitmapFromString);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
    }

    /**
     * This method will save the item card so that when the screen is rotated the data is not lost.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = gson.toJson(allItems);
        outState.putString(Constants.ALL_ITEMS, json);
    }


    /**
     * This method will save the item card in shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();
        String json = gson.toJson(allItems);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putString(Constants.ALL_ITEMS, json)
                .apply();
    }
}