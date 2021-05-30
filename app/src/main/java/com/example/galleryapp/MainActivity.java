package com.example.galleryapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ContextMenuHandler.EditItem, ContextMenuHandler.DeleteItem, ItemHelper.OnCompleteListener, GalleryImageUploader.OnCompleteListener, ItemAdapter.onClickListener {

    private static final int REQUEST_PERMISSION = 0;
    ActivityMainBinding b;
    List<Item> allItems = new ArrayList<>();
    Gson gson = new Gson();
    private static final int SELECT_PICTURE = 0;
    private boolean isSorted;
    ItemTouchHelper itemTouchHelper;
    ItemAdapter adapter;
    private boolean checkDragHandle = true;
    private ActionMode mActionMode;
    private int mCurrentItemPosition = -1;
    ContextMenuHandler contextMenuHandler = new ContextMenuHandler(this, this, this);
    //int position;
    private String sharePath = "no";

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
        checkItemsEmptyOrNot();
        registerForContextMenu(b.list);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editItem) {
            System.out.println(allItems);
            Item item1 = allItems.get(mCurrentItemPosition);
            contextMenuHandler.editItem(item1);
            allItems.remove(mCurrentItemPosition);
            Toast.makeText(this, "Editing ", Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.shareItem) {
            Toast.makeText(this, "Sharing", Toast.LENGTH_SHORT).show();
            contextMenuHandler.takeScreenshot(mCurrentItemPosition);
        }

        if(id == R.id.deleteItem) {
            System.out.println(mCurrentItemPosition);
            contextMenuHandler.deleteItem(mCurrentItemPosition);
        }
        return true;
    }


    /**
     * This method is to show no items in main activity when there are no items. Separate method is
     * made because it might be possible that allItems is not NULL but EMPTY.
     */
    private void checkItemsEmptyOrNot() {
        if(allItems.isEmpty()){
            b.list.setVisibility(View.GONE);
            b.noItemsTV.setVisibility(View.VISIBLE);
        }
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
        //b.noItemsTV.setVisibility(View.GONE);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String json = preferences.getString(Constants.ALL_ITEMS, null);
        allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if(allItems != null){
            b.noItemsTV.setVisibility(View.GONE);
            setUpRecyclerView();
        }
        else{
            allItems = new ArrayList<>();
            b.noItemsTV.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method contains the saved instance data and it will prevent loss of data when the screen
     *  is rotated.
     * @param savedInstanceState
     */
    private void savedInstance(Bundle savedInstanceState) {
        //b.noItemsTV.setVisibility(View.GONE);
        String json = savedInstanceState.getString(Constants.ALL_ITEMS, null);
        allItems = gson.fromJson(json, new TypeToken<List<Item>>() {
        }.getType());
        if(allItems != null){

                b.noItemsTV.setVisibility(View.GONE);
                setUpRecyclerView();
            }else{
            allItems = new ArrayList<>();
            b.noItemsTV.setVisibility(View.VISIBLE);
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
        if(item.getItemId() == R.id.dragHandle){
            if(checkDragHandle){
                Toast.makeText(this, "Drag and drop disabled", Toast.LENGTH_SHORT).show();
                checkDragHandle = false;
            }
            else {
                Toast.makeText(this, "Drag and drop Enabled", Toast.LENGTH_SHORT).show();
                checkDragHandle = true;
            }
            setUpRecyclerView();
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
            if(adapter != null){
                adapter.requiredNewItemList = sortedItems;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }
        }else{
            isSorted = false;
            if(adapter != null){
                adapter.requiredNewItemList = allItems;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }

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
        b.list.setVisibility(View.VISIBLE);
        b.noItemsTV.setVisibility(View.GONE);
        if(adapter == null){
            adapter = new ItemAdapter(this, allItems,checkDragHandle, this);
        }else{
            adapter.itemsList = allItems;
            adapter.checkDragHandle = checkDragHandle;
        }
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        itemRemove();
    }

    /**
     * Below code is the code for swipe functionality that will remove the card from the
     * recycler view.
     */
    private void itemRemove() {
        itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter, this));
        adapter.notifyDataSetChanged();
        if(checkDragHandle)
        itemTouchHelper.attachToRecyclerView(null);
        else itemTouchHelper.attachToRecyclerView(b.list);
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
     * This method will delete the item from recycler view.
     * @param position
     */
    private void deleteItem(int position) {
        allItems.remove(position);
        setUpRecyclerView();
        Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
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
    public void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels) {}

    @Override
    public void setError(String error) {

    }

    @Override
    public void onGalleryImageAdded(Item item) {

        allItems.add(item);
        setUpRecyclerView();
    }

    @Override
    public void onError(String error) {

    }

    /**
     * This is the callback from contextMenu and it returns the position of the selected item.
     * @param position
     */
    @Override
    public void position(int position) {
        mCurrentItemPosition = position;
    }

    /**
     * This is the callback received after the user confirms that he wants to delete the item
     * @param position
     */
    @Override
    public void deleteItemCallback(int position) {
        deleteItem(position);
    }

    @Override
    public void editedItemCallBack(Item item) {
        editItem(item);
    }

    private void editItem(Item item) {
        allItems.add(mCurrentItemPosition, item);
        setUpRecyclerView();
    }
}