package com.example.galleryapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.example.galleryapp.databinding.ActivityMainBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ContextMenuHandler implements ItemHelper.OnCompleteListener, GalleryImageUploader.OnCompleteListener, AddImageDialog.OnCompleteListener {

    ActivityMainBinding b;
    private ImageView iv;
    private String sharePath="no";
    Context context;
    DeleteItem deleteItemListener;
    ItemHelper itemHelper = new ItemHelper();
    int previousItemColor = 0;
    String previousItemLabel = null;
    EditItem editItemListener;

    /**
     * Constructor
     * @param context
     * @param deleteItemListener
     * @param editItemListener
     */
    public ContextMenuHandler(Context context, DeleteItem deleteItemListener, EditItem editItemListener) {
        this.deleteItemListener = deleteItemListener;
        this.editItemListener = editItemListener;
        this.context = context;
    }


    /**
     * This method is for sharing the card image without saving it to external memory.
     * @param binding
     */
    void shareImage(ItemCardBinding binding) {
        Bitmap bitmap = getBitmapFromView(binding.getRoot());
        try {

            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            File imagePath = new File(context.getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(context, "com.example.galleryapp.fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                context.startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is for converting the view to bitmap
     * @param view
     * @return
     */
    public Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    /**
     * This is the function to show the dialog after user selects delete item.
     * @param mCurrentItemPosition
     */
    public void deleteItem(int mCurrentItemPosition) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItemListener.deleteItemCallback(mCurrentItemPosition);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
        .show();
    }

    /**
     * This is the function which will be called first when the user selects the option for editing.
     * @param item
     */
    public void editItem(Item item){

        previousItemColor = item.color;
        previousItemLabel = item.label;
        if(item.imageRedirectedUrl != null){
            itemHelper.fetchImage(item.imageRedirectedUrl, context, this);
        }else {
            itemHelper.fetchGalleryImage(Uri.parse(item.uri), context, this);
        }
    }


    /**
     * All these below functions are the callbacks received by this activity.
     * @param uri
     * @param colors
     * @param labels
     */

    @Override
    public void onFetched(Uri uri, Set<Integer> colors, List<String> labels) {
        new GalleryImageUploader().showForEditData(context, uri, colors, labels, this, previousItemColor, previousItemLabel);
    }

    @Override
    public void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels) {
        new AddImageDialog().showForEditData(context, redirectedUrl, colors, labels, this, previousItemColor, previousItemLabel);
    }

    @Override
    public void setError(String error) {

    }

    @Override
    public void onGalleryImageAdded(Item item) {
        editItemListener.editedItemCallBack(item);
    }

    @Override
    public void onImageAdded(Item item) {
        editItemListener.editedItemCallBack(item);
    }

    @Override
    public void onError(String error) {

    }


    /**
     * This below two listeners will send the callback to the MainActivity.
     */
    interface DeleteItem{
        void deleteItemCallback(int position);
    }

    interface EditItem{
        void editedItemCallBack(Item item);
    }
}
