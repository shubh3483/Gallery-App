package com.example.galleryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;


public class GalleryImageUploader {
    private Context context;
    private GalleryImageUploader.OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private String redirectedUrl = "";
    private String uri = "";
    private AlertDialog dialog;
    int previousItemColor = 0;
    String previousItemLabel = null;

    /**
     * This method will take the uri, colors and the labels and will help us to inflate the dialog
     * builder.
     * @param context
     * @param uri
     * @param colors
     * @param labels
     * @param listener
     */
    void show(Context context, Uri uri, Set<Integer> colors, List<String> labels, GalleryImageUploader.OnCompleteListener listener){
        this.context = context;
        this.listener = listener;
        this.uri = uri.toString();
        b.addBtn.setText("ADD");
        if(context instanceof MainActivity){
            inflater = ((MainActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
        }else{
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();
        showData(uri, colors, labels,false);

    }

    void showForEditData(Context context, Uri uri, Set<Integer> colors, List<String> labels, GalleryImageUploader.OnCompleteListener listener, int previousItemColor, String previousItemLabel ) {
        this.context = context;
        this.listener = listener;
        this.uri = uri.toString();
        this.previousItemColor = previousItemColor;
        this.previousItemLabel = previousItemLabel;
        b.addBtn.setText("EDIT");
        if (context instanceof MainActivity) {
            inflater = ((MainActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
        } else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();
        showData(uri, colors, labels, true);
    }

    /**
     * This method will show us the dialog which contains the image, color chips and label chips.
     * @param uri
     * @param colors
     * @param labels
     */
    void showData(Uri uri, Set<Integer> colors, List<String> labels, boolean isEdit) {
        //this.image = image;
        //this.redirectedUrl = redirectedUrl;
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(b.imageView);
        //b.imageView.setImageBitmap(image);
        inflateLabelChips(labels, isEdit);
        inflateColourChips(colors, isEdit);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInput.setVisibility(View.GONE);
        handleCustomInputLayout();
        handleAddImageEvent(uri);
        handleShareImageEvent();
    }

    /**
     * This function will share the generated image with another apps.
     */
    private void handleShareImageEvent() {

        b.shareImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Glide.with(context)
                            .asBitmap()
                            .load(uri)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Calling the intent to share the bitmap
                                    Bitmap icon = resource;
                                    Intent share = new Intent(Intent.ACTION_SEND);
                                    share.setType("image/jpeg");

                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "title");
                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);


                                    OutputStream outputStream;
                                    try {
                                        outputStream = context.getContentResolver().openOutputStream(uri);
                                        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                        outputStream.close();
                                    } catch (Exception e) {
                                        System.err.println(e.toString());
                                    }

                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    context.startActivity(Intent.createChooser(share, "Share Image"));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                    /*Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    //String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "", null);
                    Uri uri =  Uri.parse( redirectedUrl );
                    //sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    sharingIntent.setType("image/jpeg");
                    sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);

                    context.startActivity(Intent.createChooser(sharingIntent,"Share via"));*/

                    /*Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Insert Subject here");
                    String app_url = redirectedUrl;
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"));*/
                } catch (Exception e) {
                    Log.e("Error on sharing", e + " ");
                    Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method will check whether the user has opted for custom input or not.
     */
    private void handleCustomInputLayout() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChipGrp.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.customInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;

            }
        });
    }


    /**
     * This method will handle the functionality when the user presses Add btn
     * @param uri
     */
    private void handleAddImageEvent(Uri uri) {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colourPaletteChipGrp.getCheckedChipId()
                        , labelChipId = b.labelChipGrp.getCheckedChipId();
                if(colorChipId == -1 || labelChipId == -1){
                    Toast.makeText(context, "Please choose color and label", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label = "";
                if(isCustomLabel){
                    label = b.customED.getText().toString().trim();
                    if(label.isEmpty()){
                        Toast.makeText(context, "Please Enter label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    label = ((Chip)b.labelChipGrp.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip)b.colourPaletteChipGrp.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();
                listener.onGalleryImageAdded(new Item(null, uri.toString(), color, label));
                dialog.dismiss();
            }
        });
    }

    /**
     * These methods to inflate the Chip groups
     * @param labels
     */

    private void inflateLabelChips(List<String> labels, boolean isEdit) {
        if(isEdit) {
            boolean labelChecker = false;
            for (String label : labels) {
                if (label.equals(previousItemLabel)) {
                    ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
                    binding.getRoot().setText(label);
                    b.labelChipGrp.addView(binding.getRoot());
                    binding.getRoot().setChecked(true);
                    labelChecker = true;
                } else {
                    ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
                    binding.getRoot().setText(label);
                    b.labelChipGrp.addView(binding.getRoot());
                }
            }
            if (!labelChecker) {
                ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
                binding.getRoot().setText(previousItemLabel);
                b.labelChipGrp.addView(binding.getRoot());
                binding.getRoot().setChecked(true);
            }
        }else{
            for (String label : labels) {
                ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
                binding.getRoot().setText(label);
                b.labelChipGrp.addView(binding.getRoot());
            }
        }
    }

    private void inflateColourChips(Set<Integer> colors, boolean isEdit) {
        if(isEdit){
            boolean colorChecker = false;
            for (int colour : colors) {
                if (colour == previousItemColor) {
                    ChipColourBinding binding = ChipColourBinding.inflate(inflater);
                    binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(colour));
                    b.colourPaletteChipGrp.addView(binding.getRoot());
                    binding.getRoot().setChecked(true);
                    colorChecker = true;
                } else {
                    ChipColourBinding binding = ChipColourBinding.inflate(inflater);
                    binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(colour));
                    b.colourPaletteChipGrp.addView(binding.getRoot());
                }
            }
            if (!colorChecker) {
                ChipColourBinding binding = ChipColourBinding.inflate(inflater);
                binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(previousItemColor));
                b.colourPaletteChipGrp.addView(binding.getRoot());
                binding.getRoot().setChecked(true);
            }
        }else {
            for (int colour : colors) {
                ChipColourBinding binding = ChipColourBinding.inflate(inflater);
                binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(colour));
                b.colourPaletteChipGrp.addView(binding.getRoot());
            }
        }
    }

    /**
     * This is the listener for this activity.
     */
    interface OnCompleteListener{
        void onGalleryImageAdded(Item item);
        void onError(String error);
    }

}






