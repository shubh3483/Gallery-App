package com.example.galleryapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private AlertDialog dialog;

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
        showData(uri, colors, labels);

    }

    /**
     * This method will show us the dialog which contains the image, color chips and label chips.
     * @param uri
     * @param colors
     * @param labels
     */
    void showData(Uri uri, Set<Integer> colors, List<String> labels) {
        //this.image = image;
        //this.redirectedUrl = redirectedUrl;
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(b.imageView);
        //b.imageView.setImageBitmap(image);
        inflateColourChips(colors);
        inflateLabelChips(labels);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInput.setVisibility(View.GONE);
        handleCustomInputLayout();
        handleAddImageEvent(uri);
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
                listener.onImageAdded(new Item(null, uri, color, label));
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

    /**
     * This is the listener for this activity.
     */
    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }

}






