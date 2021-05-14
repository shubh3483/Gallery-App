package com.example.galleryapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setTitle("Gallery");
        setContentView(b.getRoot());
    }

    /**
     * This method will inflate our menu resource file.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_app, menu);
        return true;
    }

    /**
     * This method will show a refresh button in our main activity.
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
     * It will take the callback from other method when the final image, color and label is ready.
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

    /**
     * This function will add the item card into the list.
     * @param item
     */
    private void inflateViewForItem(Item item) {

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind Data
        binding.imageView.setImageBitmap(item.bitmap);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
    }
}