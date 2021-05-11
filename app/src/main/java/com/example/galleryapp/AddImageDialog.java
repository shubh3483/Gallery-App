package com.example.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.galleryapp.databinding.ActivityDemoBinding;
import com.example.galleryapp.databinding.ChipColourBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
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
                    fetchRandomImage(height);
                }else if(heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }else {
                    int height = Integer.parseInt(widthStr);
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width,height);
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(b.width.getWindowToken(), 0);
    }

    private void fetchRandomImage(int width, int height) {
        new ItemHelper()
                .fetchData(width, height, context, this);
    }

    private void showData(Bitmap image, Set<Integer> colors, List<String> labels) {
        this.image = image;
        b.imageView.setImageBitmap(image);
        inflateColourChips(colors);
        inflateLabelChips(labels);
        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInput.setVisibility(View.GONE);
        handleCustomInputLayout();
        handleAddImageEvent();
    }

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

                listener.onImageAdded(new Item(image, color, label));
                dialog.dismiss();
            }
        });
    }

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

    private void fetchRandomImage(int height) {
        new ItemHelper()
                .fetchData(height, context, this);
    }

    @Override
    public void onFetched(Bitmap image, Set<Integer> colors, List<String> labels) {

        showData(image, colors, labels);
    }

    @Override
    public void setError(String error) {
        dialog.dismiss();
        listener.onError(error);
    }

    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }
}
