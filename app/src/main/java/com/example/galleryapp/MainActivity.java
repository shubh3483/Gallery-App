package com.example.galleryapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ItemHelper.OnCompleteListener, GalleryImageUploader.OnCompleteListener {

    ActivityMainBinding b;
    Bitmap bitmapFromString;
    List<Item> allItems = new ArrayList<>();
    Gson gson = new Gson();
    private static final int SELECT_PICTURE = 0;

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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void inflateViewForItem(Item item) {

        //This is adding items in array list
        allItems.add(item);

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());


        //Bind Data
        if(item.imageRedirectedUrl != null){
            Glide.with(this)
                    .asBitmap()
                    .load(item.imageRedirectedUrl)
                    .into(binding.imageView);
        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(item.uri)
                    .into(binding.imageView);
        }
        //binding.imageView.setImageBitmap(bitmapFromString);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);
        //Add it to the list
        b.list.addView(binding.getRoot());
    }

    /**
     * These 2 methods are the methods that will allow the user to upload the image from the gallery
     * and it will call other methods to extract the color and label palette.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri galleryImageUri = data.getData();
            new ItemHelper().fetchGalleryImage(galleryImageUri, MainActivity.this, this);
        }
    }

    public void uploadImage(View v){
        Toast.makeText(MainActivity.this, "Upload Image", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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

    /**
     * All these below methods are the callbacks from the itemHelper and the galleryImageUploader
     * classes.
     * @param uri
     * @param colors
     * @param labels
     */
    @Override
    public void onFetched(Uri uri, Set<Integer> colors, List<String> labels) {

        new GalleryImageUploader().show(MainActivity.this, uri, colors, labels, this);

    }

    @Override
    public void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels) {

    }

    @Override
    public void setError(String error) {

    }

    @Override
    public void onImageAdded(Item item) {
        inflateViewForItem(item);
    }

    @Override
    public void onError(String error) {

    }
}