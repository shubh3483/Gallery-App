package com.example.galleryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ItemHelper.OnCompleteListener, GalleryImageUploader.OnCompleteListener {

    private static final int REQUEST_PERMISSION = 0;
    ActivityMainBinding b;
    List<Item> allItems = new ArrayList<>();
    Gson gson = new Gson();
    private static final int SELECT_PICTURE = 0;
    private boolean isSorted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setTitle("Gallery");
        setContentView(b.getRoot());
        if(savedInstanceState != null){
            savedInstance(savedInstanceState);
        }else{
            sharedPreferences();
        }
        permissionAccess();
    }

    /**
     * This below code is for granting permission so that we do not encounter permission denied
     * exception while running the app.
     */
    private void permissionAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            List<String> permissions = new ArrayList<String>();
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
//              preferencesUtility.setString("storage", "true");
            }

            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

            } else {
//              preferencesUtility.setString("storage", "true");
            }

            if (!permissions.isEmpty()) {
//              requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);

                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                        REQUEST_PERMISSION);
            }
        }
    }

    /**
     * This method will prevent loss of data when the app is completely stopped.
     */
    private void sharedPreferences() {
        b.noItemsTV.setVisibility(View.GONE);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String json = preferences.getString(Constants.ALL_ITEMS, null);
        allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if(allItems != null){
            setUpRecyclerView();
        }
        else{
            allItems = new ArrayList<>();
        }
    }

    /**
     * This method contains the saved instance data and it will prevent loss of data when the screen
     *  is rotated.
     * @param savedInstanceState
     */
    private void savedInstance(Bundle savedInstanceState) {
        b.noItemsTV.setVisibility(View.GONE);
        String json = savedInstanceState.getString(Constants.ALL_ITEMS, null);
        allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if(allItems != null){
            /*for(Item item : allItems){
                //Bind Data
                *//*ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
                if(item.imageRedirectedUrl != null){
                    Glide.with(this)
                            .asBitmap()
                            .load(item.imageRedirectedUrl)
                            .into(binding.imageView);
                }
                else{
                    Glide.with(this)
                            .asBitmap()
                            .load(Uri.parse(item.uri))
                            .into(binding.imageView);
                }
                //binding.imageView.setImageBitmap(bitmapFromString);
                binding.title.setText(item.label);
                binding.title.setBackgroundColor(item.color);

                b.list.addView(binding.getRoot());*/
                setUpRecyclerView();
            }else{
            allItems = new ArrayList<>();
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

        SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();

        /**
         * Listener for SearchView
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(query, allItems);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                /**
                 * We are creating new reference of adapter everytime so that whenever user removes
                 * the filter search, the lists should not be pointing to the same reference and this
                 * was the main cause of the bug in the code(Resolved).
                 */
                ItemAdapter adapter = new ItemAdapter(MainActivity.this);
                adapter.filter(newText, allItems);
                b.list.setAdapter(adapter);
                return true;
            }
        });
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
        if(item.getItemId() == R.id.sort){
            sortList();
            return true;
        }
        return false;
    }

    /**
     * This function will sort the list alphabetically.
     */
    private void sortList() {
        if(!isSorted){
            isSorted = true;
            List<Item> sortedItems = new ArrayList<>(allItems);
            Collections.sort(sortedItems, (p1,p2) -> p1.label.compareTo(p2.label));
            ItemAdapter adapter = new ItemAdapter(this, sortedItems);
            adapter.showSortedItems();
            b.list.setAdapter(adapter);
        }else{
            isSorted = false;
            ItemAdapter adapter = new ItemAdapter(this, allItems);
            adapter.showSortedItems();
            b.list.setAdapter(adapter);
        }
    }

    /**
     * The below method will call another method and item will be passed which will be added accordingly
     * into the recycler view.
     */
    private void showAddImageDialog() {
        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        allItems.add(item);
                        setUpRecyclerView();
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

    /**
     * This method will call adapter of ItemAdapter to add the card into the recycler view.
     *
     */
    private void setUpRecyclerView(){
        ItemAdapter adapter = new ItemAdapter(this, allItems);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
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

        allItems.add(item);
        setUpRecyclerView();
    }

    @Override
    public void onError(String error) {

    }
}