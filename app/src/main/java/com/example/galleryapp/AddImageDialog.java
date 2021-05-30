package com.example.galleryapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
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
    int previousItemColor = 0;
    String previousItemLabel = null;

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

    /**
     * This function is for hiding the errors of the TIL
     */
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
                    int height = Integer.parseInt(heightStr);
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
        inflateColourChips(colors, false);
        inflateLabelChips(labels, false);
        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.addBtn.setText("ADD");
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInput.setVisibility(View.GONE);
        handleCustomInputLayout();
        handleAddImageEvent();
        handleShareImageEvent();
    }

    /**
     * This method is for editing the card.
     * @param context
     * @param redirectedUrl
     * @param colors
     * @param labels
     * @param listener
     * @param previousItemColor
     * @param previousItemLabel
     */
    public void showForEditData(Context context, String redirectedUrl, Set<Integer> colors,
                                List<String> labels, OnCompleteListener listener, int previousItemColor, String previousItemLabel){
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
        this.redirectedUrl = redirectedUrl;
        this.previousItemColor = previousItemColor;
        this.previousItemLabel = previousItemLabel;
        this.listener = listener;
        b.addBtn.setText("EDIT");
        Glide.with(context)
                .asBitmap()
                .load(redirectedUrl)
                .into(b.imageView);
        inflateColourChips(colors, true);
        inflateLabelChips(labels, true);
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
                                    try {
                                        File cachePath = new File(context.getCacheDir(), "images");
                                        cachePath.mkdirs(); // don't forget to make the directory
                                        FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // overwrites this image every time
                                        resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
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

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
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

                listener.onImageAdded(new Item(redirectedUrl,null, color, label));
                dialog.dismiss();
            }
        });
    }

    /**
     * These methods to inflate the Chip groups
     * @param labels
     */

    /**
     * These are the color and label inflaters and will work for both(for new card as well as for
     * editing the card).
     * @param labels
     * @param isEdit
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
     * The below three methods are listener of ItemHelper class.
     *
     * @param colors
     * @param labels
     */
    @Override
    public void onFetched(Uri uri, Set<Integer> colors, List<String> labels) {

    }

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
     * This will send the callback to the activities
     */
    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }
}
