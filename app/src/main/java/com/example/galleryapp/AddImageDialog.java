package com.example.galleryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private String redirectedUrl = "";
    private AlertDialog dialog;

    void show(Context context, OnCompleteListener listener){
        this.context = context;
        this.listener = listener;

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
            b.enterDimensionsRoot.setVisibility(View.VISIBLE);
            handleDimensionsInput();
            hideErrorsForET();
    }

    private void hideErrorsForET() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * This function will handle input dimesnions.
     */
    private void handleDimensionsInput() {

        b.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String widthStr = b.width.getText().toString().trim()
                        , heightStr = b.height.getText().toString().trim();

                if(widthStr.isEmpty() && heightStr.isEmpty()){
                    b.width.setError("Enter at least on dimension");
                    return;
                }

                b.enterDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);

                //Hide keyboard
                hideKeyboard();

                if(widthStr.isEmpty()){
                    int height = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    try {
                        fetchRandomImage(width,height);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * It will hide keyboard after user has entered the dimensions.
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(b.width.getWindowToken(), 0);
    }

    /**
     * The below two methods will call the fetchImage methods of the ItemHelper class to fetch the
     * image.
     * @param width
     * @param height
     * @throws IOException
     */
    private void fetchRandomImage(int width, int height) throws IOException {
        new ItemHelper()
                .fetchData(width, height, context, this);
    }

    private void fetchRandomImage(int height) throws IOException {
        new ItemHelper()
                .fetchData(height, context, this);
    }

    /**
     * After we got the image, colors and labels by taking it from the callback we will show the
     * final image color chips and label chips and ask the user to choose from them.
     *
     * @param colors
     * @param labels
     */
    private void showData(String redirectedUrl, Set<Integer> colors, List<String> labels) {
        //this.image = image;
        this.redirectedUrl = redirectedUrl;
        Glide.with(context)
                .asBitmap()
                .load(redirectedUrl)
                .into(b.imageView);
        inflateColourChips(colors);
        inflateLabelChips(labels);
        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInput.setVisibility(View.GONE);
        handleCustomInputLayout();
        handleAddImageEvent();
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
                            .load(redirectedUrl)
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
     * At last it will get the final image, color and label and it will send a callback in the
     * MainActivity to send the final image, color and label.
     */
    private void handleAddImageEvent() {
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
                /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                String temp = Base64.encodeToString(b, Base64.DEFAULT);*/

                listener.onImageAdded(new Item(redirectedUrl,null, color, label));
                dialog.dismiss();
            }
        });
    }

    /**
     * These methods to inflate the Chip groups
     * @param labels
     */

    private void inflateLabelChips(List<String> labels) {
        for(String label : labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChipGrp.addView(binding.getRoot());
        }
    }

    private void inflateColourChips(Set<Integer> colors) {
        for(int colour : colors){
            ChipColourBinding binding = ChipColourBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(colour));
            b.colourPaletteChipGrp.addView(binding.getRoot());
        }
    }

    @Override
    public void onFetched(Uri uri, Set<Integer> colors, List<String> labels) {

    }

    /**
     * The below two methods are listener of ItemHelper class.
     *
     * @param colors
     * @param labels
     */
    @Override
    public void onFetched(String redirectedUrl, Set<Integer> colors, List<String> labels) {

        showData(redirectedUrl, colors, labels);
    }

    @Override
    public void setError(String error) {
        dialog.dismiss();
        listener.onError(error);
    }

    /**
     * This is the listener for this activity.
     */
    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }
}
